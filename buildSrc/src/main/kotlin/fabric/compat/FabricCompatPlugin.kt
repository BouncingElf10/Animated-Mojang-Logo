package fabric.compat

import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project

class FabricCompatPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("checkMinecraftCompatibility") {
            group = "fabric"
            description = "Tests compilation across Minecraft versions"

            doLast {
                try {
                    val output = runBlocking { GradleRunner.doTests() }
                    runBlocking { GradleRunner.tryAndBuild() }
                    println(output.toString())
                } catch (_: Throwable) { println("whoops") }
            }
        }
    }
}