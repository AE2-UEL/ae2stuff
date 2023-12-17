package net.bdew.ae2stuff.top

import mcjty.theoneprobe.api.{IProbeHitData, IProbeInfo, ProbeMode}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

trait TOPTileProvider {

  def addProbeInfo(tileEntity: TileEntity,
                   mode: ProbeMode,
                   probeInfo: IProbeInfo,
                   player: EntityPlayer,
                   world: World,
                   state: IBlockState,
                   data: IProbeHitData): Unit = ???
}
