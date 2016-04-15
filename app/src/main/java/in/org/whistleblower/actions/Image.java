package in.org.whistleblower.actions;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import in.org.whistleblower.AddIssueActivity;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.interfaces.PermissionResultListener;
import in.org.whistleblower.utilities.PermissionUtil;

public class Image
{
    public static final int LOAD_GALLERY_REQUEST = 303;
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

    public static void captureImage(final AppCompatActivity mActivity)
    {
        if (!PermissionUtil.isCameraAndStoragePermissionsAvailable())
        {
            MiscUtil.log("Permission Not Available");
            PermissionUtil.requestPermission(PermissionUtil.SDCARD_PERMISSION, new PermissionResultListener()
            {
                @Override
                public void onGranted()
                {
                    MiscUtil.log("Permission Obtained");
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getOutputMediaFileUri(MEDIA_TYPE_IMAGE));
                    mActivity.startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
                }

                @Override
                public void onDenied()
                {
                    Toast.makeText(mActivity, "This Permission is required", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            MiscUtil.log("Permission Available");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getOutputMediaFileUri(MEDIA_TYPE_IMAGE));
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

    public static void getImageFromGallery(AppCompatActivity activity)
    {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, LOAD_GALLERY_REQUEST);
    }

    public static String getStringImage(Bitmap bmp)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
}
