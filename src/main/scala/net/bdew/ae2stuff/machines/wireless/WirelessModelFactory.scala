package net.bdew.ae2stuff.machines.wireless

import appeng.api.util.AEColor
import net.bdew.ae2stuff.AE2Stuff
import net.minecraft.client.renderer.block.model.IBakedModel
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
class WirelessModelFactory(name: String) extends IModel {

  private final val colorMap: Map[AEColor, ResourceLocation] = AEColor.values map (t =>
    t -> new ResourceLocation(AE2Stuff.modId, "blocks/" + name + "/side_on_" + t.name.toLowerCase)
  ) toMap

  private final val inactiveColorMap: Map[AEColor, ResourceLocation] = AEColor.values map (t =>
    t -> new ResourceLocation(AE2Stuff.modId, "blocks/" + name + "/side_off_" + t.name.toLowerCase)
  ) toMap

  override def bake(state: IModelState,
                    format: VertexFormat,
                    bakedTextureGetter: function.Function[ResourceLocation, TextureAtlasSprite]
                   ): IBakedModel = {
    new WirelessBakedModel(
      format,
      colorMap map (e => e._1 -> bakedTextureGetter.apply(e._2)),
      inactiveColorMap map (e => e._1 -> bakedTextureGetter.apply(e._2)))
  }

  override def getTextures: util.Collection[ResourceLocation] = {
    val list = new util.ArrayList[ResourceLocation]()
    list.addAll(colorMap.values.asJavaCollection)
    list.addAll(inactiveColorMap.values.asJavaCollection)
    list
  }
}
