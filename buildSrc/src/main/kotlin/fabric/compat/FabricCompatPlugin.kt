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

                val versions = listOf(
                    "1.20.6",
                    "1.21",
                    "1.21.1",
                    "1.21.2",
                    "1.21.3"
                )

                versions.forEach { mc ->

                    val resolved = runBlocking {
                        FabricMeta.resolveVersions(mc)
                    }

                    println(
                        """
                        minecraft_version=$mc
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
}