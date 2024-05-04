package gg.norisk.heroes.ironman.client.render.armor

import gg.norisk.heroes.ironman.IronManManager.toId
import gg.norisk.heroes.ironman.item.IronManArmorItem
import software.bernie.geckolib.model.DefaultedItemGeoModel
import software.bernie.geckolib.renderer.GeoArmorRenderer

class IronManArmorRenderer : GeoArmorRenderer<IronManArmorItem>(DefaultedItemGeoModel("armor/ironman_armor".toId())) {
}
