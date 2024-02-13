package com.example.camerarobot

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class SelectDeviceActivity : AppCompatActivity() {

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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        } else {
            val bluetoothManager =
                this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter?.bondedDevices?.map { it.name }
        }
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, pairedDevices.orEmpty())
        deviceList.adapter = adapter

        // Handle device selection
        deviceList.setOnItemClickListener { _, _, position, _ ->
            val selectedDeviceName = pairedDevices?.get(position)
            selectedDeviceName?.let {
                // Return selected device name to MainActivity
                val intent = Intent()
                intent.putExtra("selectedDeviceName", it)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
}