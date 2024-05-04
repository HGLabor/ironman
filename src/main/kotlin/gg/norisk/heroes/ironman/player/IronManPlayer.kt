package gg.norisk.heroes.ironman.player

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity

interface IronManPlayer {
    fun getFlyingLeaningPitch(tickDelta: Float): Float
    var startFlightTimestamp: Long
}

val flyTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val ironManTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)

var PlayerEntity.isIronMan: Boolean
    get() = this.dataTracker.get(ironManTracker)
    set(value) = this.dataTracker.set(ironManTracker, value)

var PlayerEntity.isIronManFlying: Boolean
    get() = this.dataTracker.get(flyTracker)
    set(value) = this.dataTracker.set(flyTracker, value)
