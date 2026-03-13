package fabric.compat

import fabric.compat.structs.Ansi
import kotlinx.coroutines.runBlocking
import org.gradle.api.Plugin
import org.gradle.api.Project

class FabricCompatPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("checkMinecraftCompatibility") {
            group = "fabric"
            description = "Tests compilation across Minecraft versions"

            doLast {
                val results = runBlocking { GradleRunner.doTests(project.projectDir) }

                val sorted = results.sortedWith(compareBy { r ->
                    r.versions.mcVersion.version.split(".").map { it.toIntOrNull() ?: 0 }
                        .let { p -> p.getOrElse(0) { 0 } * 10_000 + p.getOrElse(1) { 0 } * 100 + p.getOrElse(2) { 0 } }
                })

                val passes = sorted.count { it.buildResult.success }
                val failures = sorted.count { !it.buildResult.success }

                println()
                println("${Ansi.BOLD}Fabric Compatibility Results${Ansi.RESET}")
                println()

                sorted.forEach { r ->
                    val mc = r.versions.mcVersion.version
                    val yarn = r.versions.yarnVersion.version
                    val api = r.versions.fabricApiVersion

                    if (r.buildResult.success) {
                        println("${Ansi.GREEN}PASS${Ansi.RESET}  ${Ansi.BOLD}MC $mc${Ansi.RESET}  ${Ansi.DIM}yarn $yarn    api $api${Ansi.RESET}")
                    } else {
                        println("${Ansi.RED}FAIL${Ansi.RESET}  ${Ansi.BOLD}MC $mc${Ansi.RESET}  ${Ansi.DIM}yarn $yarn    api $api${Ansi.RESET}")
                    }
                }

                println("${Ansi.DIM}${"─".repeat(44)}${Ansi.RESET}")
                val passText = "${Ansi.GREEN}$passes passed${Ansi.RESET}"
                val failText = if (failures > 0) "${Ansi.RED}$failures failed${Ansi.RESET}" else "${Ansi.DIM}0 failed${Ansi.RESET}"
                println("${sorted.size} versions tested    $passText    $failText")
                println()

                if (failures > 0) {
                    throw RuntimeException("Compatibility check failed for $failures version(s).")
                }
            }
        }
    }
}