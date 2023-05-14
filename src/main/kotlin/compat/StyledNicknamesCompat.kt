package de.olivermakesco.switchykit.compat

import de.olivermakesco.switchykit.data.MinimalMemberJson
import de.olivermakesco.switchykit.data.MinimalSystemJson
import de.olivermakesco.switchykit.hex
import de.olivermakesco.switchykit.times
import folk.sisby.switchy.api.module.SwitchyModule
import folk.sisby.switchy.api.presets.SwitchyPreset
import folk.sisby.switchy.modules.StyledNicknamesModule
import net.minecraft.util.Identifier

object StyledNicknamesCompat : CompatModule {
    override val id: Identifier = "switchy"*"styled_nicknames"

    override fun apply(preset: SwitchyPreset, system: MinimalSystemJson, member: MinimalMemberJson): SwitchyModule {
        val bio = "${member.pronouns ?: ""} ${system.tag ?: ""}".trim()
        val styled = StyledNicknamesModule()
        var nick = member.displayName ?: member.name
        if (bio.isNotEmpty())
            nick = "<hover:'$bio'>$nick</hover>"
        val hex = member.color?.hex
        if (!hex.isNullOrEmpty())
            nick = "<color:$hex>$nick</color>"
        styled.styled_nickname = nick
        return styled
    }
}
