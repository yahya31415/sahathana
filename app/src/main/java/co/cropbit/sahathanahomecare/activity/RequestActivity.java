package co.cropbit.sahathanahomecare.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.cropbit.sahathanahomecare.R;
import co.cropbit.sahathanahomecare.model.Request;

public class RequestActivity extends AppCompatActivity {

    Activity mContext;

    RecyclerView recyclerView;

    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    public final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleApiClient mGoogleApiClient;

    FirebaseAuth mAuth;
    FirebaseDatabase database;

    List<Request> waiting_list = new ArrayList<Request>();

    String type;
    boolean isEmergency;

//    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        String[] titles = getResources().getStringArray(R.array.request_type_array);
        String[] infos = getResources().getStringArray(R.array.request_type_info_array);

        recyclerView = (RecyclerView) findViewById(R.id.request_type_list);
        RequestTypeAdapter adapter = new RequestTypeAdapter(this, titles, infos);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        // checkBox = (CheckBox) findViewById(R.id.emergency_check);

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
    }

//    public void send(View view) {
//
//    }
//
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
                            database.getReference("requests").child(mAuth.getCurrentUser().getUid()).push().setValue(request);
                            // TODO: requestFragment.setLoadingText("Request Sent", false);
                            waiting_list.clear();
                        }
                    }
                }
            });
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return  mLocationRequest;
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

    class RequestTypeAdapter extends RecyclerView.Adapter<RequestTypeAdapter.ViewHolder> {

        // Provide a direct reference to each of the views within a data item
        // Used to cache the views within the item layout for fast access
        public class ViewHolder extends RecyclerView.ViewHolder {
            // Your holder should contain a member variable
            // for any view that will be set as you render a row
            public TextView nameTextView;
            public TextView messageButton;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);

                nameTextView = (TextView) itemView.findViewById(R.id.rtitle);
                messageButton = (TextView) itemView.findViewById(R.id.rsub);
            }
        }

        String[] titles;
        String[] subtitles;

        Context context;

        RequestTypeAdapter(Context c, String[] a, String b[]) {
            context = c;
            titles = a;
            subtitles = b;
        }

        @Override
        public RequestTypeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View thisItemsView = LayoutInflater.from(context).inflate(R.layout.request_type_list_item,
                    parent, false);
            thisItemsView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    type = titles[recyclerView.indexOfChild(view)];
                    isEmergency = false;

                    CharSequence[] items = {"Check if this is an emergency request?"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(type)
                            .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                    isEmergency = b;
                                }
                            })
                            .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(mLastKnownLocation == null) {
                                        Request request = new Request();
                                        request.uid = mAuth.getCurrentUser().getUid();
                                        request.datetime = new Date().getTime();
                                        request.status = Request.SENT;
                                        request.type = type;
                                        request.isEmergency = isEmergency;
                                        waiting_list.add(request);
                                    } else {
                                        co.cropbit.sahathanahomecare.model.Location location = new co.cropbit.sahathanahomecare.model.Location(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                        Request request = new Request();
                                        request.uid = mAuth.getCurrentUser().getUid();
                                        request.location = location;
                                        request.datetime = new Date().getTime();
                                        request.type = type;
                                        request.isEmergency = isEmergency;
                                        database.getReference("requests").child(mAuth.getCurrentUser().getUid()).push().setValue(request).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent intent = new Intent(mContext, SentActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            }
                                        });
                                        // TODO: requestFragment.setLoadingText("Request Sent", false);
                                    }
                                }
                            });
                    Dialog dialog = builder.create();
                    dialog.show();
                }
            });
            return new ViewHolder(thisItemsView);
        }

        @Override
        public void onBindViewHolder(RequestTypeAdapter.ViewHolder holder, int position) {
            // Set item views based on your views and data model
            TextView textView = holder.nameTextView;
            textView.setText(titles[position]);
            TextView textView1 = holder.messageButton;
            textView1.setText(subtitles[position]);
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }
    }



}
