package net.bdew.ae2stuff.machines.wireless

import net.bdew.ae2stuff.AE2Stuff
import net.minecraft.client.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.{ICustomModelLoader, IModel}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class WirelessModelLoader(private final val models: Map[String, IModel]) extends ICustomModelLoader {

  override def accepts(modelLocation: ResourceLocation): Boolean = {
    if (!modelLocation.getNamespace.equals(AE2Stuff.modId)) {
      return false
    }
    models.contains(modelLocation.getPath)
  }

  override def loadModel(modelLocation: ResourceLocation): IModel = {
    models(modelLocation.getPath)
  }

  override def onResourceManagerReload(resourceManager: IResourceManager): Unit = {}
}
