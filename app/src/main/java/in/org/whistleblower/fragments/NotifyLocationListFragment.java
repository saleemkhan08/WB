package in.org.whistleblower.fragments;


import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import in.org.whistleblower.adapters.NotifyLocationAdapter;
import in.org.whistleblower.models.NotifyLocation;
import in.org.whistleblower.dao.NotifyLocationDao;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.NavigationUtil;

public class NotifyLocationListFragment extends DialogFragment
{
    public static final String NOTIFY_LOC_LIST_EMPTY_TEXT = "NOTIFY_LOC_LIST_EMPTY_TEXT";

    @BindString(R.string.noLocationNotificationsAreSet)
    String noLocationNotificationsAreSet;

    @BindString(R.string.noLocationNotificationsAreReceived)
    String noLocationNotificationsAreReceived;

    @Bind(R.id.emptyList)
    ViewGroup emptyList;

    @Bind(R.id.emptyListTextView)
    TextView emptyListTextView;

    @Bind(R.id.sending)
    TextView sending;

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

    @Bind(R.id.sendingHighlight)
    ViewGroup sendingHighlight;

    @Bind(R.id.receivingHighlight)
    ViewGroup receivingHighlight;

    private boolean isSendingClicked = true;


    public NotifyLocationListFragment()
    {
    }

    @Bind(R.id.notifyLocationList)
    RecyclerView notifyLocationListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_notify_location_list, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);

        TextView dialogTitle = (TextView) parentView.findViewById(R.id.dialogTitle);
        dialogTitle.setTypeface(WhistleBlower.getTypeface());

        ArrayList<NotifyLocation> mNotifyLocationList = NotifyLocationDao.getList();
        AppCompatActivity mActivity = (AppCompatActivity) getActivity();
        if (mNotifyLocationList.size() < 1)
        {
            showEmptyListString();
        }
        notifyLocationListView.setAdapter(new NotifyLocationAdapter(mActivity, mNotifyLocationList));
        notifyLocationListView.setLayoutManager(new LinearLayoutManager(mActivity));
        return parentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(NavigationUtil.NOTIFY_LOCATION_RECEIVING_FRAGMENT_TAG))
        {
            onReceivingClick();
        }
        else
        {
            onSendingClick();
        }
    }


    @Subscribe
    public void showEmptyListString(String msg)
    {
        if (msg.equals(NOTIFY_LOC_LIST_EMPTY_TEXT))
        {
            showEmptyListString();
            emptyListTextView.setText(isSendingClicked ? noLocationNotificationsAreSet : noLocationNotificationsAreReceived);
        }
    }

    private void showEmptyListString()
    {
        TransitionManager.beginDelayedTransition(emptyList);
        emptyListTextView.setText(isSendingClicked ? noLocationNotificationsAreSet : noLocationNotificationsAreReceived);
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

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.post(MapFragment.DIALOG_DISMISS);
    }

    @OnClick(R.id.sending)
    public void onSendingClick()
    {
        sending.setTextColor(enabled);
        receiving.setTextColor(disabled);
        isSendingClicked = true;

        sending.setTypeface(WhistleBlower.getTypeface(), Typeface.BOLD);
        receiving.setTypeface(WhistleBlower.getTypeface(), Typeface.NORMAL);

        TransitionManager.beginDelayedTransition(sendingHighlight, new Slide());
        sendingHighlight.setBackgroundColor(colorAccent);
        receivingHighlight.setBackgroundColor(transparent);
    }

    @OnClick(R.id.receiving)
    public void onReceivingClick()
    {
        isSendingClicked = false;
        sending.setTextColor(disabled);
        receiving.setTextColor(enabled);

        sending.setTypeface(WhistleBlower.getTypeface(), Typeface.NORMAL);
        receiving.setTypeface(WhistleBlower.getTypeface(), Typeface.BOLD);

        TransitionManager.beginDelayedTransition(receivingHighlight, new Slide());
        sendingHighlight.setBackgroundColor(transparent);
        receivingHighlight.setBackgroundColor(colorAccent);
    }
}
