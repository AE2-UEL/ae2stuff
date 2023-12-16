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

package net.bdew.ae2stuff.waila

import appeng.api.config.PowerMultiplier
import appeng.api.util.AEColor
import mcp.mobius.waila.api.{IWailaConfigHandler, IWailaDataAccessor}
import net.bdew.ae2stuff.machines.wireless.TileWireless
import net.bdew.lib.PimpVanilla._
import net.bdew.lib.nbt.NBT
import net.bdew.lib.{DecFormat, Misc}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object WailaWirelessDataProvider extends BaseDataProvider(classOf[TileWireless]) {
  override def getNBTTag(player: EntityPlayerMP, te: TileWireless, tag: NBTTagCompound, world: World, pos: BlockPos): NBTTagCompound = {


    tag.setTag("wireless_waila",
      te.link map (link => NBT(
        "connected" -> true,
        "target" -> link,
        "channels" -> (if (te.connection != null) te.connection.getUsedChannels else 0),
        "power" -> PowerMultiplier.CONFIG.multiply(te.getIdlePowerUsage),
        "name" -> (if (te.customName != null) te.customName else ""),
        "color" -> te.color.ordinal()
      )) getOrElse NBT(
        "connected" -> false,
        "name" -> te.customName,
        "color" -> te.color.ordinal()
      )
    )
    tag
  }

  override def getBodyStrings(target: TileWireless, stack: ItemStack, acc: IWailaDataAccessor, cfg: IWailaConfigHandler): Iterable[String] = {
    if (acc.getNBTData.hasKey("wireless_waila")) {
      val data = acc.getNBTData.getCompoundTag("wireless_waila")
      val name = data.getString("name")
      val color = data.getInteger("color")
      if (data.getBoolean("connected")) {
        data.get[BlockPos]("target") map { pos =>
          List(
            Misc.toLocalF("ae2stuff.waila.wireless.connected", pos.getX, pos.getY, pos.getZ),
            Misc.toLocalF("ae2stuff.waila.wireless.channels", data.getInteger("channels")),
            Misc.toLocalF("ae2stuff.waila.wireless.power", DecFormat.short(data.getDouble("power")))
          )
            .++(if (name != "") {
              Misc.toLocalF("ae2stuff.waila.wireless.name", name) :: Nil
            } else Nil)
            .++(if (color != AEColor.TRANSPARENT.ordinal()) {
              Misc.toLocal(AEColor.values().apply(color).unlocalizedName) :: Nil
            } else Nil)
        } getOrElse List(Misc.toLocal("ae2stuff.waila.wireless.notconnected"))
      } else {
        List(Misc.toLocal("ae2stuff.waila.wireless.notconnected"))
          .++(if (name != "") {
            Misc.toLocalF("ae2stuff.waila.wireless.name", name) :: Nil
          } else Nil)
          .++(if (color != AEColor.TRANSPARENT.ordinal()) {
            Misc.toLocal(AEColor.values().apply(color).unlocalizedName) :: Nil
          } else Nil)
      }
    } else List.empty
  }
}
