package minecraft.compat

import java.io.File

/**
 * Configuration for the fabric-compat plugin.
 *
 * In build.gradle.kts:
 *
 *   fabricCompat {
 *       // Loader is auto-detected from project files; override explicitly if needed:
 *       modLoader = ModLoader.NEOFORGE
 *
 *       maxStrikes = 2
 *       checkNewerVersions = false
 *
 *       // Fabric-specific overrides
 *       loomVersion = "1.9-SNAPSHOT"
 *
 *       // NeoForge-specific overrides
 *       neoforgeVersion = "21.1.85"
 *   }
 */
open class FabricCompatExtension {

    /**
     * The mod loader to target.
     * Default: null — auto-detected from project files via [ModLoader.detect].
     * Set explicitly if auto-detection picks the wrong loader.
     */
    var modLoader: ModLoader? = null

    /**
     * How many consecutive build failures in a given direction (older/newer)
     * before the runner stops walking that direction.
     * Default: 3
     */
    var maxStrikes: Int = 3

    /**
     * Whether to test Minecraft versions newer than your current version.
     * Default: true
     */
    var checkNewerVersions: Boolean = true

    /**
     * Whether to test Minecraft versions older than your current version.
     * Default: true
     */
    var checkOlderVersions: Boolean = true

    /**
     * Override the Fabric Loom version used for all test builds.
     * If null (default), auto-resolved from Fabric Maven metadata.
     * Example: "1.9-SNAPSHOT"
     */
    var loomVersion: String? = null

    /**
     * Override the Fabric loader version used for all test builds.
     * If null (default), the latest stable loader is used.
     */
    var loaderVersion: String? = null

    /**
     * Override the NeoForge version used for all test builds.
     * If null (default), the latest release for each tested MC version is used.
     * Example: "21.1.85"
     */
    var neoforgeVersion: String? = null

    /**
     * Gradle tasks to invoke for each test build.
     * Default: ["build"] — change to e.g. ["compileJava"] for a faster check.
     */
    var buildTasks: List<String> = listOf("build")

    /**
     * Extra JVM arguments passed to each spawned Gradle daemon.
     * Useful for memory tuning: e.g. listOf("-Xmx2g")
     */
    var daemonJvmArgs: List<String> = emptyList()

    /**
     * If true, each build's full output is printed even on success.
     * Default: false — only failures print a tail of their output.
     */
    var verbose: Boolean = false

    /** Returns the configured loader, or auto-detects from [projectDir]. */
    fun resolvedModLoader(projectDir: File): ModLoader =
        modLoader ?: ModLoader.detect(projectDir)
}
