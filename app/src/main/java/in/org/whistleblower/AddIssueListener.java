package in.org.whistleblower;

import android.content.Context;
import android.view.View;

import in.org.whistleblower.services.MiscUtil;

/**
 * Created by Saleem Khan on 12/8/2015.
 */
public class AddIssueListener implements View.OnClickListener
{
    Context context;
    MiscUtil util;
    Dialogs dialogs;

    public AddIssueListener(Context context)
    {
        this.context = context;
        util = new MiscUtil(context);
        dialogs = new Dialogs(context);
    }

    @Override
    public void onClick(View v)
    {
        //util.toast("clicked");
        int id = v.getId();
        switch (id)
        {
            case R.id.radius:
                dialogs.showRadiusDialog();
                break;
            case R.id.zone:
                dialogs.showZoneDialog();
                break;
            case R.id.anonymus:
                dialogs.showAnonymousDialog();
                break;
            case R.id.post:
                break;
        }
    }
}
