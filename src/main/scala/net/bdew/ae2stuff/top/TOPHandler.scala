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
import net.bdew.ae2stuff.machines.wireless.TileWireless
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World

object BaseInfoProvider extends IProbeInfoProvider {

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
      if (wireless.link.isDefined) {
        val pos = wireless.link.get
        probeInfo.text(TextStyleClass.OK + "{*ae2stuff.top.wireless.connected*}" +
          " " + pos.getX + "," + pos.getY + "," + pos.getZ)
        if (wireless.connection != null && AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS)) {
          val usedChannels = wireless.connection.getUsedChannels
          probeInfo.text(TextStyleClass.INFO + "{*ae2stuff.top.wireless.channels*}" + " " + usedChannels)
        }
        probeInfo.text(TextStyleClass.INFO + "{*ae2stuff.top.wireless.power*}" + " " +
          (math rint PowerMultiplier.CONFIG.multiply(wireless.getIdlePowerUsage) * 10) / 10 + " AE/t")
      } else {
        probeInfo.text(TextStyleClass.WARNING + "{*ae2stuff.waila.wireless.notconnected*}")
      }
      val name = if (wireless.customName != null) wireless.customName else null
      if (name != null) {
        probeInfo.text(TextStyleClass.INFO + "{*ae2stuff.top.wireless.name*}" + " " + name)
      }
      if (wireless.color != AEColor.TRANSPARENT) {
        probeInfo.text(getTextColor(wireless.color) + "{*" + wireless.color.unlocalizedName + "*}")
      }
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
