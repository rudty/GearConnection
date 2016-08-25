package org.gears2.connect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public final class GearServiceConnecter {
	private GearService gearService = null;
	private boolean isBound = false;
	private GearServiceListener gearListener = null;
	private final Context context;

	public GearServiceConnecter(Context context) {
		this.context = context;
	}

	public boolean serviceConnect() {
		isBound = context.bindService(new Intent(context.getApplicationContext(), GearService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		return isBound;
	}

	public void findPeers() {
		if (isBound && gearService != null) {
			gearService.findPeers();
		}
	}

	public void sendData(String data) {
		if (gearService != null)
			gearService.sendData(data);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		try {
			release();
		} catch (Exception e) {
		}
	}

	public void setOnGearServiceListener(GearServiceListener l) {
		this.gearListener = l;
		if (gearService != null) {
			gearService.setOnGearServiceListener(gearListener);
		}
	}

	public void release() {
		// Clean up connections
		if (isBound == true && gearService != null) {
			if (gearService.closeConnection() == false) {
				gearListener.onGearConnectionLost();
			}
		}
		// Un-bind service
		if (isBound) {
			context.unbindService(mConnection);
			isBound = false;
		}
	}

	// gear service connection
	private final ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			gearService = ((GearService.LocalBinder) service).getService();
			gearService.setOnGearServiceListener(gearListener);
			if (gearListener != null)
				gearListener.onGearServiceConnected(gearService);
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			gearService = null;
			isBound = false;
			if (gearListener != null)
				gearListener.onGearServiceDisconnected();
		}
	};

}
