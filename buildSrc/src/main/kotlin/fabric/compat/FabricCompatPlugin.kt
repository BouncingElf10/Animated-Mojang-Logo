package fabric.compat

import org.gradle.api.Plugin
import org.gradle.api.Project

class FabricCompatPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("checkMinecraftCompatibility") {
            group = "fabric"
            description = "Tests compilation across Minecraft versions"

            doLast {
                val output = GradleRunner.doTests()
            }
        }
    }
}