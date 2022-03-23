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

import com.android.volley.Cache;
import com.android.volley.Network;
//import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
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
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
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
        // Instantiate the RequestQueue with the cache and network.
        //Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
       //Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        //mRequestQueue = new RequestQueue(cache, network);
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

            System.out.println(file.toString());
            if(file.exists()){
                recoverData(file);
                file.delete();
                System.out.println("Evento enviado");
            }
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
/*


            // Start the queue
            mRequestQueue.start();


            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Handle error
                        }
                    });
            mRequestQueue.add(stringRequest);*/
            //mSocket.emit("my event", d);
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
            //mSocket.emit("my event", f);

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