package de.olivermakesco.switchykit.mixin

import de.olivermakesco.switchykit.EnqueuedAction
import de.olivermakesco.switchykit.SwitchyKitPlayer
import net.minecraft.entity.player.PlayerEntity
import org.spongepowered.asm.mixin.Mixin

@Suppress("ClassName")
@Mixin(PlayerEntity::class)
class Mixin_PlayerEntity : SwitchyKitPlayer {
    override var `SwitchyKit - EnqueuedAction`: EnqueuedAction? = null
}
