package in.org.whistleblower.utilities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import in.org.whistleblower.FontDrawable;
import in.org.whistleblower.LoginActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.icon.Icon;

public class MiscUtil
{
    private Context mContext;
    FontDrawable fontDrawable;
    public ProgressDialog mProgressDialog;
    Resources resources;

    public MiscUtil(Context mContext)
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
    public boolean isConnected()
    {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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
        fontDrawable = new FontDrawable(mContext);
        fontDrawable.setIcon(icon);
        fontDrawable.setIconColor(resources.getColor(color, null));
        return fontDrawable;
    }

    public Drawable getIcon(Icon icon)
    {
        fontDrawable = new FontDrawable(mContext);
        fontDrawable.setIcon(icon);
        fontDrawable.setIconColor(resources.getColor(R.color.colorAccent, null));
        return fontDrawable;
    }

    public boolean hasUserSignedIn()
    {
        //Login Check
        if (!PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(LoginActivity.LOGIN_STATUS, false))
        {
            mContext.startActivity(new Intent(mContext, LoginActivity.class));
            return false;
        }
        else
        {
            return true;
        }
    }

    public void isConnected(final ConnectivityListener listener)
    {
        if (!isConnected())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.no_network_access);
            builder.setPositiveButton("Try Again",
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (isConnected())
                            {
                                listener.onInternetConnected();
                                dialog.dismiss();
                            }
                            else
                            {
                                isConnected(listener);
                            }
                        }
                    });
            builder.setCancelable(false);
            builder.setTitle(R.string.internet_failure);
            builder.setOnKeyListener(new DialogInterface.OnKeyListener()
            {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event)
                {
                    if (keyCode == KeyEvent.KEYCODE_BACK
                            && event.getAction() == KeyEvent.ACTION_UP
                            && !event.isCanceled())
                    {
                        dialog.dismiss();
                    }
                    return false;
                }
            });
            builder.show();
        }
        else
        {
            listener.onInternetConnected();
        }
    }
    public void showIndeterminateProgressDialog(String msg)
    {
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }
    public void showProgressDialog(String msg)
    {
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
