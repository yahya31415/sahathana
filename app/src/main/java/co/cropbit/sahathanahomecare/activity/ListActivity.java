package co.cropbit.sahathanahomecare.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import co.cropbit.sahathanahomecare.R;
import co.cropbit.sahathanahomecare.RequestAdapter;
import co.cropbit.sahathanahomecare.model.Request;

public class ListActivity extends AppCompatActivity {

    // Firebase API
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;

    // List view and adapters for Requests
    ListView requestListView;
    RequestAdapter adapter;

    // List of requests
    ArrayList<Request> requestList = new ArrayList<Request>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Get Firebase API Instance
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    setList(firebaseAuth.getCurrentUser());
                }
            }
        });
    }

    private void setList(FirebaseUser user) {
        adapter = new RequestAdapter(this, requestList);
        mDatabase.getReference("requests").child(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Request request = dataSnapshot.getValue(Request.class);
                Log.v("debug", request.toString());
                request.key = dataSnapshot.getKey();
                request.setApproved(mDatabase.getReference("users"), new Runnable() {
                    @Override
                    public void run() {
                        final Request r = request;
                        requestList.add(r);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Request request = dataSnapshot.getValue(Request.class);
                request.key = dataSnapshot.getKey();

                for(int i=0; i<requestList.size(); i++) {
                    if(requestList.get(i).key.equals(request.key)) {
                        requestList.remove(i);
                    }
                }

                requestList.add(request);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for(int i=0; i<requestList.size(); i++) {
                    if(requestList.get(i).key.equals(dataSnapshot.getKey())) {
                        requestList.remove(i);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                requestList.clear();
                adapter.notifyDataSetChanged();
            }
        });
        requestListView = (ListView) findViewById(R.id.request_list_home);
        requestListView.setAdapter(adapter);
    }
}
