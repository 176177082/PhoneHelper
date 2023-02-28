package com.example.phonehelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private static final int REQUEST_WRITE_SETTINGS_PERMISSION = 3;

    private static final String DEVICE_ADDRESS = "12:34:56:78:90:AB"; // 目标蓝牙设备地址
    private static final String TARGET_DEVICE_NAME = "OPPO Enco W31 Lite"; // 目标蓝牙设备名字


    private static final UUID GATT_SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"); // GATT 服务 UUID
    private static final UUID GATT_CHARACTERISTIC_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"); // GATT 特征值 UUID

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothGattServer mGattServer;
    private WifiManager mWifiManager;
    private TextView mLogView;

    private boolean mIsScanning = false;
    private boolean mIsGattConnected = false;
    private boolean mIsCharacteristicNotified = false;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Button btnphoneinfo;
    private Button btnbluetoothconwifi;
    private Button btnbluetoothtest;

    private Button btnwifitest;



    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogView=(TextView) findViewById(R.id.LogView);
        mLogView.setMovementMethod(ScrollingMovementMethod.getInstance());

        btnbluetoothtest = (Button) findViewById(R.id.buttonbluetoothtest);
        btnbluetoothtest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendLog("btnbluetoothtest,开始");
            }
        });


        btnwifitest = (Button) findViewById(R.id.buttonwifitest);
        btnwifitest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendLog("btnwifitest,开始");
            }
        });


        btnphoneinfo = (Button) findViewById(R.id.buttongetphoneinfo);
        Intent intent1 = new Intent(this, phoneinfo.class);

        btnphoneinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent1);
            }
        });


        btnbluetoothconwifi = (Button) findViewById(R.id.buttonbluetoothconwifi);
