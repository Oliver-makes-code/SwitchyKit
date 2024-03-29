package de.olivermakesco.switchykit.compat

import de.olivermakesco.switchykit.data.MinimalMemberJson
import de.olivermakesco.switchykit.data.MinimalSystemJson
import de.olivermakesco.switchykit.times
import folk.sisby.switchy.api.module.SwitchyModule
import folk.sisby.switchy.api.presets.SwitchyPreset
import folk.sisby.switchy_proxy.ProxyTag
import folk.sisby.switchy_proxy.modules.ProxyModule
import net.minecraft.util.Identifier

object SwitchyProxyCompat : CompatModule {
    override val id: Identifier = "switchy_proxy"*"proxies"

    override fun apply(preset: SwitchyPreset, system: MinimalSystemJson, member: MinimalMemberJson): SwitchyModule? {
        if (member.proxyTags.isEmpty()) return null
        val proxy = ProxyModule()
        member.proxyTags.forEach {
            proxy.addTag(ProxyTag(it.prefix, it.suffix))
        }
        return proxy
    }
}
