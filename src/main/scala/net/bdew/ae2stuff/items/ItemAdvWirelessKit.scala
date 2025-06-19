package net.bdew.ae2stuff.items

import appeng.api.config.SecurityPermissions
import appeng.api.exceptions.FailedConnectionException
import net.bdew.ae2stuff.grid.Security
import net.bdew.ae2stuff.machines.wireless.{BlockWireless, TileWireless}
import net.bdew.ae2stuff.misc.AdvItemLocationStore
import net.bdew.lib.Misc
import net.bdew.lib.PimpVanilla.pimpBlockAccess
import net.bdew.lib.items.BaseItem
import net.bdew.lib.nbt.converters.TBlockPos
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{ActionResult, EnumActionResult, EnumFacing, EnumHand}
import net.minecraft.world.World

import java.util

object ItemAdvWirelessKit extends BaseItem("adv_wireless_kit") with AdvItemLocationStore {

  setMaxStackSize(1)

  val MODE_QUEUEING = 0
  private val MODE_BINDING = 1

  override def onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult[ItemStack] = {
    import net.bdew.lib.helpers.ChatHelper._
    val stack = player.getHeldItem(hand)
    if (!world.isRemote && player.isSneaking) {
      toggleMode(stack)
      if (getMode(stack) == MODE_QUEUEING) {
        player.sendStatusMessage(L("ae2stuff.wireless.advtool.queueing.activated").setColor(Color.GREEN), true)
      } else {
        player.sendStatusMessage(L("ae2stuff.wireless.advtool.binding.activated").setColor(Color.GREEN), true)
      }
      return ActionResult.newResult(EnumActionResult.SUCCESS, stack)
    }
    ActionResult.newResult(EnumActionResult.PASS, stack)
  }

  override def onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult = {
    import net.bdew.lib.helpers.ChatHelper._
    val stack = player.getHeldItem(hand)
    if (world.getBlockState(pos).getBlock != BlockWireless) return EnumActionResult.PASS
    if (!world.isRemote) {
      if (player.isSneaking) {
        toggleMode(stack)
        if (getMode(stack) == MODE_QUEUEING) {
          player.sendStatusMessage(L("ae2stuff.wireless.advtool.queueing.activated").setColor(Color.GREEN), true)
        } else if (getMode(stack) == MODE_BINDING) {
          player.sendStatusMessage(L("ae2stuff.wireless.advtool.binding.activated").setColor(Color.GREEN), true)
        }
      }
      world.getTileSafe[TileWireless](pos) foreach { tile =>
        val pid = Security.getPlayerId(player)
        // Check that the player can modify the network
        if (!Security.playerHasPermission(tile.getNode.getGrid, pid, SecurityPermissions.BUILD)) {
          player.sendStatusMessage(L("ae2stuff.wireless.tool.security.player").setColor(Color.RED), true)
        } else if (getMode(stack) == MODE_QUEUEING) {
          addLocation(stack, pos, world.provider.getDimension)
          player.sendStatusMessage(L(
            "ae2stuff.wireless.advtool.queued",
            pos.getZ.toString,
            pos.getY.toString,
            pos.getX.toString).setColor(Color.GREEN), true)
        } else if (getMode(stack) == MODE_BINDING) {
          if (hasLocation(stack)) {
            // Have other location - start connecting
            getNextLocation(stack) match {
              case Some(otherPos) =>
                if (getDimension(stack) != world.provider.getDimension) {
                  // Different dimensions - error out
                  player.sendStatusMessage(L("ae2stuff.wireless.tool.dimension").setColor(Color.RED), true)
                } else if (pos == otherPos) {
                  // Same block - clear the location
                  popLocation(stack)
                } else {
                  world.getTileSafe[TileWireless](otherPos) match {
                    // Check that the other tile is still around
                    case Some(other: TileWireless) =>
                      // And check that the player can modify it too
                      if (!Security.playerHasPermission(other.getNode.getGrid, pid, SecurityPermissions.BUILD)) {
                        player.sendStatusMessage(L("ae2stuff.wireless.tool.security.player").setColor(Color.RED), true)
                      } else {
                        // Player can modify both sides - unlink current connections if any
                        tile.doUnlink()
                        other.doUnlink()

                        // Make player the owner of both blocks
                        tile.getNode.setPlayerID(pid)
                        other.getNode.setPlayerID(pid)
                        try {
                          if (tile.doLink(other)) {
                            player.sendStatusMessage(L(
                              "ae2stuff.wireless.tool.connected",
                              pos.getX.toString,
                              pos.getY.toString,
                              pos.getZ.toString).setColor(Color.GREEN), true)
                          } else {
                            player.sendStatusMessage(L("ae2stuff.wireless.tool.failed").setColor(Color.RED), true)
                          }
                        } catch {
                          case e: FailedConnectionException =>
                            player.sendStatusMessage((L("ae2stuff.wireless.tool.failed") & ": " & e.getMessage).setColor(Color.RED), true)
                            tile.doUnlink()
                            print("Failed to link wireless connector: " + e)
                        }
                      }
                      popLocation(stack)
                    case _ =>
                      // The other block is gone - error out
                      player.sendStatusMessage(L("ae2stuff.wireless.tool.noexist").setColor(Color.RED), true)
                      popLocation(stack)
                  }
                }
              case None =>
                player.sendStatusMessage(L("ae2stuff.wireless.advtool.noconnectors").setColor(Color.RED), true)
            }
          } else {
            player.sendStatusMessage(L("ae2stuff.wireless.advtool.noconnectors").setColor(Color.RED), true)
          }
        }
      }
    }
    EnumActionResult.SUCCESS
  }

  override def addInformation(stack: ItemStack, world: World, list: util.List[String], flag: ITooltipFlag): Unit = {
    if (getLocations(stack).tagCount() > 0) {
      getNextLocation(stack) match {
        case Some(next) =>
          list.add(Misc.toLocalF("ae2stuff.wireless.advtool.connector.next", next.getX, next.getY, next.getZ))
      }
    }
    if (getMode(stack) == MODE_QUEUEING) {
      list.add(Misc.toLocal("ae2stuff.wireless.advtool.queueing"))
      if (getLocations(stack).tagCount() == 0) {
        list.add(Misc.toLocal("ae2stuff.wireless.advtool.queueing.empty"))
      } else {
        list.add(Misc.toLocal("ae2stuff.wireless.advtool.queueing.notempty"))
        for (i <- 0 until getLocations(stack).tagCount()) {
          val loc = TBlockPos.decode(getLocations(stack).getCompoundTagAt(i)).get
          list.add(loc.getX + "," + loc.getY + "," + loc.getZ)
        }
      }
    } else if (getMode(stack) == MODE_BINDING) {
      list.add(Misc.toLocal("ae2stuff.wireless.advtool.binding"))
      if (getLocations(stack).tagCount() == 0) {
        list.add(Misc.toLocal("ae2stuff.wireless.advtool.binding.empty"))
      } else {
        list.add(Misc.toLocal("ae2stuff.wireless.advtool.binding.notempty"))
        for (i <- 0 until getLocations(stack).tagCount()) {
          val loc = TBlockPos.decode(getLocations(stack).getCompoundTagAt(i)).get
          list.add(loc.getX + "," + loc.getY + "," + loc.getZ)
        }
      }
    }
    list.add(Misc.toLocal("ae2stuff.wireless.advtool.extra"))
  }
}
