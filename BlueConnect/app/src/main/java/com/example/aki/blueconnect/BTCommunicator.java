package com.example.aki.blueconnect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Aki on 2016/03/08.
 */
public class BTCommunicator extends Thread {
    public static final int DISPLAY_TOAST = 1000;
    public static final int STATE_CONNECTED = 1001;
    public static final int STATE_CONNECTERROR = 1002;
    public static final int STATE_RECEIVEERROR = 1003;
    public static final int STATE_SENDERROR = 1004;

    private static final UUID SERIAL_PORT_SERVICE_CLASS_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Handler uiHandler;
    private String mMACaddress;
    private MainActivity myMainActivity;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket pcBTsocket = null;
    private InputStream sIn = null;
    private OutputStream sOut = null;
    private boolean connected = false;



    public BTCommunicator(MainActivity myMainAct, Handler uiHandler, BluetoothAdapter btAdapter){
        this.myMainActivity = myMainAct;
        this.uiHandler = uiHandler;
        this.btAdapter = btAdapter;
    }

    @Override
    public void run() {
        createPConnection();
    }

    private void createPConnection(){
        try{
            BluetoothSocket pcBTsocketTEMPORARY;
            BluetoothDevice pcDevice = null;
            pcDevice = btAdapter.getRemoteDevice(mMACaddress);

            if(pcDevice == null){
                sendToast(myMainActivity.getResources().getString(R.string.nopaired_pc));
                sendState(STATE_CONNECTERROR);
                return;
            }

            pcBTsocketTEMPORARY = pcDevice.createInsecureRfcommSocketToServiceRecord(SERIAL_PORT_SERVICE_CLASS_UUID);
            pcBTsocketTEMPORARY.connect();
            pcBTsocket = pcBTsocketTEMPORARY;
            sIn = pcBTsocket.getInputStream();
            sOut = pcBTsocket.getOutputStream();

            connected = true;

        }catch (IOException e){
            Log.d("BTCommunicator","error createPConnection()", e);
            if (myMainActivity.newDevice){
                sendToast(myMainActivity.getResources().getString(R.string.pairing_message));
                sendState(STATE_CONNECTERROR);
            } else {
                sendState(STATE_CONNECTERROR);
            }
            return;
        }

        sendState(STATE_CONNECTED);
    }

    final Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message myMessage) {
            switch (myMessage.getData().getInt("message")){

            }
        }
    };

    public  void write(byte[] buf) {
        try {
            sOut.write(buf);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Handler getHandler(){
        return myHandler;
    }

    public void setMACAddress(String mMACaddress){
        this.mMACaddress = mMACaddress;
    }

    private void sendToast(String toastText) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", DISPLAY_TOAST);
        myBundle.putString("toastText", toastText);
        sendBundle(myBundle);
    }
    private void sendState(int message){
        Bundle myBundle = new Bundle();
        myBundle.putInt("message",message);
        sendBundle(myBundle);
    }
    private void sendBundle(Bundle myBundle){
        Message myMessage = myHandler.obtainMessage();
        myMessage.setData(myBundle);
        uiHandler.sendMessage(myMessage);
    }
}
