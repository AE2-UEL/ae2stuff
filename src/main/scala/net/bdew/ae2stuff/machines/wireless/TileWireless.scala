/*
 * Copyright (c) bdew, 2014 - 2020
 * https://github.com/bdew/ae2stuff
 *
 * This mod is distributed under the terms of the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package net.bdew.ae2stuff.machines.wireless

import java.util
import appeng.api.AEApi
import appeng.api.implementations.tiles.IColorableTile
import appeng.api.networking.security.IActionHost
import appeng.api.networking.{GridFlags, IGridConnection, IGridNode}
import appeng.api.util.{AEColor, AEPartLocation}
import appeng.helpers.ICustomNameObject
import net.bdew.ae2stuff.AE2Stuff
import net.bdew.ae2stuff.grid.{GridTile, VariableIdlePower}
import net.bdew.lib.PimpVanilla._
import net.bdew.lib.data.base.{TileDataSlots, UpdateKind}
import net.bdew.lib.multiblock.data.DataSlotPos
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TileWireless extends TileDataSlots
  with GridTile
  with IActionHost
  with VariableIdlePower
  with ICustomNameObject
  with IColorableTile {

  val cfg = MachineWireless

  val link = DataSlotPos("link", this).setUpdate(UpdateKind.SAVE, UpdateKind.WORLD)

  var connection: IGridConnection = null

  var customName: String = null
  var color: AEColor = AEColor.TRANSPARENT
  def isLinked = link.isDefined
  def getLink = link flatMap world.getTileSafe[TileWireless]

  override def getFlags = util.EnumSet.of(GridFlags.DENSE_CAPACITY)

  serverTick.listen(() => {
    if (connection == null && link.isDefined) {
      setupConnection()
    }
  })

  def doLink(other: TileWireless): Boolean = {
    if (other.link.isEmpty) {
      other.link.set(pos)
      this.customName = other.customName
      link.set(other.getPos)
      setupConnection()
    } else false
  }

  def doUnlink(): Unit = {
    breakConnection()
    getLink foreach { that =>
      this.link := None
      that.link := None
    }
  }

  def setupConnection(): Boolean = {
    getLink foreach { that =>
      try {
        connection = AEApi.instance().grid().createGridConnection(this.getNode, that.getNode)
        that.connection = connection

        val dx = this.getPos.getX - that.getPos.getX
        val dy = this.getPos.getY - that.getPos.getY
        val dz = this.getPos.getZ - that.getPos.getZ
        val dist = math.sqrt(dx * dx + dy * dy + dz * dz)
        val power = cfg.powerBase + cfg.powerDistanceMultiplier * dist * math.log(dist * dist + 3)
        this.setIdlePowerUse(power)
        that.setIdlePowerUse(power)
        if (world.isBlockLoaded(pos)) {
          BlockWireless.setActive(world, pos, true)
        }
        if (world.isBlockLoaded(that.getPos)) {
          BlockWireless.setActive(world, that.getPos, true)
        }
        return true
      } catch {
        case t: Exception =>
          AE2Stuff.logWarnException("Failed setting up wireless link %s <-> %s", t, pos, that.getPos)
          doUnlink()
      }
    }
    false
  }

  def breakConnection(): Unit = {
    if (connection != null)
      connection.destroy()
    connection = null
    setIdlePowerUse(0D)
    getLink foreach { other =>
      other.connection = null
      other.setIdlePowerUse(0D)
      if (world.isBlockLoaded(other.getPos)) {
        BlockWireless.setActive(world, other.getPos, false)
      }
    }
    if (world.isBlockLoaded(pos)) {
      BlockWireless.setActive(world, pos, false)
    }
  }

  override def getMachineRepresentation: ItemStack = new ItemStack(BlockWireless)

  override def shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newSate: IBlockState): Boolean = newSate.getBlock != BlockWireless

  override def doSave(kind: UpdateKind.Value, t: NBTTagCompound): Unit = {
    super.doSave(kind, t)
    if (customName != null) {
      t.setString("CustomName", customName)
    }
    t.setShort("Color", color.ordinal().toShort)
  }

  override def doLoad(kind: UpdateKind.Value, t: NBTTagCompound): Unit = {
    super.doLoad(kind, t)
    if (t.hasKey("CustomName")) {
      this.customName = t.getString("CustomName")
    }
    if (!t.hasKey("Color")) {
      t.setShort("Color", AEColor.TRANSPARENT.ordinal().toShort)
    }
    val colorIdx = t.getShort("Color").toInt
    this.color = AEColor.values().apply(colorIdx)
    if (hasWorld) {
      scheduleRenderUpdate()
    }
  }

  override def recolourBlock(enumFacing: EnumFacing, aeColor: AEColor, entityPlayer: EntityPlayer): Boolean = {
    if (this.color == aeColor) {
      return false
    }
    this.color = aeColor
    if (getGridNode(AEPartLocation.fromFacing(enumFacing)) != null) {
      getGridNode(AEPartLocation.fromFacing(enumFacing)).updateState()
      scheduleRenderUpdate()
      markDirty()
    }
    true
  }

  private def scheduleRenderUpdate(): Unit = {
    world.markBlockRangeForRenderUpdate(
      pos.getX - 1, pos.getY - 1, pos.getZ - 1,
      pos.getX + 1, pos.getY + 1, pos.getZ + 1
    )
  }

  override def getActionableNode: IGridNode = this.node
  override def getColor: AEColor = color
  override def getGridColor: AEColor = color
  override def getCustomInventoryName: String = customName
  override def hasCustomInventoryName: Boolean = customName != null

  override def setCustomName(name: String): Unit = {
    this.customName = name
    this.getLink.foreach(te => te.customName = name)
    markDirty()
  }
}
