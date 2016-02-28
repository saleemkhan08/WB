package in.org.whistleblower;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

import in.org.whistleblower.actions.Image;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.icon.FontAwesomeIcon;
import in.org.whistleblower.icon.Icon;
import in.org.whistleblower.icon.IconicIcon;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.IssuesDao;
import in.org.whistleblower.storage.RStorageObject;
import in.org.whistleblower.storage.StorageListener;
import in.org.whistleblower.utilities.DialogUtilListener;
import in.org.whistleblower.utilities.DialogsUtil;
import in.org.whistleblower.utilities.MiscUtil;

public class AddIssueActivity extends AppCompatActivity implements View.OnClickListener, DialogUtilListener
{
    private ImageView imgPreview, editIcon;
    Bitmap image;
    private VideoView videoPreview;
    private ImageView playPause;
    private MiscUtil mUtil;
    private EditText placeTypeName, description;
    String filePath;
    SharedPreferences preferences;
    DialogsUtil dialogsUtil;
    private boolean isPhoto;
    private ImageView displayPic;
    private TextView username;


    private void setUpIcon(int id, Icon icon, int color)
    {
        ImageView image = (ImageView) findViewById(id);
        image.setBackground(mUtil.getIcon(icon, color));
        image.setOnClickListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        MiscUtil.log("Add Issue OnResume");
        showAreaNameAndTypeDetails();
        showProfileDetails();
    }

    @Override
    public void showProfileDetails()
    {
        displayPic = (ImageView) findViewById(R.id.displayPic);
        username = (TextView) findViewById(R.id.username);
        String dpUrl = preferences.getString(Accounts.PHOTO_URL, "");
        if (preferences.getBoolean(IssuesDao.ANONYMOUS, false))
        {
            displayPic.setBackground(mUtil.getIcon(FontAwesomeIcon.ANONYMOUS, R.color.white));
            displayPic.setImageDrawable(null);
            username.setText("Anonymous");
        }
        else
        {
            displayPic.setBackground(null);

            username.setText(preferences.getString(Accounts.NAME, "Anonymous"));
            if (dpUrl == null)
            {
                displayPic.setBackground(mUtil.getIcon(FontAwesomeIcon.ANONYMOUS, R.color.white));
            }
            else if (dpUrl.trim().isEmpty())
            {
                displayPic.setBackground(mUtil.getIcon(FontAwesomeIcon.ANONYMOUS, R.color.white));
            }
            else
            {
                DisplayImageOptions dpOptions = new DisplayImageOptions.Builder()
                        .showImageOnLoading(R.drawable.ic_stub)
                        .showImageForEmptyUri(R.drawable.ic_empty)
                        .showImageOnFail(R.drawable.ic_error)
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .considerExifParams(true)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .displayer(new RoundedBitmapDisplayer(100))
                        .build();
                ImageLoader mImageLoader = ImageLoader.getInstance();
                mImageLoader.init(ImageLoaderConfiguration.createDefault(this));
                mImageLoader.displayImage(dpUrl, displayPic, dpOptions);
            }
        }
    }

