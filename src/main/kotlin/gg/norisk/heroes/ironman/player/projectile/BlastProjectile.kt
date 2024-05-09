package gg.norisk.heroes.ironman.player.projectile

import gg.norisk.heroes.ironman.registry.EntityRegistry
import gg.norisk.heroes.ironman.registry.SoundRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.Arm
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.entity.modifyVelocity
import kotlin.random.Random

class BlastProjectile(entityType: EntityType<out BlastProjectile>, world: World) :
    ProjectileEntity(entityType, world) {
    var shootDirectionVector: Vec3d = Vec3d.ZERO

    override fun tick() {
        super.tick()
        val vec3d = this.velocity
        val hitResult = ProjectileUtil.getCollision(this, this::canHit)
        this.onCollision(hitResult)
        val d = this.x + vec3d.x
        val e = this.y + vec3d.y
        val f = this.z + vec3d.z
        this.updateRotation()
        val g = 0.99f
        val h = 0.06f
        if (world.getStatesInBox(this.boundingBox).noneMatch { it.isAir }) {
            this.discard()
            return
        }
        if (this.isInsideWaterOrBubbleColumn) {
            this.discard()
            return
        }
        this.velocity = vec3d.multiply(0.99)
        this.setPosition(d, e, f)
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        val entity = this.owner
        if (entity is LivingEntity) {
            createExplosion()
            entityHitResult.entity.damage(this.damageSources.mobProjectile(this, entity), 2.5f)
            (entityHitResult.entity as? LivingEntity?)?.takeKnockback(
                (2 * 0.5f).toDouble(),
                Random.nextDouble(-5.0,5.0),
                Random.nextDouble(-5.0,5.0))
        }
    }

    private fun createExplosion() {
        world.createExplosion(owner, this.x, this.y, this.z, 1F, false, World.ExplosionSourceType.MOB)
    }

    override fun onBlockHit(blockHitResult: BlockHitResult) {
        super.onBlockHit(blockHitResult)
        if (!world.isClient) {
            createExplosion()
            this.discard()
        }
    }

    override fun shouldRender(d: Double): Boolean {
        return true
    }

    override fun initDataTracker() {
    }

    override fun onSpawnPacket(entitySpawnS2CPacket: EntitySpawnS2CPacket) {
        super.onSpawnPacket(entitySpawnS2CPacket)
        val d = entitySpawnS2CPacket.velocityX
        val e = entitySpawnS2CPacket.velocityY
        val f = entitySpawnS2CPacket.velocityZ

        (this.owner as? LivingEntity?)?.apply {
            val raycast = this.raycast(256.0, 0.0f, true)
            val targetPos = raycast.pos
            val lerpPlayerPos = this.pos.add(0.0, this.getEyeHeight(this.pose).toDouble(), 0.0)
            shootDirectionVector = targetPos.subtract(lerpPlayerPos).normalize()
        }

        this.setVelocity(d, e, f)
    }

    fun shootFrom(livingEntity: LivingEntity) {
        this.owner = livingEntity

        val armOffset = 6.5
        val d = 0.22 * (if (livingEntity.getMainArm() == Arm.RIGHT) -armOffset else armOffset)
        val g = MathHelper.lerp(1f * 0.5f, livingEntity.pitch, livingEntity.prevPitch) * (Math.PI / 180.0).toFloat()
        val h = MathHelper.lerp(1f, livingEntity.prevBodyYaw, livingEntity.bodyYaw) * (Math.PI / 180.0).toFloat()
        val m = livingEntity.boundingBox.lengthY
        val e = if (livingEntity.isInSneakingPose) -0.2 else 0.07
        val spawnPos =  livingEntity.getLerpedPos(1f).add(Vec3d(d, m, e).rotateY(-h))

        this.setPosition(spawnPos.x, spawnPos.y, spawnPos.z)
        setVelocity(livingEntity, livingEntity.pitch, livingEntity.yaw, 0.0f, 1.5f, 1f)
        modifyVelocity(velocity.multiply(2.0))

        if (!this.isSilent) {
            this.world.playSoundFromEntity(
                null,
                this.owner,
                SoundRegistry.REPULSOR_SHOOT,
                (this.owner as LivingEntity).soundCategory,
                1.0f,
                1.0f
            )
        }
        this.world.spawnEntity(this)
    }

    companion object {
        fun debug() {
            if (FabricLoader.getInstance().isDevelopmentEnvironment) {
                command("blast") {
                    runs {
                        val player = this.source.playerOrThrow
                        val world = player.serverWorld
                        val blastProjectile = EntityRegistry.BLAST.create(world)
                        blastProjectile?.shootFrom(player)
                    }
                }
            }
        }
    }
}

