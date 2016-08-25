package org.example.gearconnection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.gears2.connect.GearService;
import org.gears2.connect.GearServiceConnecter;
import org.gears2.connect.GearServiceListener;

public class MainActivity extends AppCompatActivity implements GearServiceListener{

    private static final String REAL_TIME_HEART_RATE = "STOP_SENSOR";
    private static final String AVERAGE_HEART_RATE = "START_SENSOR";
    private static final String CHECK_CONNECTION = "CHECK_CONNECTION";
    private GearServiceConnecter conn;
    private TextView debugView, tv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        debugView = (TextView) findViewById(R.id.textView);
        tv2 = (TextView) findViewById(R.id.textView2);

        conn = new GearServiceConnecter(getApplicationContext());
        conn.setOnGearServiceListener(this);
        conn.serviceConnect();
    }

    public void onGearMessageSendButtonClick(View v){
        conn.sendData(AVERAGE_HEART_RATE);
    }

    public void onCheckButtonClick(View v){conn.sendData("CHECK_CONNECTION");}

    private static final void log(String m){
        final String TAG = MainActivity.class.getName();
        Log.e(TAG, m);
    }

    @Override
    public void onGearServiceConnected(GearService gearService) {
        log("onGearServiceConnected");conn.findPeers();
    }

    @Override
    public void onGearServiceDisconnected() {
        log("GEAR DISCONN");
    }

    @Override
    public void onGearConnected() {
        log("GEAR CONNECTED");
    }

    @Override
    public void onGearConnectionFailure() {
        log("GEAR CONN FAIL");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                debugView.setText("GEAR CONN FAIL");
            }
        });
    }

    @Override
    public void onGearConnectionLost() {
        log("GEAR CONN LOST");
    }

    @Override
    public void onMessageReceive(byte[] b) {
        final String msg = new String(b);
        log("MSG : " + new String(b));
        try {
            final int heart_rate = Integer.parseInt(msg);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    debugView.setText(msg);
                    tv2.setText("" + heart_rate);
                }
            });
        }catch(Exception e){}
    }
}
