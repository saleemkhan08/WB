package in.org.whistleblower.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.org.whistleblower.R;
import in.org.whistleblower.fragments.MapFragment;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.utilities.MiscUtil;
import in.org.whistleblower.utilities.NavigationUtil;

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
        holder.adminAreaView.setText(address.addressLine1);
        holder.subLocalityView.setText(address.addressLine0);
        holder.featureName.setText(address.featureName);
        holder.item.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Bundle bundle = new Bundle();
                bundle.putFloat(MapFragment.LONGITUDE,address.longitude);
                bundle.putFloat(MapFragment.LATITUDE, address.latitude);
                bundle.putBoolean(MapFragment.SHOW_MARKER, true);
                bundle.putBoolean(MapFragment.ANIMATE, true);
                bundle.putInt(MapFragment.RADIUS, 1000);
                bundle.putInt(MapFragment.MARKER, R.drawable.star_marker);
                NavigationUtil.showMapFragment((AppCompatActivity)mContext, bundle);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mAddressList.size();
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder
    {
        TextView adminAreaView, subLocalityView, featureName;
        View item;

        public PlaceViewHolder(View itemView)
        {
            super(itemView);
            adminAreaView = (TextView) itemView.findViewById(R.id.adminArea);
            subLocalityView = (TextView) itemView.findViewById(R.id.subLocality);
            featureName = (TextView) itemView.findViewById(R.id.featureName);
            item = itemView;
        }
    }
}