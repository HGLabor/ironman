package gg.norisk.heroes.ironman.player

import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity

interface IronManPlayer {
    fun getFlyingLeaningPitch(tickDelta: Float): Float
}

val flyTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)

var PlayerEntity.isIronManFlying: Boolean
    get() = this.dataTracker.get(flyTracker)
    set(value) = this.dataTracker.set(flyTracker, value)
