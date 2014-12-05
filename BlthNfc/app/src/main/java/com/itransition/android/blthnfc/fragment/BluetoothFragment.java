package com.itransition.android.blthnfc.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.views.ProgressBarIndeterminate;
import com.itransition.android.blthnfc.R;
import com.itransition.android.blthnfc.activity.MainActivity;
import com.skyfishjy.library.RippleBackground;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by p.dolbik on 25.11.2014.
 */
public class BluetoothFragment extends android.support.v4.app.Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {


    static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");


    private final String dStarted = BluetoothAdapter.ACTION_DISCOVERY_STARTED;
    private final String dFinished = BluetoothAdapter.ACTION_DISCOVERY_FINISHED;


    private ButtonFloat onOffBtn;
    private BluetoothAdapter bluetoothAdapter;
    private MainActivity mainActivity;

    private ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();

    private ArrayList<String> nameDevice = new ArrayList<String>();
    private ListView deviceNameListView;
    private ArrayAdapter<String> deviceNameAdapter;

    private RippleBackground rippleBackground;
    private  BluetoothDevice selectedDevice;
    private  BluetoothSocket mmSocket;
    private BluetoothSocket transferSocket;
    private ProgressDialog sendProgDialog;
    private ProgressDialog receiveProgDialog;



    @Override
    public void onStart() {
        super.onStart();
        isOnOffBluetooth();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("Pasha", "onResume");
        String actionStateChanging = BluetoothAdapter.ACTION_STATE_CHANGED;
        mainActivity.registerReceiver(bluetoothState, new IntentFilter(actionStateChanging));
        mainActivity.registerReceiver(discoveryMonitor, new IntentFilter(dStarted));
        mainActivity.registerReceiver(discoveryMonitor, new IntentFilter(dFinished));
        mainActivity.registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        mainActivity.registerReceiver(pairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d("Pasha", "onPause");
        mainActivity.unregisterReceiver(bluetoothState);
        mainActivity.unregisterReceiver(discoveryMonitor);
        mainActivity.unregisterReceiver(discoveryResult);
        mainActivity.unregisterReceiver(pairReceiver);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        onOffBtn = (ButtonFloat) view.findViewById(R.id.on_off_btn);
        onOffBtn.setOnClickListener(this);

        deviceNameListView = (ListView) view.findViewById(R.id.blth_listView);
        deviceNameAdapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1, nameDevice );
        deviceNameListView.setAdapter(deviceNameAdapter);
        deviceNameListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        deviceNameListView.setOnItemClickListener(this);

        rippleBackground = (RippleBackground) view.findViewById(R.id.ripple_background);

        sendProgDialog = new ProgressDialog(mainActivity);
        sendProgDialog.setMessage("Send data ....");

        receiveProgDialog = new ProgressDialog(mainActivity);
        receiveProgDialog.setMessage("Receive data ....");

        return view;
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.on_off_btn:
                onOffBtn.setEnabled(false);

                if( bluetoothAdapter.isEnabled() ) {
                    bluetoothAdapter.disable();
                } else{
                    bluetoothAdapter.enable();
                }

                break;
        }
    }




    private void isOnOffBluetooth(){
        onOffBtn.setEnabled(true);

        if( bluetoothAdapter.isEnabled() ) {
            onOffBtn.setBackgroundColor(getResources().getColor(R.color.blue));
            onOffBtn.setDrawableIcon(getResources().getDrawable(R.drawable.ic_blth_on));

            if( !bluetoothAdapter.isDiscovering() ) {
                deviceList.clear();
                nameDevice.clear();
                bluetoothAdapter.startDiscovery();
            }

        } else{
            onOffBtn.setBackgroundColor(getResources().getColor(R.color.red));
            onOffBtn.setDrawableIcon(getResources().getDrawable(R.drawable.ic_blth_off));
        }
    }



    // Turn on or turn off bluetooth
    private BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String stateExtra = BluetoothAdapter.EXTRA_STATE;
            int state = intent.getIntExtra(stateExtra, 1);

            switch ( state ){
                case ( BluetoothAdapter.STATE_TURNING_ON):
                    Log.d("Pasha", "STATE_TURNING_ON");
                    break;
                case ( BluetoothAdapter.STATE_ON):
                    isOnOffBluetooth();
                    break;
                case ( BluetoothAdapter.STATE_TURNING_OFF):
                    Log.d("Pasha", "STATE_TURNING_OFF");
                    break;
                case ( BluetoothAdapter.STATE_OFF):
                    isOnOffBluetooth();
                    break;
            }
        }
    };



    //Start and finish search device
    private BroadcastReceiver discoveryMonitor = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if( dStarted.equals(intent.getAction())) {
                rippleBackground.startRippleAnimation();
                Log.d("Pasha", "Start search device");
            } else if ( dFinished.equals(intent.getAction())) {
                if( rippleBackground.isRippleAnimationRunning()) {
                    rippleBackground.stopRippleAnimation();
                }

                Log.d("Pasha", "Stop search device");

                try {
                    final BluetoothServerSocket btserver = bluetoothAdapter.listenUsingRfcommWithServiceRecord("servak", MY_UUID);
                    Thread acceptTread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                BluetoothSocket serverSocket = btserver.accept();
                                transferSocket = serverSocket;
                                Log.d("Pasha", "listenForMessages 1");
                                listenForMessages(transferSocket);

                                Log.d("Pasha", "TEST ");

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    acceptTread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }
    };



    //Result search
    private BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if( remoteDeviceName != null ) {
                deviceList.add(remoteDevice);
                nameDevice.add(remoteDeviceName);
                deviceNameAdapter.notifyDataSetChanged();
                Log.d("Pasha", "remoteDeviceName "+remoteDeviceName);
            }


        }
    };


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("Pasha", "position "+position+" "+deviceList.get(position).toString());

        selectedDevice = deviceList.get(position);



        Thread sendDate = new Thread(new Runnable() {
            @Override
            public void run() {

                BluetoothSocket clientSocket = null;
                try {
                    clientSocket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    clientSocket.connect();
                    transferSocket = clientSocket;
                    Log.d("Pasha", "listenForMessages 2");
                    Log.d("Pasha", "set connection ");
                    sendMessage(transferSocket);

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
        sendDate.start();




    }




    private final BroadcastReceiver pairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Log.d("Pasha", "Pair");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    Log.d("Pasha", "Unpair");
                }
            }
        }
    };






        public void sendMessage( final BluetoothSocket socket){

                    mHandler.sendEmptyMessage(1);

                    Log.d("Pasha", "Work method sendMessage( BluetoothSocket socket) ");
                    OutputStream outputStream;

                    try {
                        outputStream = socket.getOutputStream();
                        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.JPEG, 100,baos); //bm is the bitmap object
                        byte[] b = baos.toByteArray();
                        outputStream.write(b);
                        outputStream.flush();
                    } catch (IOException e) {
                    }

            mHandler.sendEmptyMessage(2);

        }






    private void listenForMessages( BluetoothSocket socket ) {

        Log.d("Pasha", "Work method listenForMessages( BluetoothSocket socket ) ");
        String dir = Environment.getExternalStorageDirectory().toString();
        File destinationFile = new File(dir,"downloadImage.JPEG");

        mHandler.sendEmptyMessage(3);



        try {
            InputStream inputStream = socket.getInputStream();
            FileOutputStream os = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];
            int length;

            mHandler.sendEmptyMessageDelayed(4, 1000);


            while ((length = inputStream.read(buffer))>0)
            {
                os.write(buffer, 0, length);
            }


            inputStream.close();
            os.close();

        } catch (IOException e) {
            Log.d("Pasha", "EXEPTION");
            e.printStackTrace();
        }
    }




    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int)msg.arg1;
            int end = (int)msg.arg2;


            switch(msg.what) {
                case 1:
                    Log.d("Pasha", "Start");
                    sendProgDialog.show();
                    break;
                case 2:
                    Log.d("Pasha", "Stop");
                    if ( sendProgDialog.isShowing() ) {
                        sendProgDialog.dismiss();
                    }
                    break;
                case 3:
                    Log.d("Pasha", "Start_2");
                    receiveProgDialog.show();
                    break;
                case 4:
                    Log.d("Pasha", "Stop_2");
                    if ( receiveProgDialog.isShowing() ) {
                        receiveProgDialog.dismiss();
                    }
                    break;
                case 5:
                    Log.d("Pasha", "Stop_***** "+begin+" "+end);
                    break;

            }
        }
    };


}
