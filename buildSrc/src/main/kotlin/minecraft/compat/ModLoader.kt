package minecraft.compat

import java.io.File

enum class ModLoader {
    FABRIC, NEOFORGE;

    companion object {
        fun detect(projectDir: File): ModLoader {
            if (File(projectDir, "src/main/resources/META-INF/neoforge.mods.toml").exists()) {
                return NEOFORGE
            }

            val buildFiles = listOf(File(projectDir, "build.gradle.kts"), File(projectDir, "build.gradle"))
            val buildContent = buildFiles.firstOrNull { it.exists() }?.readText().orEmpty()
            if (buildContent.contains("net.neoforged") || buildContent.contains("neogradle")) {
                return NEOFORGE
            }

            return FABRIC
        }
    }
}
