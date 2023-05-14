package de.olivermakesco.switchykit.compat

import de.olivermakesco.switchykit.data.MinimalMemberJson
import de.olivermakesco.switchykit.data.MinimalSystemJson
import folk.sisby.switchy.api.module.SwitchyModule
import folk.sisby.switchy.api.presets.SwitchyPreset
import net.minecraft.util.Identifier

interface CompatModule {
    val id: Identifier

    fun apply(preset: SwitchyPreset, system: MinimalSystemJson, member: MinimalMemberJson): SwitchyModule?
}
