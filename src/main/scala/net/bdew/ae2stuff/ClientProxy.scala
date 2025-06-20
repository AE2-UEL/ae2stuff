package net.bdew.ae2stuff

import appeng.api.util.AEColor
import net.bdew.ae2stuff.items.visualiser.VisualiserOverlayRender
import net.bdew.ae2stuff.machines.wireless.{BlockWireless, WirelessModelFactory, WirelessModelLoader, WirelessOverlayRender}
import net.bdew.ae2stuff.misc.{Icons, MouseEventHandler, OverlayRenderHandler}
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.{ModelLoader, ModelLoaderRegistry}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ClientProxy extends CommonProxy {

  override def preInit(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
    Icons.init()
    ModelLoaderRegistry.registerLoader(new WirelessModelLoader(Map("models/block/builtin/wireless" -> new WirelessModelFactory())))
  }

  @SubscribeEvent
  def registerModels(event: ModelRegistryEvent): Unit = {
    registerWirelessItemModel(0, AEColor.TRANSPARENT)
    registerWirelessHubItemModel(17, AEColor.TRANSPARENT)
    for (i <- 0 to 16) {
      registerWirelessItemModel(i + 1, AEColor.values.apply(i))
      registerWirelessHubItemModel(i + 18, AEColor.values.apply(i))
    }
  }

  private def registerWirelessItemModel(meta: Int, color: AEColor): Unit = {
    ModelLoader.setCustomModelResourceLocation(
      BlockWireless.itemBlockInstance,
      meta,
      new ModelResourceLocation(String.format("%s:wireless/%s", AE2Stuff.modId, color.name.toLowerCase)))
  }

  private def registerWirelessHubItemModel(meta: Int, color: AEColor): Unit = {
    ModelLoader.setCustomModelResourceLocation(
      BlockWireless.itemBlockInstance,
      meta,
      new ModelResourceLocation(String.format("%s:wireless_hub/%s", AE2Stuff.modId, color.name.toLowerCase)))
  }

  override def init(): Unit = {
    OverlayRenderHandler.register(WirelessOverlayRender)
    OverlayRenderHandler.register(VisualiserOverlayRender)
    MouseEventHandler.init()
  }
}
