package com.android.lontopsoft.lightswich;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import android_serialport_api.SerialPortFinder;
import bean.ComBean;

public class MainActivity extends AppCompatActivity {
    RadioGroup radioGroup;
    Button send_btn;
    TextView recv;
    EditText send;
    Spinner spinner;
    ToggleButton onoff;
    SerialPortFinder portFinder;
    SerialHelper serialHelper;
    StringBuilder stringBuilder;
    static Process process = null;
    String sPort = "/dev/ttyS2";
    GPIO gpio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        radioGroup = (RadioGroup)findViewById(R.id.switch_radio);
        send_btn = (Button)findViewById(R.id.send_btn);
        recv = (TextView)findViewById(R.id.recv);
        send = (EditText)findViewById(R.id.send);
        spinner = (Spinner)findViewById(R.id.all_port);
        onoff = (ToggleButton)findViewById(R.id.onoff);
//        sPort = spinner.getSelectedItem().toString();
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }
        gpio = new GPIO(process);
        portFinder = new SerialPortFinder();
        stringBuilder = new StringBuilder();
        serialHelper = new SerialHelper(sPort,process) {

            @Override
            protected void onDataReceived(ComBean ComRecData) {
                Log.e("Log",ComRecData.sComPort);
                stringBuilder.append(ComRecData.sRecTime+" :"+ComRecData.sComPort+"\r\n");
                recv.setText(stringBuilder.toString());
            }
        };

        String[] entry = portFinder.getAllDevicesPath();
        List<String> allDevices = new ArrayList<String>();
        for (int i = 0; i < entry.length; i++) {
            allDevices.add(entry[i]);
        }
        ArrayAdapter<String> aspnDevices = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, allDevices);
        aspnDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aspnDevices);


        onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sPort = spinner.getSelectedItem().toString();
                    serialHelper.setPort(sPort);
                    OpenComPort(serialHelper);
                } else {
                    CloseComPort(serialHelper);
                }
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {


                switch (checkedId){
                    case R.id.light_on:

                        GPIO.gpio_crtl("PH20", "", 01);
                        break;
                    case R.id.light_off:
//                        gpio = new GPIO(process);
                        GPIO.gpio_crtl("PH20", "", 00);
                        break;
                    default:
                        break;
                }
            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serialHelper.sendTxt(send.getText().toString());
            }
        });
    }
    //----------------------------------------------------打开串口
    private void OpenComPort(SerialHelper ComPort){
        try
        {
            ComPort.open();
        } catch (SecurityException e) {
            ShowMessage("打开串口失败:没有串口读/写权限!");
        } catch (IOException e) {
            ShowMessage("打开串口失败:未知错误!");
        } catch (InvalidParameterException e) {
            ShowMessage("打开串口失败:参数错误!");
        }
    }
    //----------------------------------------------------关闭串口
    private void CloseComPort(SerialHelper ComPort){
        if (ComPort!=null){
            ComPort.stopSend();
            ComPort.close();
        }
    }
    //------------------------------------------显示消息
    private void ShowMessage(String sMsg)
    {
        Toast.makeText(this, sMsg, Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CloseComPort(serialHelper);
    }
}
