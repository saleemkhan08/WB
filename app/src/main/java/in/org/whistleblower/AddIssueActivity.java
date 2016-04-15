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
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import in.org.whistleblower.actions.Image;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.IssuesDao;
import in.org.whistleblower.utilities.AddIssueService;
import in.org.whistleblower.interfaces.DialogUtilListener;
import in.org.whistleblower.utilities.DialogsUtil;
import in.org.whistleblower.utilities.ImageUtil;
import in.org.whistleblower.utilities.MiscUtil;

public class AddIssueActivity extends AppCompatActivity implements View.OnClickListener, DialogUtilListener
{
    private ImageView imgPreview, editIcon;
    Bitmap image;
    String mImageUri;
    private EditText areaTypeName, description;
    SharedPreferences preferences;
    DialogsUtil dialogsUtil;
    private ImageView displayPic;
    private TextView username;

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

        findViewById(R.id.radius).setOnClickListener(this);
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
        String placeTypeNameText = preferences.getString(MapFragment.ADDRESS, "@Unknown Place");
        String temp = dialogsUtil.getSelectedZonesStrings();
        placeTypeNameText += ", " + temp;
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
            case R.id.radius:
                dialogsUtil.showRadiusDialog();
                break;
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
                Image.getImageFromGallery(this);
                break;
            case R.id.editIcon:
                areaTypeName.requestFocus();
                break;
        }
    }

    private void addIssue()
    {
        Intent intent = new Intent(this, AddIssueService.class);

        intent.putExtra(IssuesDao.DESCRIPTION, description.getText().toString());
        intent.putExtra(IssuesDao.IMAGE_LOCAL_URI, mImageUri);
        intent.putExtra(IssuesDao.AREA_TYPE, areaTypeName.getText().toString());
        startService(intent);

        Toast.makeText(this, "Please See the Notification for progress", Toast.LENGTH_LONG).show();
        intent = new Intent(AddIssueActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case Image.CAPTURE_IMAGE_REQUEST:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                        mImageUri = Image.storagePath;
                        image = getCompressedImageFile(mImageUri);
                        imgPreview.setImageBitmap(image);
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
                        mImageUri = cursor.getString(columnIndex);
                        image = getCompressedImageFile(mImageUri);
                        imgPreview.setImageBitmap(image);
                        cursor.close();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    Bitmap getCompressedImageFile(String path)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        try
        {
            bitmap = MiscUtil.getResizedBitmap(bitmap, 1080, 1080);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 75, bos);
            byte[] bitmapData = bos.toByteArray();
            File f = new File(path);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            bitmap = BitmapFactory.decodeFile(path);
        }catch (Exception e)
        {

        }
        return bitmap;
    }
}
