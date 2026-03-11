package fabric.compat

object GradleRunner {
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
}