package de.olivermakesco.switchykit

import de.olivermakesco.switchykit.compat.addTagsModule
import de.olivermakesco.switchykit.platform.PK
import de.olivermakesco.switchykit.platform.TUL
import folk.sisby.switchy.api.SwitchyApi
import folk.sisby.switchy.api.presets.SwitchyPreset
import folk.sisby.switchy.api.presets.SwitchyPresets
import folk.sisby.switchy.modules.DrogtorModule
import folk.sisby.switchy.modules.StyledNicknamesModule
import folk.sisby.switchy.presets.SwitchyPresetImpl
import folk.sisby.switchy.util.Feedback
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qkl.library.brigadier.execute
import org.quiltmc.qkl.library.brigadier.register
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.quiltmc.qsl.command.api.CommandRegistrationCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("SwitchyKit")

object SwitchyKit : ModInitializer {
    override fun onInitialize(mod: ModContainer?) {
        logger.."Initializing SwitchyKit."
        loadConfig()
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register("skit-confirm") {
                execute {
                    enqueuedAction.executeOrElse {
                        reply("commands.switchykit.pk.confirm.fail")
                    }
                    enqueuedAction = null
                }
            }

            PK.register(dispatcher)
            TUL.register(dispatcher)
        }
        logger.."SwitchyKit Initialized."
    }
}

val regex = Regex("[a-z0-9_\\-.+]", RegexOption.IGNORE_CASE)

fun import(system: MinimalSystemJson, oldPresets: SwitchyPresets, player: ServerPlayerEntity, command: String) {
    val updatedPresets = hashMapOf<String, SwitchyPreset>()
    val modules = mutableListOf<Identifier>()
    if (oldPresets.modules["switchy"*"drogtor"] == true) modules += "switchy"*"drogtor"
    if (oldPresets.modules["switchy"*"styled_nicknames"] == true) modules += "switchy"*"styled_nicknames"
    if (oldPresets.modules["switchy_proxy"*"proxies"] == true) modules += "switchy_proxy"*"proxies"

    for (member in system.members) {
        val name = member.name.filter {
            regex.matches("$it")
        }

        val preset = SwitchyPresetImpl(name, mapOf())
        val bio = "${member.pronouns ?: ""} ${system.tag ?: ""}".trim()
        if (oldPresets.modules["switchy"*"drogtor"] == true) {
            val drogtor = DrogtorModule()
            drogtor.nickname = member.displayName
            drogtor.bio = bio
            drogtor.nameColor = member.color?.closestFormat()
            preset.putModule("switchy"*"drogtor", drogtor)
        }

        if (oldPresets.modules["switchy"*"styled_nicknames"] == true) {
            val styled = StyledNicknamesModule()
            var nick = member.displayName ?: member.name
            if (bio.isNotEmpty())
                nick = "<hover:'$bio'>$nick</hover>"
            val hex = member.color?.hex
            if (!hex.isNullOrEmpty())
                nick = "<color:$hex>$nick</color>"
            styled.styled_nickname = nick
            preset.putModule("switchy"*"styled_nicknames", styled)
        }

        if (oldPresets.modules["switchy_proxy"*"proxies"] == true) {
            addTagsModule(preset, member.proxyTags)
        }

        updatedPresets[name] = preset
    }
    SwitchyApi.confirmAndImportPresets(player, updatedPresets, modules, command
    ) { t -> Feedback.sendMessage(player, t) }
}
