package fabric.compat

import java.io.File

object FileManager {
    fun dupeGradleProperties() {
        val gradleProperties = File("gradle.properties")
        val dupeProperties = File("gradle.properties.dupe")
        gradleProperties.copyTo(dupeProperties, overwrite = true)
    }
}