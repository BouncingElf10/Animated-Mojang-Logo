package fabric.compat

import fabric.compat.structs.Ansi
import fabric.compat.structs.Versions
import kotlinx.coroutines.*
import java.io.File

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
object GradleRunner {
    private val isWindows = System.getProperty("os.name").lowercase().contains("win")
    private val gradleWrapper = if (isWindows) "gradlew.bat" else "gradlew"

    suspend fun doTests(projectDir: File, config: FabricCompatExtension): List<TestResult> {
        val currentVersion = FabricMeta.getCurrentVersion(projectDir)
        FabricMeta.prewarmLoader(config.loaderVersion, projectDir)

        val allVersions = FabricMeta.getAllStableVersions()
        val currentIndex = allVersions.indexOfFirst { it.version == currentVersion }
        require(currentIndex != -1) { "Current version $currentVersion not found in stable list" }

        val forwardVersions =
            if (config.checkNewerVersions) allVersions.subList(0, currentIndex).reversed().take(config.maxStrikes * 2)
            else emptyList()

        val backwardVersions = if (config.checkOlderVersions) allVersions.subList(currentIndex + 1, allVersions.size)
            .take(config.maxStrikes * 2)
        else emptyList()

        val versionsToTest = buildList {
            add(currentVersion)
            addAll(forwardVersions.map { it.version })
            addAll(backwardVersions.map { it.version })
        }

        val resolved: Map<String, Versions> = coroutineScope {
            versionsToTest.map { v -> async { v to FabricMeta.resolveVersions(v, config, projectDir) } }.awaitAll()
                .toMap()
        }

        val currentResult = runBuildForVersion(resolved[currentVersion]!!, projectDir, config)
        val forwardResults = applyStrikes(
            forwardVersions.map { runBuildForVersion(resolved[it.version]!!, projectDir, config) }, config.maxStrikes
        )
        val backwardResults = applyStrikes(
            backwardVersions.map { runBuildForVersion(resolved[it.version]!!, projectDir, config) }, config.maxStrikes
        )

        return buildList {
            add(currentResult)
            addAll(forwardResults)
            addAll(backwardResults)
        }
    }

    private fun applyStrikes(results: List<TestResult>, maxStrikes: Int): List<TestResult> {
        var strikes = 0
        return results.takeWhile { result ->
            if (!result.buildResult.success) strikes++ else strikes = 0
            strikes <= maxStrikes
        }
    }

    private fun runBuildForVersion(versions: Versions, projectDir: File, config: FabricCompatExtension): TestResult {
        return TestResult(runBuild(projectDir, versions, config), versions)
    }

    fun runBuild(
        projectDir: File, versions: Versions? = null, config: FabricCompatExtension = FabricCompatExtension()
    ): BuildResult {
        val wrapperPath = File(projectDir, gradleWrapper).absolutePath
        val cacheDir = versions?.let { File(projectDir, ".gradle-compat-${it.mcVersion.version}") }

        val args = buildList {
            add(wrapperPath)
            addAll(config.buildTasks)
            add("--warning-mode=all")
            if (config.daemonJvmArgs.isNotEmpty()) {
                add("-Dorg.gradle.jvmargs=${config.daemonJvmArgs.joinToString(" ")}")
            }
            if (versions != null && cacheDir != null) {
                add("-Pminecraft_version=${versions.mcVersion.version}")
                add("-Ploader_version=${versions.loaderVersion.version}")
                add("-Ploom_version=${versions.loomVersion}")
                add("-Pfabric_api_version=${versions.fabricApiVersion}")
                add("--project-cache-dir")
                add(cacheDir.absolutePath)
            }
        }

        val pb = ProcessBuilder(args).directory(projectDir).redirectErrorStream(true)
        val process = pb.start()
        val output = process.inputStream.bufferedReader().readText()
        val exit = process.waitFor()

        cacheDir?.deleteRecursively()

        val label = versions?.mcVersion?.version ?: "current"
        val success = exit == 0

        if (success) {
            println("  ${Ansi.GREEN}PASS${Ansi.RESET}  [$label]")
            if (config.verbose) println(output.lines().joinToString("\n") { "        ${Ansi.DIM}$it${Ansi.RESET}" })
        } else {
            println("  ${Ansi.RED}FAIL${Ansi.RESET}  [$label] (exit $exit)")
            val errorLines = output.lines()
                .filter { it.contains(": error:") || it.startsWith("* What went wrong") || it.startsWith("> ") }
            val tail = errorLines.ifEmpty { output.lines().takeLast(15) }
            println(tail.joinToString("\n") { "        ${Ansi.DIM}$it${Ansi.RESET}" })
        }

        return BuildResult(success, exit, output)
    }

    data class BuildResult(val success: Boolean, val exitCode: Int, val output: String)
    data class TestResult(val buildResult: BuildResult, val versions: Versions)
}