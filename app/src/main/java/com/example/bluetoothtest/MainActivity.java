package com.example.bluetoothtest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {
    private BluetoothSPP bt;


    //String[] sensorData = {"0000000000000000"};
    TextView dustText, tempText, humText, coText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // full screen
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 가로 화면으로 고정
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        bt = new BluetoothSPP(this);
        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "블루투스 사용 불가가"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            //데이터 수신
           TextView dustText =  findViewById(R.id.dustText);
           TextView tempText = findViewById(R.id.tempText);
           TextView humText =  findViewById(R.id.humText);
           TextView coText = findViewById(R.id.coText);

            public void onDataReceived(byte[] data, String message) {
                String btn= message.substring(0,0);
                String dust = message.substring(1,5);
                String co= message.substring(5,10);
                String hum=message.substring(10,13);
                String temp = message.substring(13);
                int bbtn = Integer.parseInt(btn);
                int ddust = Integer.parseInt(dust);
                if(bbtn == 1)
                {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("긴급상황입니다.")
                            .setPositiveButton("확인", null)
                            .create()
                            .show();
                    finish();
                }
                if(ddust >=0 && ddust <=30)
                {
                    dustText.setTextColor(Color.BLUE);
                    dustText.setText("좋음\n"+dust.concat("㎍/㎥"));
                }else if( ddust>=31 && ddust <=80)
                {   dustText.setTextColor(Color.GREEN);
                    dustText.setText("보통\n"+dust.concat("㎍/㎥"));
                }else if( ddust>=81 && ddust <= 150)
                {    dustText.setTextColor(Color.parseColor("#FF7F00"));
                    dustText.setText("나쁨 \n(" +dust.concat("㎍/㎥"));
                }else if( ddust>=151 )
                {   dustText.setTextColor(Color.RED);
                    dustText.setText("매우나쁨 \n(" +dust.concat("㎍/㎥"));
                }
                tempText.setText(temp.concat(" ℃"));
                humText.setText(hum.concat("%"));
                coText.setText(co.concat("ppm"));

            }
        });
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeviceDisconnected() {//연결해제
                Toast.makeText(getApplicationContext()
                        , "연결해제", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeviceConnectionFailed() {//연결실패
                Toast.makeText(getApplicationContext()
                        , "연결실패", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

                }
            }
        });
    }
        public void onDestroy() {
            super.onDestroy();
            bt.stopService(); //블루투스 중지
        }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);

            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }
}


















