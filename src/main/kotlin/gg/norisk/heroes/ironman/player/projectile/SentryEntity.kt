package gg.norisk.heroes.ironman.player.projectile

import gg.norisk.heroes.ironman.abilities.TransformAbility
import gg.norisk.heroes.ironman.player.isIronMan
import gg.norisk.heroes.ironman.registry.EntityRegistry
import gg.norisk.heroes.ironman.registry.SoundRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.ActiveTargetGoal
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.goal.RevengeGoal
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion
import java.util.*
import kotlin.math.sqrt
import kotlin.random.Random

class SentryEntity(entityType: EntityType<out PathAwareEntity>, world: World) : PathAwareEntity(entityType, world) {
    var owner: UUID? = null
    var hasTarget: Boolean
        get() = this.dataTracker.get(TARGET)
        set(value) = this.dataTracker.set(TARGET, value)

    override fun onPlayerCollision(playerEntity: PlayerEntity) {
        super.onPlayerCollision(playerEntity)
        if (playerEntity.uuid == owner && distanceTo(playerEntity) < 0.3) {
            if (!world.isClient) {
                TransformAbility.transform(playerEntity as ServerPlayerEntity)
                discard()
            }
        }
    }

    override fun initDataTracker() {
        super.initDataTracker()
        this.dataTracker.startTracking(TARGET, false)
    }

    override fun initGoals() {
        targetSelector.add(1, RevengeGoal(this))
        targetSelector.add(2, ActiveTargetGoal(this, HostileEntity::class.java, true))
        targetSelector.add(3, ActiveTargetGoal(this, PlayerEntity::class.java, true) {
            it.uuid != owner
        })
        goalSelector.add(0, ProjectileAttackGoal(1.0, 20, 20, 20.0f))
    }

    override fun tick() {
        super.tick()
        if (owner != null) {
            if (world.getPlayerByUuid(owner)?.isIronMan == true) {
                discard()
            }
        }
    }

    fun toggleTarget(boolean: Boolean) {
        hasTarget = boolean
        if (hasTarget) {
            world.playSoundFromEntity(
                null,
                this,
                SoundRegistry.REPULSOR_CHARGE,
                SoundCategory.PLAYERS,
                2f,
                1f
            )
        }
    }

    override fun isPushable(): Boolean {
        return false
    }

    override fun pushAway(entity: Entity?) {

    }

    override fun pushAwayFrom(entity: Entity?) {

    }

    override fun isImmuneToExplosion(explosion: Explosion?): Boolean {
        return true
    }

    inner class ProjectileAttackGoal(d: Double, i: Int, j: Int, f: Float) : Goal() {
        private var target: LivingEntity? = null
        private var updateCountdownTicks = -1
        private var mobSpeed = 0.0
        private var seenTargetTicks = 0
        private var minIntervalTicks = 0
        private var maxIntervalTicks = 0
        private var maxShootRange = 0f
        private var squaredMaxShootRange = 0f

        init {
            this.mobSpeed = d
            this.minIntervalTicks = i
            this.maxIntervalTicks = j
            this.maxShootRange = f
            this.squaredMaxShootRange = f * f
            this.controls = EnumSet.of(Control.MOVE, Control.LOOK)
        }

        override fun canStart(): Boolean {
            val livingEntity = this@SentryEntity.target
            if (livingEntity != null && livingEntity.isAlive) {
                this.target = livingEntity
                return true
            } else {
                return false
            }
        }

        override fun shouldContinue(): Boolean {
            return this.canStart() || target!!.isAlive && !this@SentryEntity.navigation.isIdle
        }

        override fun start() {
            super.start()
            toggleTarget(true)
        }

        override fun stop() {
            toggleTarget(false)
            this.target = null
            this.seenTargetTicks = 0
            this.updateCountdownTicks = -1
        }

        override fun shouldRunEveryTick(): Boolean {
            return true
        }

        override fun tick() {
            val d = this@SentryEntity.squaredDistanceTo(
                target!!.x,
                target!!.y, target!!.z
            )
            val bl = this@SentryEntity.visibilityCache.canSee(this.target)
            if (bl) {
                ++this.seenTargetTicks
            } else {
                this.seenTargetTicks = 0
            }

            if (!(d > squaredMaxShootRange.toDouble()) && this.seenTargetTicks >= 5) {
                this@SentryEntity.navigation.stop()
            } else {
                this@SentryEntity.navigation.startMovingTo(this.target, this.mobSpeed)
            }

            this@SentryEntity.lookControl.lookAt(this.target, 30.0f, 30.0f)
            if (--this.updateCountdownTicks == 0) {
                if (!bl) {
                    return
                }

                val f = sqrt(d).toFloat() / this.maxShootRange
                val g = MathHelper.clamp(f, 0.1f, 1.0f)
                shootMissile(this.target ?: return)
                this.updateCountdownTicks =
                    MathHelper.floor(f * (this.maxIntervalTicks - this.minIntervalTicks).toFloat() + minIntervalTicks.toFloat())
            } else if (this.updateCountdownTicks < 0) {
                this.updateCountdownTicks = MathHelper.floor(
                    MathHelper.lerp(
                        sqrt(d) / maxShootRange.toDouble(),
                        minIntervalTicks.toDouble(), maxIntervalTicks.toDouble()
                    )
                )
            }
        }
    }

    private fun shootMissile(target: LivingEntity) {
        val player = world.getPlayerByUuid(owner ?: return) ?: return
        val missile = MissileEntity(EntityRegistry.MISSILE, player.world)
        missile.owner = player
        missile.targetGoal = target
        missile.setPosition(
            this.pos.add(
                Random.nextDouble(-1.0, 1.0),
                3.0,
                Random.nextDouble(-1.0, 1.0)
            )
        )
        this.world.spawnEntity(missile)
        world.playSound(
            null,
            player.x,
            player.y,
            player.z, SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT,
            0.8f,
            Random.nextDouble(0.9, 1.2).toFloat()
        )
    }

    companion object {
        val TARGET: TrackedData<Boolean> =
            DataTracker.registerData(SentryEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)

        fun createSentryAttributes(): DefaultAttributeContainer.Builder {
            return HostileEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 2.0)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0)
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS)
        }
    }
}
