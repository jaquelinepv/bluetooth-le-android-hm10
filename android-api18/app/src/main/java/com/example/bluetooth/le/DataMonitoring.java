package com.example.bluetooth.le;

import static java.nio.charset.StandardCharsets.*;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;

public class DataMonitoring extends AppCompatActivity {
    JSONObject data;
    private Socket mSocket;
    private Context context;
    //private AccessibilityService m_context;

    {
        try {
            mSocket = IO.socket("https://iotacuicola.herokuapp.com");
            //mSocket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public DataMonitoring(JSONObject data, Context context) throws IOException {
        this.data = data;
        this.context = context;

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSocket.connect();
        System.out.println("conectado!!");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendData() throws IOException {
        //Arreglo de bytes para almacenar los datos y enviarlos directamente cuando si hay internet
        byte[] d = new byte[0];
        //Arreglo de bytes para almacenar los datos del txt y enviarlos cuando regresa el internet
        ContextWrapper c = new ContextWrapper(context);
        String directory = c.getFilesDir().getPath();
        File file = new File(directory, "monitoreo.txt");
        try {
            //Convertir los Json a bytes
            d = this.data.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //Verificar conexión
        // Si hay conexión a Internet en este momento se verifica si en txt existe, si es así, se envían, si no, solo se envían los que llegan del BL.
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mb = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(!wifi.isConnected() && !mb.isConnected()) {
            // No hay conexión a Internet en este momento. Entonces los datos se almacenan temporalmente en un txt.
            System.out.println("Se perdió la conexión");


            if(!file.exists()){
                try{
                    file.createNewFile();
                    storeData(file);

                }catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                storeData(file);
            }

        }else {
            mSocket.connect();
            System.out.println("conectado!!");

            System.out.println(file.toString());
            if(file.exists()){
                recoverData(file);
                file.delete();
                System.out.println("Evento enviado");
            }
            mSocket.emit("my event", d);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void recoverData(File file) throws IOException {
        byte[] f;
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(fis);
        BufferedReader bf = new BufferedReader(inputStreamReader);
        String line;

        while((line = bf.readLine())!=null) {

            f = line.getBytes(UTF_8);
            System.out.println(f);
            mSocket.emit("my event", f);

        }

    }

    public void storeData(File file) {

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            bw.write(String.valueOf(this.data));
            bw.write("\n");
            System.out.println("información agregada!");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}