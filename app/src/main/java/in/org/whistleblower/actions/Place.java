package in.org.whistleblower.actions;

import android.support.v7.app.AppCompatActivity;

import in.org.whistleblower.utilities.MiscUtil;

public class Place
{
    AppCompatActivity mActivity;
    MiscUtil mUtil;

    public Place(AppCompatActivity activity)
    {
        mActivity = activity;
        mUtil = new MiscUtil(activity);
    }

    public void addFavPlace()
    {
        mUtil.toast("Add Favorite Place");
    }
}
