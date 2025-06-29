package net.bdew.ae2stuff.machines.wireless

import appeng.api.util.{AEColor, AEPartLocation}
import appeng.core.sync.GuiBridge
import appeng.items.tools.quartz.ToolQuartzCuttingKnife
import appeng.util.Platform
import net.bdew.ae2stuff.machines.wireless.BlockWirelessProperties.COLOR_PROPERTY
import net.bdew.ae2stuff.misc.{BlockActiveTexture, BlockWrenchable, MachineMaterial}
import net.bdew.lib.Misc
import net.bdew.lib.block.{BaseBlock, HasItemBlock, HasTE, ItemBlockTooltip}
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{EnumDyeColor, ItemBlock, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.{BlockPos, RayTraceResult}
import net.minecraft.util.{EnumFacing, EnumHand, NonNullList}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.common.property.{IExtendedBlockState, IUnlistedProperty}

import java.util

object BlockWirelessHub extends BaseBlock("wireless_hub", MachineMaterial) with HasTE[TileWireless] with BlockWrenchable with BlockActiveTexture with HasItemBlock {
  override val TEClass: Class[_ <: TileEntity] = classOf[TileWirelessHub]
  override val itemBlockInstance: ItemBlock = new ItemBlockWirelessHub(this)

  setHardness(1)
  setHarvestLevel("pickaxe", 2)

  override def getDrops(drops: NonNullList[ItemStack], world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int): Unit = {
    val stack = new ItemStack(this)
    val te = world.getTileEntity(pos).asInstanceOf[TileWirelessHub]
    if (te != null) {
      if (te.color != AEColor.TRANSPARENT) {
        stack.setItemDamage(te.color.ordinal + 1)
      }
    }
    drops.add(stack)
  }

  override def getSubBlocks(item: CreativeTabs, items: NonNullList[ItemStack]): Unit = {
    for (meta <- 0 to 16) {
      items.add(new ItemStack(this, 1, meta))
    }
  }

  override def getPickBlock(state: IBlockState, target: RayTraceResult, world: World, pos: BlockPos, player: EntityPlayer): ItemStack = {
    val stack = new ItemStack(this)
    val te = getTE(world, pos)
    if (te.color != AEColor.TRANSPARENT) {
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
      if (te.isInstanceOf[TileWirelessHub]) {
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
    val te = getTE(world, pos)
    if (placer.isInstanceOf[EntityPlayer]) {
      te.placingPlayer = placer.asInstanceOf[EntityPlayer]
    }
    if (stack != ItemStack.EMPTY) {
      if (stack.hasDisplayName) {
        te.customName = stack.getDisplayName
      }
      if (stack.getItemDamage > 0) {
        te.color = AEColor.values.apply(stack.getItemDamage - 1)
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

class ItemBlockWirelessHub(b: Block) extends ItemBlockTooltip(b) {

  setHasSubtypes(true)

  override def addInformation(stack: ItemStack, world: World, list: util.List[String], flags: ITooltipFlag): Unit = {
    list.add(Misc.toLocalF("tile.ae2stuff.wireless_hub.tooltip1", MachineWirelessHub.maxConnections))
    val itemDamage = stack.getItemDamage
    if (itemDamage == 0) {
      list.add(Misc.toLocal(AEColor.TRANSPARENT.unlocalizedName))
    } else {
      list.add(Misc.toLocal(AEColor.values().apply(itemDamage - 1).unlocalizedName))
    }
  }
}
