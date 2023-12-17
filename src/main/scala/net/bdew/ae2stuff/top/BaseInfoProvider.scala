package net.bdew.ae2stuff.top

import mcjty.theoneprobe.TheOneProbe
import mcjty.theoneprobe.api.{IProbeHitData, IProbeInfo, IProbeInfoProvider, ProbeMode}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

object BaseInfoProvider extends IProbeInfoProvider {

  private final val providers = List[TOPTileProvider](
    TOPPoweredInfoProvider, TOPWirelessInfoProvider
  )

  def init(): Unit = {
    val oneProbe = TheOneProbe.theOneProbeImp
    oneProbe.registerProvider(this)
  }

  override def getID: String = "ae2stuff:BaseInfoProvider"

  override def addProbeInfo(probeMode: ProbeMode,
                            probeInfo: IProbeInfo,
                            player: EntityPlayer,
                            world: World,
                            state: IBlockState,
                            data: IProbeHitData): Unit = {
    val tile = world.getTileEntity(data.getPos)
    if (tile != null) {
      providers.foreach(p => p.addProbeInfo(tile, probeMode, probeInfo, player, world, state, data))
    }
  }
}
