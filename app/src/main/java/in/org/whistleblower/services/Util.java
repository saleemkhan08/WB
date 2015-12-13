package in.org.whistleblower.services;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import in.org.whistleblower.IconicFontDrawable;
import in.org.whistleblower.LoginActivity;
import in.org.whistleblower.MainActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.icon.Icon;

public class Util
{
    private Context mContext;
    IconicFontDrawable fontDrawable;
    private ProgressDialog mProgressDialog;
    Resources resources;

    public Util(Context mContext)
    {
        this.mContext = mContext;
        resources = mContext.getResources();
    }

    public void toast(String msg)
    {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public static void log(String msg)
    {
        Log.d("Whistle", msg);
    }

    public static boolean isConnected(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                return true;
            }
        }
        return false;
    }

    public Drawable getIcon(Icon icon, int color)
    {
        fontDrawable = new IconicFontDrawable(mContext);
        fontDrawable.setIcon(icon);
        fontDrawable.setIconColor(resources.getColor(color, null));
        return fontDrawable;
    }

    public Drawable getIcon(Icon icon)
    {
        fontDrawable = new IconicFontDrawable(mContext);
        fontDrawable.setIcon(icon);
        fontDrawable.setIconColor(resources.getColor(R.color.colorAccent, null));
        return fontDrawable;
    }

    public boolean hasUserSignedIn()
    {
        //Login Check
        if (!mContext.getSharedPreferences(MainActivity.WHISTLE_BLOWER_PREFERENCE, Context.MODE_PRIVATE).getBoolean(LoginActivity.LOGIN_STATUS, false))
        {
            mContext.startActivity(new Intent(mContext, LoginActivity.class));
            return false;
        }
        else
        {
            return true;
        }

    }

    public void showProgressDialog(String msg)
    {
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog()
    {
        if (mProgressDialog != null && mProgressDialog.isShowing())
        {
            mProgressDialog.hide();
        }
    }

    public int dp(double value)
    {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        double dp = (float) value;
        double fpixels = metrics.density * dp;
        return (int) fpixels;
    }

}
