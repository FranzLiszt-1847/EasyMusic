package com.franz.easymusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.franz.easymusicplayer.callback.MusicInfo;
import com.franz.easymusicplayer.utils.HttpUtil;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivityLog";
    private static final String URL = "https://dselegent-music.vercel.app/homepage/dragon/ball";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HttpUtil.INSTANCE.getMusicInfo(URL, new MusicInfo() {
            @Override
            public void onRespond(String json) {
                Log.d(TAG,json.toString());
            }

            @Override
            public void onFailed(Exception e) {
                Log.d(TAG,e.getMessage());
                e.printStackTrace();
            }
        });
    }
}