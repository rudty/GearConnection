package org.gears2.connect;
/**
 * Gear Service Connect.
 * 
 * @author d
 *
 */
public interface GearServiceListener {
	public void onGearServiceConnected(GearService gearService);
	public void onGearServiceDisconnected();
	public void onGearConnected();
	public void onGearConnectionFailure();
	public void onGearConnectionLost();
	public void onMessageReceive(byte[] b);
}
