package net.bdew.ae2stuff.top

import appeng.api.config.PowerMultiplier
import appeng.api.util.AEColor
import appeng.core.AEConfig
import appeng.core.features.AEFeature
import mcjty.theoneprobe.api.{IProbeHitData, IProbeInfo, ProbeMode, TextStyleClass}
import net.bdew.ae2stuff.machines.wireless.TileWireless
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World

object TOPWirelessInfoProvider extends TOPTileProvider {

  override def addProbeInfo(tileEntity: TileEntity,
                            mode: ProbeMode,
                            probeInfo: IProbeInfo,
                            player: EntityPlayer,
                            world: World,
                            state: IBlockState,
                            data: IProbeHitData): Unit = {
    tileEntity match {
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
      case _ =>
    }
  }

  private def getTextColor(aeColor: AEColor): String = {
    (aeColor match {
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
}
