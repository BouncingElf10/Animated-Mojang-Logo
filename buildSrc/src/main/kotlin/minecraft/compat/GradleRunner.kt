package minecraft.compat

import minecraft.compat.structs.Ansi
import minecraft.compat.structs.Versions
import kotlinx.coroutines.*
import java.io.File


sealed class VersionInfo {
    abstract val mcVersion: String
    class Fabric(val versions: Versions) : VersionInfo() { override val mcVersion get() = versions.mcVersion.version }
    class NeoForge(override val mcVersion: String, val neoforgeVersion: String) : VersionInfo()
}

@OptIn(ExperimentalCoroutinesApi::class)
object GradleRunner {
    private val isWindows = System.getProperty("os.name").lowercase().contains("win")
    private val gradleWrapper = if (isWindows) "gradlew.bat" else "gradlew"

    suspend fun doTests(projectDir: File, config: FabricCompatExtension): List<TestResult> =
        when (config.resolvedModLoader(projectDir)) {
            ModLoader.FABRIC -> doFabricTests(projectDir, config)
            ModLoader.NEOFORGE -> doNeoForgeTests(projectDir, config)
        }

    private suspend fun doFabricTests(projectDir: File, config: FabricCompatExtension): List<TestResult> {
        val currentVersion = FabricMeta.getCurrentVersion(projectDir)
        FabricMeta.prewarmLoader(config.loaderVersion, projectDir)

        val allVersions = FabricMeta.getAllStableVersions()
            .sortedWith(compareByDescending {
                it.version.split(".").map { p -> p.toIntOrNull() ?: 0 }
                    .let { p -> p.getOrElse(0) { 0 } * 10_000 + p.getOrElse(1) { 0 } * 100 + p.getOrElse(2) { 0 } }
            })

        val currentIndex = allVersions.indexOfFirst { it.version == currentVersion }
        require(currentIndex != -1) { "Current version $currentVersion not found in stable list" }

        val forwardVersions = if (config.checkNewerVersions)
            allVersions.subList(0, currentIndex).reversed()
        else emptyList()

        val backwardVersions = if (config.checkOlderVersions)
            allVersions.subList(currentIndex + 1, allVersions.size).reversed()
        else emptyList()

        val versionsToTest = buildList {
            add(currentVersion)
            addAll(forwardVersions.map { it.version })
            addAll(backwardVersions.map { it.version })
        }

        val resolved: Map<String, Versions> = coroutineScope {
            versionsToTest.map { v -> async { v to FabricMeta.resolveVersions(v, config, projectDir) }
            }.awaitAll().toMap()
        }

        val currentResult = runBuildForVersion(VersionInfo.Fabric(resolved[currentVersion]!!), projectDir, config)
        val forwardResults = applyStrikes(
            forwardVersions.map { runBuildForVersion(VersionInfo.Fabric(resolved[it.version]!!), projectDir, config) },
            config.maxStrikes
        )
        val backwardResults = applyStrikes(
            backwardVersions.map { runBuildForVersion(VersionInfo.Fabric(resolved[it.version]!!), projectDir, config) },
            config.maxStrikes
        )

        return buildList {
            add(currentResult)
            addAll(forwardResults)
            addAll(backwardResults)
        }
    }

    private suspend fun doNeoForgeTests(projectDir: File, config: FabricCompatExtension): List<TestResult> {
        val currentVersion = NeoForgeMeta.getCurrentMcVersion(projectDir)

        val allVersions = NeoForgeMeta.getAllSupportedMcVersions()
            .sortedWith(compareByDescending {
                it.split(".").map { p -> p.toIntOrNull() ?: 0 }
                    .let { p -> p.getOrElse(0) { 0 } * 10_000 + p.getOrElse(1) { 0 } * 100 + p.getOrElse(2) { 0 } }
            })

        val currentIndex = allVersions.indexOfFirst { it == currentVersion }
        require(currentIndex != -1) {
            "Current MC version $currentVersion not found in NeoForge-supported version list.\n" +
                    "Available: ${allVersions.take(10).joinToString()}"
        }

        val forwardVersions = if (config.checkNewerVersions)
            allVersions.subList(0, currentIndex).reversed()
        else emptyList()

        val backwardVersions = if (config.checkOlderVersions)
            allVersions.subList(currentIndex + 1, allVersions.size).reversed()
        else emptyList()

        val versionsToTest = buildList {
            add(currentVersion)
            addAll(forwardVersions)
            addAll(backwardVersions)
        }


        val resolved: Map<String, NeoForgeVersions> = coroutineScope {
            versionsToTest.map { v -> async { v to NeoForgeMeta.resolveVersions(v, config.neoforgeVersion) }
            }.awaitAll().toMap()
        }

        fun toInfo(mcVer: String): VersionInfo.NeoForge {
            val v = resolved[mcVer] ?: error("Version $mcVer was in versionsToTest but missing from resolved map")
            return VersionInfo.NeoForge(v.mcVersion, v.neoforgeVersion)
        }

        val currentResult = runBuildForVersion(toInfo(currentVersion), projectDir, config)
        val forwardResults =
            applyStrikes(forwardVersions.map { runBuildForVersion(toInfo(it), projectDir, config) }, config.maxStrikes)
        val backwardResults =
            applyStrikes(backwardVersions.map { runBuildForVersion(toInfo(it), projectDir, config) }, config.maxStrikes)

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

    private fun runBuildForVersion(versionInfo: VersionInfo, projectDir: File, config: FabricCompatExtension) =
        TestResult(runBuild(projectDir, versionInfo, config), versionInfo)

    fun runBuild(projectDir: File, versionInfo: VersionInfo? = null, config: FabricCompatExtension = FabricCompatExtension()): BuildResult {
        val wrapperPath = File(projectDir, gradleWrapper).absolutePath
        val cacheDir = versionInfo?.let { File(projectDir, ".gradle-compat-${it.mcVersion}") }

        val args = buildList {
            add(wrapperPath)
            addAll(config.buildTasks)
            add("--warning-mode=all")
            if (config.daemonJvmArgs.isNotEmpty()) {
                add("-Dorg.gradle.jvmargs=${config.daemonJvmArgs.joinToString(" ")}")
            }
            if (cacheDir != null) {
                add("--project-cache-dir")
                add(cacheDir.absolutePath)
            }
            when (versionInfo) {
                is VersionInfo.Fabric -> {
                    val v = versionInfo.versions
                    add("-Pminecraft_version=${v.mcVersion.version}")
                    add("-Ploader_version=${v.loaderVersion.version}")
                    add("-Ploom_version=${v.loomVersion}")
                    add("-Pfabric_api_version=${v.fabricApiVersion}")
                }

                is VersionInfo.NeoForge -> {
                    add("-Pminecraft_version=${versionInfo.mcVersion}")
                    add("-Pneo_version=${versionInfo.neoforgeVersion}")
                }

                null -> {}
            }
        }

        val pb = ProcessBuilder(args).directory(projectDir).redirectErrorStream(true)
        val process = pb.start()
        val output = process.inputStream.bufferedReader().readText()
        val exit = process.waitFor()

        cacheDir?.deleteRecursively()

        val label = versionInfo?.mcVersion ?: "current"
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
    data class TestResult(val buildResult: BuildResult, val versions: VersionInfo)
}