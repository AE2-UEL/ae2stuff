package net.bdew.ae2stuff

import appeng.api.util.AEColor
import net.bdew.ae2stuff.items.{ItemAdvWirelessKit, ItemWirelessKit}
import net.bdew.ae2stuff.machines.wireless.{BlockWireless, BlockWirelessHub}
import net.minecraft.item.{EnumDyeColor, ItemStack}
import net.minecraft.item.crafting.Ingredient
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.GameRegistry

object Recipes {

  def load(): Unit = {
    wirelessColorRecipes()
    nbtClearRecipes()
  }

  private val colorOreDicts: Map[EnumDyeColor, String] = Map(
    EnumDyeColor.WHITE -> "dyeWhite",
    EnumDyeColor.ORANGE -> "dyeOrange",
    EnumDyeColor.MAGENTA -> "dyeMagenta",
    EnumDyeColor.LIGHT_BLUE -> "dyeLightBlue",
    EnumDyeColor.YELLOW -> "dyeYellow",
    EnumDyeColor.LIME -> "dyeLime",
    EnumDyeColor.PINK -> "dyePink",
    EnumDyeColor.GRAY -> "dyeGray",
    EnumDyeColor.SILVER -> "dyeLightGray",
    EnumDyeColor.CYAN -> "dyeCyan",
    EnumDyeColor.PURPLE -> "dyePurple",
    EnumDyeColor.BLUE -> "dyeBlue",
    EnumDyeColor.BROWN -> "dyeBrown",
    EnumDyeColor.GREEN -> "dyeGreen",
    EnumDyeColor.RED -> "dyeRed",
    EnumDyeColor.BLACK -> "dyeBlack"
  )

  private def wirelessColorRecipes(): Unit = {
    AEColor.values.foreach(color =>
      if (color != AEColor.TRANSPARENT) {
        GameRegistry.addShapedRecipe(
          new ResourceLocation(AE2Stuff.modId, "wireless_coloring_" + color.name.toLowerCase), null,
          new ItemStack(BlockWireless, 8, 1 + color.ordinal),
          "WWW", "WDW", "WWW",
          Char.box('W'), new ItemStack(BlockWireless),
          Char.box('D'), colorOreDicts(color.dye))

        GameRegistry.addShapedRecipe(
          new ResourceLocation(AE2Stuff.modId, "wireless_hub_coloring_" + color.name.toLowerCase), null,
          new ItemStack(BlockWirelessHub, 8, 1 + color.ordinal),
          "HHH", "HDH", "HHH",
          Char.box('H'), new ItemStack(BlockWirelessHub),
          Char.box('D'), colorOreDicts(color.dye))
      }
    )

    GameRegistry.addShapelessRecipe(
      new ResourceLocation(AE2Stuff.modId, "wireless_color_clearing"), null,
      new ItemStack(BlockWireless),
      Ingredient.fromItem(BlockWireless.itemBlockInstance))

    GameRegistry.addShapelessRecipe(
      new ResourceLocation(AE2Stuff.modId, "wireless_hub_color_clearing"), null,
      new ItemStack(BlockWirelessHub),
      Ingredient.fromItem(BlockWirelessHub.itemBlockInstance))
  }

  private def nbtClearRecipes(): Unit = {
    GameRegistry.addShapelessRecipe(
      new ResourceLocation(AE2Stuff.modId, "wireless_setup_kit_clear"), null,
      new ItemStack(ItemWirelessKit),
      Ingredient.fromItem(ItemWirelessKit))

    GameRegistry.addShapelessRecipe(
      new ResourceLocation(AE2Stuff.modId, "advanced_wireless_setup_kit_clear"), null,
      new ItemStack(ItemAdvWirelessKit),
      Ingredient.fromItem(ItemAdvWirelessKit))
  }
}
