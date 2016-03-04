package in.org.whistleblower.fragments;


import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import in.org.whistleblower.R;
import in.org.whistleblower.adapters.PlaceAdapter;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.models.FavPlacesDao;
import in.org.whistleblower.utilities.MiscUtil;

public class FavoritePlacesFragment extends Fragment
{
    public FavoritePlacesFragment()
    {
    }
    RecyclerView favPlacesRecyclerView;
    ArrayList<FavPlaces> favPlacesList;
    AppCompatActivity mActivity;
    PlaceAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_favorite_places, container, false);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mActivity = (AppCompatActivity) getActivity();
        new LocalDataTask().execute();
    }

    private class LocalDataTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            MiscUtil.log("LocalDataTask : onPreExecute");
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            MiscUtil.log("LocalDataTask : doInBackground");
            favPlacesList = new FavPlacesDao(mActivity).getFavPlacesList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            if(null != favPlacesList)
            {
                favPlacesRecyclerView = (RecyclerView) mActivity.findViewById(R.id.favoritePlaceList);
                adapter = new PlaceAdapter(mActivity, favPlacesList);
                favPlacesRecyclerView.setAdapter(adapter);
                favPlacesRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
            }
        }
    }

}
