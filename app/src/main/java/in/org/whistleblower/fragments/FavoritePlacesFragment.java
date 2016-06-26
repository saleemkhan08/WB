package in.org.whistleblower.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.adapters.PlaceAdapter;
import in.org.whistleblower.dao.FavPlacesDao;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.TransitionUtil;

public class FavoritePlacesFragment extends DialogFragment
{
    public static final String SHOW_FAV_PLACE_EMPTY_LIST = "SHOW_FAV_PLACE_EMPTY_LIST";

    @Bind(R.id.emptyList)
    ViewGroup emptyList;

    @Bind(R.id.favoritePlaceList)
    RecyclerView favPlacesRecyclerView;

    @Bind(R.id.emptyListTextView)
    TextView emptyListTextView;

    @BindString(R.string.allTheFavoritePlacesAreRemoved)
    String removedAllTheFavoritePlaces;

    public FavoritePlacesFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_fav_places_list, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        ArrayList<FavPlaces> favPlacesList = FavPlacesDao.getList();
        AppCompatActivity mActivity = (AppCompatActivity) getActivity();
        if (favPlacesList.size() < 1)
        {
            showEmptyListString();
        }
        favPlacesRecyclerView.setAdapter(new PlaceAdapter(mActivity, favPlacesList));
        favPlacesRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        TextView dialogTitle = (TextView) parentView.findViewById(R.id.dialogTitle);
        dialogTitle.setTypeface(WhistleBlower.getTypeface());
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return parentView;
    }

    @OnClick(R.id.closeDialog)
    public void close()
    {
        dismiss();
    }

    @Subscribe
    public void showEmptyListString(String msg)
    {
        if (msg.equals(SHOW_FAV_PLACE_EMPTY_LIST))
        {
            showEmptyListString();
            emptyListTextView.setText(removedAllTheFavoritePlaces);
        }
    }

    private void showEmptyListString()
    {
        TransitionUtil.defaultTransition(emptyList);
        emptyList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.post(MapFragment.DIALOG_DISMISS);
        Otto.unregister(this);
    }
}
