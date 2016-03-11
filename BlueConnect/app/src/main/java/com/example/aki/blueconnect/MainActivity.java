package com.example.aki.blueconnect;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUEST_CONNECT_DEVICE = 1000;
    private static final int REQUEST_ENABLE_BT = 2000;

    boolean newDevice;
    private ProgressDialog connectingProgressDialog;
    private boolean connected = false;
    private BTCommunicator myBTCommunicator = null;
    private Handler btcHandler;


    Toast mLongToast;
    Toast mShortToast;
    TextView txt01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLongToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mShortToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        setContentView(R.layout.activity_main);

        txt01 = (TextView)findViewById(R.id.txt01);
        Button button01 = (Button)findViewById(R.id.button01);
        button01.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button01:
                byte[] buf = new byte[8];
                myBTCommunicator.write(buf);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(BluetoothAdapter.getDefaultAdapter().equals(null)){
            //非対応端末
            finish();
        }else{
            //対応端末
            if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
                showToastShort(getResources().getString(R.string.wait_till_bt_on));
                //有効化ダイアログ
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }else{
                //すでに有効だった場合
                selectTarget();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CONNECT_DEVICE:
                if(resultCode==RESULT_OK){
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    newDevice = data.getExtras().getBoolean(DeviceListActivity.PAIRING);
                    if(newDevice==true){
                        //向こうにアンドロイドに気づいてもらう
                        enDiscoverable();
                    }
                    startBTCommunicator(address);
                }
                break;
            case REQUEST_ENABLE_BT:
                switch (resultCode){
                    case Activity.RESULT_OK:
                        selectTarget();
                        break;
                    case Activity.RESULT_CANCELED:
                        showToastShort(getResources().getString(R.string.bt_needs_to_be_enabled));
                        finish();
                        break;
                    default:
                        showToastShort(getResources().getString(R.string.problems_at_connecting));
                        finish();
                        break;
                }
        }
    }

    public void selectTarget(){
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    private void enDiscoverable(){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        startActivity(discoverableIntent);
    }

    public void startBTCommunicator(String mac_address){
        connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.connecting_please_wait), true);
        if(myBTCommunicator == null){
            createBTCommunicator();
        }

        switch (((Thread)myBTCommunicator).getState()){
            case NEW:
                myBTCommunicator.setMACAddress(mac_address);
                myBTCommunicator.start();
                break;
            default:
                connected = false;
                myBTCommunicator = null;
                createBTCommunicator();
                myBTCommunicator.setMACAddress(mac_address);
                myBTCommunicator.start();
                break;
        }

        updateButtonsAndMenu();
    }

    public void createBTCommunicator(){
        myBTCommunicator = new BTCommunicator(this, myHandler, BluetoothAdapter.getDefaultAdapter());
        btcHandler = myBTCommunicator.getHandler();
    }

    final Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message myMessage) {
            switch (myMessage.getData().getInt("message")){
                case BTCommunicator.STATE_CONNECTED:
                    connected = true;
                    connectingProgressDialog.dismiss();
                    updateButtonsAndMenu();
                    showToastLong(getResources().getString(R.string.connected));
                    String hello = "hello";
                    try{
                         myBTCommunicator.write(hello.getBytes("UTF-8"));
                    }catch (UnsupportedEncodingException e){

                    }
                    break;
                case BTCommunicator.STATE_CONNECTERROR:
                    connectingProgressDialog.dismiss();
                    break;
                case BTCommunicator.DISPLAY_TOAST:
                    showToastShort(myMessage.getData().getString("toastText"));
                    break;
            }
        }
    };

    private void updateButtonsAndMenu(){
        //TODO 中身
        if(connected) {
            txt01.setText(getText(R.string.main_pairing));
        }else{
            txt01.setText(getText(R.string.main_nopairing));
        }

    }


    private void showToastShort(String textToShow) {
        mShortToast.setText(textToShow);
        mShortToast.show();
    }
    private void showToastLong(String textToShow) {
        mLongToast.setText(textToShow);
        mLongToast.show();
    }
}
