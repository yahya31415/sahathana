package co.cropbit.sahathanahomecare.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import co.cropbit.sahathanahomecare.R;
import co.cropbit.sahathanahomecare.RequestAdapter;
import co.cropbit.sahathanahomecare.model.Hospital;
import co.cropbit.sahathanahomecare.model.Request;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Google Map
    GoogleMap mMap;

    // Firebase API
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;

    // Current User
    private FirebaseUser currentUser;

    // Hospitals list
    private List<Hospital> hospitals = new ArrayList<Hospital>();

    // Google API
    private GoogleApiClient mGoogleApiClient;

    // Device location
    private Location mLastKnownLocation;

    // Map defaults
    private final int DEFAULT_ZOOM = 13;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);

    // Current activity
    private Activity mContext;

    // Constants
    public final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // Do if logged in
    private void loggedIn (FirebaseUser user) {

        // Set current user
        currentUser = user;

        // Change button text and action
        Button button = (Button) findViewById(R.id.profile_action);
        button.setText("Logout");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
            }
        });

        Button gButton = (Button) findViewById(R.id.gotoRequestsButton);
        gButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent listIntent = new Intent(mContext, ListActivity.class);
                startActivity(listIntent);
            }
        });

        // Change profile details
        getTextView(R.id.profile_email).setText(currentUser.getEmail());
        DatabaseReference ref = mDatabase.getReference("user").child(currentUser.getUid());
        DatabaseReference nameRef = ref.child("name");
        DatabaseReference phnoRef = ref.child("phno");
        nameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getTextView(R.id.profile_name).setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        phnoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getTextView(R.id.profile_phno).setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Do if not logged in
    private void notLoggedIn () {
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // Set location permission granted flag
    private boolean locationPermissionGranted () {
        if (ContextCompat.checkSelfPermission(mContext.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            return true;
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            ActivityCompat.requestPermissions(mContext,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Get Map
        mapFragment.getMapAsync(this);

        // Get Firebase API Instance
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get activity
        mContext = this;

        // Inititate Google API Client Request
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if(locationPermissionGranted()) {
                            try {
                                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
                                        if(mMap == null) {
                                            // Map not loaded
                                            return;
                                        }
                                        if(mLastKnownLocation != null) {
                                            // Already centered to location
                                        } else {
                                            // Getting location first time
                                            mLastKnownLocation = location;
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),
                                                    mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                                        }
                                    }
                                });
                            } catch (SecurityException e) {

                            }
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
    }

    public void onResume() {
        super.onResume();
        // Check auth state
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    loggedIn(firebaseAuth.getCurrentUser());
                } else {
                    notLoggedIn();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getHospitals();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    private void getDeviceLocation() {
        if(locationPermissionGranted()) {
            try {
                mLastKnownLocation = LocationServices.FusedLocationApi
                        .getLastLocation(mGoogleApiClient);
                if(mLastKnownLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            } catch (SecurityException e) {

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }

    private void drawMarkers() {
        for(int i=0; i<hospitals.size(); i++) {
            Hospital hospital = hospitals.get(i);
            LatLng latLng = new LatLng(hospital.location.lat, hospital.location.lng);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(hospital.name);
            mMap.addMarker(markerOptions);
        }
    }

    private void getHospitals() {
        DatabaseReference ref = mDatabase.getReference("hospital");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Hospital hospital = dataSnapshot.getValue(Hospital.class);
                hospitals.add(hospital);
                drawMarkers();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return  mLocationRequest;
    }

    public void request(View view) {
        Intent intent = new Intent(this, SwipeActivity.class);
        startActivity(intent);
    }

    private TextView getTextView(@IdRes int id) {
        return ((TextView) findViewById(id));
    }
}
