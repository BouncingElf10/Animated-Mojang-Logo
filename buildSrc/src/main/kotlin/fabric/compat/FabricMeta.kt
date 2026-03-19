package fabric.compat

import fabric.compat.structs.LoaderVersion
import fabric.compat.structs.MinecraftVersion
import fabric.compat.structs.Versions
import fabric.compat.structs.VersionsResponse
import fabric.compat.structs.YarnVersion
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale.getDefault

object FabricMeta {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Volatile
    private var loaderCache: LoaderVersion? = null

    @Volatile
    private var loomVersionCache: String? = null

    fun getCurrentVersion(projectDir: File): String = readGradleProperty(projectDir, "minecraft_version") ?: error(
        "minecraft_version not found in ${File(projectDir, "gradle.properties").absolutePath}"
    )

    fun getLoomVersionFromProperties(projectDir: File): String? = readGradleProperty(projectDir, "loom_version")

    fun getLoaderVersionFromProperties(projectDir: File): String? = readGradleProperty(projectDir, "loader_version")

    private fun readGradleProperty(projectDir: File, key: String): String? =
        File(projectDir, "gradle.properties").readLines().firstOrNull { it.startsWith("$key=") }?.substringAfter('=')
            ?.trim()

    private suspend fun getStableVersionList(): List<MinecraftVersion> {
        return client.get("https://meta.fabricmc.net/v2/versions").body<VersionsResponse>().game.filter { mv ->
            mv.stable && mv.version.lowercase(getDefault()).trim().matches(Regex("""^\d+\.\d+(\.\d+)?$"""))
        }
    }

    suspend fun getAllStableVersions(): List<MinecraftVersion> = getStableVersionList()

    suspend fun getNextStableVersionAfter(version: String): String? {
        val list = getStableVersionList()
        val idx = list.indexOfFirst { it.version == version }
        return if (idx > 0) list[idx - 1].version else null
    }

    suspend fun prewarmLoader(overrideVersion: String? = null, projectDir: File? = null) {
        if (overrideVersion != null) {
            loaderCache = LoaderVersion(overrideVersion, true)
            return
        }
        if (loaderCache != null) return

        val fromProps = projectDir?.let { getLoaderVersionFromProperties(it) }
        if (fromProps != null) {
            loaderCache = LoaderVersion(fromProps, true)
            return
        }

        val loaders = client.get("https://meta.fabricmc.net/v2/versions/loader").body<List<LoaderVersion>>()
        loaderCache = loaders.firstOrNull { it.stable } ?: loaders.first()
    }

    suspend fun resolveLoomVersion(override: String? = null, projectDir: File? = null): String {
        if (override != null) return override
        loomVersionCache?.let { return it }

        val fromProps = projectDir?.let { getLoomVersionFromProperties(it) }
        if (fromProps != null) {
            loomVersionCache = fromProps
            return fromProps
        }

        val xml = client.get("https://maven.fabricmc.net/net/fabricmc/loom/maven-metadata.xml").body<String>()

        val versions = Regex("<version>([^<]+)</version>").findAll(xml).map { it.groupValues[1] }.toList()

        val stable = versions.lastOrNull { !it.contains("SNAPSHOT", ignoreCase = true) }
        val resolved =
            stable ?: versions.lastOrNull() ?: error("Could not resolve Fabric Loom version from Maven metadata")

        loomVersionCache = resolved
        return resolved
    }

    suspend fun resolveVersions(mcVersion: String, config: FabricCompatExtension, projectDir: File): Versions {
        val loader = loaderCache ?: error("Call prewarmLoader() before resolveVersions()")
        val loom = resolveLoomVersion(config.loomVersion, projectDir)

        val yarnList = client.get("https://meta.fabricmc.net/v2/versions/yarn/$mcVersion").body<List<YarnVersion>>()

        val yarn = yarnList.firstOrNull { it.stable } ?: yarnList.firstOrNull()
        ?: error("No yarn mappings found for $mcVersion")

        val fabricApi = getFabricApiVersion(mcVersion)

        return Versions(
            mcVersion = MinecraftVersion(mcVersion, true),
            loaderVersion = loader,
            yarnVersion = yarn,
            fabricApiVersion = fabricApi,
            loomVersion = loom
        )
    }

    suspend fun getFabricApiVersion(mcVersion: String): String {
        val xml = client.get("https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml")
            .body<String>()

        return Regex("<version>([^<]+\\+$mcVersion)</version>").findAll(xml).map { it.groupValues[1] }.lastOrNull()
            ?: error("No Fabric API version for $mcVersion")
    }
}