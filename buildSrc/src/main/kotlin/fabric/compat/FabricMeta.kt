package fabric.compat

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object FabricMeta {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private var loaderCache: LoaderVersion? = null

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