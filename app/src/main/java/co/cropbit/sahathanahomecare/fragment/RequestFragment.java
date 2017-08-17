package co.cropbit.sahathanahomecare.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;

import co.cropbit.sahathanahomecare.R;
import co.cropbit.sahathanahomecare.RequestAdapter;
import co.cropbit.sahathanahomecare.model.Request;

public class RequestFragment extends Fragment {

    public final static String LOADING_TEXT_TAG = "loading_text";
    public final static String SHOW_PROGRESS_TAG = "show_progress";

    ListView requestListView;
    RequestAdapter adapter;

    ArrayList<Request> requestList = new ArrayList<Request>();

    FirebaseDatabase database;
    FirebaseAuth auth;

    public RequestFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static RequestFragment newInstance(String loadingText, boolean showProgress, FirebaseDatabase database, FirebaseAuth auth) {
        RequestFragment fragment = new RequestFragment();
        Bundle bundle = new Bundle();
        bundle.putString(LOADING_TEXT_TAG, loadingText);
        bundle.putBoolean(SHOW_PROGRESS_TAG, showProgress);
        fragment.database = database;
        fragment.auth = auth;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request, container, false);
    }

    public void onResume() {
        super.onResume();

        TextView loading_text_view = (TextView) getActivity().findViewById(R.id.loading_text);
        loading_text_view.setText(getArguments().getString(LOADING_TEXT_TAG));
        ContentLoadingProgressBar contentLoadingProgressBar = (ContentLoadingProgressBar) getActivity().findViewById(R.id.indicator);
        contentLoadingProgressBar.setVisibility(getArguments().getBoolean(SHOW_PROGRESS_TAG) ? View.VISIBLE : View.INVISIBLE);

        adapter = new RequestAdapter(getContext(), requestList);

        database.getReference("request").child(auth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Request request = dataSnapshot.getValue(Request.class);
                request.key = dataSnapshot.getKey();
                Log.v("added", request.key);
                requestList.add(request);
                adapter.notifyDataSetChanged();
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

            }
        });
        requestListView = (ListView) getActivity().findViewById(R.id.request_list);
        requestListView.setAdapter(adapter);
    }

    public void setLoadingText(String lt, boolean b) {
        TextView loading_text_view = (TextView) getActivity().findViewById(R.id.loading_text);
        loading_text_view.setText(lt);
        ContentLoadingProgressBar contentLoadingProgressBar = (ContentLoadingProgressBar) getActivity().findViewById(R.id.indicator);
        contentLoadingProgressBar.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
    }
}
