package net.bdew.ae2stuff

import net.bdew.ae2stuff.items.visualiser.VisualiserOverlayRender
import net.bdew.ae2stuff.machines.wireless.{WirelessModelFactory, WirelessModelLoader, WirelessOverlayRender}
import net.bdew.ae2stuff.misc.{Icons, MouseEventHandler, OverlayRenderHandler}
import net.minecraftforge.client.model.ModelLoaderRegistry

class ClientProxy extends CommonProxy {

  override def preInit(): Unit = {
    Icons.init()
    ModelLoaderRegistry.registerLoader(new WirelessModelLoader(Map("models/block/builtin/wireless" -> new WirelessModelFactory())))
  }

  override def init(): Unit = {
    OverlayRenderHandler.register(WirelessOverlayRender)
    OverlayRenderHandler.register(VisualiserOverlayRender)
    MouseEventHandler.init()
  }
}
