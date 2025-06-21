package net.bdew.ae2stuff

import appeng.api.util.AEColor
import net.bdew.ae2stuff.items.visualiser.VisualiserOverlayRender
import net.bdew.ae2stuff.machines.wireless.{BlockWireless, BlockWirelessHub, WirelessModelFactory, WirelessModelLoader, WirelessOverlayRender}
import net.bdew.ae2stuff.misc.{Icons, MouseEventHandler, OverlayRenderHandler}
import net.bdew.ae2stuff.network.{MsgAdvWirelessKitKeybind, NetHandler}
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.model.{ModelLoader, ModelLoaderRegistry}
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.{ClientTickEvent, Phase}
import org.lwjgl.input.Keyboard

class ClientProxy extends CommonProxy {

  val advWirelessKitKeybind = new KeyBinding("ae2stuff.keybinding.adv_wireless_second", KeyConflictContext.IN_GAME, Keyboard.KEY_LCONTROL, "AE2Stuff")
  private var isKeyDown = false

  override def preInit(): Unit = {
    super.preInit()
    MinecraftForge.EVENT_BUS.register(this)
    Icons.init()
    ModelLoaderRegistry.registerLoader(
      new WirelessModelLoader(
        Map(
          "models/block/builtin/wireless" -> new WirelessModelFactory("wireless"),
          "models/block/builtin/wireless_hub" -> new WirelessModelFactory("wireless_hub")
        )
      )
    )
    ClientRegistry.registerKeyBinding(advWirelessKitKeybind)
  }

  @SubscribeEvent
  def registerModels(event: ModelRegistryEvent): Unit = {
    registerWirelessItemModel(0, AEColor.TRANSPARENT)
    registerWirelessHubItemModel(0, AEColor.TRANSPARENT)
    for (i <- 0 to 15) {
      registerWirelessItemModel(i + 1, AEColor.values.apply(i))
      registerWirelessHubItemModel(i + 1, AEColor.values.apply(i))
    }
  }

  @SubscribeEvent
  def clientTick(event: ClientTickEvent): Unit = {
    if (event.phase != Phase.START) return
    val isKeyDownNew = advWirelessKitKeybind.isKeyDown
    if (isKeyDownNew != isKeyDown) {
      isKeyDown = isKeyDownNew
      NetHandler.sendToServer(MsgAdvWirelessKitKeybind(isKeyDown))
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
      BlockWirelessHub.itemBlockInstance,
      meta,
      new ModelResourceLocation(String.format("%s:wireless_hub/%s", AE2Stuff.modId, color.name.toLowerCase)))
  }

  override def init(): Unit = {
    super.init()
    OverlayRenderHandler.register(WirelessOverlayRender)
    OverlayRenderHandler.register(VisualiserOverlayRender)
    MouseEventHandler.init()
  }
}
