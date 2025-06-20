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
import net.bdew.ae2stuff.machines.wireless.BlockWirelessProperties.{COLOR_PROPERTY, HUB_PROPERTY}
import net.bdew.ae2stuff.misc.{BlockActiveTexture, BlockWrenchable, MachineMaterial}
import net.bdew.lib.Misc
import net.bdew.lib.block.{BaseBlock, HasItemBlock, HasTE, ItemBlockTooltip}
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{EnumDyeColor, ItemStack}
import net.minecraft.util.math.{BlockPos, RayTraceResult}
import net.minecraft.util.text.translation.I18n
import net.minecraft.util.{EnumFacing, EnumHand, NonNullList}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.common.property.{IExtendedBlockState, IUnlistedProperty}

import java.util

object BlockWirelessProperties {
  val COLOR_PROPERTY = new WirelessColorProperty("wireless_color_property")
  val HUB_PROPERTY = new WirelessHubProperty("wireless_hub_property")
}

object BlockWireless extends BaseBlock("wireless", MachineMaterial) with HasTE[TileWireless] with BlockWrenchable with BlockActiveTexture with HasItemBlock {
  override val TEClass = classOf[TileWireless]
  override val itemBlockInstance = new ItemBlockWireless(this)

  setHardness(1)

  var isHub = false

  override def getDrops(drops: NonNullList[ItemStack], world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int): Unit = {
    val stack = new ItemStack(this)
    val te = world.getTileEntity(pos).asInstanceOf[TileWireless]
    if (te != null) {
      if (te.isHub) {
        if (te.color != AEColor.TRANSPARENT) {
          stack.setItemDamage(te.color.ordinal + 18)
        } else {
          stack.setItemDamage(17)
        }
      } else if (te.color != AEColor.TRANSPARENT) {
        stack.setItemDamage(te.color.ordinal + 1)
      }
    } else if (isHub) {
      stack.setItemDamage(17)
    }
    drops.add(stack)
  }

  override def getSubBlocks(item: CreativeTabs, items: NonNullList[ItemStack]): Unit = {
    for (meta <- 0 to 33) {
      items.add(new ItemStack(this, 1, meta))
    }
  }

  override def getPickBlock(state: IBlockState, target: RayTraceResult, world: World, pos: BlockPos, player: EntityPlayer): ItemStack = {
    val stack = new ItemStack(this)
    val te = getTE(world, pos)
    if (te.isHub) {
      if (te.color != AEColor.TRANSPARENT) {
        stack.setItemDamage(te.color.ordinal + 18)
      } else {
        stack.setItemDamage(17)
      }
    } else if (te.color != AEColor.TRANSPARENT) {
      stack.setItemDamage(te.color.ordinal() + 1)
    }
    stack
  }

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
    val te = getTE(world, pos)
    te.doUnlink()
    isHub = te.isHub
    super.breakBlock(world, pos, state)
  }

  override def onBlockPlacedBy(world: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack): Unit = {
    super.onBlockPlacedBy(world, pos, state, placer, stack)
    val te = getTE(world, pos)
    if (placer.isInstanceOf[EntityPlayer]) {
      te.placingPlayer = placer.asInstanceOf[EntityPlayer]
    }
    if (stack != ItemStack.EMPTY) {
      val itemDamage = stack.getItemDamage
      if (stack.hasDisplayName) {
        te.customName = stack.getDisplayName
      }
      if (itemDamage > 16) {
        te.isHub = true
        if (itemDamage == 17) {
          te.color = AEColor.values.apply(16)
        } else {
          te.color = AEColor.values.apply(itemDamage - 18)
        }
      } else if (itemDamage > 0) {
        te.color = AEColor.values.apply(itemDamage - 1)
      }
    }
  }

  override def getUnlistedProperties: List[IUnlistedProperty[_]] =
    super.getUnlistedProperties :+ COLOR_PROPERTY :+ HUB_PROPERTY

  override def getExtendedState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState = {
    val te = getTE(world, pos)
    if (te.isEmpty) return state
    super.getExtendedState(state, world, pos).asInstanceOf[IExtendedBlockState]
      .withProperty(COLOR_PROPERTY, te.get.color)
      .withProperty(HUB_PROPERTY, Boolean.box(te.get.isHub))
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

class ItemBlockWireless(b: Block) extends ItemBlockTooltip(b) {

  setHasSubtypes(true)

  override def addInformation(stack: ItemStack, world: World, list: util.List[String], flags: ITooltipFlag): Unit = {
    val itemDamage = stack.getItemDamage
    if (itemDamage == 0 || itemDamage == 17) {
      list.add(Misc.toLocal(AEColor.values.apply(16).unlocalizedName))
    } else if (itemDamage > 16) {
      list.add(Misc.toLocal(AEColor.values.apply(itemDamage - 18).unlocalizedName))
    } else if (itemDamage > 0) {
      list.add(Misc.toLocal(AEColor.values().apply(itemDamage - 1).unlocalizedName))
    }
  }

  override def getItemStackDisplayName(stack: ItemStack): String = {
    if (stack.getItemDamage >= 17) {
      return I18n.translateToLocal("tile.ae2stuff.wireless_hub.name")
    }
    super.getItemStackDisplayName(stack)
  }
}