    @Override
    public void showAreaNameAndTypeDetails()
    {
        String placeTypeNameText = preferences.getString(MapFragment.ADDRESS, "@Unknown Place");
        String temp = dialogsUtil.getSelectedZonesStrings();
        placeTypeNameText += ", " + temp;
        placeTypeName.setText(placeTypeNameText);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mUtil = new MiscUtil(this);

        if (!mUtil.hasUserSignedIn())
        {
            finish();
        }
        else
        {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            dialogsUtil = new DialogsUtil(this);
            setContentView(R.layout.activity_add_issue);
            placeTypeName = (EditText) findViewById(R.id.placeTypeName);
            description = (EditText) findViewById(R.id.issueTitle);
            editIcon = (ImageView) findViewById(R.id.editIcon);
            editIcon.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mUtil.toast("Edit");
                    placeTypeName.requestFocus();
                }
            });
            placeTypeName.setOnFocusChangeListener(new View.OnFocusChangeListener()
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
            imgPreview = (ImageView) findViewById(R.id.image_view);
            videoPreview = (VideoView) findViewById(R.id.video_view);
            playPause = (ImageView) findViewById(R.id.play_pause_icon);

            setUpIcon(R.id.radius, IconicIcon.LOCATION_INV, R.color.colorAccent);
            setUpIcon(R.id.editIcon, FontAwesomeIcon.PENCIL, R.color.white);
            setUpIcon(R.id.zone, FontAwesomeIcon.TAGS, R.color.colorAccent);
            setUpIcon(R.id.anonymus, FontAwesomeIcon.ANONYMOUS, R.color.colorAccent);
            setUpIcon(R.id.post, FontAwesomeIcon.CIRCLE_ARROW_RIGHT, R.color.colorAccent);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
            {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
            }

            imgPreview.setVisibility(View.VISIBLE);
            videoPreview.setVisibility(View.GONE);
            playPause.setVisibility(View.GONE);

            ///*

            Intent intent = getIntent();
            filePath = intent.getStringExtra(Image.FILE_PATH);
            isPhoto = intent.getBooleanExtra(Image.IS_PHOTO, true);
            if (isPhoto)
            {
                image = BitmapFactory.decodeFile(filePath);
                imgPreview.setImageBitmap(image);
                imgPreview.setVisibility(View.VISIBLE);
                videoPreview.setVisibility(View.GONE);
                playPause.setVisibility(View.GONE);
            }
            else
            {
                videoPreview.setVideoPath(filePath);
                videoPreview.setVisibility(View.VISIBLE);
                imgPreview.setVisibility(View.GONE);
                playPause.setVisibility(View.VISIBLE);
                videoPreview.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (videoPreview.isPlaying())
                        {
                            videoPreview.stopPlayback();
                            playPause.setImageResource(R.drawable.ic_media_play);
                        }
                        else
                        {
                            playPause.setImageResource(R.drawable.ic_media_pause);
                            videoPreview.start();
                        }
                    }
                });
            }//*/
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v)
    {
        //mUtil.toast("clicked");
        int id = v.getId();
        switch (id)
        {
            case R.id.radius:
                dialogsUtil.showRadiusDialog();
                break;
            case R.id.zone:
                dialogsUtil.showZoneDialog();
                break;
            case R.id.anonymus:
                dialogsUtil.showAnonymousDialog();
                break;
            case R.id.post:
                addIssue();
                break;
        }
    }

    private void addIssue()
    {
        boolean isAnonymous = preferences.getBoolean(IssuesDao.ANONYMOUS, false);

        RStorageObject issue = new RStorageObject(IssuesDao.TABLE);
        issue.put(IssuesDao.DESCRIPTION, description.getText().toString());
        issue.put(IssuesDao.PLACE_NAME, placeTypeName.getText().toString());
        String username = "Anonymous",
                userProfilePic = "";

        if (!isAnonymous)
        {
            username = preferences.getString(Accounts.NAME, username);
            userProfilePic = preferences.getString(Accounts.PHOTO_URL, "");
        }

        issue.put(IssuesDao.USERNAME, username);
        issue.put(IssuesDao.IMAGE_URL, saveFile());
        issue.put(IssuesDao.USER_DP_URL, userProfilePic);
        issue.put(IssuesDao.USER_ID, preferences.getString(Accounts.GOOGLE_ID, ""));
        issue.put(IssuesDao.AREA_TYPE, preferences.getString(IssuesDao.AREA_TYPE, "#Unknown Type"));

        issue.put(IssuesDao.RADIUS, preferences.getInt(IssuesDao.RADIUS, 0));

        issue.put(IssuesDao.LATITUDE, preferences.getFloat(MapFragment.LATITUDE, 0));
        issue.put(IssuesDao.LONGITUDE, preferences.getFloat(MapFragment.LONGITUDE, 0));

        issue.store(new StorageListener()
        {
            @Override
            public void onSuccess()
            {
                mUtil.hideProgressDialog();
                Intent intent = new Intent(AddIssueActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                mUtil.toast("Saved");
            }

            @Override
            public void onError(String e)
            {
                mUtil.hideProgressDialog();
                mUtil.toast(e);
            }
        });
    }

    private ParseFile saveFile()
    {
        mUtil.showProgressDialog("Uploading ...");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 75, stream);
        byte[] imageBytes = stream.toByteArray();
        ParseFile file = new ParseFile("issue.jpg", imageBytes);
        file.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                mUtil.mProgressDialog.setMessage("Saving ...");
                mUtil.mProgressDialog.setIndeterminate(true);
            }
        }, new ProgressCallback()
        {
            @Override
            public void done(Integer percentDone)
            {
                mUtil.mProgressDialog.setProgress(percentDone);
            }
        });
        return file;
    }
}
