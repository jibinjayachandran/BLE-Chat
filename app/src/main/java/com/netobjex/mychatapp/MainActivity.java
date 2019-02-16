package com.netobjex.mychatapp;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netobjex.mychatapp.callbacks.SelectDeviceCallback;
import com.netobjex.mychatapp.entities.ChatMessage;
import com.netobjex.mychatapp.utilities.BluetoothChatService;
import com.netobjex.mychatapp.utilities.Constants;
import com.netobjex.mychatapp.utilities.DividerItemDecoration;
import com.netobjex.mychatapp.utilities.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements SelectDeviceCallback {

    private int REQUEST_ENABLE_BLUETOOTH=100;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothChatService mChatService = null;
    private DiscoveredDeviceListAdapter deviceListAdapter,pairedListAdapter;
    private ArrayList<String> items = new ArrayList<>();
    private ArrayList<String> pairedItems = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private List<ChatMessage> messages = new ArrayList<>();
    private EditText mOutEditText;
    TextView tvConnectedName;

    String deviceName="";
    String deviceAddress="";
    private Dialog dialog;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkBluetoothSupport();
        deviceListAdapter = new DiscoveredDeviceListAdapter(MainActivity.this,items);
        pairedListAdapter = new DiscoveredDeviceListAdapter(MainActivity.this,pairedItems);
        ImageView ivBluetooth = (ImageView)findViewById(R.id.ivBluetooth);
        tvConnectedName = (TextView)findViewById(R.id.tvConnectedName);
        ivBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDevices();
            }
        });
    }

    private void enableBlluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }else if (mChatService == null) {
            setupChat();
        }
    }

    private void checkBluetoothSupport() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            enableBlluetooth();
        }
    }
    private void setupChat() {
        // Initialize the array adapter for the conversation thread
        messageAdapter = new MessageAdapter(MainActivity.this,messages);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        Button mSendButton = (Button)findViewById(R.id.btn_chat_send);
        mOutEditText = (EditText)findViewById(R.id.msg_type);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        recyclerView.setAdapter(messageAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                    String message = mOutEditText.getText().toString();
                    sendMessage(message);

            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(MainActivity.this, mHandler);

    }
    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private EditText.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(MainActivity.this,"Device is not connected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            mOutEditText.setText("");
        }
    }
    private void showDevices() {
        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.devices_list_layout);
        RecyclerView rlPairedDevices = (RecyclerView)dialog.findViewById(R.id.rlPairedDevices);
        RecyclerView rlDevices = (RecyclerView)dialog.findViewById(R.id.rlDevices);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rlPairedDevices.setLayoutManager(mLayoutManager);
        rlPairedDevices.addItemDecoration(new DividerItemDecoration(MainActivity.this));
        rlPairedDevices.setAdapter(pairedListAdapter);

        RecyclerView.LayoutManager mLayoutManager1 = new LinearLayoutManager(getApplicationContext());
        rlDevices.setLayoutManager(mLayoutManager1);
        rlDevices.addItemDecoration(new DividerItemDecoration(MainActivity.this));
        rlDevices.setAdapter(deviceListAdapter);

        Button dialogButton = (Button) dialog.findViewById(R.id.btnClose);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void searchDevices() {
        items.clear();
        pairedItems.clear();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedItems.add(device.getName() + "\n" + device.getAddress());
                pairedListAdapter.notifyDataSetChanged();
               // pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedItems.clear();
            pairedItems.add("Nothing found");
            pairedListAdapter.notifyDataSetChanged();
           // pairedDevicesAdapter.add(getString(R.string.none_paired));
        }
        showDevices();
    }
    @Override
    public void onDeviceSelected(String name, String address) {
        deviceName=name;
        deviceAddress = address;
        // Get the BluetoothDevice object
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(MainActivity.this, mHandler);
        // Attempt to connect to the device
        mChatService.connect(device, false);
        dialog.dismiss();
    }


    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    items.add(device.getName() + "\n" + device.getAddress());
                    deviceListAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (deviceListAdapter.getItemCount() == 0) {
                    items.clear();
                    items.add("Nothing found");
                    deviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    };
    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Activity activity = MainActivity.this;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(deviceName);
                          //  mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus("connecting");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus("not connected");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    ChatMessage message = new ChatMessage(writeMessage,true);
                    messages.add(message);
                    if(messageAdapter==null){
                        setupChat();
                    }else{
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                // Call smooth scroll
                                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                            }
                        });
                    }

                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    ChatMessage message1 = new ChatMessage(readMessage,false);
                    messages.add(message1);
                    if(messageAdapter==null){
                        setupChat();
                    }else{
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                // Call smooth scroll
                                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                            }
                        });
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    deviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + deviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
    private void setStatus(String subTitle) {
        Activity activity =  MainActivity.this;
        if (null == activity) {
            return;
        }
        tvConnectedName.setText(subTitle);

    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }


}
