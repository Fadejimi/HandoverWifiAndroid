package com.handoverapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import model.Message;

public class MasterActivity extends AppCompatActivity implements SalutDataCallback {

    private LinearLayout fileBrowserLayout;
    private EditText pathNameEditText;
    private View wifiView;
    private TextView messageTextView;

    private List<Message> messageList;
    public Salut network;
    public SalutDataReceiver dataReceiver;
    public SalutServiceData serviceData;

    private static final String TAG = MasterActivity.class.getSimpleName();
    SalutCallback callback;

    long startTime, stopTime;
    long handOverStartTime, handOverStopTime;

    //private ClientAdapter clientAdapter;

    private List<String> resultString;
    private List<String> filesDir;
    private int foundDevices = -1;
    private int results = 0;
    private int status = -1;

    private Random random;
    private File targetFile;
    private Button btnSendData, btnStopConnection, btnRegisterDevice;
    private float initialBatteryLevel;

    boolean handOverStatus = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        fileBrowserLayout = (LinearLayout) findViewById(R.id.file_browser_layout);
        pathNameEditText = (EditText) findViewById(R.id.server_file_path);

        btnSendData = (Button) findViewById(R.id.btn_send_data);
        btnStopConnection = (Button) findViewById(R.id.btn_stop_connection);
        btnRegisterDevice = (Button) findViewById(R.id.btn_register_device);

        btnSendData.setEnabled(false);
        btnStopConnection.setEnabled(false);

        wifiView = (View) findViewById(R.id.server_view);
        messageTextView = (TextView) findViewById(R.id.message_textview);

