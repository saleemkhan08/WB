package in.org.whistleblower.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.org.whistleblower.FriendListActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.adapters.CommonUserListAdapter;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.AccountsDao;
import in.org.whistleblower.models.ShareLocation;
import in.org.whistleblower.models.ShareLocationDao;
import in.org.whistleblower.services.LocationTrackingService;
import in.org.whistleblower.singletons.Otto;

public class ShareLocationFragment extends DialogFragment
{
    AppCompatActivity mActivity;
    RecyclerView shareLocationFriendList;
    ArrayList<Accounts> mFriendList;
    @Bind(R.id.continuously)
    RadioButton continuously;

    @Bind(R.id.justOnce)
    RadioButton justOnce;

    private SharedPreferences preferences;

    public ShareLocationFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_share_location, container, false);
        ButterKnife.bind(this, parentView);


        mActivity = (AppCompatActivity) getActivity();
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        shareLocationFriendList = (RecyclerView) parentView.findViewById(R.id.shareLocationFriendList);
        Otto.register(this);
        AccountsDao dao = new AccountsDao(mActivity);
        mFriendList = dao.getFriendsList();

        CommonUserListAdapter mAdapter = new CommonUserListAdapter(mActivity, mFriendList);
        shareLocationFriendList.setLayoutManager(new LinearLayoutManager(mActivity));
        shareLocationFriendList.setAdapter(mAdapter);
        return parentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if(mFriendList.size() < 1)
        {
            dismiss();
            if(!preferences.getBoolean(FriendListActivity.KEY_USERS_FETCHED, false))
            {
                Toast.makeText(mActivity, "Please add friends to share location", Toast.LENGTH_SHORT).show();
            }
            mActivity.startActivity(new Intent(mActivity, FriendListActivity.class));
        }
    }

    @Subscribe
    public void shareLocation(Accounts account)
    {
        dismiss();
        ShareLocation location = new ShareLocation();
        location.email = preferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com");
        location.userEmail = account.email;
        location.photoUrl = preferences.getString(Accounts.PHOTO_URL, "");
        location.name = preferences.getString(ShareLocation.NAME, "Saleem");
        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        intent.putExtra(ShareLocation.LOCATION, location);

        if (continuously.isChecked())
        {
            ShareLocationDao dao = new ShareLocationDao(mActivity);
            intent.putExtra(LocationTrackingService.KEY_SHARE_LOCATION_REAL_TIME, true);

            location.name = account.name;
            location.photoUrl = account.photo_url;

            dao.insert(location);
            Log.d("shareLocation", "continuously : checked");
        }
        else
        {
            intent.putExtra(LocationTrackingService.KEY_SHARE_LOCATION, true);
            Log.d("shareLocation", "just once : checked");
        }

        mActivity.startService(intent);
        Log.d("shareLocation", "startService");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Otto.unregister(this);
    }

    @OnClick(R.id.continuously)
    public void onContinuouslyClick()
    {
        justOnce.setChecked(false);
    }

    @OnClick(R.id.justOnce)
    public void onJustOnceClick()
    {
        continuously.setChecked(false);
    }
}
