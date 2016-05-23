package in.org.whistleblower;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import java.util.List;

import in.org.whistleblower.adapters.ShareLocationListAdapter;
import in.org.whistleblower.models.OttoCommunicator;
import in.org.whistleblower.models.ShareLocation;
import in.org.whistleblower.models.ShareLocationDao;
import in.org.whistleblower.services.LocationTrackingService;
import in.org.whistleblower.singletons.Otto;

public class LocationListActivity extends AppCompatActivity
{
    RecyclerView recyclerView;
    public static final String SHARE_LOCATION_FRAGMENT = "shareLocationFragment";
    public static final String SHARE_LOCATION_LIST = "shareLocationList";
    public static final String FRIEND_LIST = "friendList";
    public static final String FINISH_ACTIVITY = "FINISH_ACTIVITY";
    private SharedPreferences preferences;
    View closeDialog;
    ViewGroup emptyList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        recyclerView = (RecyclerView) findViewById(R.id.common_list);
        closeDialog = findViewById(R.id.closeDialog);
        emptyList = (ViewGroup) findViewById(R.id.emptyList);
        closeDialog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        Otto.register(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = getIntent();
        if (intent != null)
        {
            if (intent.hasExtra(SHARE_LOCATION_LIST))
            {
                ShareLocationDao dao = new ShareLocationDao(this);
                List<ShareLocation> shareLocationList = dao.getList();

                recyclerView.setAdapter(new ShareLocationListAdapter(this, shareLocationList));
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                if (shareLocationList.size() < 1)
                {
                    showShareLocationIntroCard();
                }
            }
        }
    }

    @Subscribe
    public void ottoCommunicator(String action)
    {
        switch (action)
        {
            case FINISH_ACTIVITY :
                showShareLocationIntroCard();
                break;
        }
    }

    private void showShareLocationIntroCard()
    {
        preferences.edit().putBoolean(LocationTrackingService.KEY_SHARE_LOCATION_REAL_TIME, false).commit();
        Otto.post(new OttoCommunicator(LocationTrackingService.STOP_SERVICE));
        TransitionManager.beginDelayedTransition(emptyList, new Slide());
        emptyList.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }
}
