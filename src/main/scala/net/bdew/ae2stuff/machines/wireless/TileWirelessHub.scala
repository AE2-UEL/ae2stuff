package net.bdew.ae2stuff.machines.wireless

import appeng.api.networking.IGridConnection
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TileWirelessHub extends TileWireless {

  cfg = MachineWirelessHub

  var connectionsList: Array[TileWireless] = Array[TileWireless]()
  private var hubPowerUsage = 0d

  override def isHub: Boolean = true

  override def doUnlink(): Unit = {
    connectionsList foreach { that =>
      that.doUnlink()
    }
  }

  override def setConnection(connection: IGridConnection, to: TileWireless): Unit = {
    this.connectionsList = this.connectionsList :+ to
  }

  override def breakConnection(): Unit = {
    connectionsList = connectionsList.filterNot(_ == this)
    setPowerUse(-getIdlePowerUsage)
    setActive(world, active = false)
  }

  override def setPowerUse(power: Double): Unit = {
    hubPowerUsage += power
    this.setIdlePowerUse(hubPowerUsage)
  }

  def getHubChannels: Int = {
    var channels = 0
    connectionsList foreach { that =>
      channels += that.connection.getUsedChannels
    }
    channels
  }

  override def setActive(world: World, active: Boolean): Unit = {
    if (world.isBlockLoaded(pos)) {
      BlockWirelessHub.setActive(world, pos, active)
    }
  }

  override def getMachineRepresentation: ItemStack = new ItemStack(BlockWirelessHub)

  override def shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newSate: IBlockState): Boolean = newSate.getBlock != BlockWirelessHub
}
