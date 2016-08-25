package org.gears2.connect;

import java.io.IOException;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class GearService extends SAAgent {

	private static String TAG = GearService.class.getName();
	private LocalBinder binder = new LocalBinder();
	private static final int CHANNEL_ID = 107; 
	private SAAgentConnection socket = null;
	private Handler handler = new Handler();
	private GearServiceListener serviceListener = null;

	public GearService() {
		super(TAG, SAAgentConnection.class);
	}

	@Override
	public void onCreate() {
		super.onCreate(); 
		SA accessory = new SA();
		try {
			accessory.initialize(this);
		} catch (SsdkUnsupportedException e) {
			e.printStackTrace();
			if (processUnsupportedException(e)) {
				return;
			}
		} catch (Exception e) { 
			e.printStackTrace();
			stopSelf();
		}
	}
 
	/**
	 * find gear service.
	 */
	@Override
	protected void onFindPeerAgentsResponse(SAPeerAgent[] peerAgents, int result) {
		if ((result == SAAgent.PEER_AGENT_FOUND) && (peerAgents != null)) {
			for (SAPeerAgent peerAgent : peerAgents)
				requestServiceConnection(peerAgent);
		} else {
			if (serviceListener != null)
				serviceListener.onGearConnectionFailure();
		}
	}

	@Override
	protected void onServiceConnectionRequested(SAPeerAgent peerAgent) {
		if (peerAgent != null) {
			acceptServiceConnectionRequest(peerAgent);
		}
	}

	@Override
	protected void onServiceConnectionResponse(SAPeerAgent peerAgent, SASocket socket, int result) {
		if (result == SAAgent.CONNECTION_SUCCESS) {
			this.socket = (SAAgentConnection) socket;
			if (serviceListener != null)
				serviceListener.onGearConnected();
		} else if (result == SAAgent.CONNECTION_ALREADY_EXIST) {
			if (serviceListener != null)
				serviceListener.onGearConnected();
		} else if (result == SAAgent.CONNECTION_DUPLICATE_REQUEST) {
			 Toast.makeText(getBaseContext(), "CONNECTION_DUPLICATE_REQUEST",
			 Toast.LENGTH_LONG).show();
		} else {
			 Toast.makeText(getBaseContext(), "CONNECTION FAILURE",
			 Toast.LENGTH_LONG).show();
			if (serviceListener != null)
				serviceListener.onGearConnectionFailure();
		}
	}

	@Override
	protected void onError(SAPeerAgent peerAgent, String errorMessage, int errorCode) {
		super.onError(peerAgent, errorMessage, errorCode);
	}

	@Override
	protected void onPeerAgentsUpdated(SAPeerAgent[] peerAgents, int result) {
//		final SAPeerAgent[] peers = peerAgents;
//		final int status = result;
//		handler.post(new Runnable() {
//			@Override
//			public void run() {
//				if (peers != null) {
//					if (status == SAAgent.PEER_AGENT_AVAILABLE) {
//						Toast.makeText(getApplicationContext(), "PEER_AGENT_AVAILABLE", Toast.LENGTH_LONG).show();
//					} else {
//						Toast.makeText(getApplicationContext(), "PEER_AGENT_UNAVAILABLE", Toast.LENGTH_LONG).show();
//					}
//				}
//			}
//		});
	}

	private class SAAgentConnection extends SASocket {
		public SAAgentConnection() {
			super(SAAgentConnection.class.getName());
		}

		@Override
		public void onError(int channelId, String errorMessage, int errorCode) {
		}

		@Override
		public void onReceive(int channelId, byte[] data) {
			final String message = new String(data);
			Log.v(TAG, "Received Message : " + message);
//			addMessage("Received: ", message);
			if(serviceListener != null) 
            	serviceListener.onMessageReceive(data);
		}

		@Override
		protected void onServiceConnectionLost(int reason) {
			Log.e(TAG, "CONNLOST");
//			updateTextView("Disconnected");
			if(serviceListener != null) 
            	serviceListener.onGearConnectionLost();
			closeConnection();
		}

	}

	public boolean sendData(final String data) {
		boolean retvalue = false;
		if (socket != null) {
			try {
				socket.send(CHANNEL_ID, data.getBytes());
				retvalue = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.d(TAG, "Sent: " + data);
		}
		return retvalue;
	}

	public void findPeers() {
		findPeerAgents();
	}

	public synchronized boolean closeConnection() {
		if (socket != null) {
			socket.close();
			socket = null;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public class LocalBinder extends android.os.Binder {
		public GearService getService() {
			return GearService.this;
		}
	}

	private boolean processUnsupportedException(SsdkUnsupportedException e) {
		e.printStackTrace();
		int errType = e.getType();
		if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED
				|| errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
			/*
			 * Your application can not use Samsung Accessory SDK. You
			 * application should work smoothly without using this SDK, or you
			 * may want to notify user and close your app gracefully (release
			 * resources, stop Service threads, close UI thread, etc.)
			 */
			stopSelf();
		} else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
			Log.e(TAG, "You need to install Samsung Accessory SDK to use this application.");
		} else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {
			Log.e(TAG, "You need to update Samsung Accessory SDK to use this application.");
		} else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
			Log.e(TAG, "We recommend that you update your Samsung Accessory SDK before using this application.");
			return false;
		}
		return true;
	}

	@Override
	public void onDestroy() {
		closeConnection();
		super.onDestroy();
	}

	public void setOnGearServiceListener(GearServiceListener l) {
		this.serviceListener = l;
	}

//	private void addMessage(final String prefix, final String data) {
//		final String strToUI = prefix.concat(data);
//		handler.post(new Runnable() {
//			@Override
//			public void run() {
//				ConsumerActivity.addMessage(strToUI);
//			}
//		});
//	}
}
