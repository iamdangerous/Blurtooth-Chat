package com.rahul.in.bluetooth_demo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivityPresenter {

    public PresenterCallback callback;

    public void readData(BluetoothSocket bluetoothSocket) {
        try {
            if (bluetoothSocket != null) {
                InputStream is = bluetoothSocket.getInputStream();

                callback.printInScreen("Read data from socket");
                while (true) {
                    if (bluetoothSocket != null && !bluetoothSocket.isConnected()) {
                        log("bluetooth socket is null/closed WHILE READING");
                        break;
                    } else {
                        BufferedInputStream bis = new BufferedInputStream(is);

                        StringBuilder sb = new StringBuilder();
                        while (bis.available() > 0) {
                            sb.append((char) bis.read());
                        }
                        if (sb.length() > 0) {
                            callback.printInScreen("data from IS = " + sb);
                            callback.printInScreen("Closing connection AS Reading is  DONE");
                            bluetoothSocket.close();
                            callback.reConnectAsServer();
                        }
                    }

                }
            } else {
                callback.printInScreen("bluetooth socket is null/closed WHILE READING");
            }
        } catch (IOException e) {
            callback.printInScreen("IO exception in read");
            e.printStackTrace();
            callback.reConnectAsServer();
        }
    }

    public void writeData(BluetoothSocket bluetoothSocket, BluetoothAdapter bluetoothAdapter, BluetoothDevice device) {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                OutputStream os = bluetoothSocket.getOutputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream(12);
                String deviceName = bluetoothAdapter != null ? bluetoothAdapter.getName() : "Unkown";
                String text = prepareMessge(deviceName, device.getAddress());
                bos.write(text.getBytes());
                bos.writeTo(os);
                bos.flush();
                bos.close();

                //Close the connection once writing is done
                os.flush();
                os.close();

                callback.printInScreen("Closing connection AS Writing DONE");
            } else {
                callback.printInScreen("bluetooth socket is null WHILE WRITING");
            }
        } catch (IOException e) {
            callback.printInScreen("IO exception in WRITE");
            e.printStackTrace();
        }
    }

    public String prepareMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
            ex.printStackTrace();
        }
        return "";
    }

    @SuppressLint("MissingPermission")
    public String imeiNo(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return "";
        }
        return telephonyManager.getDeviceId();
    }

    public void log(String msg) {
        Log.d("presenter:", msg);
    }

    public interface PresenterCallback {
        void printInScreen(String msg);
        void reConnectAsServer();
    }

    public String prepareMessge(String deviceName, String userName) {
        String imageUrl = "image_url_" + deviceName;
        String displayName = "display_name" + deviceName;
        Double lat = 12.4d;
        Double lng = 33.22d;
        String aboutMe = "About me lorem ipsum lorem ipsum lorem ipsum " +
                "About me lorem ipsum lorem ipsum lorem ipsum " +
                "About me lorem ipsum lorem ipsum lorem ipsum " + deviceName;

        Message message = new Message(deviceName, userName, imageUrl, displayName, lat, lng, aboutMe);

        return new Gson().toJson(message);
    }

    public class Message {
        String deviceName;
        String userName;
        String imageUrl;
        String displayName;
        Double lat;
        Double lng;
        String aboutMe;

        public Message(String deviceName, String userName, String imageUrl, String displayName, Double lat, Double lng, String aboutMe) {
            this.deviceName = deviceName;
            this.userName = userName;
            this.imageUrl = imageUrl;
            this.displayName = displayName;
            this.lat = lat;
            this.lng = lng;
            this.aboutMe = aboutMe;
        }
    }
}
