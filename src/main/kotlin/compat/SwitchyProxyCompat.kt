package de.olivermakesco.switchykit.compat

import de.olivermakesco.switchykit.times
import dev.proxyfox.pluralkt.types.PkProxyTag
import folk.sisby.switchy.api.presets.SwitchyPreset
import folk.sisby.switchy_proxy.ProxyTag
import folk.sisby.switchy_proxy.modules.ProxyModule

fun addTagsModule(preset: SwitchyPreset, tags: ArrayList<PkProxyTag>) {
    if (tags.isNotEmpty()) {
        val proxy = ProxyModule()
        tags.forEach { proxy.addTag(ProxyTag(it.prefix ?: "", it.suffix ?: "")) }
        preset.putModule("switchy_proxy"*"proxies", proxy)
    }
}