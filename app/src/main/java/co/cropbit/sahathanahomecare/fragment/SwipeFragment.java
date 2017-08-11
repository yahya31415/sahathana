package co.cropbit.sahathanahomecare.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.cropbit.sahathanahomecare.R;

public class SwipeFragment extends Fragment {


    public SwipeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SwipeFragment newInstance() {
        SwipeFragment fragment = new SwipeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_swipe, container, false);
    }

}
