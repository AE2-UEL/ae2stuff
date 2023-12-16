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

import appeng.api.util.{AEColor, AEPartLocation}
import appeng.core.sync.GuiBridge
import appeng.items.tools.quartz.ToolQuartzCuttingKnife
import appeng.util.Platform
import net.bdew.ae2stuff.machines.wireless.BlockWirelessProperties.COLOR_PROPERTY
import net.bdew.ae2stuff.misc.{BlockActiveTexture, BlockWrenchable, MachineMaterial}
import net.bdew.lib.block.{BaseBlock, HasTE}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{EnumDyeColor, ItemStack}
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.common.property.{IExtendedBlockState, IUnlistedProperty}

object BlockWirelessProperties {
  val COLOR_PROPERTY = new WirelessColorProperty("wireless_color_property")
}

object BlockWireless extends BaseBlock("wireless", MachineMaterial) with HasTE[TileWireless] with BlockWrenchable with BlockActiveTexture {
  override val TEClass = classOf[TileWireless]

  setHardness(1)

  override def onBlockActivatedReal(world: World,
                                    pos: BlockPos,
                                    state: IBlockState,
                                    player: EntityPlayer,
                                    hand: EnumHand,
                                    heldItem: ItemStack,
                                    side: EnumFacing,
                                    hitX: Float,
                                    hitY: Float,
                                    hitZ: Float
                                   ): Boolean = {
    val item = player.getHeldItem(hand)
    if (item != ItemStack.EMPTY && item.getItem.isInstanceOf[ToolQuartzCuttingKnife]) {
      val te = world.getTileEntity(pos)
      if (te.isInstanceOf[TileWireless]) {
        if (Platform.isServer) {
          Platform.openGUI(player, te, AEPartLocation.fromFacing(side), GuiBridge.GUI_RENAMER)
        }
      }
      return true
    }
    false
  }

  override def breakBlock(world: World, pos: BlockPos, state: IBlockState): Unit = {
    getTE(world, pos).doUnlink()
    super.breakBlock(world, pos, state)
  }

  override def onBlockPlacedBy(world: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack): Unit = {
    super.onBlockPlacedBy(world, pos, state, placer, stack)
    if (placer.isInstanceOf[EntityPlayer]) {
      val te = getTE(world, pos)
      te.placingPlayer = placer.asInstanceOf[EntityPlayer]
      if (stack != ItemStack.EMPTY && stack.hasDisplayName) {
        te.customName = stack.getDisplayName
      }
    }
  }

  override def getUnlistedProperties: List[IUnlistedProperty[_]] = super.getUnlistedProperties :+ COLOR_PROPERTY

  override def getExtendedState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState = {
    val te = getTE(world, pos)
    if (te.isEmpty) return state
    super.getExtendedState(state, world, pos).asInstanceOf[IExtendedBlockState]
      .withProperty(COLOR_PROPERTY, te.get.color)
  }

  override def recolorBlock(world: World, pos: BlockPos, side: EnumFacing, color: EnumDyeColor): Boolean = {
    val te = getTE(world, pos)
    if (te != null) {
      val aeColor = if (color != null) AEColor.values()(color.ordinal()) else AEColor.TRANSPARENT
      return te.recolourBlock(side, aeColor, null)
    }
    false
  }
}
