package net.bdew.ae2stuff.machines.wireless

import appeng.api.util.AEColor
import appeng.client.render.cablebus.CubeBuilder
import com.google.common.collect.ImmutableList
import net.bdew.ae2stuff.misc.BlockActiveTexture
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.{BakedQuad, IBakedModel, ItemOverrideList}
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.property.IExtendedBlockState
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import java.util
import java.util.Collections
import scala.language.postfixOps

@SideOnly(Side.CLIENT)
class WirelessBakedModel(format: VertexFormat, colorMap: Map[AEColor, TextureAtlasSprite], inactiveColorMap: Map[AEColor, TextureAtlasSprite]) extends IBakedModel {

  private final val textures = colorMap
  private final val inactiveTextures = inactiveColorMap

  private final val quadMap: Map[AEColor, util.List[BakedQuad]] = textures map (e => e._1 -> {
    val quads = new util.ArrayList[BakedQuad]
    val builder = new CubeBuilder(format, quads)
    builder.setTexture(e._2)
    builder.addCube(0, 0, 0, 16, 16, 16)
    ImmutableList.copyOf(quads.iterator())
  })

  private final val inactiveQuadMap: Map[AEColor, util.List[BakedQuad]] = inactiveTextures map (e => e._1 -> {
    val quads = new util.ArrayList[BakedQuad]
    val builder = new CubeBuilder(format, quads)
    builder.setTexture(e._2)
    builder.addCube(0, 0, 0, 16, 16, 16)
    ImmutableList.copyOf(quads.iterator())
  })

  override def getQuads(state: IBlockState, side: EnumFacing, rand: Long): util.List[BakedQuad] = {
    if (side != null || state == null) return Collections.emptyList()

    val ext = state.asInstanceOf[IExtendedBlockState]

    // Color
    var key = ext.getValue(BlockWirelessProperties.COLOR_PROPERTY)
    if (key == null) key = AEColor.TRANSPARENT

    // Active
    var active = state.getValue(BlockActiveTexture.Active)
    if (key == null) active = false

    if (active) {
      quadMap(key)
    } else {
      inactiveQuadMap(key)
    }
  }

  override def isAmbientOcclusion: Boolean = true

  override def isGui3d: Boolean = false

  override def isBuiltInRenderer: Boolean = false

  override def getParticleTexture: TextureAtlasSprite = textures(AEColor.TRANSPARENT)

  override def getOverrides: ItemOverrideList = ItemOverrideList.NONE
}
