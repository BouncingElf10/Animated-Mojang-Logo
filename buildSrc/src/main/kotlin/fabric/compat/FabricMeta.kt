package fabric.compat

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

    private var loaderCache: LoaderVersion? = null

    fun getCurrentVersion(): String {
        val version = File("gradle.properties").readLines().filter { str -> str.startsWith("minecraft_version") }
        return version[0].split("=")[1].trim()
    }

    private suspend fun getStableVersionList(): List<MinecraftVersion> {
        val response = client.get("https://meta.fabricmc.net/v2/versions").body<VersionsResponse>()
        return response.game.filter {
            val mc = it.version.lowercase(getDefault()).trim()
            it.stable && mc.contains(Regex("^\\d+\\.\\d+\\.\\d+$"))
        }
    }

    suspend fun getNextVersion(version: String): String {
        val stableVersionList = getStableVersionList()
        val currentIndex = stableVersionList.indexOfFirst { it.version == version }
        return try {
            stableVersionList[currentIndex + 1].version
        } catch (_: Exception) {
            "No more versions to check!"
        }
    }

    suspend fun getPrevVersion(version: String): String {
        val stableVersionList = getStableVersionList()
        val currentIndex = stableVersionList.indexOfFirst { it.version == version }
        return try {
            stableVersionList[currentIndex - 1].version
        } catch (_: Exception) {
            "No more versions to check!"
        }
    }

    suspend fun resolveVersions(mcVersion: String): Versions {
        val loader = loaderCache ?: run {
            val loaders = client
                .get("https://meta.fabricmc.net/v2/versions/loader")
                .body<List<LoaderVersion>>()

            val l = loaders.firstOrNull { it.stable }
                ?: loaders.first()

            loaderCache = l
            l
        }

        val yarnList = client
            .get("https://meta.fabricmc.net/v2/versions/yarn/$mcVersion")
            .body<List<YarnVersion>>()

        val yarn = yarnList.firstOrNull { it.stable }
            ?: yarnList.firstOrNull()
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
        val xml = client.get(
            "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml"
        ).body<String>()

        val regex = Regex("<version>([^<]+\\+$mcVersion)</version>")
        val matches = regex.findAll(xml).map { it.groupValues[1] }.toList()

        return matches.lastOrNull()
            ?: error("No Fabric API version for $mcVersion")
    }
}