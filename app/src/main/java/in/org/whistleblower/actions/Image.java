package in.org.whistleblower.actions;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import in.org.whistleblower.AddIssueActivity;
import in.org.whistleblower.MainActivity;
import in.org.whistleblower.utilities.MiscUtil;

public class Image
{
    AppCompatActivity mActivity;
    MiscUtil mUtil;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int CAPTURE_IMAGE_REQUEST = 301;
    public static final int RECORD_VIDEO_REQUEST = 302;
    public static String storagePath;
    public static final String FILE_PATH = "IMAGE_URL";
    public static final String IS_PHOTO = "IS_PHOTO";
    private static final String IMAGE_DIRECTORY_NAME = "WhistleBlower";

    public Image(AppCompatActivity activity)
    {
        mActivity = activity;
        mUtil = new MiscUtil(activity);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isCameraAndStoragePermissionsAvailable(AppCompatActivity mActivity)
    {
        return ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static void captureImage(AppCompatActivity mActivity)
    {
        if (!isCameraAndStoragePermissionsAvailable(mActivity))
        {
            MiscUtil.log("Permission Not Available");
            requestCameraAndStoragePermissions(MainActivity.IMAGE_AND_STORAGE_REQUEST, mActivity);
        }
        else
        {
            MiscUtil.log("Permission Available");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getOutputMediaFileUri(MEDIA_TYPE_IMAGE));
            // start the image capture Intent
            mActivity.startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
        }
    }

    public static Uri getOutputMediaFileUri(int type)
    {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type)
    {
        // External sdcard location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists())
        {
            if (mediaStorageDir.mkdirs())
            {
                mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE)
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        }
        else if (type == MEDIA_TYPE_VIDEO)
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        }
        else
        {
            return null;
        }
        storagePath = mediaFile.getAbsolutePath();
        return mediaFile;
    }

    public static void launchIssueEditor(AppCompatActivity mActivity, boolean isPhoto)
    {
        Intent intent = new Intent(mActivity, AddIssueActivity.class);
        intent.putExtra(FILE_PATH, storagePath);
        intent.putExtra(IS_PHOTO, isPhoto);
        mActivity.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void requestCameraAndStoragePermissions(int requestCode, AppCompatActivity mActivity)
    {
        MainActivity.requestPermission(
                Arrays.asList(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE),
                requestCode, mActivity);
    }

}
