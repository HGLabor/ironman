package gg.norisk.heroes.ironman.registry

import gg.norisk.heroes.ironman.IronManManager.toId
import gg.norisk.heroes.ironman.player.projectile.BlastProjectile
import gg.norisk.heroes.ironman.player.projectile.MissileEntity
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object EntityRegistry {
    val BLAST: EntityType<BlastProjectile> = register("blast", ::BlastProjectile, 0.45f, 0.45f)
    val MISSILE: EntityType<MissileEntity> = register("missile", ::MissileEntity, 0.45f, 0.45f)

    fun init() {
        FabricDefaultAttributeRegistry.register(MISSILE, MissileEntity.createMissileAttributes())
    }

    fun <T : Entity> register(
        name: String, entity: EntityType.EntityFactory<T>,
        width: Float, height: Float
    ): EntityType<T> {
        return Registry.register<EntityType<*>, EntityType<T>>(
            Registries.ENTITY_TYPE,
            name.toId(),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, entity)
                .dimensions(EntityDimensions.changing(width, height)).build()
        )
    }
}