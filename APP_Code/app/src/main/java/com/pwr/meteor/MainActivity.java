package com.pwr.meteor;

import static android.os.SystemClock.sleep;
import static java.lang.Integer.valueOf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String APP_NAME = "MeteoR";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String HC06mac = "00:20:04:BD:D2:C4";

    TextView status;
    TextView rdTemp;
    TextView rdPress;
    TextView rdHum;

    TextView progTemp;
    TextView progCisn;
    TextView progWilg;
    TextView progPogo;
    TextView progOpad;
    TextView progInfo;

    //TextView test;

    BarChart wykTemp;
    BarChart wykPress;
    BarChart wykHum;

    int pomT = 1;
    int pomP = 1;
    int pomH = 1;

    boolean upT;
    boolean upP;
    boolean upH;

    int counterT = -1;
    int counterP = -1;
    int counterH = -1;



    List<Float> lTemp  = new ArrayList<>();
    List<Float> lPress = new ArrayList<>();
    List<Float> lHum   = new ArrayList<>();


    ArrayList<BarEntry> wTempEnt  = new ArrayList<>();
    ArrayList<BarEntry> wPressEnt = new ArrayList<>();
    ArrayList<BarEntry> wHumEnt   = new ArrayList<>();

    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findAllViewsById();
        if(!btAdapter.isEnabled()){
            btAdapter.enable();
            sleep(1000);
        }

        BTclient btc = new BTclient();
        btc.start();

    }


    Handler handler = new Handler(new Handler.Callback() {
        int currentValue = 0;
        @Override
        public boolean handleMessage(@NonNull Message message) {

            if(message.arg1==1){status.setText("THREAD_START");}
            if(message.arg1==2){status.setText("SOCKET_CONNECT_ERROR");}
            if(message.arg1==3){status.setText("SOCKET_CONNECTED");}
            if(message.arg1==4){status.setText("SOCKET_CONNECT_FAILURE");}
            if(message.arg1==5){status.setText("SOCKET_CLOSE_ERROR");}
            if(message.arg1==6){status.setText("SOCKET_CLOSED");}
            if(message.arg1==7){status.setText("SLEEP_COMMAND_ERROR");}
            if(message.arg1==8){status.setText("INPUT_STREAM_ERROR");}
            if(message.arg1==9){status.setText("INPUT_STREAM_INITIALISED");}

            if(message.arg1==98){status.setText("INPUT_STREAM_READ_ERROR");}
            if(message.arg1>=99){status.setText("LOOP : " + String.valueOf(message.arg1));}

            return false;
        }
    });

    Handler handlerTemp = new Handler(new Handler.Callback() {
        int currentValue = 0;

        @Override
        public boolean handleMessage(@NonNull Message messageT) {


            float pomTempt = (valueOf(messageT.arg1));
            float pomTemp = pomTempt/1000;
            rdTemp.setText(String.valueOf(pomTemp));
            lTemp.add(pomTemp);

            BarEntry tempEn = new BarEntry(pomT,pomTemp);
            wTempEnt.add(tempEn);

            BarDataSet bDStemp = new BarDataSet(wTempEnt,"Wykres Temperatur");
            bDStemp.setColor(Color.rgb(194, 50, 0));
            bDStemp.setDrawValues(false);


            wykTemp.setData(new BarData(bDStemp));
            wykTemp.animateY(0);
            wykTemp.getDescription().setText(" ");
            wykTemp.getDescription().setTextColor(Color.BLACK);

            //test.setText(String.valueOf(lTemp));

            if(counterT==-1)
            {
                progTemp.setText("Trwa analiza danych pogodowych!");
                progCisn.setText("Trwa analiza danych pogodowych!");
                progWilg.setText("Trwa analiza danych pogodowych!");
                progPogo.setText("Trwa analiza danych pogodowych!");
                progOpad.setText("Trwa analiza danych pogodowych!");
                progInfo.setText("Trwa analiza danych pogodowych!");
                counterT++;
            }

            if(counterT > 162)
            {
                float D1 = lTemp.get(pomT - 161);   //pomiar 0
                float D2 = lTemp.get(pomT - 81);    //pomiar 80
                float D3 = lTemp.get(pomT - 1);     //pomiar 160

                if((D1 > D2) && (D2 > D3)) upT=false; //temperatura maleje
                if((D1 < D2) && (D2 < D3)) upT=true;  //temperatura rośnie
                if(((D1 > D2) && (D2 < D3))||((D1 < D2) && (D2 > D3)))
                {
                    if(D1>D3)upT=false; //temperatura maleje
                    if(D1<D3)upT=true;  //temperatura rośnie
                }

                counterT = 0;
            }

            pomT++;
            counterT++;

            return false;
        }
    });

    Handler handlerPress = new Handler(new Handler.Callback() {
        int currentValue = 0;
        @Override
        public boolean handleMessage(@NonNull Message messageP) {

            float pomPresst = (valueOf(messageP.arg1));
            float pomPress = pomPresst/100;
            rdPress.setText(String.valueOf(pomPress));
            lPress.add(pomPress);

            BarEntry pressEn = new BarEntry(pomP,pomPress);
            wPressEnt.add(pressEn);

            BarDataSet bDSpress = new BarDataSet(wPressEnt,"Wykres Ciśnień");
            bDSpress.setColor(Color.rgb(0, 176, 119));
            bDSpress.setDrawValues(false);

            wykPress.setData(new BarData(bDSpress));
            wykPress.animateY(0);
            wykPress.getDescription().setText(" ");
            wykPress.getDescription().setTextColor(Color.BLACK);


            if(counterP > 162)
            {
                float D1 = lPress.get(pomP - 161);   //pomiar 0
                float D2 = lPress.get(pomP - 81);    //pomiar 80
                float D3 = lPress.get(pomP - 1);     //pomiar 160

                if((D1 > D2) && (D2 > D3)) upP=false; //cisnienie maleje
                if((D1 < D2) && (D2 < D3)) upP=true;  //cisnienie rośnie
                if(((D1 > D2) && (D2 < D3))||((D1 < D2) && (D2 > D3)))
                {
                    if(D1>D3)upP=false; //cisnienie maleje
                    if(D1<D3)upP=true;  //cisnienie rośnie
                }

                counterP = 0;
            }


            pomP++;
            counterP++;



            return false;
        }
    });

    Handler handlerHum = new Handler(new Handler.Callback() {
        int currentValue = 0;
        @Override
        public boolean handleMessage(@NonNull Message messageH) {

            float pomHumt = (valueOf(messageH.arg1));
            float pomHum = pomHumt/1000;
            rdHum.setText(String.valueOf(pomHum));
            lHum.add(pomHum);

            BarEntry humEn = new BarEntry(pomH,pomHum);
            wHumEnt.add(humEn);

            BarDataSet bDShum = new BarDataSet(wHumEnt,"Wykres Wilgotności");
            bDShum.setColor(Color.rgb(5, 123, 194));
            bDShum.setDrawValues(false);

            wykHum.setData(new BarData(bDShum));
            wykHum.animateY(0);
            wykHum.getDescription().setText(" ");
            wykHum.getDescription().setTextColor(Color.BLACK);

            if(counterH > 162)
            {
                float D1 = lHum.get(pomH - 161);   //pomiar 0
                float D2 = lHum.get(pomH - 81);    //pomiar 80
                float D3 = lHum.get(pomH - 1);     //pomiar 160

                if((D1 > D2) && (D2 > D3)) upH=false; //cisnienie maleje
                if((D1 < D2) && (D2 < D3)) upH=true;  //cisnienie rośnie
                if(((D1 > D2) && (D2 < D3))||((D1 < D2) && (D2 > D3)))
                {
                    if(D1>D3)upH=false; //cisnienie maleje
                    if(D1<D3)upH=true;  //cisnienie rośnie
                }

                //prognoza

                //temperatura
                if(upT==true) progTemp.setText("możliwy wzrost wartości");
                if(upT==false) progTemp.setText("możliwy spadek wartości");

                //ciśnienie
                if(upP==true) progCisn.setText("możliwy wzrost wartości");
                if(upP==false) progCisn.setText("możliwy spadek wartości");

                //wilgotność
                if(upH==true) progWilg.setText("możliwy wzrost wartości");
                if(upH==false) progWilg.setText("możliwy spadek wartości");

                //pogoda
                if(upP==true) progPogo.setText("możliwa poprawa");
                if(upP==false) progPogo.setText("możliwe pogorszenie");

                //opady
                if((upP==false)&&(upH!=true)&&(upT!=true)) progOpad.setText("możliwe opady");
                if((upP==false)&&(upH==true)&&(upT==true)) progOpad.setText("prawdopodobne opady i wiatr");
                if(upP==true) progOpad.setText("brak/nieznaczne opady");

                //dodatkowe
                if((upH==true)&&(upT==false)) progInfo.setText("możliwe mgły");
                else progInfo.setText("---");

                counterH = 0;
            }

            counterH++;
            pomH++;



            return false;
        }
    });


    private class BTclient extends Thread
    {
        BluetoothSocket btSocket;
        public void run()
        {



            BluetoothDevice hc06 = btAdapter.getRemoteDevice(HC06mac);

            Message message = Message.obtain();
            message.arg1 = 1;
            handler.sendMessage(message);

            int counter = 0;

            do {
                try {
                    btSocket = hc06.createRfcommSocketToServiceRecord(MY_UUID);
                    btSocket.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                    message = Message.obtain();
                    message.arg1 = 2;
                    handler.sendMessage(message);
                }
                counter++;
            }while(!btSocket.isConnected() && counter<3);

            if(btSocket.isConnected())
            {
                counter = 0;
                message = Message.obtain();
                message.arg1 = 3;
                handler.sendMessage(message);
            }
            else
            {
                message = Message.obtain();
                message.arg1 = 4;
                handler.sendMessage(message);
            }

            int inputStreamErrorFlag = 0;
            int dataMonitoringFlag = 99;

                InputStream inputStream = null;
                int pom = 0;
                int iterate = 0;
                char temperaturaOdczytana[] = new char[10];
                char cisnienieOdczytane[] = new char[10];
                char wilgotnoscOdczytana[] = new char[10];
                char charPom[] = new char[50];;
                try {
                    inputStream = btSocket.getInputStream();
                    inputStream.skip(inputStream.available());

                    message = Message.obtain();
                    message.arg1 = 9;
                    handler.sendMessage(message);


                    while(true)
                    {
                        dataMonitoringFlag++;
                        message = Message.obtain();
                        message.arg1 = dataMonitoringFlag;
                        handler.sendMessage(message);

                        do
                        {
                            charPom[iterate] = (char) inputStream.read();


                            if(charPom[iterate] == ':')  pom++;

                            iterate++;
                        }while(pom != 3);

                        iterate = 0;
                        pom = 0;

                        String stringPom = new String(charPom);
                        String[] separated = stringPom.split(":");
                        String sT = separated[0];
                        String sP = separated[1];
                        String sH = separated[2];



                        int iT = Integer.parseInt(sT);
                        int iP = Integer.parseInt(sP);
                        int iH = Integer.parseInt(sH);


                        Message messageT = Message.obtain();
                        messageT.arg1 = iT;
                        handlerTemp.sendMessage(messageT);

                        Message messageP = Message.obtain();
                        messageP.arg1 = iP;
                        handlerPress.sendMessage(messageP);

                        Message messageH = Message.obtain();
                        messageH.arg1 = iH;
                        handlerHum.sendMessage(messageH);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    inputStreamErrorFlag = 1;
                    counter++;
                    message = Message.obtain();
                    message.arg1 = 8;
                    handler.sendMessage(message);
                }


            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                message = Message.obtain();
                message.arg1 = 7;
                handler.sendMessage(message);
            }

            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                message = Message.obtain();
                message.arg1 = 5;
                handler.sendMessage(message);
            }

            message = Message.obtain();
            message.arg1 = 6;
            handler.sendMessage(message);
        }
    }




    private void findAllViewsById() {
        status  = (TextView) findViewById(R.id.status);
        rdTemp  = (TextView) findViewById(R.id.rdTemp);
        rdPress = (TextView) findViewById(R.id.rdPress);
        rdHum   = (TextView) findViewById(R.id.rdHum);

        wykTemp  = (BarChart) findViewById(R.id.wykresTemperatury);
        wykPress = (BarChart) findViewById(R.id.wykresCisnienia);
        wykHum   = (BarChart) findViewById(R.id.wykresWilgotnosci);

        progTemp  = (TextView) findViewById(R.id.progTemperatura);
        progCisn  = (TextView) findViewById(R.id.progCisnienie);
        progWilg  = (TextView) findViewById(R.id.progWilgotnosc);
        progPogo  = (TextView) findViewById(R.id.progPogoda);
        progOpad  = (TextView) findViewById(R.id.progOpady);
        progInfo  = (TextView) findViewById(R.id.progInfo);

    }
}