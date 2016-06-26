package in.org.whistleblower.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.adapters.CommonUserListAdapter;
import in.org.whistleblower.dao.AccountsDao;
import in.org.whistleblower.dao.ShareLocationDao;
import in.org.whistleblower.interfaces.ResultListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.Notifications;
import in.org.whistleblower.models.ShareLocation;
import in.org.whistleblower.services.LocationTrackingService;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.TransitionUtil;
import in.org.whistleblower.utilities.VolleyUtil;

public class ShareLocationFragment extends DialogFragment
{
    AppCompatActivity mActivity;
    RecyclerView shareLocationFriendList;
    ArrayList<Accounts> mFriendList;
    @Bind(R.id.continuously)
    TextView continuously;

    @Bind(R.id.justOnce)
    TextView justOnce;

    @BindColor(R.color.colorAccent)
    int colorAccent;

    @BindColor(R.color.transparent)
    int transparent;

    @Bind(R.id.continuouslyHighlight)
    ViewGroup continuouslyHighlight;

    @Bind(R.id.justOnceHighlight)
    ViewGroup justOnceHighlight;

    private SharedPreferences preferences;
    private boolean isContinuouslyClicked = true;

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
        mFriendList = AccountsDao.getFriendsList();

        TextView dialogTitle = (TextView) parentView.findViewById(R.id.dialogTitle);
        dialogTitle.setTypeface(WhistleBlower.getTypeface());

        continuously.setTypeface(WhistleBlower.getTypeface(), Typeface.BOLD);
        justOnce.setTypeface(WhistleBlower.getTypeface());

        CommonUserListAdapter mAdapter = new CommonUserListAdapter(mActivity, mFriendList);
        shareLocationFriendList.setLayoutManager(new LinearLayoutManager(mActivity));
        shareLocationFriendList.setAdapter(mAdapter);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return parentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (mFriendList.size() < 1)
        {
            dismiss();
            if (!preferences.getBoolean(FriendListFragment.KEY_USERS_FETCHED, false))
            {
                Toast.makeText(mActivity, "Please add friends to share location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Subscribe
    public void shareLocation(Accounts account)
    {
        dismiss();
        ShareLocation location = new ShareLocation();
        location.senderEmail = preferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com");
        location.receiverEmail = account.email;
        location.senderPhotoUrl = preferences.getString(Accounts.PHOTO_URL, "");
        location.senderName = preferences.getString(ShareLocation.SENDER_NAME, "Saleem");
        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        intent.putExtra(ShareLocation.LOCATION, location);

        if (isContinuouslyClicked)
        {
            Log.d("shareLocation", "continuously : checked");
            initiateShareLocation(location, account);
        }
        else
        {
            intent.putExtra(LocationTrackingService.KEY_SHARE_LOCATION, true);
            Log.d("shareLocation", "just once : checked");
            mActivity.startService(intent);
        }
        Log.d("shareLocation", "startService");
    }

    private void initiateShareLocation(final ShareLocation location, final Accounts account)
    {
        //Initiate location sharing.
        Map<String, String> data = new HashMap<>();
        data.put(VolleyUtil.KEY_ACTION, Notifications.KEY_INITIATE_SHARE_LOCATION);
        data.put(Notifications.RECEIVER_EMAIL, location.receiverEmail);
        data.put(Notifications.SENDER_EMAIL, location.senderEmail);
        data.put(Notifications.SENDER_NAME, location.senderName);
        data.put(Notifications.SENDER_PHOTO_URL, location.senderPhotoUrl);
        data.put(Notifications.TIME_STAMP, System.currentTimeMillis()+"");

        WhistleBlower.toast("Requesting " + account.name+"...");
        VolleyUtil.sendPostData(data, new ResultListener<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                Log.d("ShareLocation", "Initiate sharing :" + result);
                try
                {
                    long notificationId = Long.parseLong(result.trim());
                    if (notificationId != -1)
                    {
                        location.senderName = account.name;
                        location.senderPhotoUrl = account.photo_url;
                        location.serverNotificationId = notificationId;
                        ShareLocationDao.insert(location);
                        WhistleBlower.toast("Request sent to "+account.name);
                    }
                    else
                    {
                        WhistleBlower.toast(account.name +" is offline");
                    }
                }catch (Exception e)
                {
                    WhistleBlower.toast("Please Try Sharing Again!");
                }
            }

            @Override
            public void onError(VolleyError error)
            {
                Log.d("ShareLocation", "Initiate sharing :" + error.getMessage());
                WhistleBlower.toast("Please Try Again!");
            }
        });
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Otto.unregister(this);
    }

    @BindColor(R.color.colorAccent)
    int enabled;

    @BindColor(R.color.divider)
    int disabled;

    @OnClick(R.id.continuously)
    public void onContinuouslyClick()
    {
        continuously.setTextColor(enabled);
        justOnce.setTextColor(disabled);
        isContinuouslyClicked = true;

        continuously.setTypeface(WhistleBlower.getTypeface(), Typeface.BOLD);
        justOnce.setTypeface(WhistleBlower.getTypeface(), Typeface.NORMAL);

        TransitionUtil.defaultTransition(continuouslyHighlight);
        continuouslyHighlight.setBackgroundColor(colorAccent);
        justOnceHighlight.setBackgroundColor(transparent);
    }

    @OnClick(R.id.justOnce)
    public void onJustOnceClick()
    {
        isContinuouslyClicked = false;
        continuously.setTextColor(disabled);
        justOnce.setTextColor(enabled);

        continuously.setTypeface(WhistleBlower.getTypeface(), Typeface.NORMAL);
        justOnce.setTypeface(WhistleBlower.getTypeface(), Typeface.BOLD);

        TransitionUtil.defaultTransition(justOnceHighlight);
        continuouslyHighlight.setBackgroundColor(transparent);
        justOnceHighlight.setBackgroundColor(colorAccent);

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
