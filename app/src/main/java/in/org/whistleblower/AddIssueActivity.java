package in.org.whistleblower;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import in.org.whistleblower.icon.FontAwesomeIcon;
import in.org.whistleblower.icon.Icon;
import in.org.whistleblower.services.Util;

public class AddIssueActivity extends AppCompatActivity
{
    private ImageView imgPreview;
    private VideoView videoPreview;
    private ImageView playPause;
    private Util util;

    AddIssueListener listener;// = new AddIssueListener();
    private void setUpIcon(int id, Icon icon)
    {
        ImageView image = (ImageView) findViewById(id);
        image.setBackground(util.getIcon(icon));
        image.setOnClickListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /*
        if (!getSharedPreferences(MainActivity.WHISTLE_BLOWER_PREFERENCE, Context.MODE_PRIVATE).getBoolean(LoginActivity.LOGIN_STATUS, false))
        {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        */

        util = new Util(this);
        listener = new AddIssueListener(this);
        setContentView(R.layout.activity_add_issue);
        imgPreview = (ImageView) findViewById(R.id.image_view);
        videoPreview = (VideoView) findViewById(R.id.video_view);
        playPause = (ImageView) findViewById(R.id.play_pause_icon);

        findViewById(R.id.radius).setOnClickListener(listener);
        setUpIcon(R.id.zone, FontAwesomeIcon.TAGS);
        setUpIcon(R.id.anonymus,FontAwesomeIcon.ANONYMOUS);
        setUpIcon(R.id.post,FontAwesomeIcon.CIRCLE_ARROW_RIGHT);

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

       // /*

        Intent intent = getIntent();
        String path = intent.getStringExtra(MapsActivity.FILE_PATH);
        if (intent.getBooleanExtra(MapsActivity.IS_PHOTO, true))
        {
            Bitmap image = BitmapFactory.decodeFile(path);
            imgPreview.setImageBitmap(image);
            imgPreview.setVisibility(View.VISIBLE);
            videoPreview.setVisibility(View.GONE);
            playPause.setVisibility(View.GONE);
        }
        else
        {
            videoPreview.setVideoPath(path);
            videoPreview.setVisibility(View.VISIBLE);
            imgPreview.setVisibility(View.GONE);
            playPause.setVisibility(View.VISIBLE);
            videoPreview.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(videoPreview.isPlaying())
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
}