        dataReceiver = new SalutDataReceiver(this, this);

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
                Toast.makeText(getApplicationContext(), "Sorry, but this device does not support WiFi Direct.",
                        Toast.LENGTH_SHORT).show();
                //Log.d(TAG, "Number of registered devices is now " + network.registeredClients.size());
            }
        });

        random = new Random();

        messageList = new ArrayList<>();
        //clientList = new ArrayList<>();

        resultString = new ArrayList<>();

        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData();
            }
        });

        btnStopConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopHost();
            }
        });
        btnRegisterDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerDevice();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            status = extras.getInt("status");

            btnRegisterDevice.setVisibility(View.GONE);
            btnStopConnection.setVisibility(View.GONE);
            btnSendData.setEnabled(true);
            btnSendData.setText("Solve Data");
        }
    }

    private void sendMessage(String str) {
        Message message = new Message();
        message.description = str;

        try {
            foundDevices = network.registeredClients.size();
            Log.d(TAG, "Found devices number = " + foundDevices);
            if (foundDevices > 0) {
                if (handOverStatus) {
                    handOverStartTime = System.currentTimeMillis();
                }
                int i = random.nextInt(foundDevices);
                if (foundDevices == 1) {
                    i = 0;
                }
                System.out.println("The device pointer is " + i);
                final SalutDevice device = network.registeredClients.get(i);

                //messageTextView.append("\n" + str + " is been sent to another " + device.deviceName);

                network.sendToDevice(device, message, new SalutCallback() {
                    @Override
                    public void call() {
                        Log.d(TAG, "Could not send message to " + device.deviceName);
                        messageTextView.append("\n" + "Could not send message to " + device.deviceName);
                    }
                });

                if (handOverStatus) {
                    handOverStopTime = System.currentTimeMillis();
                    long handOverTime = handOverStopTime - handOverStartTime;
                    messageTextView.setText("\nHandOver time is " + handOverTime);

                    Toast.makeText(getApplicationContext(), "Handover time is " + handOverTime + " microseconds",
                            Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Could not find any of the connected devices",
                        Toast.LENGTH_SHORT).show();
                messageTextView.setText("Could not find any of the connected devices to send the message to");
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendData() {
        startTime = System.currentTimeMillis();
        if (status == -1) {
            btnRegisterDevice.setEnabled(false);
            int x = 0;
            for (Message msg : messageList) {
                String message = msg.description;

                try {
                    Message message1 = new Message();
                    message1.description = message;

                    foundDevices = network.registeredClients.size();
                    Log.d(TAG, "Found devices number = " + foundDevices);
                    if (foundDevices > 0) {
                        btnSendData.setEnabled(false);
                        int i = random.nextInt(foundDevices);
                        //int i = ThreadLocalRandom.current().nextInt(1, foundDevices + 1);
                        System.out.println("The device pointer is " + i);
                        final SalutDevice device = network.registeredClients.get(i);

                        messageTextView.append("\nNumber of messages sent "+ x++);
                        //messageTextView.append(encryptedMsg );
                        network.sendToDevice(device, message1, new SalutCallback() {
                            @Override
                            public void call() {
                                Log.d(TAG, "Could not send message to " + device.deviceName);
                            }
                        });
                    } else {
                        String mess = "Could not find any device to connect to";
                        Log.d(TAG, mess);
                        messageTextView.append("\n" + mess);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        else {
            startTime = System.currentTimeMillis();
            for (Message msg : messageList) {
                String message = msg.description;

                try {
                    String[] msgResult = message.split(",");

                    int sum = 0;
                    for (String str : msgResult) {
                        if (isInteger(str)) {
                            int i = Integer.parseInt(str);
                            sum += i;
                        }
                    }

                    Log.d(TAG, "Sum is " + sum);
                    resultString.add(String.valueOf(sum));
                    --results;

                    if (results == 0) {
                        Toast.makeText(getApplicationContext(), "Application has finished solving all the data ",
                                Toast.LENGTH_SHORT).show();
                        messageTextView.setText("\nApplication has solved all the data recieved");

                        sendToFile();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void stopHost() {
        network.stopNetworkService(false);
        btnRegisterDevice.setEnabled(true);
        btnSendData.setEnabled(false);
    }

    private void formatString(String str) {
        Message message = new Message();
        message.description = str;

        //resultString += "\n" + sum;
        messageList.add(message);
    }

    private void setUpConnection() {
        network.startNetworkService(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice salutDevice) {
                Toast.makeText(getApplicationContext(), "Device: " + salutDevice.instanceName + " connected.", Toast.LENGTH_SHORT).show();

                messageTextView.append("\nDevice: " + salutDevice.deviceName + " connected");
            }
        });
    }

    @Override
    public void onDataReceived(Object data) {
        Log.d(TAG, "Received network data.");
        try
        {
            Message newMessage = LoganSquare.parse(String.valueOf(data), Message.class);
            Log.d(TAG, newMessage.description);  //See you on the other side!
            //Do other stuff with data.
            messageTextView.append("\nMessage: " + newMessage.description);
            createMessage(newMessage.description);

            --results;
            Log.d(TAG, "Result is " + results);
            if (results == 0) {
                sendToFile();
            }
        }
        catch (IOException ex)
        {
            Log.e(TAG, "Failed to parse network data.");
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

    private void createMessage(String msg) {
        try {
            //String messageAfterDecrypt = AESCrypt.decrypt(Encryption.PASSWORD, msg);

            if (isInteger(msg)) {
                if (msg.equals("0")) {
                    Toast.makeText(getApplicationContext(), "Application has recieved all the data",
                            Toast.LENGTH_SHORT).show();
                    sendToFile();
                }

                resultString.add(msg);
            }
            else {
                handOverStatus = true;
                sendMessage(msg);
            }
        }catch (Exception e){
            //handle error - could be due to incorrect password or tampered encryptedMsg
            Log.d(TAG, "Could not decrypt this message: " + msg);
        }

    }

    public void startFileBrowseActivity(View view) {
        initialBatteryLevel = getBatteryLevel();
        final Dialog dialog = new Dialog(MasterActivity.this);
        dialog.setContentView(R.layout.list_of_files);
        dialog.setTitle("List of Files");

        final ListView listView = (ListView) dialog.findViewById(R.id.list_view);
        String [] list;
        try {
            list = getAssets().list("");

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, list);
            listView.setAdapter(adapter);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            dialog.dismiss();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int itemPosition = position;
                String fileName = (String) listView.getItemAtPosition(itemPosition);
                setUpFile(fileName);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void setUpFile(String fileName) {
        pathNameEditText.setText(fileName);
        AssetManager am = getApplicationContext().getAssets();
        try {
            InputStream is = am.open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                Log.d(TAG, "Unformated Line is: " + strLine);
                formatString(strLine);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        results = messageList.size();
        Log.d(TAG, "Length of message is " + results);

        Toast.makeText(getApplicationContext(), "Length of message is " + results, Toast.LENGTH_LONG).show();

        btnRegisterDevice.setEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        network.stopNetworkService(false);
    }


    private void sendToFile() {
        float finalBatteryLevel = getBatteryLevel();
        stopTime = System.currentTimeMillis();
        Long time = stopTime - startTime;
        setTime("Time taken for application to run", "Time taken for the application to run is " + time + " milliseconds");
        Toast.makeText(getApplicationContext(), "Time taken for the application to run is " + time + " milliseconds", Toast
                .LENGTH_SHORT).show();
        messageTextView.setText("Time taken for the application to run is " + time + " milliseconds");
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Results");
            long timeMillis = System.currentTimeMillis();
            String filename = timeMillis + ".txt";
            //String filename = "results.txt";
            //OutputStream

            if (!root.isDirectory()) {
                root.mkdirs();
            }
            File file = new File(root, filename);
            FileWriter fileWriter = new FileWriter(file);
            for (String str : resultString) {
                fileWriter.append(str + "\n");
                //messageTextView.append("\nMessage written to file: " + str);
            }
            fileWriter.flush();
            fileWriter.close();

            Toast.makeText(getApplicationContext(), "File has been saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            messageTextView.append("\n\nFile has been saved to " + file.getAbsolutePath());
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }

        messageTextView.append("\nInitial battery level: " + initialBatteryLevel);
        messageTextView.append("\nFinal battery level: " + finalBatteryLevel);
    }


    private void registerDevice() {
        setUpConnection();
        btnSendData.setEnabled(true);
        btnStopConnection.setEnabled(true);
        btnRegisterDevice.setEnabled(false);
    }

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

    private void setTime(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
}
