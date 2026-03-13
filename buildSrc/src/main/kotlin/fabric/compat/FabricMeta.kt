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

    fun getCurrentVersion(projectDir: File): String {
        val props = File(projectDir, "gradle.properties")
        return props.readLines().firstOrNull { it.startsWith("minecraft_version=") }?.substringAfter('=')?.trim()
            ?: error("minecraft_version not found in ${props.absolutePath}")
    }

    private suspend fun getStableVersionList(): List<MinecraftVersion> {
        return client.get("https://meta.fabricmc.net/v2/versions").body<VersionsResponse>().game.filter { mv ->
            mv.stable && mv.version.lowercase(getDefault()).trim().matches(Regex("""^\d+\.\d+(\.\d+)?$"""))
        }
    }

    suspend fun getAllStableVersions(): List<MinecraftVersion> = getStableVersionList()

    suspend fun prewarmLoader() {
        if (loaderCache != null) return
        val loaders = client.get("https://meta.fabricmc.net/v2/versions/loader").body<List<LoaderVersion>>()
        loaderCache = loaders.firstOrNull { it.stable } ?: loaders.first()
    }

    suspend fun resolveVersions(mcVersion: String): Versions {
        val loader = loaderCache ?: error("Call prewarmLoader() before resolveVersions()")

        val yarnList = client.get("https://meta.fabricmc.net/v2/versions/yarn/$mcVersion").body<List<YarnVersion>>()

        val yarn = yarnList.firstOrNull { it.stable } ?: yarnList.firstOrNull()
            ?: error("No yarn mappings found for $mcVersion")

        val fabricApi = getFabricApiVersion(mcVersion)

        return Versions(
            mcVersion = MinecraftVersion(mcVersion, true),
            loaderVersion = loader,
            yarnVersion = yarn,
            fabricApiVersion = fabricApi,
            loomVersion = "1.15-SNAPSHOT"
        )
    }

    suspend fun getFabricApiVersion(mcVersion: String): String {
        val xml = client.get("https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml")
            .body<String>()

        return Regex("<version>([^<]+\\+$mcVersion)</version>").findAll(xml).map { it.groupValues[1] }.lastOrNull()
            ?: error("No Fabric API version for $mcVersion")
    }
}