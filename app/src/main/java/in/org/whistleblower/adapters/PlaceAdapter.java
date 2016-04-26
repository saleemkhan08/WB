package in.org.whistleblower.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.org.whistleblower.R;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.MiscUtil;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>
{
    Context mContext;
    LayoutInflater inflater;
    List<FavPlaces> mAddressList;
    MiscUtil mUtil;
    SharedPreferences preferences;

    public PlaceAdapter(Context context, List<FavPlaces> addressList)
    {
        mContext = context;
        mAddressList = addressList;
        inflater = LayoutInflater.from(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mUtil = new MiscUtil(mContext);
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.place_search_result_row, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder holder, final int position)
    {
        final FavPlaces address = mAddressList.get(position);
        holder.addressLine.setText(address.addressLine);
        holder.placeTypeName.setText(address.placeType);
        holder.placeTypeImg.setImageResource(getDrawableResId(address.placeTypeIndex));
        holder.item.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Otto.getBus().post(address);
                /*Bundle bundle = new Bundle();
                bundle.putString(MapFragment.LONGITUDE, address.longitude + "");
                bundle.putString(MapFragment.LATITUDE, address.latitude + "");
                bundle.putBoolean(MapFragment.ANIMATE, true);
                bundle.putInt(MapFragment.ACCURACY, address.radius);
                bundle.putInt(MapFragment.MARKER, address.placeTypeIndex);
                NavigationUtil.showMapFragment((AppCompatActivity) mContext, bundle);*/
            }
        });
    }

    int getDrawableResId(int index)
    {
        /*
        <item>Home</item>
        <item>Friends Place</item>
        <item>Work Place</item>
        <item>School / College</item>
        <item>Shopping Mall</item>
        <item>Cinema Hall</item>
        <item>Library</item>
        <item>Play Ground</item>
        <item>Hospital</item>
        <item>Jogging Place</item>
        <item>Gym</item>
        <item>Restaurant</item>
        <item>Coffee Shop</item>
        <item>Pub</item>
        <item>Others</item>
        */
        switch (index)
        {
            case 0:
                return R.mipmap.home_primary_dark;
            case 1:
                return R.mipmap.friends_place_primary_dark;
            case 2:
                return R.mipmap.work_primary_dark;
            case 3:
                return R.mipmap.school_primary_dark;
            case 4:
                return R.mipmap.shopping_primary_dark;
            case 5:
                return R.mipmap.movie_primary_dark;
            case 6:
                return R.mipmap.library_primay_dark;
            case 7:
                return R.mipmap.play_ground_primary_dark;
            case 8:
                return R.mipmap.hospital_primary_dark;
            case 9:
                return R.mipmap.jogging_primary_dark;
            case 10:
                return R.mipmap.gym_primary_dark;
            case 11:
                return R.mipmap.hotel_primary_dark;
            case 12:
                return R.mipmap.coffee_shop_primay_dark;
            case 13:
                return R.mipmap.bar_primary_dark;
            default:
                return R.mipmap.others_primary_dark;
        }
    }

    @Override
    public int getItemCount()
    {
        return mAddressList.size();
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder
    {
        View item;

        @Bind(R.id.addressLine)
        TextView addressLine;

        @Bind(R.id.placeTypeName)
        TextView placeTypeName;

        @Bind(R.id.deleteFavPlace)
        View deleteFavPlace;

        @Bind(R.id.setAlarm)
        View setAlarm;

        @Bind(R.id.arrivalNotification)
        View arrivalNotification;

        @Bind(R.id.placeTypeImg)
        ImageView placeTypeImg;

        public PlaceViewHolder(View itemView)
        {
            super(itemView);
            item = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}