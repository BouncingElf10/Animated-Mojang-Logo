package fabric.compat

/**
 * Configuration for the fabric-compat plugin.
 *
 * In build.gradle:
 *
 *   fabricCompat {
 *       maxStrikes = 2
 *       checkNewerVersions = false
 *       loomVersion = "1.9-SNAPSHOT"
 *   }
 */
open class FabricCompatExtension {

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
     * If null (default), the loom version is resolved automatically from the
     * Fabric Maven metadata to the latest version compatible with each MC version.
     * Set this if auto-resolution picks a version that doesn't work for you.
     * Example: "1.9-SNAPSHOT"
     */
    var loomVersion: String? = null

    /**
     * Override the Fabric loader version used for all test builds.
     * If null (default), the latest stable loader is used.
     */
    var loaderVersion: String? = null

    /**
     * Gradle tasks to invoke for each test build.
     * Default: ["build"] — change to e.g. ["compileJava"] for a faster check
     * that skips jar packaging and other post-compile steps.
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

    /**
     * If true, the next version will bump up the version of the mod, if false, it will bump down.
     * Default: true
     */
    var bumpUp: Boolean = true
}