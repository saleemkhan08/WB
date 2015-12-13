package in.org.whistleblower;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import in.org.whistleblower.services.NavigationUtil;
import in.org.whistleblower.icon.FontAwesomeIcon;
import in.org.whistleblower.services.Util;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback
{
    public static final String FILE_PATH = "FILE_PATH";
    public static final String IS_PHOTO = "IS_PHOTO";
    private static final int REQUEST_PHOTO_STORAGE = 112;
    private static final int REQUEST_VIDEO_STORAGE = 113;
    public static final String REQUIRED_PERMISSION = "REQUIRED_PERMISSION";
    // Activity request codes
    Util util;
    private String storagePath = "";
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "WhistleBlower";

    private Uri fileUri; // file url to store image/video
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferenceEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        util = new Util(this);
        //Login Check
        if (!getSharedPreferences(MainActivity.WHISTLE_BLOWER_PREFERENCE, Context.MODE_PRIVATE).getBoolean(LoginActivity.LOGIN_STATUS, false))
        {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        setContentView(R.layout.activity_maps);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Navigation Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = ((NavigationView) findViewById(R.id.nav_view));
        navigationView.setNavigationItemSelectedListener(new NavigationUtil(drawer, this));

        Menu menu = navigationView.getMenu();
        menu.getItem(0).setIcon(util.getIcon(FontAwesomeIcon.HOME));
        menu.getItem(1).setIcon(util.getIcon(FontAwesomeIcon.MAP_MARKER));
        menu.getItem(2).setIcon(util.getIcon(FontAwesomeIcon.STAR));
        menu.getItem(3).setIcon(util.getIcon(FontAwesomeIcon.GROUP));
        menu.findItem(R.id.nav_share_loc).setIcon(util.getIcon(FontAwesomeIcon.SHARE_ALT));
        menu.findItem(R.id.nav_add_friend).setIcon(util.getIcon(FontAwesomeIcon.USER));

        menu.findItem(R.id.nav_logout).setIcon(util.getIcon(FontAwesomeIcon.SIGNOUT));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        preferences = getSharedPreferences(MainActivity.WHISTLE_BLOWER_PREFERENCE, Context.MODE_PRIVATE);
        preferenceEditor = preferences.edit();
        preferenceEditor.putBoolean(REQUIRED_PERMISSION, (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED));
        preferenceEditor.commit();

        FloatingActionButton buttonPhoto = (FloatingActionButton) findViewById(R.id.photo);

        buttonPhoto.setIconDrawable(util.getIcon(FontAwesomeIcon.CAMERA));
        buttonPhoto.setStrokeVisible(false);
        buttonPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!preferences.getBoolean(REQUIRED_PERMISSION, false))
                {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_PHOTO_STORAGE);
                }
                else
                {
                    captureImage();
                }
            }
        });

        FloatingActionButton buttonVideo = (FloatingActionButton) findViewById(R.id.video);
        buttonVideo.setIconDrawable(util.getIcon(FontAwesomeIcon.VIDEO));
        buttonVideo.setStrokeVisible(false);
        buttonVideo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!preferences.getBoolean(REQUIRED_PERMISSION, false))
                {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_VIDEO_STORAGE);
                }
                else
                {
                    recordVideo();
                }

            }
        });
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_PHOTO_STORAGE:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    captureImage();
                }
                else
                {
                    Toast.makeText(this, "To save images Locally this permissions are required!", Toast.LENGTH_LONG).show();
                }
            }
            break;

            case REQUEST_VIDEO_STORAGE:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    recordVideo();
                }
                else
                {
                    Toast.makeText(this, "To save videos Locally this permissions are required!", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    private void captureImage()
    {
        preferenceEditor.putBoolean(REQUIRED_PERMISSION, true).commit();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    private void recordVideo()
    {
        preferenceEditor.putBoolean(REQUIRED_PERMISSION, true).commit();
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        // name

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    public Uri getOutputMediaFileUri(int type)
    {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type)
    {
        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists())
        {
            mediaStorageDir.mkdirs();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                launchIssueEditor(true);
            }
            else if (resultCode == RESULT_CANCELED)
            {
                // user cancelled Image capture
                util.toast("User cancelled image capture");
            }
            else
            {
                util.toast("Sorry! Failed to capture image");
            }
        }
        else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                launchIssueEditor(false);
            }
            else if (resultCode == RESULT_CANCELED)
            {
                // user cancelled recording
                util.toast("User cancelled video recording");
            }
            else
            {
                // failed to record video
                util.toast("Sorry! Failed to record video");
            }
        }
    }

    private void launchIssueEditor(boolean isPhoto)
    {
        Intent intent = new Intent(this, AddIssueActivity.class);
        intent.putExtra(FILE_PATH, storagePath);
        intent.putExtra(IS_PHOTO, isPhoto);
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        LatLng sydney = new LatLng(-34, 151);
        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

}
