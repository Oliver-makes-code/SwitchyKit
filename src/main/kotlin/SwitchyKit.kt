package de.olivermakesco.switchykit

import de.olivermakesco.switchykit.platform.PK
import de.olivermakesco.switchykit.platform.TUL
import folk.sisby.switchy.api.presets.SwitchyPreset
import folk.sisby.switchy.api.presets.SwitchyPresets
import folk.sisby.switchy.modules.DrogtorCompat
import folk.sisby.switchy.modules.StyledNicknamesCompat
import folk.sisby.switchy.presets.SwitchyPresetImpl
import net.minecraft.server.network.ServerPlayerEntity
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

fun import(system: MinimalSystemJson, oldPresets: SwitchyPresets, player: ServerPlayerEntity, reportSuccess: (Int, Int) -> Unit) {
    var created = 0
    var updated = 0
    val updatedPresets = hashMapOf<String, SwitchyPreset>()
    for (member in system.members) {
        val name = member.name.filter {
            regex.matches("$it")
        }
        if (!oldPresets.presetNames.contains(name)) {
            created++
        } else {
            updated++
        }


        val preset = SwitchyPresetImpl(name, mapOf())
        val bio = "${member.pronouns ?: ""} ${system.tag ?: ""}".trim()
        if (oldPresets.modules["switchy"*"drogtor"] == true) {
            val drogtor = DrogtorCompat()
            drogtor.nickname = member.displayName
            drogtor.bio = bio
            drogtor.nameColor = member.color?.closestFormat()
            preset.putModule("switchy"*"drogtor", drogtor)
        }

        if (oldPresets.modules["switchy"*"styled_nicknames"] == true) {
            val styled = StyledNicknamesCompat()
            var nick = member.displayName ?: member.name
            if (bio.isNotEmpty())
                nick = "<hover:'$bio'>$nick</hover>"
            val hex = member.color?.hex
            if (!hex.isNullOrEmpty())
                nick = "<color:$hex>$nick</color>"
            styled.styled_nickname = nick
            preset.putModule("switchy"*"styled_nicknames", styled)
        }

        updatedPresets[name] = preset
    }
    oldPresets.importFromOther(player, updatedPresets)
    reportSuccess(created, updated)
}
