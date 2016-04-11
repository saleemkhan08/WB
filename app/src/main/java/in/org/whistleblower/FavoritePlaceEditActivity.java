package in.org.whistleblower;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.models.FavPlacesDao;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;

public class FavoritePlaceEditActivity extends AppCompatActivity implements View.OnClickListener
{
    GoogleMap mMap;
    static LatLng latLng;
    static EditText placeNameEditView;
    FavPlacesDao favPlacesDao;
    static FavPlaces favPlace;
    static View progressBar, retryButton;
    //String whereClause;
    static TextView address, coordinates;
    static Context mContext;
    static FloatingActionButton fab;
    GeoCoderTask mGeoCoderTask;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_place_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mContext = this;
        latLng = getIntent().getParcelableExtra("LatLang");
        progressBar = findViewById(R.id.uploadProgressBar);
        retryButton = findViewById(R.id.retry);
        retryButton.setOnClickListener(this);
        favPlacesDao = new FavPlacesDao(FavoritePlaceEditActivity.this);

        address = ((TextView) findViewById(R.id.address));
        favPlace = new FavPlaces();
        coordinates = (TextView) findViewById(R.id.coordinates);

        String coordinate = "Latitude : " + (float) latLng.latitude
                + "\n" + "Longitude : " + (float) latLng.longitude;

        coordinates.setText(coordinate);

        placeNameEditView = ((EditText) findViewById(R.id.placeName));

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener()
        {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset)
            {
                if (scrollRange == -1)
                {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0)
                {
                    collapsingToolbarLayout.setTitle("Favorite Place");
                    isShow = true;
                }
                else if (isShow)
                {
                    collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.save_fav_place);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String placeName = String.valueOf(placeNameEditView.getText());
                if (placeName == null || placeName.isEmpty())
                {
                    Toast.makeText(mContext, "Please Enter The Place Name!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    favPlace.featureName = placeName;
                    favPlacesDao.insert(favPlace);
                    finish();
                }
            }
        });
        if (NavigationUtil.isGoogleServicesOk(this))
        {
            if (initMap())
            {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.addMarker(new MarkerOptions()
                        .icon(MiscUtil.getMapMarker(this, R.drawable.star_marker, 60))
                        .position(latLng));
                mMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(1000)
                        .strokeWidth(2)
                        .strokeColor(getResources().getColor(R.color.colorAccent, null))
                        .fillColor(getResources().getColor(R.color.my_location_radius, null)));

                mGeoCoderTask = new GeoCoderTask();
                mGeoCoderTask.execute();
            }
        }

    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (mGeoCoderTask != null && mGeoCoderTask.getStatus().equals(AsyncTask.Status.RUNNING))
        {
            mGeoCoderTask.cancel(true);
        }
    }

    private boolean initMap()
    {
        if (mMap == null)
        {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fav_loc_map);
            mMap = mapFragment.getMap();
        }
        return (mMap != null);
    }

    @Override
    public void onBackPressed()
    {
        //favPlacesDao.delete(whereClause);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v)
    {
        mGeoCoderTask = new GeoCoderTask();
        mGeoCoderTask.execute();
    }

    static class GeoCoderTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
            progressBar.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
            retryButton.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                Geocoder gcd = new Geocoder(mContext);
                Address address = gcd.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                if (address != null)
                {
                    favPlace.featureName = address.getFeatureName();
                    favPlace.addressLine0 = address.getAddressLine(0);
                    favPlace.addressLine1 = address.getAddressLine(1);
                    favPlace.subLocality = address.getSubLocality();
                    favPlace.locality = address.getLocality();
                    favPlace.subAdminArea = address.getSubAdminArea();
                    favPlace.adminArea = address.getAdminArea();
                    favPlace.country = address.getCountryName();
                    favPlace.postalCode = address.getPostalCode();
                    favPlace.latitude = (float) latLng.latitude;
                    favPlace.longitude = (float) latLng.longitude;
                    return true;
                }
            }
            catch (Exception e)
            {
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            progressBar.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            if (result)
            {
                String addr = favPlace.addressLine0 + "\n" + favPlace.addressLine1;
                address.setText(addr);
                placeNameEditView.setText(favPlace.featureName != null ? favPlace.featureName : "");
            }
            else
            {
                retryButton.setVisibility(View.VISIBLE);
                Toast.makeText(mContext, "Please Retry!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
