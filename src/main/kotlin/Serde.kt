package de.olivermakesco.switchykit

import dev.proxyfox.pluralkt.types.PkColor
import dev.proxyfox.pluralkt.types.PkProxyTag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.quiltmc.loader.api.QuiltLoader
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Serializable
class SwitchyKitConfig {
    val tokens = HashMap<String, String>()
}

lateinit var config: SwitchyKitConfig

@Serializable
data class MinimalSystemJson(val tag: String?, val members: Array<MinimalMemberJson> = arrayOf()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MinimalSystemJson) return false

        if (tag != other.tag) return false
        if (!members.contentEquals(other.members)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + members.contentHashCode()
        return result
    }
}

@Serializable
data class MinimalMemberJson(
    val name: String,
    @SerialName("display_name") val displayName: String?,
    val pronouns: String?,
    val color: PkColor?,
    @SerialName("proxy_tags") val proxyTags: ArrayList<PkProxyTag>
)

val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

val path = QuiltLoader.getConfigDir().resolve("switchykit.json")

fun loadConfig() {
    if (!path.exists()) {
        config = SwitchyKitConfig()
        return
    }
    config = json.decodeFromString(path.readText())
}

fun saveConfig() {
    if (!path.exists()) {
        path.createFile()
    }
    path.writeText(json.encodeToString(config))
}
