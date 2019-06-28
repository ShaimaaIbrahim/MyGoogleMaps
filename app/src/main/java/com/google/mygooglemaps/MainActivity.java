package com.google.mygooglemaps;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST=9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isServicesOk()){
            init();
        }
    }


    private void init(){
        Button button=findViewById(R.id.btn_maps);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,MapsActivity.class));
            }
        });
    }
public boolean isServicesOk(){
    Log.d(TAG, "isServicesOk:  checking if services ok..");

    int available= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

    if (available== ConnectionResult.SUCCESS){
        Log.d(TAG, "isServicesOk: Google play services is working ");
        return true;
    }
    else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
        Log.d(TAG, "isServicesOk: an error occured and we can resolve it");
        Dialog dialog=GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
        dialog.show();
    }
    else{
     Toast.makeText(MainActivity.this, "we can not make maps request ", Toast.LENGTH_LONG).show();
    }
    return false;
}

}
