package net.bdew.ae2stuff.top

import appeng.integration.modules.theoneprobe.TheOneProbeText
import appeng.util.Platform
import mcjty.theoneprobe.api.{IProbeHitData, IProbeInfo, ProbeMode}
import net.bdew.ae2stuff.grid.PoweredTile
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

object TOPPoweredInfoProvider extends TOPTileProvider {

  override def addProbeInfo(tile: TileEntity,
                            mode: ProbeMode,
                            probeInfo: IProbeInfo,
                            player: EntityPlayer,
                            world: World,
                            state: IBlockState,
                            data: IProbeHitData): Unit = {
    tile match {
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
      case _ =>
    }
  }
}
