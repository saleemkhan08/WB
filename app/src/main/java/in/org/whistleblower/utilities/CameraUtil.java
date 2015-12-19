package in.org.whistleblower.utilities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import in.org.whistleblower.AddIssueActivity;
import in.org.whistleblower.MainActivity;
import in.org.whistleblower.R;
import in.org.whistleblower.icon.FontAwesomeIcon;

public class CameraUtil
{
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int CAPTURE_IMAGE_REQUEST = 301;
    public static final int RECORD_VIDEO_REQUEST = 302;
    public String storagePath = "";
    public static final String FILE_PATH = "FILE_PATH";
    public static final String IS_PHOTO = "IS_PHOTO";
    private Uri fileUri; // file url to store image/video

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "WhistleBlower";
    Activity activity;

    public CameraUtil(Activity activity)
    {
        this.activity = activity;
    }

    public void setUp(MiscUtil util)
    {
        FloatingActionButton buttonPhoto = (FloatingActionButton) activity.findViewById(R.id.photo);
        buttonPhoto.setIconDrawable(util.getIcon(FontAwesomeIcon.CAMERA));
        buttonPhoto.setStrokeVisible(false);
        buttonPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                captureImage();
            }
        });

        FloatingActionButton buttonVideo = (FloatingActionButton) activity.findViewById(R.id.video);
        buttonVideo.setIconDrawable(util.getIcon(FontAwesomeIcon.VIDEO));
        buttonVideo.setStrokeVisible(false);
        buttonVideo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                recordVideo();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean isCameraAndStoragePermissionsAvailable()
    {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public void captureImage()
    {
        if (!isCameraAndStoragePermissionsAvailable())
        {
            requestCameraAndStoragePermissions(MainActivity.IMAGE_AND_STORAGE_REQUEST);
        }
        else
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            // start the image capture Intent
            activity.startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
        }
    }

    public void recordVideo()
    {
        if (!isCameraAndStoragePermissionsAvailable())
        {
            requestCameraAndStoragePermissions(MainActivity.VIDEO_AND_STORAGE_REQUEST);
        }
        else
        {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
            // set video quality
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            // start the video capture Intent
            activity.startActivityForResult(intent, RECORD_VIDEO_REQUEST);
        }
    }

    public Uri getOutputMediaFileUri(int type)
    {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type)
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

    public void launchIssueEditor(boolean isPhoto)
    {
        Intent intent = new Intent(activity, AddIssueActivity.class);
        intent.putExtra(FILE_PATH, storagePath);
        intent.putExtra(IS_PHOTO, isPhoto);
        activity.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void requestCameraAndStoragePermissions(int requestCode)
    {
        MainActivity.requestPermission(
                Arrays.asList(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE),
                requestCode, activity);
    }
}
