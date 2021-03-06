package in.org.whistleblower.fragments;


import android.graphics.Typeface;
import android.os.Bundle;
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
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.adapters.ShareLocationListAdapter;
import in.org.whistleblower.dao.ShareLocationDao;
import in.org.whistleblower.models.ShareLocation;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.NavigationUtil;
import in.org.whistleblower.utilities.TransitionUtil;

public class ShareLocationListFragment extends android.support.v4.app.DialogFragment
{
    public static final String SHARE_LOC_LIST_EMPTY_TEXT = "SHARE_LOC_LIST_EMPTY_TEXT";
    public ShareLocationListFragment()
    {
    }

    @BindString(R.string.locationIsntBeingShared)
    String locationIsntBeingShared;

    @BindString(R.string.locationIsntBeingReceived)
    String locationIsntBeingReceived;

    @Bind(R.id.emptyList)
    ViewGroup emptyList;

    @Bind(R.id.emptyListTextView)
    TextView emptyListTextView;

    @Bind(R.id.shareLocationList)
    RecyclerView shareLocationListView;

    @Bind(R.id.sharing)
    TextView sharing;

    @Bind(R.id.receiving)
    TextView receiving;

    @BindColor(R.color.colorAccent)
    int colorAccent;

    @BindColor(R.color.transparent)
    int transparent;

    @BindColor(R.color.colorAccent)
    int enabled;

    @BindColor(R.color.divider)
    int disabled;

    @Bind(R.id.sharingHighlight)
    ViewGroup sharingHighlight;

    @Bind(R.id.receivingHighlight)
    ViewGroup receivingHighlight;

    private boolean isSharingClicked = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_share_location_list, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);

        TextView dialogTitle = (TextView) parentView.findViewById(R.id.dialogTitle);
        dialogTitle.setTypeface(WhistleBlower.getTypeface());
        onSharingClick();
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return parentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Bundle bundle = getArguments();
        if(bundle!=null && bundle.containsKey(NavigationUtil.FRAGMENT_TAG_RECEIVING_SHARED_LOCATION))
        {
            onReceivingClick();
        }
        else
        {
            onSharingClick();
        }
    }

    @Subscribe
    public void showEmptyListString(String msg)
    {
        if(msg.equals(SHARE_LOC_LIST_EMPTY_TEXT))
        {
            showEmptyListString();
            emptyListTextView.setText(isSharingClicked ? locationIsntBeingShared : locationIsntBeingReceived);
        }
    }


    private void showEmptyListString()
    {
        emptyList.setVisibility(View.VISIBLE);
        emptyListTextView.setText(isSharingClicked ? locationIsntBeingShared : locationIsntBeingReceived);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Otto.unregister(this);
    }

    @OnClick(R.id.closeDialog)
    public void close()
    {
        dismiss();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    @OnClick(R.id.sharing)
    public void onSharingClick()
    {
        emptyList.setVisibility(View.GONE);
        sharing.setTextColor(enabled);
        receiving.setTextColor(disabled);
        isSharingClicked = true;

        sharing.setTypeface(WhistleBlower.getTypeface(), Typeface.BOLD);
        receiving.setTypeface(WhistleBlower.getTypeface(), Typeface.NORMAL);

        sharingHighlight.setBackgroundColor(colorAccent);
        receivingHighlight.setBackgroundColor(transparent);

        ArrayList<ShareLocation> mShareLocationList = ShareLocationDao.getList();

        AppCompatActivity mActivity = (AppCompatActivity) getActivity();
        if(mShareLocationList.size() < 1)
        {
            showEmptyListString();
        }
        shareLocationListView.setAdapter(new ShareLocationListAdapter(mActivity, mShareLocationList));
        shareLocationListView.setLayoutManager(new LinearLayoutManager(mActivity));
    }

    @OnClick(R.id.receiving)
    public void onReceivingClick()
    {
        emptyList.setVisibility(View.GONE);
        isSharingClicked = false;
        sharing.setTextColor(disabled);
        receiving.setTextColor(enabled);

        sharing.setTypeface(WhistleBlower.getTypeface(), Typeface.NORMAL);
        receiving.setTypeface(WhistleBlower.getTypeface(), Typeface.BOLD);

        TransitionUtil.defaultTransition(receivingHighlight);
        sharingHighlight.setBackgroundColor(transparent);
        receivingHighlight.setBackgroundColor(colorAccent);

        //Fetch from notifications tables and show.

        ArrayList<ShareLocation> mShareLocationList = new ArrayList<>();
        AppCompatActivity mActivity = (AppCompatActivity) getActivity();
        if(mShareLocationList.size() < 1)
        {
            showEmptyListString();
        }
        shareLocationListView.setAdapter(new ShareLocationListAdapter(mActivity, mShareLocationList));
        shareLocationListView.setLayoutManager(new LinearLayoutManager(mActivity));
    }
}
