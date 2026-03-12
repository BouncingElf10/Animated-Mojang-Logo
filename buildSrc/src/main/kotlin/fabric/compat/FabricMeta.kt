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
    const val NO_MORE_VERSIONS = "__NO_MORE_VERSIONS__"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private var loaderCache: LoaderVersion? = null

    fun getCurrentVersion(): String {
        val lines = File("gradle.properties").readLines()
        val versionLine = lines.firstOrNull { it.startsWith("minecraft_version=") }
            ?: error("minecraft_version not found in gradle.properties")
        return versionLine.split("=")[1].trim()
    }

    private suspend fun getStableVersionList(): List<MinecraftVersion> {
        val response = client.get("https://meta.fabricmc.net/v2/versions").body<VersionsResponse>()
        return response.game.filter {
            val mc = it.version.lowercase(getDefault()).trim()
            // Match both x.y and x.y.z version formats
            it.stable && mc.contains(Regex("^\\d+\\.\\d+(\\.\\d+)?$"))
        }
    }

    suspend fun getNextVersion(version: String): String {
        val stableVersionList = getStableVersionList()
        val currentIndex = stableVersionList.indexOfFirst { it.version == version }
        return if (currentIndex != -1 && currentIndex + 1 < stableVersionList.size) {
            stableVersionList[currentIndex + 1].version
        } else {
            NO_MORE_VERSIONS
        }
    }

    suspend fun getPrevVersion(version: String): String {
        val stableVersionList = getStableVersionList()
        val currentIndex = stableVersionList.indexOfFirst { it.version == version }
        return if (currentIndex > 0) {
            stableVersionList[currentIndex - 1].version
        } else {
            NO_MORE_VERSIONS
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

    fun close() {
        client.close()
    }
}