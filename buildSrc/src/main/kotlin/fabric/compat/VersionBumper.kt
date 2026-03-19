package fabric.compat

import fabric.compat.structs.Ansi
import fabric.compat.structs.Versions
import java.io.File

object VersionBumper {
    data class BumpPlan(
        val oldVersion: String,
        val newVersion: String,
        val newVersions: Versions,
        val knownUpdates: List<PropertyUpdate>,
        val unknownHits: List<UnknownProperty>
    )

    data class PropertyUpdate(
        val key: String, val oldValue: String, val newValue: String
    )

    data class UnknownProperty(
        val key: String, val value: String, val reason: String
    )

    private val KNOWN_KEYS = setOf(
        "minecraft_version", "yarn_mappings", "loader_version", "fabric_api_version", "loom_version"
    )

    fun resolveCurrentTargetVersion(projectDir: File): String? {
        val candidates = listOf(
            File(projectDir, "src/main/resources/fabric.mod.json"), File(projectDir, "fabric.mod.json")
        )
        val modJson = candidates.firstOrNull { it.exists() } ?: return null
        val content = modJson.readText()

        val minecraftDep = Regex(""""minecraft"\s*:\s*"([^"]+)"""").find(content)?.groupValues?.get(1) ?: return null
        val versionPattern = Regex("""\d+\.\d+(?:\.\d+)?""")
        val found = versionPattern.findAll(minecraftDep).map { it.value }.toList()

        return found.maxWithOrNull(compareBy {
            it.split(".").map { p -> p.toIntOrNull() ?: 0 }
                .let { parts -> parts.getOrElse(0) { 0 } * 10_000 + parts.getOrElse(1) { 0 } * 100 + parts.getOrElse(2) { 0 } }
        })
    }

    fun buildPlan(projectDir: File, oldVersion: String, newVersions: Versions): BumpPlan {
        val newVersion = newVersions.mcVersion.version
        val props = readProperties(projectDir)

        val knownUpdates = mutableListOf<PropertyUpdate>()
        val unknownHits = mutableListOf<UnknownProperty>()

        val knownNewValues = mapOf(
            "minecraft_version" to newVersion,
            "yarn_mappings" to newVersions.yarnVersion.version,
            "loader_version" to newVersions.loaderVersion.version,
            "fabric_api_version" to newVersions.fabricApiVersion,
            "loom_version" to newVersions.loomVersion
        )

        for ((key, currentValue) in props) {
            if (key in KNOWN_KEYS) {
                val newValue = knownNewValues[key] ?: continue
                if (currentValue != newValue) {
                    knownUpdates.add(PropertyUpdate(key, currentValue, newValue))
                }
            } else {
                if (currentValue.contains(oldVersion)) {
                    unknownHits.add(
                        UnknownProperty(
                            key = key, value = currentValue, reason = "value contains \"$oldVersion\" - update manually"
                        )
                    )
                }
            }
        }

        return BumpPlan(oldVersion, newVersion, newVersions, knownUpdates, unknownHits)
    }

    fun applyBump(projectDir: File, plan: BumpPlan) {
        val propsFile = File(projectDir, "gradle.properties")
        val original = propsFile.readText()

        CompatPatcher.writeBackup(projectDir, propsFile, original)

        var updated = original
        for (change in plan.knownUpdates) {
            updated = updated.replace(
                Regex("""(?m)^(${Regex.escape(change.key)}\s*=\s*).*$"""), "$1${change.newValue}"
            )
        }

        propsFile.writeText(updated)
    }

    fun printPlan(plan: BumpPlan) {
        println("  ${Ansi.DIM}${"─".repeat(50)}${Ansi.RESET}")
        println("  Bumping  ${Ansi.BOLD}${plan.oldVersion}${Ansi.RESET}  →  ${Ansi.BOLD}${Ansi.GREEN}${plan.newVersion}${Ansi.RESET}")
        println("  ${Ansi.DIM}${"─".repeat(50)}${Ansi.RESET}")
        println()

        if (plan.knownUpdates.isEmpty()) {
            println("  ${Ansi.DIM}No gradle.properties changes needed.${Ansi.RESET}")
        } else {
            println("  ${Ansi.BOLD}gradle.properties changes:${Ansi.RESET}")
            plan.knownUpdates.forEach { u ->
                val old = "${Ansi.DIM}${u.oldValue}${Ansi.RESET}"
                val new = "${Ansi.GREEN}${u.newValue}${Ansi.RESET}"
                println("    ${u.key.padEnd(24)} $old  →  $new")
            }
        }

        if (plan.unknownHits.isNotEmpty()) {
            println()
            println("${Ansi.YELLOW}Custom properties referencing ${plan.oldVersion} - update these manually:${Ansi.RESET}")
            plan.unknownHits.forEach { u ->
                println("    ${Ansi.BOLD}${u.key}${Ansi.RESET} = ${Ansi.DIM}${u.value}${Ansi.RESET}")
                println("    ${Ansi.DIM}    ${u.reason}${Ansi.RESET}")
            }
        }

        println()
    }

    private fun readProperties(projectDir: File): Map<String, String> {
        return File(projectDir, "gradle.properties").readLines()
            .filter { it.contains('=') && !it.trimStart().startsWith('#') }.associate { line ->
                val key = line.substringBefore('=').trim()
                val value = line.substringAfter('=').trim()
                key to value
            }
    }
}