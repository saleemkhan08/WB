package in.org.whistleblower.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.interfaces.PermissionResultListener;
import in.org.whistleblower.interfaces.SettingsResultListener;

public class PermissionUtil extends AppCompatActivity implements ResultCallback<LocationSettingsResult>
{
    public static final String LOCATION_PERMISSION = "LOCATION_PERMISSION";
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 101;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 102;
    private static final int REQUEST_CODE_SDCARD_PERMISSION = 103;
    public static final String LOCATION_SETTINGS = "LOCATION_SETTINGS";
    private static PermissionResultListener mPermissionResultListener;
    public static final String SDCARD_PERMISSION = "SDCARD_PERMISSION";
    private static String currentRequest;
    private static Context mContext;
    private static GoogleApiClient mGoogleApiClient;
    private static LocationSettingsRequest mLocationSettingsRequest;
    private static SettingsResultListener mSettingsResultListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(SDCARD_PERMISSION))
        {
            requestSdCardPermission();
        }
        if (intent.hasExtra(LOCATION_SETTINGS))
        {
            if (isLocationPermissionAvailable())
            {
                requestLocationSettings();
            }
            else
            {
                requestLocationPermission();
            }
        }
    }

    private void requestSdCardPermission()
    {
        currentRequest = SDCARD_PERMISSION;
        String[] permissionTemp = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(this, permissionTemp, REQUEST_CODE_SDCARD_PERMISSION);
    }

    private void requestLocationSettings()
    {
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest);
        result.setResultCallback(this);
    }

    private void requestLocationPermission()
    {
        currentRequest = LOCATION_PERMISSION;
        String[] permissionTemp = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        ActivityCompat.requestPermissions(this, permissionTemp, REQUEST_CODE_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean result = true;
        for (int i = 0; i < grantResults.length; i++)
        {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
            {
                result &= false;
            }
        }

        switch (requestCode)
        {
            case REQUEST_CODE_SDCARD_PERMISSION:
                if (result)
                {
                    mPermissionResultListener.onGranted();
                    finish();
                }
                else
                {
                    showSdCardPermissionDialog();
                }
                break;
            case REQUEST_CODE_LOCATION_PERMISSION:
                if (result)
                {
                    if(isLocationSettingsOn())
                    {
                        mSettingsResultListener.onLocationSettingsTurnedOn();
                        finish();
                    }
                    else
                    {
                        requestLocationSettings();
                    }
                }
                else
                {
                    showLocationPermissionDialog();
                }
                break;
        }
    }

    private void showSdCardPermissionDialog()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Permission Required");
        dialog.setMessage("You won't be able to upload images without these permissions");
        dialog.setPositiveButton("RETRY", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                requestSdCardPermission();
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                mPermissionResultListener.onDenied();
                finish();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                mPermissionResultListener.onDenied();
                finish();
            }
        });
        dialog.show();
    }

    private void showLocationPermissionDialog()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Permission Required");
        dialog.setMessage("You won't be able to use Location Based Features without this permission");
        dialog.setPositiveButton("RETRY", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                requestLocationPermission();
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                mSettingsResultListener.onLocationSettingsCancelled();
                finish();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                mSettingsResultListener.onLocationSettingsCancelled();
                finish();
            }
        });
        dialog.show();
    }

    private void showSettingsDialog()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Location Required");
        dialog.setMessage("You won't be able to use Location Based Features without turning this settings on");
        dialog.setPositiveButton("RETRY", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                requestLocationSettings();
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                mSettingsResultListener.onLocationSettingsCancelled();
                finish();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                mSettingsResultListener.onLocationSettingsCancelled();
                finish();
            }
        });
        dialog.show();
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult)
    {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode())
        {
            case LocationSettingsStatusCodes.SUCCESS:
                mSettingsResultListener.onLocationSettingsTurnedOn();
                finish();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try
                {
                    status.startResolutionForResult(this, REQUEST_CODE_LOCATION_SETTINGS);
                }
                catch (IntentSender.SendIntentException e)
                {
                    MiscUtil.log("IntentSender.SendIntentException : " + e.getMessage());
                }

                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                mSettingsResultListener.onLocationSettingsCancelled();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_CODE_LOCATION_SETTINGS:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        mSettingsResultListener.onLocationSettingsTurnedOn();
                        finish();
                        break;
                    case Activity.RESULT_CANCELED:
                        showSettingsDialog();
                        break;
                }
                break;
        }
    }

    //-------------------------------------------------------------------------- Public methods ---------------------------------------------------------------------------------------------

    public static void requestPermission(String permission, PermissionResultListener listener)
    {
        mPermissionResultListener = listener;
        Intent intent = new Intent(WhistleBlower.getAppContext(), PermissionUtil.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(permission, true);
        WhistleBlower.getAppContext().startActivity(intent);
    }

    public static void turnOnLocationSettings(GoogleApiClient googleApiClient, LocationSettingsRequest locationSettingsRequest, SettingsResultListener listener)
    {
        Log.d("saleem", "turn on Location Settings");
        mGoogleApiClient = googleApiClient;
        mLocationSettingsRequest = locationSettingsRequest;
        mSettingsResultListener = listener;

        mContext = WhistleBlower.getAppContext();
        Intent intent = new Intent(mContext, PermissionUtil.class);
        intent.putExtra(LOCATION_SETTINGS, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        Log.d("saleem", "intent sent");
    }

    public static boolean isCameraAndStoragePermissionsAvailable()
    {
        Context mContext = WhistleBlower.getAppContext();
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isLocationPermissionAvailable()
    {
        Context mContext = WhistleBlower.getAppContext();
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    public static boolean isLocationSettingsOn()
    {
        Context mContext = WhistleBlower.getAppContext();
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        try
        {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                return true;
            }
        }
        catch (Exception ex)
        {
            return false;
        }
        return false;
    }


}
