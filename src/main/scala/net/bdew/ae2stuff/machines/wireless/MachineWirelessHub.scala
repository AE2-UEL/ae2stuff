package net.bdew.ae2stuff.machines.wireless

import net.bdew.lib.machine.Machine
import net.minecraft.util.math.MathHelper

object MachineWirelessHub extends Machine("WirelessHub", BlockWirelessHub) with WirelessPower {

  lazy val powerBase: Double = tuning.getDouble("PowerBase")
  lazy val powerDistanceMultiplier: Double = tuning.getDouble("PowerDistanceMultiplier")
  lazy val maxConnections: Int = MathHelper.clamp(tuning.getInt("MaxConnections"), 2, 128)

  override def getPowerBase: Double = powerBase

  override def getPowerDistanceMultiplier: Double = powerDistanceMultiplier
}
