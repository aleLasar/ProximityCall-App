package com.example.project;

import android.Manifest;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.*;
import android.provider.Settings;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.skyfishjy.library.RippleBackground;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_REQUIRED_PERMISSION = 1;
    RippleBackground rippleBackground;
    ImageView centerImage;
    ArrayList<Point> devices_pt =new ArrayList<>();
    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    ArrayList<Device> devices = new ArrayList<>();

    ServerPart server;
    ClientPart client;
    public static final int PORT = 9584;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();
        initialSetUp();

/*        View view_device = createNewDevice("Alessio_phone");
        rippleBackground.addView(view_device);

        View view2_device = createNewDevice("Laura_phone");
        rippleBackground.addView(view2_device);

        View view3_device = createNewDevice("Anna_phone");
        rippleBackground.addView(view3_device);

        View view4_device = createNewDevice("Mariano_phone");
        rippleBackground.addView(view4_device);*/
    }

    private void initialSetUp() {
        rippleBackground = (RippleBackground)findViewById(R.id.content);
        centerImage = (ImageView) findViewById(R.id.centerImg);
        centerImage.setOnClickListener(this);


        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        devices_pt.add(new Point(size.x / 2, size.y / 2));

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    private void getPermissions() {
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_NETWORK_STATE
                ) != PackageManager.PERMISSION_GRANTED)){

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE
                    },
                    REQUEST_REQUIRED_PERMISSION);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onRestart(){
        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Disconnection occurred", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Disconnection not occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onClick(View v){
        //startActivity(new Intent(getApplicationContext(), MicWindow.class));

        if(v.getId() != R.id.centerImg){
            final WifiP2pDevice dev = getDeviceFromPeerList(v.getId());
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = dev.deviceAddress;
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), "Connected to "+ dev.deviceName, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(), "Error in connecting to "+dev.deviceName, Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            rippleBackground.startRippleAnimation();
            checkLocationEnabled();
            discoverDevices();
            //startActivity(new Intent(getApplicationContext(), MicWindow.class));
        }


    }

    private WifiP2pDevice getDeviceFromPeerList(int id){
        for(Device d : devices){
            if(d.getId() == id)
                return d.getDevice();
        }
        return null;
    }

    public void checkLocationEnabled(){
        LocationManager lm = (LocationManager)MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.gps_network_not_enabled_title)
                    .setMessage(R.string.gps_network_not_enabled)
                    .setPositiveButton(R.string.open_location_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            MainActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.Cancel,null)
                    .show();
        }
    }

    private void discoverDevices() {
        this.mManager.discoverPeers(this.mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Discovery Started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Discovery start Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peersList) {
            if(peersList.getDeviceList().size() != 0){
                devices.clear();
                rippleBackground.removeAllViews();
                for(WifiP2pDevice device : peersList.getDeviceList()){
                    View view_device = createNewDevice(device.deviceName);
                    rippleBackground.addView(view_device);
                    devices.add(new Device(view_device.getId(), device.deviceName, device));
                }
                rippleBackground.startRippleAnimation();
            }
            else
                Toast.makeText(getApplicationContext(), "No Peers Found", Toast.LENGTH_SHORT).show();
                return;
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if(info.groupFormed && info.isGroupOwner){
                server = new ServerPart(MainActivity.this, PORT);
                server.start();
            }else if(info.groupFormed){
                client = new ClientPart(MainActivity.this, groupOwnerAddress.getHostAddress(), PORT);
                client.start();
            }
        }
    };


    public View createNewDevice(String device_name){
        View device1 = LayoutInflater.from(this).inflate(R.layout.device_layout, null);
        Point new_point = generatePosition();
        RippleBackground.LayoutParams params = new RippleBackground.LayoutParams(400,400);
        params.setMargins(new_point.x, new_point.y, 0, 0);
        device1.setLayoutParams(params);

        TextView txt_device1 = device1.findViewById(R.id.myImageViewText);
        int device_id = System.identityHashCode(device1);
        txt_device1.setText(device_name);
        device1.setId(device_id);
        device1.setOnClickListener(this);

        device1.setVisibility(View.VISIBLE);
        return device1;
    }

    Point generatePosition() {
        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int x,y = 0;
        do{
            x = ThreadLocalRandom.current().nextInt(200, size.x-400);
            y = ThreadLocalRandom.current().nextInt(200, size.y-400);

        }while(checkPositionOverlap(new Point(x, y)));

        Point new_point = new Point(x, y);
        devices_pt.add(new_point);

        return new_point;
    }

    boolean checkPositionOverlap(Point new_p){
        if(!devices_pt.isEmpty()){
            for(Point p:devices_pt){
                int distance = (int)Math.sqrt(Math.pow(new_p.x - p.x, 2) + Math.pow(new_p.y - p.y, 2));
                if(distance < 400) return true;
            }
        }
        return false;
    }

}