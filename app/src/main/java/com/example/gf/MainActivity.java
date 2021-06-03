package com.example.gf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    TextView tv_lat1, tv_lon1, tv_altitude1, tv_accuracy1, tv_speed1, tv_sensor1, tv_address1, tv_updates1,tv_countOfCrumbs;
    Button btn_newWayPoint,btn_showWayPointList,btn_showMap;

    Switch sw_gps, sw_updates3;

    boolean updateOn = false;

    Location currentLocation;

    List<Location> savedLocations;

    LocationRequest locationRequest;

    LocationCallback locationCallBack;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat1 = findViewById(R.id.tv_lat1);
        tv_lon1 = findViewById(R.id.tv_lon1);
        tv_altitude1 = findViewById(R.id.tv_altitude1);
        tv_accuracy1 = findViewById(R.id.tv_accuracy1);
        tv_speed1 = findViewById(R.id.tv_speed1);
        tv_sensor1 = findViewById(R.id.tv_sensor1);
        tv_updates1 = findViewById(R.id.tv_updates1);
        tv_address1 = findViewById(R.id.tv_address1);
        sw_gps = findViewById(R.id.sw_gps);
        sw_updates3 = findViewById(R.id.sw_updates3);
        btn_newWayPoint=findViewById(R.id.btn_newWayPoint);
        btn_showWayPointList=findViewById(R.id.btn_showWayPointList);
        tv_countOfCrumbs=findViewById(R.id.tv_countOfCrumbs);
        btn_showMap=findViewById(R.id.btn_showMap);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack=new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location=locationResult.getLastLocation();
                updateUIValues(location);
            }
        };

        btn_showWayPointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainActivity.this,ShowSavedLocationList.class);
                startActivity(i);
            }
        });

        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainActivity.this,MapsActivity.class);
                startActivity(i);
            }
        });


        btn_newWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication myApplication=(MyApplication)getApplicationContext();
                savedLocations=myApplication.getMyLocations();
                savedLocations.add(currentLocation);
            }
        });

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor1.setText("using gps sensors");
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor1.setText("using towers and wifi");
                }
            }
        });

        sw_updates3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_updates3.isChecked()) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();
    }//end of onCreate

    private void startLocationUpdates() {
        tv_updates1.setText("Location is  being tracked");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack,null);
        updateGPS();

    }//end of startLocation

    private void stopLocationUpdates() {
        tv_updates1.setText("Location is not being tracked");
        tv_lat1.setText("Not tracking location");
        tv_lon1.setText("Not tracking location");
        tv_speed1.setText("Not tracking location");
        tv_address1.setText("Not tracking location");
        tv_accuracy1.setText("Not tracking location");
        tv_altitude1.setText("Not tracking location");
        tv_sensor1.setText("Not tracking location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }//end of stopLocation


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }//end of onRequest

    private void updateGPS() {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        updateUIValues(location);
                        currentLocation=location;
                    }
                });
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
                }
            }
        }//end of updateGPS


    private void updateUIValues(Location location) {
        tv_lat1.setText(String.valueOf(location.getLatitude()));
        tv_lon1.setText(String.valueOf(location.getLongitude()));
        tv_accuracy1.setText(String.valueOf(location.getAccuracy()));

        if(location.hasAltitude())
        {
            tv_altitude1.setText(String.valueOf(location.getAltitude()));
        }
        else
        {
            tv_altitude1.setText("altitude not available");
        }

        if(location.hasSpeed())
        {
            tv_speed1.setText(String.valueOf(location.getSpeed()));
        }
        else
        {
            tv_speed1.setText("speed not available");
        }

        Geocoder geocoder=new Geocoder(MainActivity.this);
        try {
            List<Address> addresses=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            tv_address1.setText(addresses.get(0).getAddressLine(0));
        }
        catch (Exception e)
        {
           tv_address1.setText("unable to get street address");
        }

        MyApplication myApplication=(MyApplication)getApplicationContext();
        savedLocations=myApplication.getMyLocations();

        tv_countOfCrumbs.setText(Integer.toString(savedLocations.size()));
    }//end of updateUI

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        if(item.getItemId()==R.id.language)
        {
            Intent languageIntent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
            startActivity(languageIntent);
        }
        return super.onOptionsItemSelected(item);
    }
}//end of main


