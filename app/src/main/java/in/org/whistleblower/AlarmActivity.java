package in.org.whistleblower;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import in.org.whistleblower.fragments.NotifyLocationFragment;
import in.org.whistleblower.models.LocationAlarm;

public class AlarmActivity extends AppCompatActivity
{
    public static final String MESSAGE = "MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_acivity);

        TextView message = (TextView) findViewById(R.id.message);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "Gabriola.ttf");
        message.setTypeface(face);
        final LocationAlarm alarm = getIntent().getParcelableExtra(LocationAlarm.ALARM);
        message.setText("You've Reached : "+ NotifyLocationFragment.getAddressLines(alarm.address, 3));

        findViewById(R.id.stopAlarm).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(AlarmActivity.this, MainActivity.class));
            }
        });
    }


}
