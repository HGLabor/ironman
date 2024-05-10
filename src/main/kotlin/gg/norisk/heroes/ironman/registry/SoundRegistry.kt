package gg.norisk.heroes.ironman.registry

import gg.norisk.heroes.ironman.IronManManager.toId
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent

object SoundRegistry {
    var JETPACK = Registry.register(Registries.SOUND_EVENT, "jetpack".toId(), SoundEvent.of("jetpack".toId()))
    var FLY_START_STOUND = Registry.register(Registries.SOUND_EVENT, "fly_start_sound".toId(), SoundEvent.of("fly_start_sound".toId()))
    var SUIT_UP = Registry.register(Registries.SOUND_EVENT, "suit_up".toId(), SoundEvent.of("suit_up".toId()))
    var REPULSOR_SHOOT = Registry.register(Registries.SOUND_EVENT, "repulsor_shoot".toId(), SoundEvent.of("repulsor_shoot".toId()))
    var REPULSOR_CHARGE = Registry.register(Registries.SOUND_EVENT, "repulsor_charge".toId(), SoundEvent.of("repulsor_charge".toId()))
    var MISSILE_TARGET_FOUND = Registry.register(Registries.SOUND_EVENT, "missile_target_found".toId(), SoundEvent.of("missile_target_found".toId()))
    var MISSILE_EMPTY = Registry.register(Registries.SOUND_EVENT, "missile_empty".toId(), SoundEvent.of("missile_empty".toId()))

    fun init() {
    }
}
