package net.bdew.ae2stuff.machines.wireless

import appeng.api.util.AEColor
import net.minecraftforge.common.property.IUnlistedProperty

class WirelessColorProperty(name: String) extends IUnlistedProperty[AEColor] {

  override def getName: String = name

  override def isValid(value: AEColor): Boolean = value != null

  override def getType: Class[AEColor] = classOf[AEColor]

  override def valueToString(value: AEColor): String = value.name.toLowerCase
}
