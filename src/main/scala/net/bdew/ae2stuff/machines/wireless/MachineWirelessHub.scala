package net.bdew.ae2stuff.machines.wireless

import net.bdew.lib.machine.Machine

// todo consider separate tunings for power
object MachineWirelessHub extends Machine("WirelessHub", BlockWirelessHub) with WirelessPower {

  lazy val powerBase: Double = tuning.getDouble("PowerBase")
  lazy val powerDistanceMultiplier: Double = tuning.getDouble("PowerDistanceMultiplier")

  override def getPowerBase: Double = powerBase

  override def getPowerDistanceMultiplier: Double = powerDistanceMultiplier
}
