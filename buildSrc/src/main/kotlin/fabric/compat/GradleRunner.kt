package fabric.compat

object GradleRunner {
    fun doTests(): List<TestResult> {
        // Dupe gradle.properties
        // record current version

        // forward pass
        // edit gradle.properties
        // try and build

        // if 3 times no build, backwards pass

        // apply results if wanted
        // prettify results
        TODO()
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