//        mLogView = findViewById(R.id.log_view);

        // 检查设备是否支持蓝牙低功耗模式
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast("设备不支持蓝牙低功耗模式");
            finish();
        }

        // 初始化蓝牙适配器和蓝牙扫描器
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // 检查是否支持蓝牙
        if (mBluetoothAdapter == null) {
            showToast("设备不支持蓝牙");
            finish();
        }

        // 检查是否已开启蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }




        // 检查是否拥有修改系统设置权限（用于打开热点）
        Log.d(TAG, String.valueOf(Build.VERSION.SDK_INT)+String.valueOf(Build.VERSION_CODES.M));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            showToast("拥有修改系统设置权限");
        } else {
            // For versions below M, the permission is already granted at installation time
            showToast("不拥有修改系统设置权限");
            finish();
        }

        // 检查是否支持蓝牙
        if (mBluetoothAdapter == null) {
            showToast("设备不支持蓝牙");
            finish();
        }

        // 初始化 WiFi 管理器
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // 设置按钮点击事件
        btnbluetoothconwifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsScanning) {
                    // 如果正在扫描，则停止扫描并断开连接
//                    stopScan();
//                    disconnectGatt();
//                    stopGattServer();
                    btnbluetoothconwifi.setText("开始检测");
//                    appendLog("停止检测");
                } else {
                    // 如果未在扫描，则开始扫描并连接到目标设备
//                    startScan();
                    btnbluetoothconwifi.setText("停止检测");
//                    appendLog("开始检测");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopScan();
//        disconnectGatt();
//        stopGattServer();
    }

    private void showToast(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

//    @SuppressLint("MissingPermission")
//    private void startScan() {
//        mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
//                // 开启 GATT 服务
//        startGattServer();
//
//        // 设置扫描标志位
//        mIsScanning = true;
//

//    }



//    private void appendLog(final String message) {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                mLogView.append(message + "\n");
//            }
//        });
//    }

//    private void startScan() {
//        // 清空设备列表
////        mDeviceList.clear();
//
//        // 开始扫描
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
//
//        // 开启 GATT 服务
//        startGattServer();
//
//        // 设置扫描标志位
//        mIsScanning = true;
//    }

//    private void stopScan() {
//        // 停止扫描
//        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
//
//        // 设置扫描标志位
//        mIsScanning = false;
//    }

//    @SuppressLint("MissingPermission")
//    private void startGattServer() {
//        // 开启 GATT 服务
//        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        if (bluetoothManager != null) {
//            mGattServer = bluetoothManager.openGattServer(this, mGattServerCallback);
//            BluetoothGattService service = new BluetoothGattService(GATT_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
//            BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(GATT_CHARACTERISTIC_UUID,
//                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
//                    BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
//            characteristic.setValue(GATT_CHARACTERISTIC_VALUE);
//            service.addCharacteristic(characteristic);
//            mGattServer.addService(service);
////            appendLog("已开启 GATT 服务");
//        }
//    }


//    private void stopGattServer() {
//        // 关闭 GATT 服务
//        if (mGattServer != null) {
//            mGattServer.close();
//            mGattServer = null;
//        }
//    }

//    private void disconnectGatt() {
//        // 断开 GATT 连接
////        if (mGattServer != null) {
////            mGattServer.disconnect();
////            mGattServer.close();
////            mGattServer = null;
////        }
//    }

//    private ScanCallback mScanCallback = new ScanCallback() {
//        @SuppressLint("MissingPermission")
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
//            BluetoothDevice device = result.getDevice();
//            if (device != null && device.getName() != null) {
//                if (device.getName().equals(TARGET_DEVICE_NAME)) {
//                    // 找到目标设备，停止扫描并连接到设备
//                    stopScan();
////                    connectToDevice(device);
//                }
//            }
//        }

//        @Override
//        public void onScanFailed(int errorCode) {
//            super.onScanFailed(errorCode);
//            showToast("扫描失败");
//        }
//    };
//
//    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
//        @SuppressLint("MissingPermission")
//        @Override
//        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
//            super.onConnectionStateChange(device, status, newState);
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                // 连接成功，开始发送数据
//                BluetoothGattCharacteristic characteristic = mGattServer.getService(GATT_SERVICE_UUID)
//                        .getCharacteristic(GATT_CHARACTERISTIC_UUID);
//                characteristic.setValue(GATT_CHARACTERISTIC_VALUE);
//                mGattServer.notifyCharacteristicChanged(device, characteristic, false);
//            }
//        }
//    };
//
//    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
//        @SuppressLint("MissingPermission")
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            super.onConnectionStateChange(gatt, status, newState);
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                // 连接成功，发现 GATT 服务
//                mBluetoothGatt.discoverServices();
//
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                // 断开连接
//                stopGattServer();
//                disconnectGatt();
//            }
//        }

//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            super.onServicesDiscovered(gatt, status);
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                // 发现 GATT 服务，读取服务特征的值
//                BluetoothGattCharacteristic characteristic = gatt.getService(GATT_SERVICE_UUID)
//                        .getCharacteristic(GATT_CHARACTERISTIC_UUID);
//                gatt.readCharacteristic(characteristic);
//            }
//        }
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            super.onCharacteristicRead(gatt, characteristic, status);
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                // 读取服务特征的值成功，检查特征值是否匹配
//                byte[] value = characteristic.getValue();
//                if (value != null && value.length == GATT_CHARACTERISTIC_VALUE.length && Arrays.equals(value, GATT_CHARACTERISTIC_VALUE)) {
//                    // 特征值匹配，打开个人共享热点
//                    toggleWifiAp(true);
//                    appendLog("已打开个人共享热点");
//                }
//            }
//        }

//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            super.onCharacteristicWrite(gatt, characteristic, status);
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                // 向服务特征写入值成功，关闭 GATT 连接
//                disconnectGatt();
//            }
//        }
//    };

//    @SuppressLint("MissingPermission")
//    private void connectToDevice(BluetoothDevice device) {
//        // 连接到设备
//        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
//    }
//
//    private void toggleWifiAp(boolean enabled) {
//        // 打开或关闭个人共享热点
//        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        if (wifiManager != null) {
//            WifiConfiguration wifiConfiguration = new WifiConfiguration();
//            wifiConfiguration.SSID = WIFI_AP_SSID;
//            wifiConfiguration.preSharedKey = WIFI_AP_PASSWORD;
//            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//            if (enabled) {
//                wifiManager.setWifiEnabled(false);
//                Method method = null;
//                try {
//                    method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
//                    method.invoke(wifiManager, wifiConfiguration, true);
//                } catch (NoSuchMethodException | IllegalAccessException |
//                         InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Method method = null;
//                try {
//                    method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
//                    method.invoke(wifiManager, wifiConfiguration, false);
//                    wifiManager.setWifiEnabled(true);
//                } catch (NoSuchMethodException | IllegalAccessException |
//                         InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private void startScan() {
//        // 扫描蓝牙设备
//        if (mBluetoothAdapter != null) {
//            mBluetoothAdapter.startLeScan(mLeScanCallback);
//            appendLog("开始扫描蓝牙设备");
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private void stopScan() {
//        // 停止扫描蓝牙设备
//        if (mBluetoothAdapter != null) {
//
//            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
//
//
////            mBluetoothLeScanner.stopScan(mScanCallback);
////            mBluetoothAdapter.stopS(mScanCallback);
//        }
//    }
//
//    private void startGattServer() {
//        // 开启 GATT 服务
//        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        if (bluetoothManager != null) {
//            mGattServer = bluetoothManager.openGattServer(this, mGattServerCallback);
//            BluetoothGattService service = new BluetoothGattService(GATT_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
//            BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(GATT_CHARACTERISTIC_UUID,
//                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
//                    BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
//            characteristic.setValue(GATT_CHARACTERISTIC_VALUE);
//            service.addCharacteristic(characteristic);
//            mGattServer.addService(service);
////            appendLog("已开启 GATT 服务");
//        }
//    }

//    private void stopGattServer() {
//        // 停止 GATT 服务
//        if (mGattServer != null) {
//            mGattServer.close();
//            mGattServer = null;
//            appendLog("已停止 GATT 服务");
//        }
//    }

//    private void disconnectGatt() {
//        // 断开 GATT 连接
//        if (mGatt != null) {
//            mGatt.disconnect();
//            mGatt.close();
//            mGatt = null;
//        }
//    }

    private void appendLog(String message) {
        // 添加日志
        mLogView.append("\n" + message);
    }


}

