package in.org.whistleblower.fragments;


import android.content.Context;
import android.content.DialogInterface;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.adapters.CommonUserListAdapter;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.dao.AccountsDao;
import in.org.whistleblower.models.NotifyLocation;
import in.org.whistleblower.dao.NotifyLocationDao;
import in.org.whistleblower.services.LocationTrackingService;
import in.org.whistleblower.singletons.Otto;

public class NotifyLocationFragment extends DialogFragment
{
    AppCompatActivity mActivity;
    ArrayList<Accounts> mFriendList;

    @Bind(R.id.shareLocationFriendList)
    RecyclerView shareLocationFriendList;

    @Bind(R.id.messageEdit)
    EditText messageEdit;

    private SharedPreferences preferences;

    NotifyLocation mNotifyLocation;

    public NotifyLocationFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_notify_location, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);

        Bundle bundle = getArguments();
        mNotifyLocation = bundle.getParcelable(NotifyLocation.FRAGMENT_TAG);
        String msg = getAddressLines(mNotifyLocation.message, 3);
        messageEdit.setText("Hi! I reached " + msg);
        mActivity = (AppCompatActivity) getActivity();
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

        TextView dialogTitle = (TextView) parentView.findViewById(R.id.dialogTitle);
        dialogTitle.setTypeface(WhistleBlower.getTypeface());

        mFriendList = AccountsDao.getFriendsList();

        CommonUserListAdapter mAdapter = new CommonUserListAdapter(mActivity, mFriendList);
        shareLocationFriendList.setLayoutManager(new LinearLayoutManager(mActivity));
        shareLocationFriendList.setAdapter(mAdapter);
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
    public void notifyLocation(Accounts account)
    {
        dismiss();
        mNotifyLocation.senderEmail = preferences.getString(Accounts.EMAIL, "saleemkhan08@gmail.com");
        mNotifyLocation.receiverEmail = account.email;
        mNotifyLocation.senderPhotoUrl = preferences.getString(Accounts.PHOTO_URL, "");
        mNotifyLocation.senderName = preferences.getString(Accounts.NAME, "Saleem");
        mNotifyLocation.message = messageEdit.getText().toString();
        mNotifyLocation.receiverName = account.name;
        mNotifyLocation.receiverName = account.name;
        mNotifyLocation.receiverPhotoUrl = account.photo_url;

        Intent intent = new Intent(mActivity, LocationTrackingService.class);
        intent.putExtra(NotifyLocation.FRAGMENT_TAG, mNotifyLocation);
        intent.putExtra(LocationTrackingService.KEY_NOTIFY_ARRIVAL, true);

        NotifyLocationDao.insert(mNotifyLocation);
        Otto.post(mNotifyLocation);
        mActivity.startService(intent);
        Log.d("shareLocation", "startService");
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Otto.unregister(this);
    }

    @Bind(R.id.editIcon)
    ImageView editIcon;

    @OnClick(R.id.editIcon)
    public void onEditIconClicked()
    {
        editIcon.setVisibility(View.GONE);
        messageEdit.requestFocus();
        messageEdit.setCursorVisible(true);

        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(messageEdit, InputMethodManager.SHOW_IMPLICIT);
    }

    public static String getAddressLines(String address, int noOfLines)
    {
        String[] addressLines = address.split(",");
        String msg = "";
        int len = addressLines.length;
        for (int i = 0; i < len; i++)
        {
            if (i < (noOfLines - 1))
            {
                msg += addressLines[i] + ", ";
            }
        }
        return msg.substring(0, msg.length() - 2);
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
