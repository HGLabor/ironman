package gg.norisk.heroes.ironman.registry

import gg.norisk.heroes.ironman.IronManManager.toId
import gg.norisk.heroes.ironman.item.IronManArmorItem
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.*
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text

object ItemRegistry {
    val IRONMAN_ARMOR_HELMET = registerItem(
        "ironman_armor_helmet",
        IronManArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, Item.Settings())
    )

    val IRONMAN_ARMOR_CHESTPLATE = registerItem(
        "ironman_armor_chestplate",
        IronManArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, Item.Settings())
    )

    val IRONMAN_ARMOR_LEGGINGS = registerItem(
        "ironman_armor_leggings",
        IronManArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, Item.Settings())
    )

    val IRONMAN_ARMOR_BOOTS = registerItem(
        "ironman_armor_boots",
        IronManArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, Item.Settings())
    )

    val ITEM_GROUP: ItemGroup = Registry.register(
        Registries.ITEM_GROUP, "geckolib_examples".toId(), FabricItemGroup
            .builder()
            .displayName(Text.translatable("itemGroup.geckolib.geckolib_examples"))
            .icon { ItemStack(IRONMAN_ARMOR_HELMET) }
            .entries { _: ItemGroup.DisplayContext, entries: ItemGroup.Entries ->
                entries.add(IRONMAN_ARMOR_HELMET)
                entries.add(IRONMAN_ARMOR_CHESTPLATE)
                entries.add(IRONMAN_ARMOR_LEGGINGS)
                entries.add(IRONMAN_ARMOR_BOOTS)
            }.build()
    )

    fun init() {

    }

    private fun <I : Item> registerItem(name: String, item: I): I {
        return Registry.register(Registries.ITEM, name.toId(), item)
    }
}
