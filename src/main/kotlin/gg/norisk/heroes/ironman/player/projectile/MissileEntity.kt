package gg.norisk.heroes.ironman.player.projectile

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.FlyingItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.control.FlightMoveControl
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.pathing.BirdNavigation
import net.minecraft.entity.ai.pathing.EntityNavigation
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.PigEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World
import net.silkmc.silk.core.entity.modifyVelocity
import kotlin.random.Random

class MissileEntity(entityType: EntityType<out MissileEntity>, world: World) : PathAwareEntity(entityType, world),
    FlyingItemEntity {
    var owner: LivingEntity? = null
    var targetGoal: LivingEntity? = null

    init {
        this.moveControl = FlightMoveControl(this, 10, true)
        this.setNoGravity(true)
    }

    override fun initGoals() {
        //goalSelector.add(0,ChargeTargetGoal())
        goalSelector.add(0, FollowTargetGoal(this, 1.1))
    }

    override fun tick() {
        super.tick()
        if (!world.isClient) {
            val hitResult: HitResult = ProjectileUtil.getCollision(this, {
                it.canHit()
            }, 2.0)
            if (hitResult.type != HitResult.Type.MISS) {
                this.onCollision(hitResult)
            }
        }
        if (target?.isAlive == false) {
            explodeAndDiscard()
        }
        if (world.isClient) {
            world.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, 0.1, 0.0, 0.1)
        }
    }

    override fun canHit(): Boolean {
        return false
    }

    private fun explodeAndDiscard() {
        world.createExplosion(
            owner,
            this.x,
            this.y,
            this.z,
            Random.nextDouble(1.0, 2.0).toFloat(),
            World.ExplosionSourceType.MOB
        )
        this.discard()
    }

    private fun onCollision(hitResult: HitResult) {
        if (hitResult is EntityHitResult) {
            explodeAndDiscard()
        }
    }


    override fun createNavigation(world: World): EntityNavigation {
        val birdNavigation = BirdNavigation(this, world)
        birdNavigation.setCanPathThroughDoors(true)
        birdNavigation.setCanSwim(true)
        birdNavigation.setCanEnterOpenDoors(true)
        return birdNavigation
    }

    override fun getTarget(): LivingEntity? {
        return targetGoal
    }

    companion object {
        fun createMissileAttributes(): DefaultAttributeContainer.Builder {
            return createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.6)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6)
        }
    }

    private class FollowTargetGoal(private val animal: MissileEntity, private val speed: Double) : Goal() {
        private var target: Entity? = null
        private var delay = 0

        override fun canStart(): Boolean {
            target = this.animal.target
            return target != null
        }

        override fun shouldContinue(): Boolean {
            if (!target!!.isAlive) {
                return false
            } else {
                val d = animal.squaredDistanceTo(this.target)
                return !(d < 9.0) && !(d > 256.0)
            }
        }

        override fun start() {
            this.delay = 0
        }

        override fun stop() {
            this.target = null
        }

        override fun tick() {
            animal.modifyVelocity(target!!.pos.subtract(animal.pos).normalize().multiply(1.2))
            animal.navigation.startMovingTo(this.target, speed)
        }
    }

    override fun getStack(): ItemStack {
        return Items.FIREWORK_ROCKET.defaultStack
    }
}