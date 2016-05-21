package in.org.whistleblower.utilities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.squareup.otto.Subscribe;

import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.singletons.Otto;

public class FABUtil
{
    public static final String ACTION = "ACTION";
    public static final String SET_ALARM = "SET_ALARM";
    public static final String ADD_ISSUE = "ADD_ISSUE";
    public static final String ADD_FAV_PLACE = "ADD_FAV_PLACE";
    public static final String NOTIFY_LOC = "NOTIFY_LOC";
    public static final String HIDE_DESCRIPTION_TOAST = "HIDE_DESCRIPTION_TOAST";
    public static final String TOAST_INDEX = "TOAST_INDEX";
    AppCompatActivity mActivity;
    MiscUtil mUtil;
    Toast radiusToast, submitToast, locationToast, placeTypeToast;
    public static FloatingActionsMenu fabMenu;
    private boolean isShowSetPlaceTypeToast;

    public FABUtil(AppCompatActivity activity)
    {
        this.mActivity = activity;
        mUtil = new MiscUtil(activity);
        WhistleBlower.getComponent().inject(this);
        Otto.register(this);
    }

    public void unregisterOtto()
    {
        Otto.unregister(this);
    }

    public void setUp()
    {
        fabMenu = (FloatingActionsMenu) mActivity.findViewById(R.id.multiple_actions);
        FloatingActionButton buttonAlarm = (FloatingActionButton) mActivity.findViewById(R.id.alarm);
        buttonAlarm.setStrokeVisible(false);
        buttonAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fabMenu.collapse();
                isShowSetPlaceTypeToast = false;
                fabAction(SET_ALARM);
            }
        });

        FloatingActionButton buttonPhoto = (FloatingActionButton) mActivity.findViewById(R.id.photo);
        buttonPhoto.setStrokeVisible(false);
        buttonPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fabMenu.collapse();
                isShowSetPlaceTypeToast = false;
                fabAction(ADD_ISSUE);
            }
        });

        FloatingActionButton buttonFavPlace = (FloatingActionButton) mActivity.findViewById(R.id.favPlace);
        buttonFavPlace.setStrokeVisible(false);
        buttonFavPlace.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fabMenu.collapse();
                isShowSetPlaceTypeToast = true;
                fabAction(ADD_FAV_PLACE);
            }
        });

        FloatingActionButton buttonNotifyPlace = (FloatingActionButton) mActivity.findViewById(R.id.notifyLoc);
        buttonNotifyPlace.setStrokeVisible(false);
        buttonNotifyPlace.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fabMenu.collapse();
                isShowSetPlaceTypeToast = false;
                fabAction(NOTIFY_LOC);
            }
        });
    }

    public void fabAction(String action)
    {
        Log.d("Action", "fab Action : " + action);
        showDescriptionToast();
        if (MiscUtil.isConnected(mActivity))
        {
            Otto.post(action);
        }
        else
        {
            Toast.makeText(mActivity, "No Internet!", Toast.LENGTH_SHORT).show();
        }

    }

    private void showDescriptionToast(int... index)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        int cnt = preferences.getInt(HIDE_DESCRIPTION_TOAST, 0);
        preferences.edit().putInt(HIDE_DESCRIPTION_TOAST, ++cnt).apply();
        if(cnt < 5)
        {
            hideDescriptionToast(HIDE_DESCRIPTION_TOAST);
            int toastIndex = 4;
            if (index != null && index.length > 0)
            {
                toastIndex = index[0];
            }

            switch (toastIndex)
            {
                case 4:
                    toastSelectLocation();
                    Log.d("ToastTrace", "toastSelectLocation");
                case 3:
                    if (isShowSetPlaceTypeToast)
                    {
                        toastSetPlaceType();
                    }
                    Log.d("ToastTrace", "toastSetPlaceType");
                case 2:
                    toastSetRadius();
                    Log.d("ToastTrace", "toastSetRadius");
                case 1:
                    toastSubmitButton();
                    Log.d("ToastTrace", "toastSubmitButton");
            }
        }
    }

    public void toastSelectLocation()
    {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.select_location_toast,
                (ViewGroup) mActivity.findViewById(R.id.toastRoot));

        locationToast = new Toast(mActivity);
        locationToast.setGravity(Gravity.CENTER_VERTICAL, MiscUtil.dp(mActivity, 150), MiscUtil.dp(mActivity, -10));
        locationToast.setDuration(Toast.LENGTH_SHORT);
        locationToast.setView(layout);
        locationToast.show();
    }

    public void toastSetPlaceType()
    {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.set_place_type_toast,
                (ViewGroup) mActivity.findViewById(R.id.toastRoot));

        placeTypeToast = new Toast(mActivity);
        placeTypeToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, MiscUtil.dp(mActivity, 160));
        placeTypeToast.setDuration(Toast.LENGTH_SHORT);
        placeTypeToast.setView(layout);
        placeTypeToast.show();
    }

    public void toastSubmitButton()
    {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.submit_button_toast,
                (ViewGroup) mActivity.findViewById(R.id.toastRoot));

        submitToast = new Toast(mActivity);
        submitToast.setGravity(Gravity.TOP | Gravity.RIGHT, MiscUtil.dp(mActivity, -5), MiscUtil.dp(mActivity, 110));
        submitToast.setDuration(Toast.LENGTH_SHORT);
        submitToast.setView(layout);
        submitToast.show();
    }

    public void toastSetRadius()
    {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.set_radius_toast,
                (ViewGroup) mActivity.findViewById(R.id.toastRoot));
        radiusToast = new Toast(mActivity);
        radiusToast.setGravity(Gravity.BOTTOM | Gravity.LEFT, MiscUtil.dp(mActivity, 5), MiscUtil.dp(mActivity, 40));
        radiusToast.setDuration(Toast.LENGTH_SHORT);
        radiusToast.setView(layout);
        radiusToast.show();
    }

    public static void closeFABMenu(AppCompatActivity mActivity)
    {
        if (null == FABUtil.fabMenu)
        {
            FABUtil.fabMenu = (FloatingActionsMenu) mActivity.findViewById(R.id.multiple_actions);
        }
        FABUtil.fabMenu.collapse();
    }

    @Subscribe
    public void hideDescriptionToast(String action)
    {
        if (action.equals(HIDE_DESCRIPTION_TOAST))
        {
            if (locationToast != null)
            {
                locationToast.cancel();
            }
            if (placeTypeToast != null)
            {
                placeTypeToast.cancel();
            }
            if (radiusToast != null)
            {
                radiusToast.cancel();
            }
            if (submitToast != null)
            {
                submitToast.cancel();
            }

        }
    }

    @Subscribe
    public void hideDescriptionToast(Bundle bundle)
    {
        if (bundle.containsKey(HIDE_DESCRIPTION_TOAST))
        {
            showDescriptionToast(bundle.getInt(TOAST_INDEX));
        }
    }
}
