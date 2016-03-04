package in.org.whistleblower.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.org.whistleblower.R;
import in.org.whistleblower.models.FavPlaces;
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
        holder.adminAreaView.setText(address.subAdminArea + ", " + address.adminArea);
        holder.subLocalityView.setText(address.featureName + ", " + address.subLocality);
    }

    @Override
    public int getItemCount()
    {
        return mAddressList.size();
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder
    {
        TextView adminAreaView, subLocalityView;

        public PlaceViewHolder(View itemView)
        {
            super(itemView);
            adminAreaView = (TextView) itemView.findViewById(R.id.adminArea);
            subLocalityView = (TextView) itemView.findViewById(R.id.subLocality);
        }
    }
}