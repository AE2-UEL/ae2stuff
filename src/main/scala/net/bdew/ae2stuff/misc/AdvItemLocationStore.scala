package net.bdew.ae2stuff.misc

import net.bdew.ae2stuff.items.ItemAdvWirelessKit.MODE_QUEUEING
import net.bdew.lib.nbt.converters.TBlockPos
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.Constants

trait AdvItemLocationStore extends Item {

  def addLocation(stack: ItemStack, loc: BlockPos, dimension: Int, isHub: Boolean): Boolean = {
    if (!stack.hasTagCompound) stack.setTagCompound(new NBTTagCompound)
    val tag = stack.getTagCompound
    if (tag.hasKey("dim") && tag.getInteger("dim") != dimension) {
      return false
    }
    val locList = tag.getTagList("loc", Constants.NBT.TAG_COMPOUND)
    for (i <- 0 until locList.tagCount()) {
      val tag = locList.getCompoundTagAt(i)
      val pos = TBlockPos.decode(tag).get
      if (pos == loc && !isHub) {
        return false
      }
    }
    locList.appendTag(TBlockPos.encode(loc))
    tag.setTag("loc", locList)
    tag.setInteger("dim", dimension)
    true
  }

  def getLocations(stack: ItemStack): NBTTagList = {
    if (!stack.hasTagCompound) stack.setTagCompound(new NBTTagCompound)
    val tag = stack.getTagCompound
    if (tag.hasKey("loc")) {
      val locList = tag.getTagList("loc", Constants.NBT.TAG_COMPOUND)
      locList
    } else {
      tag.setTag("loc", new NBTTagCompound)
      tag.getTagList("loc", Constants.NBT.TAG_COMPOUND)
    }
  }

  // location is going to be a queue of coordinates

  def hasLocation(stack: ItemStack): Boolean = {
    if (stack.getItem == this && stack.hasTagCompound && stack.getTagCompound.hasKey("loc")) {
      // check if list is not empty
      val loc = stack.getTagCompound.getTagList("loc", Constants.NBT.TAG_COMPOUND)
      if (loc.tagCount() > 0) {
        return true
      }
    }
    false
  }

  def getNextLocation(stack: ItemStack): Option[BlockPos] = {
    TBlockPos.decode(stack.getTagCompound.getTagList("loc", Constants.NBT.TAG_COMPOUND).getCompoundTagAt(0))
  }

  def getDimension(stack: ItemStack): Int =
    stack.getTagCompound.getInteger("dim")

  def setLocation(stack: ItemStack, loc: BlockPos, dimension: Int): Unit = {
    if (!stack.hasTagCompound) stack.setTagCompound(new NBTTagCompound)
    val tag = stack.getTagCompound
    val locList = tag.getTagList("loc", Constants.NBT.TAG_COMPOUND)
    locList.appendTag(TBlockPos.encode(loc))
    tag.setTag("loc", locList)
    tag.setInteger("dim", dimension)
  }

  def popLocation(stack: ItemStack): BlockPos = {
    if (stack.hasTagCompound) {
      val locList = stack.getTagCompound.getTagList("loc", Constants.NBT.TAG_COMPOUND)
      if (locList.tagCount() > 0) {
        val tag = locList.getCompoundTagAt(0)
        locList.removeTag(0)
        val pos = TBlockPos.decode(tag).get
        stack.getTagCompound.setTag("loc", locList)
        return pos
      }
      if (locList.tagCount() == 0) {
        stack.getTagCompound.removeTag("loc")
        stack.getTagCompound.removeTag("dim")
      }
    }
    null
  }

  def getMode(stack: ItemStack): Integer = {
    if (!stack.hasTagCompound) stack.setTagCompound(new NBTTagCompound)
    val tag = stack.getTagCompound
    if (tag.hasKey("mode")) {
      tag.getInteger("mode")
    } else {
      tag.setInteger("mode", MODE_QUEUEING)
      MODE_QUEUEING
    }
  }

  def toggleMode(stack: ItemStack): Integer = {
    if (!stack.hasTagCompound) stack.setTagCompound(new NBTTagCompound)
    val tag = stack.getTagCompound
    if (tag.hasKey("mode")) {
      val mode = tag.getInteger("mode")
      tag.setInteger("mode", (mode + 1) % 2)
      tag.getInteger("mode")
    } else {
      tag.setInteger("mode", MODE_QUEUEING)
      MODE_QUEUEING
    }
  }
}
