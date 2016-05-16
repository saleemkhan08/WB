package in.org.whistleblower.utilities;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.singletons.Otto;

public class FABUtil
{
    public static final String ACTION = "ACTION";
    public static final String SET_ALARM = "SET_ALARM";
    public static final String ADD_ISSUE = "ADD_ISSUE";
    public static final String ADD_FAV_PLACE = "ADD_FAV_PLACE";
    public static final String NOTIFY_LOC = "NOTIFY_LOC" ;
    AppCompatActivity mActivity;
    MiscUtil mUtil;

    public static FloatingActionsMenu fabMenu;

    public FABUtil(AppCompatActivity activity)
    {
        this.mActivity = activity;
        mUtil = new MiscUtil(activity);
        WhistleBlower.getComponent().inject(this);
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
                fabAction(NOTIFY_LOC);
            }
        });
    }

    public void fabAction(String action)
    {
        Log.d("Action", "fab Action : " + action);
        if (MiscUtil.isConnected(mActivity))
        {
            Otto.post(action);
        }
        else
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
