
package com.example.android.walkmyandroid;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements OnTaskCompleteListener {

    public static final int REQUEST_LOCATION_CODE = 1;

    private Button mTrackLocationButton;
    private Button mGetLocationButton;
    private TextView mLocationTextView;
    private ImageView mAndroidImageView;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private boolean mTrackingLocation = false;

    private LocationCallback mlocationCallback;

    private AnimatorSet mRotateAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTrackLocationButton = (Button) findViewById(R.id.button_location2);
        mLocationTextView = (TextView) findViewById(R.id.textview_location);
        mAndroidImageView = (ImageView) findViewById(R.id.imageview_android);
        mGetLocationButton = (Button) findViewById(R.id.button_location);

        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.rotate);

        mGetLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        mlocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                new FetchAddressTask(MainActivity.this).execute(locationResult.getLastLocation());
            }
        };

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mRotateAnim.setTarget(mAndroidImageView);


        mTrackLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mTrackingLocation) {
                    Observable.interval(3, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.newThread())
                            .map(new Function<Long, Integer>() {
                                @Override
                                public Integer apply(Long l) throws Exception {
                                    return 0;
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Integer integer) {
                                    startTrackingLocation();
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                } else {
                    stopTrackingLocation();
                }
            }
        });


    }

    private void startTrackingLocation() {

        mRotateAnim.start();
        mTrackingLocation = true;

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission_group.LOCATION}, REQUEST_LOCATION_CODE);
        } else {
            mFusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), mlocationCallback, null);
        }

    }

    private void stopTrackingLocation() {

        mRotateAnim.cancel();
        mTrackingLocation = false;

        mFusedLocationProviderClient.removeLocationUpdates(mlocationCallback);

    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission_group.LOCATION}, REQUEST_LOCATION_CODE);
        } else {
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {

                        new FetchAddressTask(MainActivity.this).execute(location);

                        mLocationTextView.setText(getString(R.string.location_text,
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getTime()));

                    } else {
                        mLocationTextView.setText("Last known position not found");
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(this, "User denied permission access", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onTaskComplete(String result) {
        stopTrackingLocation();

        mLocationTextView.setText(result);
    }
}
