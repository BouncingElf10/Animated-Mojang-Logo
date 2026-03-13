package fabric.compat

import fabric.compat.structs.Ansi
import java.io.File

object CompatPatcher {
    fun buildMinecraftRange(passingVersions: List<String>): String {
        if (passingVersions.isEmpty()) return "*"
        val sorted = passingVersions.sortedWith(compareBy {
            it.split(".").map { p -> p.toIntOrNull() ?: 0 }.let { parts ->
                parts.getOrElse(0) { 0 } * 10_000 + parts.getOrElse(1) { 0 } * 100 + parts.getOrElse(2) { 0 }
            }
        })
        return if (sorted.size == 1) sorted[0] else ">=${sorted.first()} <=${sorted.last()}"
    }

    fun backupDir(projectDir: File): File = File(projectDir, ".gradle/compat-backups")

    private fun writeBackup(projectDir: File, file: File, content: String) {
        File(backupDir(projectDir).also { it.mkdirs() }, file.name + ".bak").writeText(content)
    }

    fun revertFromBackups(projectDir: File): List<RevertResult> {
        val backupDir = backupDir(projectDir)
        if (!backupDir.exists()) return listOf(RevertResult.NoneFound)

        val bakFiles = backupDir.listFiles { f -> f.name.endsWith(".bak") }?.toList().orEmpty()
        if (bakFiles.isEmpty()) return listOf(RevertResult.NoneFound)

        val destinationCandidates: Map<String, List<File>> = mapOf(
            "fabric.mod.json.bak" to listOf(
                File(projectDir, "src/main/resources/fabric.mod.json"), File(projectDir, "fabric.mod.json")
            ),
            "build.gradle.kts.bak" to listOf(File(projectDir, "build.gradle.kts")),
            "build.gradle.bak" to listOf(File(projectDir, "build.gradle"))
        )

        return bakFiles.map { bak ->
            val candidates = destinationCandidates[bak.name]
            if (candidates == null) {
                RevertResult.UnknownBackup(bak.name)
            } else {
                val dest = candidates.firstOrNull { it.exists() }
                if (dest == null) {
                    RevertResult.DestinationMissing(bak.name, candidates.map { it.path })
                } else {
                    dest.writeText(bak.readText())
                    bak.delete()
                    RevertResult.Restored(dest.path)
                }
            }
        }
    }

    sealed class RevertResult {
        data class Restored(val filePath: String) : RevertResult()
        data class DestinationMissing(val bakName: String, val tried: List<String>) : RevertResult()
        data class UnknownBackup(val bakName: String) : RevertResult()
        object NoneFound : RevertResult()
    }

    fun printRevertResult(result: RevertResult) {
        when (result) {
            is RevertResult.Restored -> println("  ${Ansi.GREEN}RESTORED${Ansi.RESET}  ${Ansi.DIM}${result.filePath}${Ansi.RESET}")
            is RevertResult.DestinationMissing -> println("  ${Ansi.RED}MISSING${Ansi.RESET}   ${result.bakName} — destination not found (tried: ${result.tried.joinToString()})")
            is RevertResult.UnknownBackup -> println("  ${Ansi.DIM}SKIP${Ansi.RESET}      ${result.bakName} — not a recognised backup, leaving in place")
            is RevertResult.NoneFound -> println("  ${Ansi.DIM}Nothing to revert — no backups found in .gradle/compat-backups/${Ansi.RESET}")
        }
    }

