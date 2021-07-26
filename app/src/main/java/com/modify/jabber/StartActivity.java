package com.modify.jabber;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {
    Button login, register;
    FirebaseUser firebaseUser;
    private static final int REQUEST = 112;
    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(isConnected()) {
            //check if user is null
            if (firebaseUser != null) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
        else {
            firebaseUser = null;
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(StartActivity.this);
            builder.setMessage("Please connect to internet to use Jabber.");
            builder.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        login = findViewById(R.id.login);
        register = findViewById(R.id.register);

        if (Build.VERSION.SDK_INT >= 23)
        {
            String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.INTERNET};
            if (!hasPermissions(StartActivity.this, PERMISSIONS))
            {
                ActivityCompat.requestPermissions((Activity) StartActivity.this, PERMISSIONS, REQUEST );
            }
            else
            {
                //do here
            }
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, SigninActivity.class));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, SignupActivity.class));
            }
        });
    }
    public boolean isConnected()
    {
        boolean result;
        try {
            @SuppressLint({"NewApi", "LocalSuppress"}) ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            result = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
        }
        catch (Exception e)
        {
            result = false;
        }
        return result;
    }
    private boolean hasPermissions(Context context, String... permissions)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}