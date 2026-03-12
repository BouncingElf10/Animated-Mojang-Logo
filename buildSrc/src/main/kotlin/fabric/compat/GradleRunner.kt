package fabric.compat

object GradleRunner {
    suspend fun doTests() { // : List<TestResult>
        FileManager.dupeGradleProperties()
        val currentVersion = FabricMeta.getCurrentVersion()
        var results = ArrayList<TestResult>()

        val result = TestResult(tryAndBuild(), FabricMeta.resolveVersions(currentVersion))
        results.add(result)

        val forwardPassResults = doForwardPass(currentVersion)
        val backwardPassResults = doBackwardPass(currentVersion)

        forwardPassResults.forEach { result -> results.add(result) }
        backwardPassResults.forEach { result -> results.add(result) }
        // Dupe gradle.properties
        // record current version

        // forward pass
        // edit gradle.properties
        // try and build

        // if 3 times no build, backwards pass

        // apply results if wanted
        // prettify results
    }

    private fun doForwardPass(currentVersion: String): List<TestResult> {
        TODO("Not yet implemented")
    }

    private fun doBackwardPass(currentVersion: String): List<TestResult> {
        TODO("Not yet implemented")
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