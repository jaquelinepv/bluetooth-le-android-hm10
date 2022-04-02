package com.example.bluetooth.le;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

//import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DataMonitoring extends AppCompatActivity {
    JSONObject data;
    private Context context;
    String url = "https://iotacuicola.herokuapp.com/hola";

    RequestQueue mRequestQueue;

    public DataMonitoring(JSONObject data, Context context) throws IOException {
        this.data = data;
        this.context = context;
        mRequestQueue = Volley.newRequestQueue(context);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mSocket.connect();
        //System.out.println("conectado!!");

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void sendData() throws IOException {
        //Arreglo de bytes para almacenar los datos y enviarlos directamente cuando si hay internet
        byte[] d = new byte[0];
        ArrayList<String> f = new ArrayList<>();
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
            //mSocket.connect();
            System.out.println("conectado!!");
            //System.out.println(file.toString());
            if(file.exists()){
                f = recoverData(file);
                byte[] df = new byte[0];
                //forEach
                for (String elemento: f){
                    df = elemento.getBytes(StandardCharsets.UTF_8);
                    postCommunication(df);
                }
                file.delete();
                System.out.println("Evento enviado");
                postCommunication(d);
            }

        }
        postCommunication(d);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private ArrayList<String> recoverData(File file) throws IOException {
        ArrayList<String> f = new ArrayList<>();
        //byte[] f;
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(fis);
        BufferedReader bf = new BufferedReader(inputStreamReader);
        String line;

        while((line = bf.readLine())!=null) {

            f.add(line);
            System.out.println(f);
            //mSocket.emit("my event", f);

        }
        return f;

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
    public void postCommunication(byte[] d){
        System.out.println("Estoy por acá!!");
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, d);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("fallo!!");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("Funciono! "+response);
            }
        });

    }
}