package de.olivermakesco.switchykit

import dev.proxyfox.pluralkt.types.PkColor
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
