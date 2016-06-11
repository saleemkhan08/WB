package in.org.whistleblower.fragments;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.adapters.ShareLocationListAdapter;
import in.org.whistleblower.models.ShareLocation;
import in.org.whistleblower.dao.ShareLocationDao;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.NavigationUtil;

public class ShareLocationListFragment extends android.support.v4.app.DialogFragment
{
    public static final String SHARE_LOC_LIST_EMPTY_TEXT = "SHARE_LOC_LIST_EMPTY_TEXT";
    public ShareLocationListFragment()
    {
    }

    @BindString(R.string.locationIsntBeingShared)
    String locationIsntBeingShared;

    @Bind(R.id.emptyList)
    ViewGroup emptyList;

    @Bind(R.id.emptyListTextView)
    TextView emptyListTextView;

    @Bind(R.id.shareLocationList)
    RecyclerView shareLocationListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_share_location_list, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);

        TextView dialogTitle = (TextView) parentView.findViewById(R.id.dialogTitle);
        dialogTitle.setTypeface(WhistleBlower.getTypeface());

        ArrayList<ShareLocation> mShareLocationList = ShareLocationDao.getList();
        AppCompatActivity mActivity = (AppCompatActivity) getActivity();
        if(mShareLocationList.size() < 1)
        {
            showEmptyListString();
        }
        shareLocationListView.setAdapter(new ShareLocationListAdapter(mActivity, mShareLocationList));
        shareLocationListView.setLayoutManager(new LinearLayoutManager(mActivity));
        return parentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Bundle bundle = getArguments();
        if(bundle.containsKey(NavigationUtil.SHARE_LOCATION_RECEIVING_FRAGMENT_TAG))
        {
            //show receiving Tab
        }
        else
        {
            //show sending Tab
        }
    }

    @Subscribe
    public void showEmptyListString(String msg)
    {
        if(msg.equals(SHARE_LOC_LIST_EMPTY_TEXT))
        {
            showEmptyListString();
            emptyListTextView.setText(locationIsntBeingShared);
        }
    }


    private void showEmptyListString()
    {
        TransitionManager.beginDelayedTransition(emptyList);
        emptyList.setVisibility(View.VISIBLE);
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
}
