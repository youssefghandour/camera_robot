package com.example.camerarobot

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity() {

    // Constants
    private lateinit var bluetoothSocket: BluetoothSocket // Declare BluetoothSocket as a global variable
    private lateinit var scanButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val connectButton = findViewById<Button>(R.id.button)
        val scanButton = findViewById<Button>(R.id.button1)
        scanButton.isEnabled = false
        connectButton.setOnClickListener {
            // Check if Bluetooth permissions are granted
            if (hasBluetoothPermissions()) {
                // Bluetooth permissions are granted, proceed with the connection
                startDeviceListActivity()
            } else {
                // Bluetooth permissions are not granted, request the permissions
                requestBluetoothPermissions.launch(
                    Manifest.permission.BLUETOOTH_CONNECT,

                    )
            }
        }

        scanButton.setOnClickListener {
            // Start DeviceListActivity to select a device
            // Check if the camera permission is granted
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Camera permission is granted, start scanner activity
                startScannerActivity()
            } else {
                // Camera permission is not granted, request the permission
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }


    // Function to check if Bluetooth permissions are granted
    private fun hasBluetoothPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_ADMIN
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Register permission result callback for Bluetooth permissions
    private val requestBluetoothPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Bluetooth permissions are granted, proceed with the connection
                startDeviceListActivity()
            } else {
                // Bluetooth permissions are denied, show a message or handle it accordingly
                Toast.makeText(
                    this,
                    "Bluetooth permissions are required for connecting",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun startDeviceListActivity() {
        // Start DeviceListActivity to select a device
        blueActivity.launch(Intent(this, SelectDeviceActivity::class.java))
    }

    // Register permission result callback
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission is granted, start scanner activity
                startScannerActivity()
            } else {
                // Permission is denied, show a message or handle it accordingly
                Toast.makeText(
                    this,
                    "Camera permission is required for scanning",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun startScannerActivity() {
        val intent = Intent(this, scanner_Camera::class.java)
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE") // "PRODUCT_MODE for bar codes
        scanActivity.launch(intent)
    }

    // Handle result from DeviceListActivity
    private var blueActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val selectedDeviceName = data?.getStringExtra("selectedDeviceName")
                // Connect to the selected device
                selectedDeviceName?.let {
                    bluetoothSocket = connectToDevice(it)!!
                    scanButton.isEnabled = true
                    // After successful connection, send data

                }
            }
        }
    private var scanActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val contents = data?.getStringExtra("SCAN_RESULT")
                Toast.makeText(this, contents, Toast.LENGTH_SHORT).show()
                sendData(bluetoothSocket, contents)
            }

        }

    private fun connectToDevice(deviceName: String): BluetoothSocket? {
        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val bluetoothAdapter = bluetoothManager.adapter
        val device = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return null
        } else {
            bluetoothAdapter?.bondedDevices?.firstOrNull { it.name == deviceName }
        }

        if (device != null) {
            val uuid =
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard UUID for SPP
            val socket = device.createRfcommSocketToServiceRecord(uuid)

            // Connect to the device
            try {
                socket.connect()
                // Connection successful, perform further operations here
                // For example, you can start a background thread to handle data exchange
                // or communicate with your Arduino device
                Toast.makeText(this, "Connected to $deviceName", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                // Handle connection error
                Toast.makeText(this, "Error connecting $e", Toast.LENGTH_SHORT).show()
                socket.close()
                e.printStackTrace()
                return null
            }
            return socket
        } else {
            // Device not found or not paired
            // Handle accordingly, for example, display an error message
            Toast.makeText(this, "Device not found or not paired", Toast.LENGTH_SHORT).show()
            return null
        }

    }

    private fun sendData(socket: BluetoothSocket?, data: String?) {
        try {
            // Check if the socket is valid
            if (socket != null) {
                // Get the output stream from the socket
                val outputStream = socket.outputStream
                // Write the data to the output stream
                outputStream.write(data?.toByteArray())
                // Flush the output stream to ensure all data is sent
                outputStream.flush()
                // Close the output stream
                outputStream.close()
            } else {
                // Socket is null, log an error or handle it accordingly
                Log.e(TAG, "Socket is null")
            }
        } catch (e: IOException) {
            // Error occurred while sending data, log the error or handle it accordingly
            Log.e(TAG, "Error sending data: ${e.message}")
        }
    }


}
