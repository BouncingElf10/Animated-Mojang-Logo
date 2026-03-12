package fabric.compat

import kotlinx.coroutines.runBlocking

object GradleRunner {
    suspend fun doTests(): ArrayList<TestResult> {
        FileManager.dupeGradleProperties()
        val currentVersion = FabricMeta.getCurrentVersion()
        val results = ArrayList<TestResult>()

        val result = TestResult(tryAndBuild(), FabricMeta.resolveVersions(currentVersion))
        results.add(result)

        val forwardPassResults = doForwardPass(currentVersion)
        val backwardPassResults = doBackwardPass(currentVersion)

        forwardPassResults.forEach { result -> results.add(result) }
        backwardPassResults.forEach { result -> results.add(result) }

        FileManager.copyAndRevert()

        return results
        // apply results if wanted
        // prettify results
    }

    private fun doForwardPass(currentVersion: String): ArrayList<TestResult> {
        return doVersionPass(currentVersion) { version -> runBlocking { FabricMeta.getNextVersion(version) } }
    }

    private fun doBackwardPass(currentVersion: String): List<TestResult> {
        return doVersionPass(currentVersion) { version -> runBlocking { FabricMeta.getPrevVersion(version) } }
    }

    private fun doVersionPass(currentVersion: String, getNextVersion: (String) -> String): ArrayList<TestResult> {
        var strikes = 0;
        var version = currentVersion;
        val results = ArrayList<TestResult>();

        while (strikes < 3) {
            version = getNextVersion(version)
            val versions = runBlocking { FabricMeta.resolveVersions(version) }
            FileManager.replaceProperties(runBlocking { versions })

            val result = tryAndBuild()
            results.add(TestResult(result, versions))
            if (!result.success) {
                strikes++
            }
        }

        return results
    }

    fun tryAndBuild(): BuildResult {
        val pb = ProcessBuilder("gradlew.bat", "build", "--warning-mode=all")

        pb.redirectErrorStream(true)

        val process = pb.start()
        val output = String(process.inputStream.readAllBytes())
        val exit = process.waitFor()

        if (exit == 0) {
            println("Build succeeded")
        } else {
            println("Build failed")
        }

        if (output.contains("warning")) {
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