package fabric.compat

import java.io.File

object FileManager {
    fun dupeGradleProperties() {
        val gradleProperties = File("gradle.properties")
        val dupeProperties = File("gradle.properties.dupe")
        gradleProperties.copyTo(dupeProperties, overwrite = true)
    }

    fun replaceProperties(versions: Versions, file: File = File("gradle.properties")) {
        val lines = file.readLines().toMutableList()

        fun replace(key: String, value: String) {
            val index = lines.indexOfFirst { it.startsWith("$key=") }
            if (index != -1) {
                lines[index] = "$key=$value"
            }
        }

        replace("minecraft_version", versions.mcVersion.version)
        replace("loader_version", versions.loaderVersion.version)
        replace("loom_version", versions.loomVersion)
        replace("fabric_api_version", versions.fabricApiVersion)

        file.writeText(lines.joinToString("\n"))
    }

    fun copyAndRevert() {
        val gradleProperties = File("gradle.properties")
        val dupeProperties = File("gradle.properties.dupe")
        dupeProperties.copyTo(gradleProperties, overwrite = true)
        dupeProperties.delete()
    }
}