package gg.norisk.heroes.ironman.player

import gg.norisk.heroes.utils.Animation
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity
import java.util.Optional
import java.util.UUID

interface IronManPlayer {
    fun getFlyingLeaningPitch(tickDelta: Float): Float
    var startFlightTimestamp: Long
    var transformTimestamp: Long
    var repulsorTimestamp: Long
    var beamAnimation: Animation?
    val missileTargets: MutableMap<UUID, Int>
}

val flyTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val ironManTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val repulsorChargeTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val missileSelector: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val holdingLeftClick: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val currentMissileTargetTracker: TrackedData<Optional<UUID>> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_UUID)

var PlayerEntity.isIronMan: Boolean
    get() = this.dataTracker.get(ironManTracker)
    set(value) = this.dataTracker.set(ironManTracker, value)

var PlayerEntity.isRepulsorCharging: Boolean
    get() = this.dataTracker.get(repulsorChargeTracker)
    set(value) = this.dataTracker.set(repulsorChargeTracker, value)

var PlayerEntity.currentMissileTarget: Optional<UUID>
    get() = this.dataTracker.get(currentMissileTargetTracker)
    set(value) = this.dataTracker.set(currentMissileTargetTracker, value)

var PlayerEntity.isMissileSelecting: Boolean
    get() = this.dataTracker.get(missileSelector)
    set(value) = this.dataTracker.set(missileSelector, value)

var PlayerEntity.isHoldingLeftClick: Boolean
    get() = this.dataTracker.get(holdingLeftClick)
    set(value) = this.dataTracker.set(holdingLeftClick, value)

var PlayerEntity.isIronManFlying: Boolean
    get() = this.dataTracker.get(flyTracker)
    set(value) = this.dataTracker.set(flyTracker, value)