    fun patchFabricModJson(projectDir: File, range: String): PatchResult {
        val candidates = listOf(
            File(projectDir, "src/main/resources/fabric.mod.json"), File(projectDir, "fabric.mod.json")
        )
        val modJson = candidates.firstOrNull { it.exists() }
            ?: return PatchResult.NotFound("fabric.mod.json not found in src/main/resources/ or project root")

        val original = modJson.readText()


        val regex = Regex("""("minecraft"\s*:\s*)"([^"]*)"(?=\s*[,\}])""")
        if (!regex.containsMatchIn(original)) {
            return PatchResult.NotFound("""No "minecraft" key found inside depends in ${modJson.path}""")
        }

        val patched = regex.replace(original) { mr -> """${mr.groupValues[1]}"$range"""" }
        if (patched == original) return PatchResult.NoChange

        writeBackup(projectDir, modJson, original)
        modJson.writeText(patched)
        return PatchResult.Success(modJson.path)
    }

    fun patchBuildGradle(projectDir: File, passingVersions: List<String>): PatchResult {
        val candidates = listOf(
            File(projectDir, "build.gradle.kts"), File(projectDir, "build.gradle")
        )
        val buildFile = candidates.firstOrNull { it.exists() }
            ?: return PatchResult.NotFound("build.gradle / build.gradle.kts not found")

        val original = buildFile.readText()

        if (!original.contains("publishMods")) {
            return PatchResult.NotFound(
                "No publishMods block found in ${buildFile.name} — mod-publish-plugin not detected"
            )
        }

        val addCallRegex = Regex("""([ \t]*)minecraftVersions\.add\(["'][^"']+["']\)\n?""")

        if (!addCallRegex.containsMatchIn(original)) {
            return PatchResult.NotFound("No minecraftVersions.add(...) calls found in ${buildFile.name}")
        }

        val patched = replaceInEachPublisherBlock(original, passingVersions, addCallRegex)

        if (patched == original) return PatchResult.NoChange

        writeBackup(projectDir, buildFile, original)
        buildFile.writeText(patched)
        return PatchResult.Success(buildFile.path)
    }

    private fun replaceInEachPublisherBlock(
        source: String, passingVersions: List<String>, addCallRegex: Regex
    ): String {
        val publisherNames = setOf("modrinth", "curseforge", "github", "gitlab", "discord")
        val blockOpenRegex = Regex("""\b(${publisherNames.joinToString("|")})\s*\{""")

        val result = StringBuilder(source)
        var offset = 0

        for (match in blockOpenRegex.findAll(source)) {
            val openBracePos = match.range.last
            val blockStart = openBracePos + 1

            val blockEnd = findMatchingBrace(source, openBracePos) ?: continue


            val adjStart = blockStart + offset
            val adjEnd = blockEnd + offset
            val blockBody = result.substring(adjStart, adjEnd)


            val firstAdd = addCallRegex.find(blockBody) ?: continue
            val indent = firstAdd.groupValues[1]


            val replacement = passingVersions.joinToString("\n") { v ->
                """${indent}minecraftVersions.add("$v")"""
            } + "\n"


            var insertPos: Int? = null
            val stripped = buildString {
                var pos = 0
                for (m in addCallRegex.findAll(blockBody)) {
                    if (insertPos == null) insertPos = m.range.first
                    append(blockBody, pos, m.range.first)
                    pos = m.range.last + 1
                }
                append(blockBody, pos, blockBody.length)
            }


            val newBody = if (insertPos != null) {
                stripped.substring(0, insertPos!!) + replacement + stripped.substring(insertPos!!)
            } else {
                stripped
            }

            result.replace(adjStart, adjEnd, newBody)
            offset += newBody.length - blockBody.length
        }

        return result.toString()
    }

    private fun findMatchingBrace(source: String, openPos: Int): Int? {
        var depth = 0
        for (i in openPos until source.length) {
            when (source[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return null
    }

    sealed class PatchResult {
        data class Success(val filePath: String) : PatchResult()
        data class NotFound(val reason: String) : PatchResult()
        object NoChange : PatchResult()
    }

    fun printPatchResult(label: String, result: PatchResult) {
        when (result) {
            is PatchResult.Success -> println("  ${Ansi.GREEN}PATCHED${Ansi.RESET}  $label → ${Ansi.DIM}${result.filePath}${Ansi.RESET}")

            is PatchResult.NotFound -> println("  ${Ansi.DIM}SKIP${Ansi.RESET}     $label — ${result.reason}")

            is PatchResult.NoChange -> println("  ${Ansi.DIM}SKIP${Ansi.RESET}     $label — already up to date")
        }
    }
}