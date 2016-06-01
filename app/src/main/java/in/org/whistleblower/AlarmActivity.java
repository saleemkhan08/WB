package in.org.whistleblower;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import in.org.whistleblower.custom.view.RippleBackground;
import in.org.whistleblower.fragments.NotifyLocationFragment;
import in.org.whistleblower.models.LocationAlarm;

public class AlarmActivity extends AppCompatActivity
{
    RippleBackground rippleBackground;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_acivity);

//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        isScreenOn = pm.isInteractive();
//
//        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        locked = km.inKeyguardRestrictedInputMode();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        TextView message = (TextView) findViewById(R.id.message);
        TextView youHaveReached = (TextView) findViewById(R.id.youHaveReached);

        Typeface face = Typeface.createFromAsset(getAssets(),
                "Gabriola.ttf");
        youHaveReached.setTypeface(face);
        message.setTypeface(face);

        final LocationAlarm alarm = getIntent().getParcelableExtra(LocationAlarm.ALARM);
        message.setText(NotifyLocationFragment.getAddressLines(alarm.address, 3));

        findViewById(R.id.stopAlarm).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                stop();
                startActivity(new Intent(AlarmActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    MediaPlayer mediaPlayer;
    Vibrator vibrator;

    @Override
    protected void onResume()
    {
        super.onResume();
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        mediaPlayer.start(); // no need to call prepare(); create() does that for you
        mediaPlayer.setLooping(true);
        rippleBackground = (RippleBackground) findViewById(R.id.content);
        rippleBackground.startRippleAnimation();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 200, 975};
        vibrator.vibrate(pattern, 0);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        stop();
    }

    void stop()
    {
        mediaPlayer.stop();
        vibrator.cancel();
        rippleBackground.stopRippleAnimation();
    }
}
