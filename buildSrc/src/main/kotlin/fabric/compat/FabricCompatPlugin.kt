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

            doFirst {
                println("Checking adjacent versions to see if they build. (This might take a while)")
            }

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

                val passingVersions = sorted.filter { it.buildResult.success }.map { it.versions.mcVersion.version }

                if (passingVersions.isEmpty()) {
                    println("${Ansi.RED}No passing versions — nothing to patch.${Ansi.RESET}")
                    return@doLast
                }

                val range = CompatPatcher.buildMinecraftRange(passingVersions)

                println("${Ansi.BOLD}Compatible range:${Ansi.RESET}  $range")
                println("${Ansi.BOLD}Passing versions:${Ansi.RESET}  ${passingVersions.joinToString(", ")}")
                println()

                println("Would you like to automatically update your project files?")
                println("  ${Ansi.BOLD}[1]${Ansi.RESET} Patch fabric.mod.json only")
                println("  ${Ansi.BOLD}[2]${Ansi.RESET} Patch build.gradle (mod-publish-plugin) only")
                println("  ${Ansi.BOLD}[3]${Ansi.RESET} Patch both")
                println("  ${Ansi.BOLD}[4]${Ansi.RESET} Skip — I'll update manually")
                print("Enter choice [1-4]: ")

                val choice = readLine()?.trim() ?: "4"
                println()

                val patchModJson = choice == "1" || choice == "3"
                val patchBuild = choice == "2" || choice == "3"

                if (!patchModJson && !patchBuild) {
                    println("${Ansi.DIM}Skipped. Manually set:${Ansi.RESET}")
                    println("  fabric.mod.json  →  \"minecraft\": \"$range\"")
                    println("  build.gradle     →  one minecraftVersions.add(...) per version above")
                    println()
                    return@doLast
                }

                println("${Ansi.BOLD}Patching files…${Ansi.RESET}")
                println()

                if (patchModJson) {
                    val result = CompatPatcher.patchFabricModJson(project.projectDir, range)
                    CompatPatcher.printPatchResult("fabric.mod.json  \"minecraft\": \"$range\"", result)
                }

                if (patchBuild) {
                    val result = CompatPatcher.patchBuildGradle(project.projectDir, passingVersions)
                    CompatPatcher.printPatchResult(
                        "build.gradle     minecraftVersions (${passingVersions.size} versions)", result
                    )
                }

                println()
                println("${Ansi.DIM}Backups written as *.bak in the .gradle/compat-backups/ directory${Ansi.RESET}")
                println()
            }
        }

        project.tasks.register("revertCompatPatches") {
            group = "fabric"
            description = "Restores project files from the compat-patch backups in .gradle/compat-backups/"

            doLast {
                println()
                println("${Ansi.BOLD}Reverting Fabric compat patches…${Ansi.RESET}")
                println()

                val results = CompatPatcher.revertFromBackups(project.projectDir)
                results.forEach { CompatPatcher.printRevertResult(it) }

                val restored = results.filterIsInstance<CompatPatcher.RevertResult.Restored>()
                println()
                if (restored.isNotEmpty()) {
                    println("${Ansi.GREEN}${restored.size} file(s) restored.${Ansi.RESET}  Backups removed from .gradle/compat-backups/")
                } else {
                    println("${Ansi.DIM}No files were restored.${Ansi.RESET}")
                }
                println()
            }
        }
    }
}