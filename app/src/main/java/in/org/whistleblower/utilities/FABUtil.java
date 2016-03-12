package in.org.whistleblower.utilities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import in.org.whistleblower.R;
import in.org.whistleblower.icon.FontAwesomeIcon;

public class FABUtil
{
    public static final String ACTION = "ACTION";
    public static final String SET_ALARM = "SET_ALARM";
    public static final String ADD_ISSUE = "ADD_ISSUE";
    public static final String ADD_FAV_PLACE = "ADD_FAV_PLACE";
    AppCompatActivity mActivity;
    MiscUtil mUtil;
    public static View locationSelector;
    public static FloatingActionsMenu fabMenu;

    public FABUtil(AppCompatActivity activity)
    {
        this.mActivity = activity;
        mUtil = new MiscUtil(activity);
    }

    public void setUp()
    {
        fabMenu = (FloatingActionsMenu) mActivity.findViewById(R.id.multiple_actions);
        locationSelector = mActivity.findViewById(R.id.select_location);

        FloatingActionButton buttonAlarm = (FloatingActionButton) mActivity.findViewById(R.id.alarm);
        buttonAlarm.setIconDrawable(mUtil.getIcon(FontAwesomeIcon.BELL_ALT));
        buttonAlarm.setStrokeVisible(false);
        buttonAlarm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fabMenu.collapse();
                fabAction(SET_ALARM);
            }
        });

        FloatingActionButton buttonPhoto = (FloatingActionButton) mActivity.findViewById(R.id.photo);
        buttonPhoto.setIconDrawable(mUtil.getIcon(FontAwesomeIcon.CAMERA));
        buttonPhoto.setStrokeVisible(false);
        buttonPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fabMenu.collapse();
                fabAction(ADD_ISSUE);
            }
        });

        FloatingActionButton buttonFavPlace = (FloatingActionButton) mActivity.findViewById(R.id.favPlace);
        buttonFavPlace.setIconDrawable(mUtil.getIcon(FontAwesomeIcon.STAR));
        buttonFavPlace.setStrokeVisible(false);
        buttonFavPlace.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fabMenu.collapse();
                fabAction(ADD_FAV_PLACE);
            }
        });
    }

    public void fabAction(String action)
    {
        if(MiscUtil.isConnected(mActivity))
        {
            Bundle bundle = new Bundle();
            bundle.putString(ACTION, action);
            NavigationUtil.showMapFragment(mActivity, bundle);
            FloatingActionButton okButton = (FloatingActionButton) mActivity.findViewById(R.id.ok_map);
            switch (action)
            {
                case ADD_FAV_PLACE:
                    okButton.setIconDrawable(mUtil.getIcon(FontAwesomeIcon.STAR,R.color.white));
                    break;
                case ADD_ISSUE:
                    okButton.setIconDrawable(mUtil.getIcon(FontAwesomeIcon.CAMERA,R.color.white));
                    break;
                case SET_ALARM:
                    okButton.setIconDrawable(mUtil.getIcon(FontAwesomeIcon.BELL_ALT,R.color.white));
                    break;

            }
            okButton.setStrokeVisible(false);
            locationSelector.setVisibility(View.VISIBLE);
        }else
        {
            Toast.makeText(mActivity, "No Internet!", Toast.LENGTH_SHORT).show();
        }

    }

    public static void closeFABMenu(AppCompatActivity mActivity)
    {
        if (null == FABUtil.fabMenu)
        {
            FABUtil.fabMenu = (FloatingActionsMenu) mActivity.findViewById(R.id.multiple_actions);
        }
        FABUtil.fabMenu.collapse();
    }
}
