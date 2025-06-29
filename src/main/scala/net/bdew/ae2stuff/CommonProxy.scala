package net.bdew.ae2stuff

import net.bdew.ae2stuff.items.ItemAdvWirelessKit
import net.bdew.lib.network.SafeObjectInputStream
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent

class CommonProxy {

  def preInit(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

  def init(): Unit = {
    try {
      val clazz = Class.forName("net.bdew.lib.network.SafeObjectInputStream$")
      val field = clazz.getDeclaredField("net$bdew$lib$network$SafeObjectInputStream$$validClasses")
      field.setAccessible(true)
      val set = field.get(SafeObjectInputStream).asInstanceOf[scala.collection.immutable.Set[String]]
      val newSet = set.+("net.bdew.ae2stuff.network.MsgAdvWirelessKitKeybind")
      field.set(SafeObjectInputStream, newSet)
    } catch {
      case _: Exception =>
    }
  }

  @SubscribeEvent
  def playerLoggedOut(event: PlayerLoggedOutEvent): Unit = {
    ItemAdvWirelessKit.onPlayerLoggedOut(event.player)
  }

  @SubscribeEvent
  def registerRecipes(event: RegistryEvent.Register[IRecipe]): Unit = {
    Recipes.load()
  }
}
