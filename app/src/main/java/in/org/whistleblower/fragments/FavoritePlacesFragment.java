package in.org.whistleblower.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.org.whistleblower.R;
import in.org.whistleblower.adapters.PlaceAdapter;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.models.FavPlacesDao;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.MiscUtil;

public class FavoritePlacesFragment extends Fragment
{
    public static final String SHOW_FAV_PLACE_INTRO_CARD = "SHOW_FAV_PLACE_INTRO_CARD";
    @Bind(R.id.favPlaceIntroCard)
    ViewGroup favPlaceIntroCard;

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
        View parentView = inflater.inflate(R.layout.fragment_favorite_places, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        return parentView;
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
            if (null != favPlacesList)
            {
                favPlacesRecyclerView = (RecyclerView) mActivity.findViewById(R.id.favoritePlaceList);
                adapter = new PlaceAdapter(mActivity, favPlacesList);
                favPlacesRecyclerView.setAdapter(adapter);
                favPlacesRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
                if (favPlacesList.size() < 1)
                {
                    showFavPlaceIntroCard();
                }
                else
                {
                    hideFavPlaceIntroCard();
                }
            }
        }
    }

    private void hideFavPlaceIntroCard()
    {
        TransitionManager.beginDelayedTransition(favPlaceIntroCard, new Slide());
        favPlaceIntroCard.setVisibility(View.GONE);
    }

    @Subscribe
    public void showFavPlaceIntroCard(String action)
    {
        if(action.equals(SHOW_FAV_PLACE_INTRO_CARD))
        {
            showFavPlaceIntroCard();
        }
    }
    private void showFavPlaceIntroCard()
    {
        TransitionManager.beginDelayedTransition(favPlaceIntroCard, new Slide());
        favPlaceIntroCard.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Otto.unregister(this);
    }
}
