package net.bdew.ae2stuff.machines.wireless

import net.minecraftforge.common.property.IUnlistedProperty

class WirelessHubProperty(name: String) extends IUnlistedProperty[java.lang.Boolean] {

  override def getName: String = name

  override def isValid(value: java.lang.Boolean): Boolean = value != null

  override def getType: Class[java.lang.Boolean] = classOf[java.lang.Boolean]

  override def valueToString(value: java.lang.Boolean): String = value.toString
}
