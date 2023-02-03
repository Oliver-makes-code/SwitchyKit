package de.olivermakesco.switchykit

import com.mojang.brigadier.context.CommandContext
import dev.proxyfox.pluralkt.types.PkColor
import folk.sisby.switchy.api.SwitchyPlayer
import folk.sisby.switchy.util.Feedback
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.slf4j.Logger
import kotlin.math.abs

operator fun Logger.rangeTo(value: Any?): Logger {
    info("$value")
    return this
}
val CommandContext<ServerCommandSource>.player: ServerPlayerEntity get() = source.player
val CommandContext<ServerCommandSource>.switchyPlayer get() = player as SwitchyPlayer
val CommandContext<ServerCommandSource>.switchyKitPlayer get() = player as SwitchyKitPlayer
var CommandContext<ServerCommandSource>.enqueuedAction
    get() = switchyKitPlayer.`SwitchyKit - EnqueuedAction`
    set(value) { switchyKitPlayer.`SwitchyKit - EnqueuedAction` = value }
fun CommandContext<ServerCommandSource>.reply(translationKey: String, vararg params: MutableText) {
    player.sendMessage(
        "[SwitchyKit] ".text.withColor(Formatting.AQUA.colorValue!!).append(Feedback.translatableWithArgs(translationKey, *params)),
        false
    )
}
fun CommandContext<ServerCommandSource>.reply(translationKey: String, next: String) {
    reply(translationKey, next.text)
}
fun CommandContext<ServerCommandSource>.ratio(translationKey: String, first: Int, second: Int) {
    reply(translationKey, "$first".text, "$second".text)
}
fun CommandContext<ServerCommandSource>.command(translationKey: String, vararg commands: String) {
    val out = arrayListOf<MutableText>()
    commands.forEach {
        out += it.text.withColor(Formatting.YELLOW.colorValue!!)
    }
    reply(translationKey, *out.toTypedArray())
}
fun CommandContext<ServerCommandSource>.prompt(translationKey: String) {
    reply(translationKey)
    command("commands.switchykit.confirm.prompt", "/skit-confirm")
}
fun CommandContext<ServerCommandSource>.enqueueAction(action: () -> Unit) {
    enqueuedAction = EnqueuedAction(action)
}
fun EnqueuedAction?.executeOrElse(action: () -> Unit) = (this?.action ?: action)()
val String.text get() = Feedback.literal(this)
var MutableText.color
    get() = style.color?.rgb ?: -1
    set(value) { style = Style.EMPTY.withColor(value) }
fun MutableText.withColor(value: Int): MutableText {
    color = value
    return this
}
var SwitchyPlayer.presets
    get() = `switchy$getPresets`()
    set(value) = `switchy$setPresets`(value)
operator fun String.times(other: String) = Identifier(this, other)
val PkColor.hex: String get() {
    return "#"+color.toString(16).padStart(6, '0')
}
fun PkColor.closestFormat() = color.closestFormat()
fun Int.closestFormat(): Formatting? {
    var closest: Formatting? = null
    for (format in Formatting.values()) {
        // Drogtor bans black.
        if (format == Formatting.BLACK)
            continue
        if (format.colorValue == null)
            continue
        if (closest == null) {
            closest = format
            continue
        }
        if (dist(format.colorValue!!) > dist(closest.colorValue!!))
            closest = format
    }
    return closest
}
fun Int.dist(other: Int) = abs(this-other)
