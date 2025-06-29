package net.bdew.ae2stuff.machines.wireless

import appeng.api.networking.IGridConnection
import net.bdew.lib.data.base.UpdateKind
import net.bdew.lib.multiblock.data.DataSlotPos
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TileWirelessHub extends TileWireless {

  private val cfg = MachineWirelessHub

  def enableLinkRendering: Boolean = cfg.maxConnections <= 32

  var connectionsList: Array[TileWireless] = Array()
  var links: Array[DataSlotPos] = (1 to 32 map (x =>
    DataSlotPos("link" + x, this).setUpdate(UpdateKind.SAVE, UpdateKind.WORLD)
  )).toArray

  private var hubPowerUsage = 0d

  override def isHub: Boolean = true

  def canSupportNewLink: Boolean = connectionsList.length <= cfg.maxConnections

  def getNumMaxLinks: Int = cfg.maxConnections

  override def doUnlink(): Unit = {
    connectionsList foreach { that =>
      that.doUnlink()
    }
    clearLinks()
  }

  override def setConnection(connection: IGridConnection, to: TileWireless): Unit = {
    this.connectionsList = this.connectionsList :+ to
    addLink(to.getPos)
  }

  override def breakConnection(from: TileWireless): Unit = {
    connectionsList = connectionsList.filterNot(_ == from)
    removeLink(from.getPos)
    setPowerUse(-getIdlePowerUsage)
    if (connectionsList.length == 0) {
      setActive(world, active = false)
    }
  }

  override def setPowerUse(power: Double): Unit = {
    hubPowerUsage += power
    this.setIdlePowerUse(hubPowerUsage)
  }

  private def addLink(pos: BlockPos): Unit = {
    if (!enableLinkRendering) return
    links.foreach(link =>
      if (!link.isDefined) {
        link.set(pos)
        return
      }
    )
  }

  private def removeLink(pos: BlockPos): Unit = {
    if (!enableLinkRendering) return
    for (i <- links.indices) {
      val link = links(i)
      if (link.isDefined) {
        link.value match {
          case Some(linkPos) =>
            if (linkPos == pos) {
              links(i) := None
              return
            }
        }
      }
    }
  }

  private def clearLinks(): Unit = {
    if (!enableLinkRendering) return
    links.foreach(link => link := None)
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
