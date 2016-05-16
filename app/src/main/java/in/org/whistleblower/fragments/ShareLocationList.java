package in.org.whistleblower.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.org.whistleblower.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareLocationList extends Fragment
{


    public ShareLocationList()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_share_location_list, container, false);
    }

}
