package fabric.compat

import kotlinx.serialization.Serializable

@Serializable
data class LoaderVersion(
    val version: String,
    val stable: Boolean
)

@Serializable
data class YarnVersion(
    val version: String,
    val stable: Boolean
)

data class Versions(
    val loaderVersion: LoaderVersion,
    val yarnVersion: YarnVersion,
    val fabricApiVersion: String,
    val loomVersion: String
)