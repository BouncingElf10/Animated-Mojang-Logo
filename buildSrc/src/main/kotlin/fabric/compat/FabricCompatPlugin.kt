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
                val isDoingForwardPass = true
                val tries = 0

                var version = FabricMeta.getCurrentVersion()
                val resolved = runBlocking { FabricMeta.resolveVersions(version) }

                var versiontoloso = version
                for (i in 1..10) {
                    println(versiontoloso)
                    versiontoloso =  runBlocking { FabricMeta.getNextVersion(versiontoloso) }
                }

                //GradleRunner.tryAndBuild()

                println(
                    """
                    minecraft_version=$version
                    yarn_mappings=${resolved.yarnVersion.version}
                    loader_version=${resolved.loaderVersion.version}
                    loom_version=${resolved.loomVersion}
                    
                    # Fabric API
                    fabric_api_version=${resolved.fabricApiVersion}
                    """.trimIndent()
                )
            }
        }
    }
}