package gg.norisk.heroes.ironman.player

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity

interface IronManPlayer {
    fun getFlyingLeaningPitch(tickDelta: Float): Float
    var startFlightTimestamp: Long
    var transformTimestamp: Long
    var repulsorTimestamp: Long
}

val flyTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val ironManTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val repulsorChargeTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)

var PlayerEntity.isIronMan: Boolean
    get() = this.dataTracker.get(ironManTracker)
    set(value) = this.dataTracker.set(ironManTracker, value)

var PlayerEntity.isRepulsorCharging: Boolean
    get() = this.dataTracker.get(repulsorChargeTracker)
    set(value) = this.dataTracker.set(repulsorChargeTracker, value)

var PlayerEntity.isIronManFlying: Boolean
    get() = this.dataTracker.get(flyTracker)
    set(value) = this.dataTracker.set(flyTracker, value)
