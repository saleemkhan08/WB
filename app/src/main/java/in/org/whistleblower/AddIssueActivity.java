package in.org.whistleblower;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import in.org.whistleblower.actions.Image;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.interfaces.DialogUtilListener;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.Issue;
import in.org.whistleblower.dao.IssuesDao;
import in.org.whistleblower.services.AddIssueService;
import in.org.whistleblower.utilities.DialogsUtil;
import in.org.whistleblower.utilities.ImageUtil;

public class AddIssueActivity extends AppCompatActivity implements View.OnClickListener, DialogUtilListener
{
    public static final String ISSUE_DATA = "issueData";
    private static final int REQUIRED_WIDTH = 640;
    public static final int REQUEST_CODE_IMAGE_PICKER = 1020;
    private ImageView imgPreview, editIcon;
    Bitmap image;
    String mImageUri;
    private EditText areaTypeName, description;
    SharedPreferences preferences;
    DialogsUtil dialogsUtil;
    private ImageView displayPic;
    private TextView username;
    private String mAddress;
    private LatLng mLatLng;
    private int mRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        dialogsUtil = new DialogsUtil(this);
        setContentView(R.layout.activity_add_issue);
        areaTypeName = (EditText) findViewById(R.id.areaTypeName);
        description = (EditText) findViewById(R.id.issueDescription);
        editIcon = (ImageView) findViewById(R.id.editIcon);
        editIcon.setOnClickListener(this);

        Intent intent = getIntent();
        mAddress = intent.getStringExtra(MapFragment.ADDRESS);
        mLatLng = intent.getParcelableExtra(MapFragment.LATLNG);
        mRadius = intent.getIntExtra(MapFragment.RADIUS, 100);

