package minecraft.compat

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.File


data class NeoForgeVersions(
    val mcVersion: String,
    val neoforgeVersion: String
)

object NeoForgeMeta {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private const val NEO_MAVEN_META =
        "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml"

    fun mcVersionToNeoPrefix(mcVersion: String): String {
        val parts = mcVersion.split(".")
        val major = parts.getOrElse(1) { "0" }
        val minor = parts.getOrElse(2) { "0" }
        return "$major.$minor."
    }

    private fun neoMajorMinorToMc(majorMinor: String): String {
        val (major, minor) = majorMinor.split(".", limit = 2).let {
            it[0] to (it.getOrElse(1) { "0" })
        }
        return if (minor == "0") "1.$major" else "1.$major.$minor"
    }

    private suspend fun fetchAllNeoVersions(): List<String> {
        val xml = client.get(NEO_MAVEN_META).body<String>()
        return Regex("<version>([^<]+)</version>").findAll(xml)
            .map { it.groupValues[1] }
            .filter { v ->
                !v.contains("beta", ignoreCase = true) &&
                        !v.contains("alpha", ignoreCase = true) &&
                        v.count { it == '.' } >= 2
            }
            .toList()
    }

    suspend fun getAllSupportedMcVersions(): List<String> {
        val seen = LinkedHashSet<String>()

        for (v in fetchAllNeoVersions().reversed()) {
            val parts = v.split(".")
            if (parts.size < 3) continue
            seen.add(neoMajorMinorToMc("${parts[0]}.${parts[1]}"))
        }
        return seen.toList()
    }

    suspend fun getLatestNeoForgeFor(mcVersion: String): String {
        val prefix = mcVersionToNeoPrefix(mcVersion)
        return fetchAllNeoVersions()
            .filter { it.startsWith(prefix) }
            .lastOrNull()
            ?: error("No NeoForge release found for MC $mcVersion")
    }

    suspend fun resolveVersions(mcVersion: String, overrideNeoVersion: String? = null): NeoForgeVersions {
        val neoVersion = overrideNeoVersion ?: getLatestNeoForgeFor(mcVersion)
        return NeoForgeVersions(mcVersion, neoVersion)
    }

    fun getCurrentMcVersion(projectDir: File): String =
        readProperty(projectDir, "minecraft_version")
            ?: error("minecraft_version not found in ${File(projectDir, "gradle.properties").absolutePath}")

    fun getCurrentNeoVersion(projectDir: File): String? =
        readProperty(projectDir, "neo_version")

    private fun readProperty(projectDir: File, key: String): String? =
        File(projectDir, "gradle.properties")
            .takeIf { it.exists() }
            ?.readLines()
            ?.firstOrNull { it.startsWith("$key=") }
            ?.substringAfter('=')?.trim()

    suspend fun getNextMcVersionAfter(current: String): String? {
        val versions = getAllSupportedMcVersions().sortedWith(
            compareByDescending {
                it.split(".").map { p -> p.toIntOrNull() ?: 0 }
                    .let { p -> p.getOrElse(0) { 0 } * 10_000 + p.getOrElse(1) { 0 } * 100 + p.getOrElse(2) { 0 } }
            }
        )
        val idx = versions.indexOfFirst { it == current }
        return if (idx > 0) versions[idx - 1] else null
    }
}