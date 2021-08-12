package com.web.app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    static WebView webView;
    WebSettings webSettings;
    private static final String webLink = "https://www.google.com";
    Socket socket = null;
    OutputStream output = null;
    InputStream input = null;

    private static String bytesToHex(byte[] bytes) {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    @JavascriptInterface
    public void showToast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    @JavascriptInterface
    public int Connect(String ip, int port) {
        int retVal = 1;
        try {
            socket = new Socket(ip, port);
            output = socket.getOutputStream();
            input = socket.getInputStream();
        } catch (IOException e) {
            retVal = 0;
            e.printStackTrace();
        }
        return retVal;
    }

    @JavascriptInterface
    public int Send(byte[] data) {
        int retVal = 1;
        try {
            if ((output != null) && (data != null)) {
                output.write(data);
                output.flush();
            }
        } catch (IOException e) {
            retVal = 0;
            e.printStackTrace();
        }
        return retVal;
    }

    @JavascriptInterface
    public String Recv() {
        byte[] buff = new byte[1024];
        int count = 0;
        try {
            if ((input != null) && (input.available() > 0))
            {
                count = input.read(buff);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = "";
        if (count > 0) {
            str = bytesToHex(Arrays.copyOfRange(buff, 0, count));
        }
        return str;
    }

    public static void ClearCookies()
    {
        android.webkit.CookieManager cookieManager = CookieManager.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
        }
        else {
            cookieManager.removeAllCookie();
        }
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        webView=(WebView)findViewById(R.id.webView);
        assert webView != null;

        webView.clearCache(true);
        ClearCookies();

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        final String androidInterfaceName = "android";
        webView.addJavascriptInterface(this, androidInterfaceName);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        webView.setLongClickable(false);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.loadUrl(webLink);
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
