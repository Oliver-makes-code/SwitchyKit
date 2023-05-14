package de.olivermakesco.switchykit.compat

import net.minecraft.util.Identifier

object Compat {
    val modules = ArrayList<CompatModule>()

    operator fun plusAssign(module: CompatModule) {
        modules += module
    }

    fun getAllActive(activeModules: List<Identifier>): List<CompatModule> {
        val out = ArrayList<CompatModule>()

        modules.forEach {
            if (activeModules.contains(it.id))
                out.add(it)
        }

        return out
    }

    fun addDefault() {
        Compat += SwitchyProxyCompat
        Compat += StyledNicknamesCompat
        Compat += DrogtorCompat
    }
}
