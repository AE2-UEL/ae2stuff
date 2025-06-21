package net.bdew.ae2stuff.top

import appeng.api.config.PowerMultiplier
import appeng.api.util.AEColor
import appeng.core.AEConfig
import appeng.core.features.AEFeature
import appeng.integration.modules.theoneprobe.TheOneProbeText
import appeng.util.Platform
import mcjty.theoneprobe.TheOneProbe
import mcjty.theoneprobe.api.{IProbeHitData, IProbeInfo, IProbeInfoProvider, ProbeMode, TextStyleClass}
import net.bdew.ae2stuff.grid.PoweredTile
import net.bdew.ae2stuff.machines.wireless.{TileWireless, TileWirelessHub}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World

object TOPHandler extends IProbeInfoProvider {

  def init(): Unit = {
    val oneProbe = TheOneProbe.theOneProbeImp
    oneProbe.registerProvider(this)
  }

  override def getID: String = "ae2stuff:InfoProvider"

  override def addProbeInfo(probeMode: ProbeMode,
                            probeInfo: IProbeInfo,
                            player: EntityPlayer,
                            world: World,
                            state: IBlockState,
                            data: IProbeHitData): Unit = world.getTileEntity(data.getPos) match {
    case poweredTile: PoweredTile =>
      val formatCurrentPower = Platform.formatPowerLong(poweredTile.powerStored.toLong, false)
      val formatMaxPower = Platform.formatPowerLong(poweredTile.powerCapacity.toLong, false)
      val formattedString = String.format(TheOneProbeText.STORED_ENERGY.getLocal, formatCurrentPower, formatMaxPower)
      probeInfo.text(formattedString)

      if (poweredTile.node != null && poweredTile.node.isActive) {
        probeInfo.text(TheOneProbeText.DEVICE_ONLINE.getLocal)
      } else {
        probeInfo.text(TheOneProbeText.DEVICE_OFFLINE.getLocal)
      }

    case wireless: TileWireless =>

      // Connection status
      if (wireless.isHub) {
        val hub = wireless.asInstanceOf[TileWirelessHub]
        val connections = hub.connectionsList.length
        var color = TextFormatting.GREEN
        if (connections >= 16) {
          color = TextFormatting.YELLOW
        }
        if (connections >= 24) {
          color = TextFormatting.RED
        }
        if (connections >= 32) {
          color = TextFormatting.DARK_RED
        }

        probeInfo.text(TextStyleClass.INFO + "{*ae2stuff.top.wireless.hub_connections*}"
          + " " + color + connections + " / " + 32)
      } else if (wireless.link.isDefined) {
        val pos = wireless.link.get
        probeInfo.text(TextStyleClass.OK + "{*ae2stuff.top.wireless.connected*}" +
          " " + pos.getX + "," + pos.getY + "," + pos.getZ)
      } else {
        probeInfo.text(TextStyleClass.WARNING + "{*ae2stuff.waila.wireless.notconnected*}")
      }

      // Channels used
      if (AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS)) {
        var usedChannels = -1
        if (wireless.isHub) {
          val hub = wireless.asInstanceOf[TileWirelessHub]
          usedChannels = hub.connectionsList.length
        } else if (wireless.link.isDefined && wireless.connection != null) {
          usedChannels = wireless.connection.getUsedChannels
        }
        if (usedChannels != -1) {
          probeInfo.text(TextStyleClass.INFO + "{*ae2stuff.top.wireless.channels*}" + " " + usedChannels)
        }
      }

      // Power used
      if (wireless.isHub || wireless.link.isDefined) {
        probeInfo.text(TextStyleClass.INFO + "{*ae2stuff.top.wireless.power*}" + " " +
          (math rint PowerMultiplier.CONFIG.multiply(wireless.getIdlePowerUsage) * 10) / 10 + " AE/t")
      }

      // Custom name
      val name = if (wireless.customName != null) wireless.customName else null
      if (name != null) {
        probeInfo.text(TextStyleClass.INFO + "{*ae2stuff.top.wireless.name*}" + " " + name)
      }

      // Color
      if (wireless.color != AEColor.TRANSPARENT) {
        probeInfo.text(getTextColor(wireless.color) + "{*" + wireless.color.unlocalizedName + "*}")
      }
    case _ =>
  }

  private def getTextColor(aeColor: AEColor): String = (aeColor match {
    case AEColor.WHITE => TextFormatting.WHITE
    case AEColor.ORANGE => TextFormatting.GOLD
    case AEColor.MAGENTA => TextFormatting.LIGHT_PURPLE
    case AEColor.LIGHT_BLUE => TextFormatting.AQUA
    case AEColor.YELLOW => TextFormatting.YELLOW
    case AEColor.LIME => TextFormatting.GREEN
    case AEColor.PINK => TextFormatting.RED
    case AEColor.GRAY => TextFormatting.DARK_GRAY
    case AEColor.LIGHT_GRAY => TextFormatting.GRAY
    case AEColor.CYAN => TextFormatting.DARK_AQUA
    case AEColor.PURPLE => TextFormatting.DARK_PURPLE
    case AEColor.BLUE => TextFormatting.BLUE
    case AEColor.BROWN => TextFormatting.DARK_RED
    case AEColor.GREEN => TextFormatting.DARK_GREEN
    case AEColor.RED => TextFormatting.DARK_RED
    case AEColor.BLACK => TextFormatting.BLACK
    case _ => TextFormatting.WHITE
  }).toString
}
