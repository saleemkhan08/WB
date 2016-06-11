package in.org.whistleblower.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import in.org.whistleblower.R;
import in.org.whistleblower.interfaces.DialogUtilListener;
import in.org.whistleblower.dao.IssuesDao;

public class DialogsUtil
{
    private static final String ZONE_KEY = "zoneKey";
    private static final String UNKNOWN_TYPE = "#Unknown Type";
    Activity mActivty;
    MiscUtil util;
    SharedPreferences preferences;
    String zones[];
    Resources res;

    public DialogsUtil(AppCompatActivity context)
    {
        mActivty = context;
        util = new MiscUtil(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        res = mActivty.getResources();
    }

    private boolean[] getSelectedZones()
    {
        int len = zones.length;
        boolean selectedZones[] = new boolean[len];

        for (int i = 0; i < len; i++)
        {
            selectedZones[i] = preferences.getBoolean(ZONE_KEY + i, false);
        }

        return selectedZones;
    }

    public String getSelectedZonesStrings()
    {
        zones = res.getStringArray(R.array.zones);
        String selectedZoneString = null;
        boolean selectedZones[] = getSelectedZones();

        for (int i = 0; i < zones.length; i++)
        {
            if (selectedZones[i])
            {
                if (selectedZoneString == null)
                {
                    selectedZoneString = zones[i];
                }
                else
                {
                    selectedZoneString += ", " + zones[i];
                }
            }
        }
        if (selectedZoneString == null)
        {
            selectedZoneString = UNKNOWN_TYPE;
        }
        return selectedZoneString;
    }

    public void showZoneDialog()
    {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(mActivty);
        dialog.setTitle(res.getString(R.string.select_zone));

        dialog.setMultiChoiceItems(zones, getSelectedZones(), new DialogInterface.OnMultiChoiceClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked)
            {
                preferences.edit().putBoolean(ZONE_KEY + which, isChecked).apply();
            }
        });

        dialog.setPositiveButton("Save", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                ((DialogUtilListener) mActivty).showAreaNameAndTypeDetails();
            }
        });
        dialog.show();
    }

    public void showAnonymousDialog()
    {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(mActivty);
        dialog.setTitle(mActivty.getResources().getString(R.string.anonymous_confirmation));
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                preferences.edit().putBoolean(IssuesDao.ANONYMOUS, true).apply();
                dialog.dismiss();
                ((DialogUtilListener) mActivty).showProfileDetails();
            }
        });
        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                preferences.edit().putBoolean(IssuesDao.ANONYMOUS, false).apply();
                dialog.dismiss();
                ((DialogUtilListener) mActivty).showProfileDetails();
            }
        });
        dialog.show();
    }
}