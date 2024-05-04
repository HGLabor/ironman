package gg.norisk.heroes.ironman.registry

import gg.norisk.heroes.ironman.IronManManager.toId
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent

object SoundRegistry {
    var JETPACK = Registry.register(Registries.SOUND_EVENT, "jetpack".toId(), SoundEvent.of("jetpack".toId()))
    var FLY_START_STOUND = Registry.register(Registries.SOUND_EVENT, "fly_start_sound".toId(), SoundEvent.of("fly_start_sound".toId()))

    fun init() {
    }
}
