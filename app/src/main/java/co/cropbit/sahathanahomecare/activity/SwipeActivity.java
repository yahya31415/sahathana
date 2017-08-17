package co.cropbit.sahathanahomecare.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.cropbit.sahathanahomecare.R;
import co.cropbit.sahathanahomecare.fragment.RequestFragment;
import co.cropbit.sahathanahomecare.fragment.SwipeFragment;
import co.cropbit.sahathanahomecare.model.Request;

public class SwipeActivity extends AppCompatActivity {

    ViewPager mViewPager;
    PagerAdapter pagerAdapter;

    SwipeFragment swipeFragment;
    RequestFragment requestFragment;

    FirebaseAuth mAuth;
    FirebaseDatabase database;

    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;

    private GoogleApiClient mGoogleApiClient;

    public final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    Activity mContext;

    List<Request> waiting_list = new ArrayList<Request>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);

        mContext = this;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getLocation();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(intent);
                }
            }
        });

        mViewPager = (ViewPager) findViewById(R.id.homePager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        swipeFragment = SwipeFragment.newInstance();
        requestFragment = RequestFragment.newInstance("", false, database, mAuth);

        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d("test", Integer.toString(position));
                if(position == 1) {
                    requestFragment.setLoadingText("Sending Request", true);

                    if(mLastKnownLocation == null) {
                        Request request = new Request();
                        request.uid = mAuth.getCurrentUser().getUid();
                        request.datetime = new Date().getTime();
                        request.status = Request.SENT;
                        waiting_list.add(request);
                    } else {
                        co.cropbit.sahathanahomecare.model.Location location = new co.cropbit.sahathanahomecare.model.Location(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                        Request request = new Request();
                        request.uid = mAuth.getCurrentUser().getUid();
                        request.location = location;
                        request.datetime = new Date().getTime();
                        database.getReference("request").child(mAuth.getCurrentUser().getUid()).push().setValue(request);
                        requestFragment.setLoadingText("Request Sent", false);
                    }
                } else {
                    requestFragment.setLoadingText(" ", false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        PagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return swipeFragment;
                case 1:
                    return requestFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return  mLocationRequest;
    }

    private void getLocation () {
        if (ContextCompat.checkSelfPermission(mContext.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(mContext,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if(mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mLastKnownLocation = location;
                    if(waiting_list.size() > 0) {
                        for(int i=0; i<waiting_list.size(); i++) {
                            Request request = waiting_list.get(i);
                            co.cropbit.sahathanahomecare.model.Location loc = new co.cropbit.sahathanahomecare.model.Location(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            request.location = loc;
                            database.getReference("request").child(mAuth.getCurrentUser().getUid()).push().setValue(request);
                            requestFragment.setLoadingText("Request Sent", false);
                            waiting_list.clear();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getLocation();
                }
            }
        }
    }
}
