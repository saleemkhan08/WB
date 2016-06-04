package in.org.whistleblower.fragments;


import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.adapters.NotificationsAdapter;
import in.org.whistleblower.models.Notifications;
import in.org.whistleblower.models.NotificationsDao;
import in.org.whistleblower.singletons.Otto;

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

    private SharedPreferences preferences;
    private boolean isUnreadClicked = true;

    Typeface normal = Typeface.defaultFromStyle(Typeface.NORMAL);
    Typeface bold = Typeface.defaultFromStyle(Typeface.BOLD);

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

        mActivity = (AppCompatActivity) getActivity();
        preferences = WhistleBlower.getPreferences();

        NotificationsDao dao = new NotificationsDao();
        mAllNotificationsList = dao.getAllNotifications();
        mUnreadNotificationsList = dao.getUnreadList();

        NotificationsAdapter mAdapter = new NotificationsAdapter(mActivity, mUnreadNotificationsList);
        notificationsListView.setLayoutManager(new LinearLayoutManager(mActivity));
        notificationsListView.setAdapter(mAdapter);
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

        unread.setTypeface(bold);
        all.setTypeface(normal);

        unreadHighlight.setBackgroundColor(enabled);
        allHighlight.setBackgroundColor(transparent);

        NotificationsDao dao = new NotificationsDao();
        mUnreadNotificationsList = dao.getUnreadList();

        NotificationsAdapter mAdapter = new NotificationsAdapter(mActivity, mUnreadNotificationsList);
        notificationsListView.setLayoutManager(new LinearLayoutManager(mActivity));
        notificationsListView.setAdapter(mAdapter);


    }

    @OnClick(R.id.all)
    public void onAllClick()
    {
        isUnreadClicked = false;
        unread.setTextColor(disabled);
        all.setTextColor(enabled);

        unread.setTypeface(normal);
        all.setTypeface(bold);

        unreadHighlight.setBackgroundColor(transparent);
        allHighlight.setBackgroundColor(enabled);

        NotificationsDao dao = new NotificationsDao();
        mAllNotificationsList = dao.getAllNotifications();

        NotificationsAdapter mAdapter = new NotificationsAdapter(mActivity, mAllNotificationsList);
        notificationsListView.setLayoutManager(new LinearLayoutManager(mActivity));
        notificationsListView.setAdapter(mAdapter);

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
