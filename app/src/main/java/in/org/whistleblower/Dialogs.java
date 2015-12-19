package in.org.whistleblower;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import in.org.whistleblower.utilities.MiscUtil;

public class Dialogs
{
    Context mContext;
    MiscUtil util;

    public Dialogs(Context context)
    {
        mContext = context;
        util = new MiscUtil(context);
    }

    public void showRadiusDialog()
    {
        final String radiusOptions[] = mContext.getResources().getStringArray(R.array.radius);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(mContext.getResources().getString(R.string.loc_radius_dialog_title));
        dialog.setSingleChoiceItems(radiusOptions,radiusOptions.length-1 , new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int pos)
            {
                util.toast(radiusOptions[pos]);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void showZoneDialog()
    {
        final String radiusOptions[] = mContext.getResources().getStringArray(R.array.zones);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(mContext.getResources().getString(R.string.select_zone));
        dialog.setSingleChoiceItems(radiusOptions,radiusOptions.length-1 , new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int pos)
            {
                util.toast(radiusOptions[pos]);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void showAnonymousDialog()
    {
        final String radiusOptions[] = mContext.getResources().getStringArray(R.array.zones);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(mContext.getResources().getString(R.string.anonymous_confirmation));
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                util.toast("Yes");
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                util.toast("No");
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
