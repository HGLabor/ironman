package gg.norisk.heroes.ironman.item

import gg.norisk.heroes.ironman.client.render.armor.IronManArmorRenderer
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.ItemStack
import software.bernie.example.client.renderer.armor.GeckoArmorRenderer
import software.bernie.geckolib.animatable.GeoItem
import software.bernie.geckolib.animatable.client.RenderProvider
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar
import software.bernie.geckolib.renderer.GeoArmorRenderer
import software.bernie.geckolib.util.GeckoLibUtil
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Example [GeoAnimatable][software.bernie.geckolib.core.animatable.GeoAnimatable] [ArmorItem] implementation
 * @see GeoItem
 *
 * @see GeckoArmorRenderer
 */
class IronManArmorItem(
    armorMaterial: ArmorMaterial,
    type: Type,
    properties: Settings
) :
    ArmorItem(armorMaterial, type, properties), GeoItem {
    private val cache: AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this)
    private val renderProvider: Supplier<Any> = GeoItem.makeRenderer(this)

    // Create our armor model/renderer for Fabric and return it
    override fun createRenderer(consumer: Consumer<Any>) {
        consumer.accept(object : RenderProvider {
            private var renderer: GeoArmorRenderer<*>? = null

            override fun getHumanoidArmorModel(
                livingEntity: LivingEntity,
                itemStack: ItemStack,
                equipmentSlot: EquipmentSlot,
                original: BipedEntityModel<LivingEntity>
            ): BipedEntityModel<LivingEntity> {
                if (this.renderer == null) this.renderer = IronManArmorRenderer()

                // This prepares our GeoArmorRenderer for the current render frame.
                // These parameters may be null however, so we don't do anything further with them
                renderer!!.prepForRender(livingEntity, itemStack, equipmentSlot, original)

                return renderer!!
            }
        })
    }

    override fun getRenderProvider(): Supplier<Any> {
        return this.renderProvider
    }

    // Let's add our animation controller
    override fun registerControllers(controllers: ControllerRegistrar) {
    }

    override fun getAnimatableInstanceCache(): AnimatableInstanceCache {
        return this.cache
    }
}
