package de.olivermakesco.switchykit.platform

import com.mojang.brigadier.CommandDispatcher
import de.olivermakesco.switchykit.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import net.minecraft.server.command.ServerCommandSource
import org.quiltmc.qkl.library.brigadier.argument.greedyString
import org.quiltmc.qkl.library.brigadier.argument.literal
import org.quiltmc.qkl.library.brigadier.execute
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import java.net.URL

object TUL {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("tul") {
            required(literal("import")) {
                required(greedyString("link")) {
                    execute {
                        val oldPresets = switchyPlayer.presets
                        reply("commands.switchykit.tul.import.wait")
                        val link = getArgument("link", String::class.java)
                        val tuppers: MinimalTupperRoot = json.decodeFromString(URL(link).readText())
                        val members = arrayListOf<MinimalMemberJson>()
                        for (tupper in tuppers.tuppers) {
                            members.add(MinimalMemberJson(
                                tupper.name,
                                tupper.nick,
                                null,
                                null,
                                arrayListOf()
                            ))
                        }
                        logger.."Importing system for ${player.name} (${player.uuidAsString} from Tul Export - $link"
                        import(
                            MinimalSystemJson(
                            null, members.toTypedArray()
                        ), oldPresets, player, "tul import $link")
                    }
                }
            }
        }
    }

    @Serializable
    data class MinimalTupperRoot(val tuppers: Array<MinimalTupperJson>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MinimalTupperRoot) return false

            if (!tuppers.contentEquals(other.tuppers)) return false

            return true
        }

        override fun hashCode(): Int {
            return tuppers.contentHashCode()
        }
    }

    @Serializable
    data class MinimalTupperJson(val name: String, val nick: String?, val tag: String?)
}
