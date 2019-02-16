package com.sethlee0111.reminiscence;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Find Bluetooth devices that are close
 */
public class FindNeighbors {
    ArrayList<String> devices = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private Activity activity;
    private FindNeighborListener listener;

//    private static FindNeighbors findNeighbors = null;

//    public FindNeighbors getFindNeighbors(Activity activity, FindNeighborListener listener) {
//        if(findNeighbors == null)
//            findNeighbors = new FindNeighbors(activity, listener);
//        return findNeighbors;
//    }

    /**
     * This should always be called in an Activity. What should I do..?
     *
     * @param activity
     * @param listener
     */
    public FindNeighbors(Activity activity, FindNeighborListener listener) {
        this.activity = activity;
        this.listener = listener;
        mHandler = new Handler();
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            Toast.makeText(activity, "BLE Not Supported",
//                    Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public ArrayList<String> getDevices() {
        return devices;
    }

    public void onPause() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }
    public void onDestroy() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }
    public void onResume() {
        devices.clear();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true);
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            devices.clear();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);

                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            Log.i("Device name:", result.getDevice().getAddress());
            final BluetoothDevice btDevice = result.getDevice();
            connectToDevice(btDevice);
            if(!devices.contains(btDevice.getAddress()))
                devices.add(btDevice.getAddress());
            // https://www.geeksforgeeks.org/asynchronous-synchronous-callbacks-java/
            new Thread(new Runnable() {
                public void run()
                {
                    // perform any operation
                    System.out.println("Performing operation in Asynchronous Task");
                    // check if listener is registered.
                    if (listener != null) {
                        // invoke the callback method of class A
                        listener.onFindNeighbor(btDevice.getAddress());
                    }
                }
            }).start();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("onLeScan", device.toString());
                            connectToDevice(device);
                        }
                    });
                }
            };

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(activity, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            gatt.readCharacteristic(services.get(1).getCharacteristics().get
                    (0));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            gatt.disconnect();
        }
    };
}
