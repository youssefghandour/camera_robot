package com.example.camerarobot

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class SelectDeviceActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_device)

        // Populate ListView with paired Bluetooth devices
        val deviceList = findViewById<ListView>(R.id.device_list)
        val pairedDevices = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1
            )
            null
        } else {
            val bluetoothManager =
                this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter?.bondedDevices?.map { it.name }
        }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            pairedDevices?.toList() ?: emptyList()
        )
        deviceList.adapter = adapter

        // Handle device selection
        deviceList.setOnItemClickListener { _, _, position, _ ->
            val selectedDeviceName = pairedDevices?.get(position)
            if (selectedDeviceName != null) {
                selectedDeviceName.let {
                    // Return selected device name to MainActivity
                    val intent = Intent()
                    intent.putExtra("selectedDeviceName", it)
                    setResult(Activity.RESULT_OK, intent)

                    finish()
                }
            } else {
                // Device not selected
                Toast.makeText(this, "Device not selected", Toast.LENGTH_SHORT).show()
            }
        }
    }
}