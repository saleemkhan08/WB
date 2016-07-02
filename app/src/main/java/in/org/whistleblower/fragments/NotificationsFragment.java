package in.org.whistleblower.fragments;


import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
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

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.adapters.NotificationsAdapter;
import in.org.whistleblower.dao.NotificationsDao;
import in.org.whistleblower.models.Notifications;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.TransitionUtil;

public class NotificationsFragment extends DialogFragment
{
    public static final String NOTIFICATION_LIST_EMPTY_TEXT = "NOTIFICATION_LIST_EMPTY_TEXT";
    AppCompatActivity mActivity;

    ArrayList<Notifications> mAllNotificationsList, mUnreadNotificationsList;

    @Bind(R.id.notifications)
    RecyclerView notificationsListView;

    @Bind(R.id.unread)
    TextView unread;

    @Bind(R.id.all)
    TextView all;

    @BindColor(R.color.transparent)
    int transparent;

    @BindColor(R.color.colorAccent)
    int enabled;

    @BindColor(R.color.divider)
    int disabled;

    @Bind(R.id.unreadHighlight)
    ViewGroup unreadHighlight;

    @Bind(R.id.allHighlight)
    ViewGroup allHighlight;

    @BindString(R.string.noNewNotifications)
    String noNewNotifications;

    @BindString(R.string.noNotifications)
    String noNotifications;

    @BindString(R.string.removedAllNotifications)
    String removedAllNotifications;

    private SharedPreferences preferences;
    private boolean isUnreadClicked = true;

    @Bind(R.id.emptyList)
    ViewGroup emptyList;

    @Bind(R.id.emptyListTextView)
    TextView emptyListTextView;

    public NotificationsFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_notifcations, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);

        TextView dialogTitle = (TextView) parentView.findViewById(R.id.dialogTitle);
        dialogTitle.setTypeface(WhistleBlower.getTypeface());

        unread.setTypeface(WhistleBlower.getTypeface());
        all.setTypeface(WhistleBlower.getTypeface());

        mActivity = (AppCompatActivity) getActivity();
        preferences = WhistleBlower.getPreferences();

        mAllNotificationsList = NotificationsDao.getAllNotifications();
        mUnreadNotificationsList = NotificationsDao.getUnreadList();
        Log.d("NotificationsFragment", "mUnreadNotificationsList : " + mUnreadNotificationsList.size());
        Log.d("NotificationsFragment", "mAllNotificationsList : " + mAllNotificationsList.size());
        onUnreadClick();
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return parentView;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Otto.unregister(this);
    }

    @OnClick(R.id.unread)
    public void onUnreadClick()
    {
        unread.setTextColor(enabled);
        all.setTextColor(disabled);
        isUnreadClicked = true;

        unread.setTypeface(WhistleBlower.getTypeface(), Typeface.BOLD);
        all.setTypeface(WhistleBlower.getTypeface(), Typeface.NORMAL);

        unreadHighlight.setBackgroundColor(enabled);
        allHighlight.setBackgroundColor(transparent);

        mUnreadNotificationsList = NotificationsDao.getUnreadList();

        NotificationsAdapter mAdapter = new NotificationsAdapter(mActivity, mUnreadNotificationsList);
        notificationsListView.setLayoutManager(new LinearLayoutManager(mActivity));
        notificationsListView.setAdapter(mAdapter);

        if(mUnreadNotificationsList.size() < 1)
        {
            emptyList.setVisibility(View.VISIBLE);
            emptyListTextView.setText(noNewNotifications);
        }
        else
        {
            emptyList.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.all)
    public void onAllClick()
    {

        //Checking branch
        isUnreadClicked = false;
        unread.setTextColor(disabled);
        all.setTextColor(enabled);

        unread.setTypeface(WhistleBlower.getTypeface(), Typeface.NORMAL);
        all.setTypeface(WhistleBlower.getTypeface(), Typeface.BOLD);

        unreadHighlight.setBackgroundColor(transparent);
        allHighlight.setBackgroundColor(enabled);

        mAllNotificationsList = NotificationsDao.getAllNotifications();

        NotificationsAdapter mAdapter = new NotificationsAdapter(mActivity, mAllNotificationsList);
        notificationsListView.setLayoutManager(new LinearLayoutManager(mActivity));
        notificationsListView.setAdapter(mAdapter);
        if(mAllNotificationsList.size() < 1)
        {
            emptyList.setVisibility(View.VISIBLE);
            emptyListTextView.setText(noNotifications);
        }
        else
        {
            emptyList.setVisibility(View.GONE);
        }

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

    @Subscribe
    public void showEmptyListString(String msg)
    {
        if(msg.equals(NOTIFICATION_LIST_EMPTY_TEXT))
        {
            showEmptyListString();
            emptyListTextView.setText(noNewNotifications);
            emptyListTextView.setText(isUnreadClicked ? noNewNotifications : removedAllNotifications);
        }
    }


    private void showEmptyListString()
    {
        TransitionUtil.defaultTransition(emptyList);
        emptyList.setVisibility(View.VISIBLE);
    }
}
