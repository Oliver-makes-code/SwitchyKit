package de.olivermakesco.switchykit.platform

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.olivermakesco.switchykit.*
import de.olivermakesco.switchykit.data.MinimalGroupJson
import de.olivermakesco.switchykit.data.MinimalMemberJson
import de.olivermakesco.switchykit.data.MinimalProxyTag
import de.olivermakesco.switchykit.data.MinimalSystemJson
import dev.proxyfox.pluralkt.PluralKt
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import net.minecraft.server.command.ServerCommandSource
import org.quiltmc.qkl.library.brigadier.argument.greedyString
import org.quiltmc.qkl.library.brigadier.argument.literal
import org.quiltmc.qkl.library.brigadier.argument.string
import org.quiltmc.qkl.library.brigadier.execute
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qkl.library.brigadier.required
import java.net.URL

object PK {
    private fun CommandContext<ServerCommandSource>.import(system: MinimalSystemJson, command: String, group: String?) {
        import(system, switchyPlayer.presets, player, command, group)
    }

    private fun CommandContext<ServerCommandSource>.importLinked(command: String, group: String?) {
        reply("commands.switchykit.pk.import.wait")
        val link = getArgument("link", String::class.java)
        val json: MinimalSystemJson = json.decodeFromString(URL(link).readText())
        logger.."Importing system for ${player.name} (${player.uuidAsString} from PK Export - $link"
        import(json, "$command $link", group)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun CommandContext<ServerCommandSource>.importApi(command: String, group: String?) {
        val token = config.tokens[player.uuidAsString] ?: run {
            command("commands.switchykit.pk.import.fail.token", "/pk token", "/pk import")
            return@importApi
        }
        reply("commands.switchykit.pk.import.wait")
        // Launch a thread to not block main thread
        GlobalScope.launch {
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
                    membersJson += MinimalMemberJson(it.id, it.name, it.displayName, it.pronouns, it.color, it.proxyTags.map { tag -> MinimalProxyTag(tag.prefix, tag.suffix) }.toList())
                }

                val groups = PluralKt.Group.getGroups(system.id, token).await().run {
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
                val groupsJson = arrayListOf<MinimalGroupJson>()
                groups.forEach {
                    val members = PluralKt.Group.getGroupMembers(it.id, token).await().run {
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
                    val groupMembers = arrayListOf<String>()
                    members.forEach {
                        groupMembers += it.id
                    }
                    groupsJson += MinimalGroupJson(it.id, groupMembers.toTypedArray())
                }
                logger.."Importing system for ${player.name} (${player.uuidAsString} from PK API - ${system.id}"
                import(MinimalSystemJson(system.tag, membersJson.toTypedArray(), groupsJson.toTypedArray()), command, group)
            }
        }
    }

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register("pk") {
            required(literal("import")) {
                execute {
                    importApi("pk import", null)
                }
                required(greedyString("link")) {
                    execute {
                        importLinked("pk import", null)
                    }
                }

                required(string("group")) {
                    execute {
                        importApi("pk import", getArgument("group", String::class.java))
                    }

                    required(greedyString("link")) {
                        execute {
                            importLinked("pk import", getArgument("group", String::class.java))
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
