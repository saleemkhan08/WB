package in.org.whistleblower.adapters;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.org.whistleblower.R;
import in.org.whistleblower.WhistleBlower;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.models.FavPlacesDao;
import in.org.whistleblower.singletons.Otto;
import in.org.whistleblower.utilities.MiscUtil;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>
{
    AppCompatActivity mActivity;
    LayoutInflater inflater;
    List<FavPlaces> mAddressList;
    MiscUtil mUtil;
    @Inject
    SharedPreferences preferences;

    public PlaceAdapter(AppCompatActivity activity, List<FavPlaces> addressList)
    {
        mActivity = activity;
        mAddressList = addressList;
        inflater = LayoutInflater.from(mActivity);
        mUtil = new MiscUtil(mActivity);
        WhistleBlower.getComponent().inject(this);
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
            }
        });

        holder.options.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(mActivity, v);
                popup.getMenuInflater()
                        .inflate(R.menu.fav_place_options, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.setAlarm:
                                setAlarm();
                                break;
                            case R.id.notify_loc:
                                notifyLoc();
                                break;
                            case R.id.deleteFavPlace:
                                deleteFavPlace(address.latitude, address.longitude);
                                removeAt(position);
                                Toast.makeText(mActivity, "Deleted : " + address.placeType, Toast.LENGTH_SHORT).show();
                                break;

                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    public void removeAt(int position)
    {
        mAddressList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mAddressList.size());
    }
    private void deleteFavPlace(String latitude, String longitude)
    {
        new FavPlacesDao(mActivity).delete(latitude, longitude);
    }

    private void notifyLoc()
    {
    }

    private void setAlarm()
    {
        
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

        @Bind(R.id.placeTypeImg)
        ImageView placeTypeImg;

        @Bind(R.id.favPlaceOptionsIcon)
        ImageView options;

        public PlaceViewHolder(View itemView)
        {
            super(itemView);
            item = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}