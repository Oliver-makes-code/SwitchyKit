package de.olivermakesco.switchykit.platform

import com.mojang.brigadier.CommandDispatcher
import de.olivermakesco.switchykit.*
import dev.proxyfox.pluralkt.PluralKt
import kotlinx.serialization.decodeFromString
import net.minecraft.server.command.ServerCommandSource
import org.quiltmc.qkl.library.brigadier.argument.greedyString
import org.quiltmc.qkl.library.brigadier.argument.literal
import org.quiltmc.qkl.library.brigadier.execute
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import java.net.URL

object PK {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("pk") {
            required(literal("import")) {
                execute {
                    val token = config.tokens[player.uuidAsString] ?: run {
                        command("commands.switchykit.pk.import.fail.token", "/pk token", "/pk import")
                        return@execute
                    }
                    val oldPresets = switchyPlayer.presets
                    reply("commands.switchykit.pk.import.wait")
                    PluralKt.System.getMe(token).onComplete {
                        if (!isSuccess()) {
                            if (!isError()) {
                                reply("commands.switchykit.pk.import.fail", "Unknown error.")
                                return@onComplete
                            }
                            reply("commands.switchykit.pk.import.fail", getError().message)
                            return@onComplete
                        }
                        val system = getSuccess()
                        val members = PluralKt.Member.getMembers(system.id, token).await().run {
                            if (!isSuccess()) {
                                if (!isError()) {
                                    reply("commands.switchykit.pk.import.fail", "Unknown error.")
                                    return@onComplete
                                }
                                reply("commands.switchykit.pk.import.fail", getError().message)
                                return@onComplete
                            }
                            getSuccess()
                        }
                        val membersJson = arrayListOf<MinimalMemberJson>()
                        members.forEach {
                            membersJson += MinimalMemberJson(it.name, it.displayName, it.pronouns, it.color, it.proxyTags)
                        }
                        logger.."Importing system for ${player.name} (${player.uuidAsString} from PK API - ${system.id}"
                        import(MinimalSystemJson(system.tag, membersJson.toTypedArray()), oldPresets, player, "pk import")
                    }
                }
                required(greedyString("link")) {
                    execute {
                        try {
                            val oldPresets = switchyPlayer.presets
                            reply("commands.switchykit.pk.import.wait")
                            val link = getArgument("link", String::class.java)
                            val json: MinimalSystemJson = json.decodeFromString(URL(link).readText())
                            logger.."Importing system for ${player.name} (${player.uuidAsString} from PK Export - $link"
                            import(json, oldPresets, player, "pk import $link")
                        } catch (e: Exception) {
                            logger..e
                        }
                    }
                }
            }

            required(literal("token")) {
                required(literal("reset")) {
                    execute {
                        enqueueAction {
                            config.tokens.remove(player.uuidAsString)
                            saveConfig()
                            reply("commands.switchykit.pk.token.clear.confirm")
                        }
                        prompt("commands.switchykit.pk.token.clear")
                    }
                }

                required(greedyString("token")) {
                    execute {
                        enqueueAction {
                            config.tokens[player.uuidAsString] = getArgument("token", String::class.java)
                            saveConfig()
                            reply("commands.switchykit.pk.token.confirm")
                        }
                        prompt("commands.switchykit.pk.token")
                    }
                }
            }
        }
    }
}
