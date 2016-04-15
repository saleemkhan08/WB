package in.org.whistleblower.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import in.org.whistleblower.R;
import in.org.whistleblower.interfaces.DialogUtilListener;
import in.org.whistleblower.models.IssuesDao;

public class DialogsUtil
{
    private static final String ZONE_KEY = "zoneKey";
    private static final String UNKNOWN_TYPE = "#Unknown Type";
    Activity mActivty;
    MiscUtil util;
    SharedPreferences preferences;
    String zones[];
    Resources res;

    public DialogsUtil(Activity context)
    {
        mActivty = context;
        util = new MiscUtil(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        res = mActivty.getResources();
    }

    public void showRadiusDialog()
    {
        final String radiusOptions[] = mActivty.getResources().getStringArray(R.array.radius);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(mActivty);
        dialog.setTitle(mActivty.getResources().getString(R.string.loc_radius_dialog_title));

        int selectedValue = preferences.getInt(IssuesDao.RADIUS, radiusOptions.length - 1);
        dialog.setSingleChoiceItems(radiusOptions, selectedValue, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int pos)
            {
                preferences.edit().putInt(IssuesDao.RADIUS, pos).apply();
                dialog.dismiss();
            }
        });
        dialog.show();
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