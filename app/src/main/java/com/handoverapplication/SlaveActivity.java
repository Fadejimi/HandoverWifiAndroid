package com.handoverapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Random;

import model.Message;

public class SlaveActivity extends AppCompatActivity implements SalutDataCallback {
    private Button btn_connect, btn_disconnect;
    private TextView messageTextView;

    public Salut network;
    public SalutDataReceiver dataReceiver;
    public SalutServiceData serviceData;

    private static final String TAG = SlaveActivity.class.getSimpleName();
    private Random random;
    private BroadcastReceiver receiver;
    private int signalStr;
    private int count;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    //double quality;
    int rssi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slave);

        random = new Random();
        wifiManager = (WifiManager) getSystemService(this.WIFI_SERVICE);


        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_disconnect = (Button) findViewById(R.id.btn_disconnect);
        messageTextView = (TextView) findViewById(R.id.message_textview);
        count = 0;

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "This application button connect is working",
                        Toast.LENGTH_SHORT).show();
                setUpConnection();
                btn_disconnect.setClickable(true);
                btn_connect.setClickable(false);
            }
        });

        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
                btn_disconnect.setClickable(false);
                btn_connect.setClickable(true);
            }
        });
        /*Create a data receiver object that will bind the callback
        with some instantiated object from our app. */
        dataReceiver = new SalutDataReceiver(this, this);


        /*Populate the details for our awesome service. */
        serviceData = new SalutServiceData("testAwesomeService", 60606,
                "HOST");

        /*Create an instance of the Salut class, with all of the necessary data from before.
        * We'll also provide a callback just in case a device doesn't support WiFi Direct, which
        * Salut will tell us about before we start trying to use methods.*/
        network = new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                // wiFiFailureDiag.show();
                // OR
                Log.e(TAG, "Sorry, but this device does not support WiFi Direct.");
                Toast.makeText(getApplicationContext(), "Sorry, but this device does not support WiFi Direct",
                        Toast.LENGTH_SHORT).show();

                messageTextView.append("Sorry, but this device does not support WiFi Direct\n");
                btn_connect.setClickable(false);
            }
        });

        // Get wifi info
        wifiInfo = wifiManager.getConnectionInfo();
        Log.d(TAG, "WIFI INFO " + wifiInfo.toString());
        setSignal();
    }

    private void setUpConnection() {
        network.discoverNetworkServices(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice device) {
                Log.d(TAG, "A device has connected with the name " + device.deviceName);

                messageTextView.setText("A device has connected with the name " + device.deviceName + "\n");
                setUpHostConnection(device);
            }
        }, false);

    }

    private void setUpHostConnection(final SalutDevice device) {
        Log.d(TAG, "STATUS " + statusIsReady());

        messageTextView.append("\nBattery level is : " + getBatteryLevel());
        if (statusIsReady()) {
            network.registerWithHost(device, new SalutCallback() {
                @Override
                public void call() {
                    Log.d(TAG, "We're now registered.");

                    messageTextView.append("\n" + device.deviceName + " Device has been successfully registered ");

                    getSignalStrenght();
                }
            }, new SalutCallback() {
                @Override
                public void call() {
                    Log.d(TAG, "We failed to register.");

                    messageTextView.append("\nFailed to register the host device ");
                }
            });
        }
        else {
            setError("Battery is too low", "The battery is too low for it to connect to the device\n" +
            "Battery level is : " + getBatteryLevel() + "\nSignal Strenght is : " + rssi);
        }
    }

    private void getSignalStrenght() {
        //int signalStr = -1 * random.nextInt(90);
        Log.d(TAG, "Signal Strenght is : " + rssi);
        messageTextView.append("Signal Strength is : " + rssi);
    }

    private double getSignal() {
        //int signalStr = -1 * random.nextInt(90);
        Log.d(TAG, "Signal Strenght is : " + rssi);
        return rssi;
    }

    private boolean statusIsReady() {
        float batteryLevel = getBatteryLevel();
        //signalStr = getSignal();
        //double sig = getSignal();
        Log.d(TAG, "BATTERY LEVEL: " + batteryLevel);
        Log.d(TAG, "Signal Strength: " + rssi);
        if (batteryLevel > 30 && rssi > -90) {
            return true;
        } else {
            if (rssi < -90) {
                messageTextView.setText("\nSignal Strength is too low to connect to the device " +
                        " Signal strength is " + rssi);
            }
            if (batteryLevel < 30) {
                messageTextView.setText("\nBattery level is to low Battery is " + batteryLevel);
            }
            return false;
        }
    }

    private boolean newStatusIsReady() {
        float batteryLevel = getBatteryLevel();
        //int signalStr = getSignal();
        Log.d(TAG, "BATTERY LEVEL: " + batteryLevel);
        if (batteryLevel > 30 && rssi > -90) {
            return true;
        } else {
            if (rssi < -90) {
                messageTextView.setText("\nSignal Strength is too low to connect to the device " +
                    " Signal strength is " + rssi);
            }
            if (batteryLevel < 30) {
                messageTextView.setText("\nBattery level is to low Battery is " + batteryLevel);
            }
            return false;
        }
    }

    /*private boolean signalStatus() {
        int signalStr = getSignal();
        Log.d(TAG, "SIGNAL STRENGTH: " + signalStr);
        if (signalStr > -90) {
            return true;
        }
        else {
            messageTextView.append("\nSignal Strenght is : " + signalStr);
            return false;
        }
    }*/

    private float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }

    private void disconnect() {
        //network.unregisterClient(true);
        network.unregisterClient(new SalutCallback() {
            @Override
            public void call() {
                messageTextView.setText("Client has been unregistered");
            }
        }, new SalutCallback() {
            @Override
            public void call() {
                messageTextView.setText("Could not unregister client");
            }
        }, true);
        //messageTextView.append("\nClient has been unregistered ");
    }

    @Override
    public void onDataReceived(Object data) {
        Log.d(TAG, "Received network data.");
        try
        {
            Message newMessage = LoganSquare.parse(String.valueOf(data), Message.class);

            if (newStatusIsReady()) {
                solveMessage(newMessage);

                messageTextView.append("\n" + newMessage.description);
                Log.d(TAG, newMessage.description);  //See you on the other side!
                //Do other stuff with data.
            }

            else {
                messageTextView.append("\nThe data is being handover to another " +
                        "device\nBattery level is : " + getBatteryLevel() + "\nSignal Strength : " +
                        getSignal());

                sendToHost(newMessage.description);
            }
        }
        catch (IOException ex)
        {
            Log.e(TAG, "Failed to parse network data.");
        }

    }

    private void solveMessage(Message message) {
        String encryptedStr = message.description;

        if (encryptedStr.equals("-1"))
        {
            String str = "1,2,5,4,2,5,6,4,4,6,4,6,6,4,4";
            messageTextView.setText("\nData has been handover to another device");
            sendToHost(str);
        }
        else {
            try {
                String str = encryptedStr;
                String[] msgResult = str.split(",");

                int sum = 0;
                for (String msg : msgResult) {
                    if (isInteger(msg)) {
                        int i = Integer.parseInt(msg);
                        sum += i;
                    }
                }

                Log.d(TAG, "Sum is " + sum);
                //Toast.makeText(getApplicationContext(), "Sum is " + sum, Toast.LENGTH_SHORT).show();
                if (sum == 0) {
                    Toast.makeText(getApplicationContext(), "Application has sent all the data recieved",
                            Toast.LENGTH_SHORT).show();
                }
                sendToHost(String.valueOf(sum));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean isInteger(String st) {
        try {
            Integer.parseInt(st);
            return true;
        }
        catch(NumberFormatException ex) {
            return false;
        }
    }

    private void sendToHost(String sum) {
        try {
            if (isInteger(sum)) {
                //String encryptedStr = String.valueOf(sum);

                Message myMessage = new Message();
                myMessage.description = sum;
                network.sendToHost(myMessage, new SalutCallback() {
                    @Override
                    public void call() {
                        Toast.makeText(getApplicationContext(), "Oh no! The data failed to send.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Oh no! The data failed to send.");
                    }
                });
            }
            else {
                Message myMessage = new Message();
                myMessage.description = sum;

                network.sendToHost(myMessage, new SalutCallback() {
                    @Override
                    public void call() {
                        Toast.makeText(getApplicationContext(), "Oh no! The data failed to send.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Oh no! The data failed to send.");
                    }
                });
                messageTextView.append("\nDevice has been disconnected");

                disconnect();
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setSignal() {
        //quality;
        rssi = wifiInfo.getRssi();

        Log.d(TAG, "QUALITY IS : " + rssi);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (network.isDiscovering) {
            network.unregisterClient(new SalutCallback() {
                @Override
                public void call() {
                    messageTextView.setText("Client has been unregistered");
                }
            }, new SalutCallback() {
                @Override
                public void call() {
                    messageTextView.setText("Could not unregister client");
                }
            }, true);
        }
        //network.stopNetworkService(true);
    }

    @Override
    public void onStop() {
        //unregisterReceiver(receiver);
        super.onStop();
    }

    private void setError(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
