package de.olivermakesco.switchykit

import de.olivermakesco.switchykit.compat.Compat
import de.olivermakesco.switchykit.compat.DrogtorCompat
import de.olivermakesco.switchykit.compat.StyledNicknamesCompat
import de.olivermakesco.switchykit.compat.SwitchyProxyCompat
import de.olivermakesco.switchykit.data.MinimalSystemJson
import de.olivermakesco.switchykit.data.getById
import de.olivermakesco.switchykit.platform.PK
import de.olivermakesco.switchykit.platform.TUL
import folk.sisby.switchy.api.SwitchyApi
import folk.sisby.switchy.api.presets.SwitchyPreset
import folk.sisby.switchy.api.presets.SwitchyPresets
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
        Compat.addDefault()
        logger.."SwitchyKit Initialized."
    }
}

val regex = Regex("[a-z0-9_\\-.+]", RegexOption.IGNORE_CASE)

fun import(system: MinimalSystemJson, oldPresets: SwitchyPresets, player: ServerPlayerEntity, command: String, group: String?) {
    val updatedPresets = hashMapOf<String, SwitchyPreset>()
    val modules = mutableListOf<Identifier>()
    Compat.modules.forEach {
        if (oldPresets.modules[it.id] == true) modules += it.id
    }

    val groupToCheck = group?.let { system.groups.getById(it) }

    system.members.forEach { member ->
        if (groupToCheck?.members?.contains(member.id) == false) return@forEach

        val name = member.name.filter {
            regex.matches("$it")
        }

        val preset = SwitchyPresetImpl(name, mapOf())

        Compat.getAllActive(modules).forEach {
            preset.putModule(it.id, it.apply(preset, system, member))
        }

        updatedPresets[name] = preset
    }

    SwitchyApi.confirmAndImportPresets(player, updatedPresets, modules, command) { t ->
        Feedback.sendMessage(player, t)
    }
}
