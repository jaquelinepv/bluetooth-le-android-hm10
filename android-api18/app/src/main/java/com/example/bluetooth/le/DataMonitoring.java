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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
    //JSONObject data;
    private Context context;
    //String url = "http://192.168.28.73:8000/hola";
    String url = "https://iotacuicola.herokuapp.com/hola";

    RequestQueue mRequestQueue;

    public DataMonitoring(Context context) throws IOException {
        //this.data = data;
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
    public void sendData(JSONObject data) throws IOException {

        JSONArray jsArray = new JSONArray();
        ContextWrapper c = new ContextWrapper(context);
        String directory = c.getFilesDir().getPath();
        File file = new File(directory, "monitoreo.json");

        //Verificar conexión
        // Si hay conexión a Internet en este momento se verifica si en txt existe, si es así, se envían, si no, solo se envían los que llegan del BL.
        /*ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mb = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(!wifi.isConnected() && !mb.isConnected()) {
            // No hay conexión a Internet en este momento. Entonces los datos se almacenan temporalmente en un txt.
            System.out.println("Se perdió la conexión");


            if(!file.exists()){
                try{
                    file.createNewFile();
                    storeData(file, data);

                }catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                storeData(file, data);
            }

        }else {
            //mSocket.connect();
            System.out.println("conectado!!");
            System.out.println(file.toString());
            //ArrayList<String> f = new ArrayList<>();*/

        if(file.exists()){
            jsArray = recoverData(file);
            jsArray.put(data);
            postCommunication(jsArray);
            System.out.println("Evento enviado");

        }else{
            jsArray.put(data);
            System.out.println("arreglo de datos:" + jsArray);
            postCommunication(jsArray);
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private JSONArray recoverData(File file) throws IOException {
        //ArrayList<String> f = new ArrayList<>();
        JSONArray js = new JSONArray();
        //byte[] f;
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(fis);
        BufferedReader bf = new BufferedReader(inputStreamReader);
        String line;
        JSONObject jsonLine = new JSONObject();

        while((line = bf.readLine())!=null) {
            try {
                jsonLine = new JSONObject(line);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            js.put(jsonLine);

            //mSocket.emit("my event", f);

        }
        System.out.println("Arraylist de recover data" + js);
        return js;

    }

    public void storeData(File file, JSONObject data) {

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            bw.write(String.valueOf(data));
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
    public void postCommunication(JSONArray d){
        //byte[] array = new byte[d.size()];
        //array = d.toArray(array);
        JSONObject value = new JSONObject();
        try {
            value = d.getJSONObject(d.length()-1);
        } catch (JSONException e){
            e.printStackTrace();
        }
        final JSONObject data = value;
        System.out.println("Estoy por acá!!");
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, String.valueOf(d));
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("fallo!! "+e);
                ContextWrapper c = new ContextWrapper(context);
                String directory = c.getFilesDir().getPath();
                File file = new File(directory, "monitoreo.json");
                if (e instanceof UnknownHostException || e instanceof  ConnectException){
                    if(!file.exists()){
                        try{
                            file.createNewFile();
                            storeData(file, data);

                        }catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }else{
                        storeData(file, data);
                    }
                }
                else if (e instanceof SocketTimeoutException){
                    if(file.exists()) {
                        file.delete();
                    }
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("Funciono! "+response);
                ContextWrapper c = new ContextWrapper(context);
                String directory = c.getFilesDir().getPath();
                File file = new File(directory, "monitoreo.json");
                if(file.exists()) {
                    file.delete();
                }
            }
        });

    }
}