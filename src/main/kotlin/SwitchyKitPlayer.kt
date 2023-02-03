package de.olivermakesco.switchykit

interface SwitchyKitPlayer {
    var `SwitchyKit - EnqueuedAction`: EnqueuedAction?
}

@JvmInline
value class EnqueuedAction(val action: () -> Unit)
