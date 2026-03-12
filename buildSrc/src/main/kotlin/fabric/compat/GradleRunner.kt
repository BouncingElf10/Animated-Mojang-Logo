package fabric.compat

import kotlinx.coroutines.runBlocking

object GradleRunner {
    private val isWindows = System.getProperty("os.name").lowercase().contains("win")
    private val gradleCommand = if (isWindows) "gradlew.bat" else "./gradlew"

    suspend fun doTests(): ArrayList<TestResult> {
        FileManager.dupeGradleProperties()
        return try {
            val currentVersion = FabricMeta.getCurrentVersion()
            val results = ArrayList<TestResult>()

            val result = TestResult(tryAndBuild(), FabricMeta.resolveVersions(currentVersion))
            results.add(result)

            val forwardPassResults = doForwardPass(currentVersion)
            val backwardPassResults = doBackwardPass(currentVersion)

            forwardPassResults.forEach { r -> results.add(r) }
            backwardPassResults.forEach { r -> results.add(r) }

            results
        } finally {
            // Always revert, even if an exception was thrown
            FileManager.copyAndRevert()
        }
    }

    private fun doForwardPass(currentVersion: String): ArrayList<TestResult> {
        return doVersionPass(currentVersion) { version -> runBlocking { FabricMeta.getNextVersion(version) } }
    }

    private fun doBackwardPass(currentVersion: String): List<TestResult> {
        return doVersionPass(currentVersion) { version -> runBlocking { FabricMeta.getPrevVersion(version) } }
    }

    private fun doVersionPass(currentVersion: String, getNextVersion: (String) -> String): ArrayList<TestResult> {
        var strikes = 0
        var version = currentVersion
        val results = ArrayList<TestResult>()

        while (strikes < 3) {
            val nextVersion = getNextVersion(version)

            if (nextVersion == FabricMeta.NO_MORE_VERSIONS) {
                println("No more versions to check in this direction.")
                break
            }

            version = nextVersion
            val versions = runBlocking { FabricMeta.resolveVersions(version) }
            FileManager.replaceProperties(versions)

            val result = tryAndBuild()
            results.add(TestResult(result, versions))
            if (!result.success) {
                strikes++
            }
        }

        return results
    }

    fun tryAndBuild(): BuildResult {
        val pb = ProcessBuilder(gradleCommand, "build", "--warning-mode=all")
        pb.redirectErrorStream(true)

        val process = pb.start()
        val output = String(process.inputStream.readAllBytes())
        val exit = process.waitFor()

        if (exit == 0) {
            println("Build succeeded")
        } else {
            println("Build failed with exit code $exit")
        }

        if (output.contains("warning", ignoreCase = true)) {
            println("Warnings detected")
        }

        return BuildResult(exit == 0, exit, output)
    }

    data class BuildResult(
        val success: Boolean,
        val exitCode: Int,
        val output: String,
    )

    data class TestResult(
        val buildResult: BuildResult,
        val versions: Versions
    )
}