package com.google.mygooglemaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(MapsActivity.this, "Map is ready.....", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onMapReady: Map is redy...");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.getUiSettings().isScrollGesturesEnabled();
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().isCompassEnabled();
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.getUiSettings().setIndoorLevelPickerEnabled(true);

            init();
        }
    }

    private static final String TAG = "MapsActivity";

    private static final String FINE_LOCATION=Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION=Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE=1234;
    private static final LatLngBounds LAT_LNG_BOUNDS =new LatLngBounds(new LatLng(-40, -168),new LatLng(71,136));

    //vars
    public static final float DEFAULT_ZOOM=15f;
   private boolean mLocationPermissionGranted=false;
   private GoogleApiClient mgoogleApiClient;

    public FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private PlaceAutocompleteAdapter mplaceAutocompleteAdapter;

 //widgets
private AutoCompleteTextView search_edit;
private ImageView mGps;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        search_edit=findViewById(R.id.input_search);
        mGps=findViewById(R.id.ic_gps);

        getLocationPermission();


        }

    private void init(){

        Log.d(TAG, "init: intializing.....");

        mgoogleApiClient=new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API).
                        addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this,this)
                .build();

        mplaceAutocompleteAdapter=new PlaceAutocompleteAdapter(this, mgoogleApiClient , LAT_LNG_BOUNDS, null);
        search_edit.setAdapter(mplaceAutocompleteAdapter);


        //if we Clicked Enter ....to start search...

      search_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
              if (actionId== EditorInfo.IME_ACTION_SEARCH || actionId==EditorInfo.IME_ACTION_DONE
              || event.getAction()==KeyEvent.ACTION_DOWN || event.getAction()==KeyEvent.KEYCODE_ENTER){

                       geoLocate();
              }
              return false;
          }
      });

 HideSoftKeyword();
    }
 // to search specified location........
    private void geoLocate() {
        Log.d(TAG, "geoLocate:  geolocating...");
        String Search_Text=search_edit.getText().toString();
  //to get all addresses according to Search Text I looked for...........
        Geocoder geocoder=new Geocoder(MapsActivity.this);
        List<Address>addressList=new ArrayList<>();

        try {

        addressList=geocoder.getFromLocationName(Search_Text ,1);

        }catch (IOException  e){
            Log.d(TAG, "geoLocate: IOException "+e.getMessage());
        }
        if (addressList.size() >0){
            Address address=addressList.get(0);
            Log.d(TAG, "geoLocate:  Found An Location "+address.toString());
           // Toast.makeText(MapsActivity.this," "+address.toString(),Toast.LENGTH_LONG).show();
            MoveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));
        }
        
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the Current  Device Location....");

        mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted){
         Task location=mFusedLocationProviderClient.getLastLocation();

             location.addOnCompleteListener(new OnCompleteListener() {
                 @Override
                 public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Log.d(TAG, "onComplete: found location..");
                         Location  currentLocation = (Location) task.getResult();

                            MoveCamera(new LatLng(30.0444196 , 31), DEFAULT_ZOOM,"My Location");
              }
                    else{
                        Log.d(TAG, "onComplete: current location is null");
                       // Toast.makeText(MapsActivity.this,"unable to get current location ",Toast.LENGTH_LONG).show();
                    }
                 }
             });

        }}catch (SecurityException e){
            Log.d(TAG, "getDeviceLocation: SecurityException "+e.getMessage());
        }
        
    }

private void MoveCamera(LatLng latLng ,float zoom ,String title) {
    Log.d(TAG, "MoveCamera: move Camera to lat " + latLng.latitude + ", lng: " + latLng.longitude);

        mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(latLng, zoom));
              if (!title.equals("My Location")){
        MarkerOptions options=new MarkerOptions().position(latLng)
                .title(title);
        mMap.addMarker(options);

}
    HideSoftKeyword();
    }
    private void intiMap(){
        Log.d(TAG, "intiMap: intialize our Map....");
        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(MapsActivity.this);
    }
    private void getLocationPermission(){

        Log.d(TAG, "getLocationPermission: Getting Location Permission...");
        String[]permissions={
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted=true;
                intiMap();
            }
            else{
                ActivityCompat.requestPermissions(this,permissions,
                     LOCATION_PERMISSION_REQUEST_CODE );
            }
        }
        else{
            ActivityCompat.requestPermissions(this,permissions,
                    LOCATION_PERMISSION_REQUEST_CODE );
        }
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick:  clicked gps icon...");
                getDeviceLocation();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted=false;
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE : {
                if (grantResults.length > 0){
                    for (int i=0;i<grantResults.length ;i++){
                        if (grantResults[i] !=PackageManager.PERMISSION_GRANTED){
                            Log.d(TAG, "onRequestPermissionsResult: Location Permission Failed..");
                            mLocationPermissionGranted=false;
                            return;
                        }
                    }
                    Log.e(TAG, "onRequestPermissionsResult: Location Permission Granted...");
                    mLocationPermissionGranted=true;
                    intiMap();
                }
            }

        }
    }
    private void HideSoftKeyword(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }



}