        areaTypeName.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    editIcon.setVisibility(View.GONE);
                }
                else
                {
                    editIcon.setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.zone).setOnClickListener(this);
        findViewById(R.id.anonymous).setOnClickListener(this);
        findViewById(R.id.gallery).setOnClickListener(this);
        findViewById(R.id.camera).setOnClickListener(this);


        imgPreview = (ImageView) findViewById(R.id.issueImage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        showAreaNameAndTypeDetails();
        showProfileDetails();
    }

    private boolean isMyServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            String LocationUpdateServiceName = getPackageName() + "LocationDetailsService";

            if (LocationUpdateServiceName.equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void showProfileDetails()
    {
        displayPic = (ImageView) findViewById(R.id.profilePic);
        username = (TextView) findViewById(R.id.username);
        String dpUrl = preferences.getString(Accounts.PHOTO_URL, "");
        Drawable anonymous = getDrawable(R.drawable.anonymous_white_primary_dark);
        if (preferences.getBoolean(IssuesDao.ANONYMOUS, false))
        {
            displayPic.setImageDrawable(anonymous);
            username.setText("Anonymous");
        }
        else
        {
            username.setText(preferences.getString(Accounts.NAME, "Anonymous"));
            if (dpUrl.trim().isEmpty())
            {
                displayPic.setBackground(anonymous);
            }
            else
            {
                ImageUtil.displayImage(this, dpUrl, displayPic, true);
            }
        }
    }

    @Override
    public void showAreaNameAndTypeDetails()
    {
        String placeTypeNameText = "@" + mAddress;
        String temp = dialogsUtil.getSelectedZonesStrings();
        placeTypeNameText += ",\n" + temp;
        areaTypeName.setText(placeTypeNameText);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            NavUtils.navigateUpFromSameTask(this);
        }
        if (id == R.id.postIssue)
        {
            addIssue();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.add_issue, menu);
        return true;
    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        switch (id)
        {
            case R.id.zone:
                dialogsUtil.showZoneDialog();
                break;
            case R.id.anonymous:
                dialogsUtil.showAnonymousDialog();
                break;
            case R.id.camera:
                Image.captureImage(this);
                break;
            case R.id.gallery:
                imgPreview.setImageDrawable(null);
                Crop.pickImage(this, REQUEST_CODE_IMAGE_PICKER);

                break;
            case R.id.editIcon:
                areaTypeName.requestFocus();
                break;
        }
    }

    private void addIssue()
    {
        if(mImageUri != null && mImageUri.contains(".png") )
        {
            Intent intent = new Intent(this, AddIssueService.class);
            Issue issue = new Issue();
            issue.anonymous = preferences.getBoolean(IssuesDao.ANONYMOUS, false);
            issue.areaType = areaTypeName.getText().toString();
            issue.description = description.getText().toString();
            issue.imgUrl = mImageUri;
            issue.latitude = mLatLng.latitude + "";
            issue.longitude = mLatLng.longitude + "";
            issue.radius = mRadius;
            issue.userDpUrl = preferences.getString(Accounts.PHOTO_URL, "");
            issue.username = preferences.getString(Accounts.NAME, "Anonymous");
            issue.userId = preferences.getString(Accounts.GOOGLE_ID, "");
            if (issue.anonymous)
            {
                issue.userDpUrl = "";
                issue.username = "Anonymous";
            }

            intent.putExtra(ISSUE_DATA, issue);
            startService(intent);

            Toast.makeText(this, "Please See the Notification for progress", Toast.LENGTH_LONG).show();
            intent = new Intent(AddIssueActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            Toast.makeText(this, "Please Upload An Image!", Toast.LENGTH_SHORT).show();
        }
    }

    private void beginCrop(Uri source)
    {
        Uri destination = Uri.fromFile(new File(Image.getMediaStorageDir(), "temp.png"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result)
    {
        if (resultCode == RESULT_OK)
        {
            Uri imgUri = Crop.getOutput(result);
            imgPreview.setImageURI(imgUri);
            mImageUri = imgUri.getPath();
            Toast.makeText(this, mImageUri,Toast.LENGTH_SHORT).show();
        }
        else if (resultCode == Crop.RESULT_ERROR)
        {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_CODE_IMAGE_PICKER:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        beginCrop(data.getData());
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        Toast.makeText(this, "Please Try Again!", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case Crop.REQUEST_CROP:
                handleCrop(resultCode, data);
                break;
            case Image.CAPTURE_IMAGE_REQUEST:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        Crop.of(Image.imageUri, Image.imageUri).asSquare().start(this);
                        /*image = getCompressedImageFile(mImageUri);
                        if (image == null)
                        {
                            Toast.makeText(this, "Please Try Again!", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            imgPreview.setImageBitmap(image);
                        }*/
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
            case Image.LOAD_GALLERY_REQUEST:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};

                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        mImageUri = copy(cursor.getString(columnIndex));
                        image = getCompressedImageFile(mImageUri);
                        if (image == null)
                        {
                            Toast.makeText(this, "Please Try Again!", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            imgPreview.setImageBitmap(image);
                        }
                        cursor.close();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    private String copy(String src)
    {
        // External sdcard location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Image.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists())
        {
            if (mediaStorageDir.mkdirs())
            {
                mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            }
        }

        String dest = mediaStorageDir + "/temp.png";
        Log.d("Img Size", "Dest : " + dest);
        Log.d("Img Size", "Src : " + src);

        try
        {
            File srcFile = new File(src);
            File destFile = new File(dest);
            Log.d("Img Size", "Try");
            if (!destFile.exists())
            {
                Log.d("Img Size", "file does not exist");
                if (!destFile.createNewFile())
                {
                    Log.d("Img Size", "Couldn't Create File");
                    return null;
                }
                Log.d("Img Size", "File Created");
            }
            Log.d("Img Size", "file exist");
            InputStream inputStream = new FileInputStream(srcFile);
            OutputStream outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception e)
        {
            Log.d("Img Size", e.getMessage());
            e.printStackTrace();
            return null;
        }
        return dest;
    }

    public Bitmap getCompressedImageFile(final String path)
    {
        if (path == null)
        {
            return null;
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        Log.d("Img Size", "Before : " + options.outWidth + "x" + options.outHeight);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, REQUIRED_WIDTH);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        final Bitmap compressedBitmap = BitmapFactory.decodeFile(path, options);

        Log.d("Img Size", "After : " + options.outWidth + "x" + options.outHeight);

        new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    File file = new File(path);
                    FileOutputStream outputStream = new FileOutputStream(file);
                    compressedBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
                    outputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }.run();
        return compressedBitmap;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth)
    {
        final int width = options.outWidth;
        float inSampleSize = 1;
        if (width > reqWidth)
        {
            inSampleSize = (int) Math.ceil((float) width / (float) reqWidth) + 1;
        }
        Log.d("Img Size", inSampleSize + "");
        return (int) inSampleSize;
    }
}
