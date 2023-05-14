package de.olivermakesco.switchykit.compat

import de.olivermakesco.switchykit.closestFormat
import de.olivermakesco.switchykit.data.MinimalMemberJson
import de.olivermakesco.switchykit.data.MinimalSystemJson
import de.olivermakesco.switchykit.times
import folk.sisby.switchy.api.module.SwitchyModule
import folk.sisby.switchy.api.presets.SwitchyPreset
import folk.sisby.switchy.modules.DrogtorModule
import net.minecraft.util.Identifier

object DrogtorCompat : CompatModule {
    override val id: Identifier = "switchy"*"drogtor"

    override fun apply(preset: SwitchyPreset, system: MinimalSystemJson, member: MinimalMemberJson): SwitchyModule? {
        val bio = "${member.pronouns ?: ""} ${system.tag ?: ""}".trim()
        val drogtor = DrogtorModule()
        drogtor.nickname = member.displayName
        drogtor.bio = bio
        drogtor.nameColor = member.color?.closestFormat()
        return drogtor
    }
}
