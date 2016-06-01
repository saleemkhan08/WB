package in.org.whistleblower.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import in.org.whistleblower.adapters.NotifyLocationAdapter;
import in.org.whistleblower.models.NotifyLocation;
import in.org.whistleblower.models.NotifyLocationDao;
import in.org.whistleblower.singletons.Otto;

public class NotifyLocationListFragment extends DialogFragment
{
    public static final String NOTIFY_LOC_LIST_EMPTY_TEXT = "NOTIFY_LOC_LIST_EMPTY_TEXT";

    @BindString(R.string.noNotificationsAreSet)
    String noNotificationsAreSet;

    @Bind(R.id.emptyList)
    ViewGroup emptyList;

    @Bind(R.id.emptyListTextView)
    TextView emptyListTextView;

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
        ArrayList<NotifyLocation> mNotifyLocationList = new NotifyLocationDao().getList();
        AppCompatActivity mActivity = (AppCompatActivity) getActivity();
        if(mNotifyLocationList.size() < 1)
        {
            showEmptyListString();
        }
        notifyLocationListView.setAdapter(new NotifyLocationAdapter(mActivity, mNotifyLocationList));
        notifyLocationListView.setLayoutManager(new LinearLayoutManager(mActivity));
        return parentView;
    }
    @Subscribe
    public void showEmptyListString(String msg)
    {
        if(msg.equals(NOTIFY_LOC_LIST_EMPTY_TEXT))
        {
            showEmptyListString();
            emptyListTextView.setText(noNotificationsAreSet);
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

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.post(MapFragment.DIALOG_DISMISS);
    }
}
