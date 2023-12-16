package net.bdew.ae2stuff.machines.wireless

import appeng.api.util.AEColor
import net.bdew.ae2stuff.AE2Stuff
import net.bdew.ae2stuff.machines.wireless.WirelessModelFactory.colorMap
import net.minecraft.client.renderer.block.model.{IBakedModel, ModelResourceLocation}
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.IModel
import net.minecraftforge.common.model.IModelState
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import java.util
import java.util.function
import scala.language.postfixOps
import collection.JavaConverters._

@SideOnly(Side.CLIENT)
object WirelessModelFactory {

  final val modelLocation = new ModelResourceLocation(new ResourceLocation(AE2Stuff.modId, "builtin/wireless"), "normal")

  final val colorMap: Map[AEColor, ResourceLocation] =
    AEColor.values() map (t => t -> texture(t.name.toLowerCase)) toMap

  private def texture(name: String): ResourceLocation = {
    new ResourceLocation(AE2Stuff.modId, "blocks/wireless/side_on_" + name)
  }
}

@SideOnly(Side.CLIENT)
class WirelessModelFactory extends IModel {

  override def bake(state: IModelState,
                    format: VertexFormat,
                    bakedTextureGetter: function.Function[ResourceLocation, TextureAtlasSprite]
                   ): IBakedModel = {
    new WirelessBakedModel(format, colorMap map (e => e._1 -> bakedTextureGetter.apply(e._2)))
  }

  override def getTextures: util.Collection[ResourceLocation] = colorMap.values.asJavaCollection
}
