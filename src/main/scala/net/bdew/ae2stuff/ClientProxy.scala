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
    registerWirelessItemModel(1, AEColor.WHITE)
    registerWirelessItemModel(2, AEColor.ORANGE)
    registerWirelessItemModel(3, AEColor.MAGENTA)
    registerWirelessItemModel(4, AEColor.LIGHT_BLUE)
    registerWirelessItemModel(5, AEColor.YELLOW)
    registerWirelessItemModel(6, AEColor.LIME)
    registerWirelessItemModel(7, AEColor.PINK)
    registerWirelessItemModel(8, AEColor.GRAY)
    registerWirelessItemModel(9, AEColor.LIGHT_GRAY)
    registerWirelessItemModel(10, AEColor.CYAN)
    registerWirelessItemModel(11, AEColor.PURPLE)
    registerWirelessItemModel(12, AEColor.BLUE)
    registerWirelessItemModel(13, AEColor.BROWN)
    registerWirelessItemModel(14, AEColor.GREEN)
    registerWirelessItemModel(15, AEColor.RED)
    registerWirelessItemModel(16, AEColor.BLACK)
  }

  private def registerWirelessItemModel(meta: Int, color: AEColor): Unit = {
    ModelLoader.setCustomModelResourceLocation(
      BlockWireless.itemBlockInstance,
      meta,
      new ModelResourceLocation(String.format("%s:wireless/%s", AE2Stuff.modId, color.name.toLowerCase)))
  }

  override def init(): Unit = {
    OverlayRenderHandler.register(WirelessOverlayRender)
    OverlayRenderHandler.register(VisualiserOverlayRender)
    MouseEventHandler.init()
  }
}
