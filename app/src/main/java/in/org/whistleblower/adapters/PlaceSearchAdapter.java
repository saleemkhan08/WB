package in.org.whistleblower.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

import in.org.whistleblower.R;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.models.FavPlaces;
import in.org.whistleblower.utilities.MiscUtil;

public class PlaceSearchAdapter extends RecyclerView.Adapter<PlaceSearchAdapter.PlaceSearchViewHolder>
{
    Context mContext;
    LayoutInflater inflater;
    List<FavPlaces> mAddressList;
    List<String> mFavPlacesList;
    MiscUtil mUtil;
    boolean mIsFavPlacesList;
    SharedPreferences preferences;

    public PlaceSearchAdapter(Context context, List<FavPlaces> addressList,List<String> favPlacesList, boolean isFavPlacesList)
    {
        mContext = context;
        mAddressList = addressList;
        mFavPlacesList = favPlacesList;
        mIsFavPlacesList = isFavPlacesList;
        inflater = LayoutInflater.from(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mUtil = new MiscUtil(mContext);
    }

    @Override
    public PlaceSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.place_search_result_row, parent, false);
        return new PlaceSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaceSearchViewHolder holder, final int position)
    {
        final FavPlaces address = mAddressList.get(position);
        holder.countryNameView.setText(address.country);
        holder.localeNameView.setText(address.locale);
        if (!mIsFavPlacesList)
        {
            if (mFavPlacesList.contains(address.locale))
            {
                disableButton(holder.addPlaceView);
            }
            else
            {
                holder.addPlaceView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        addPlace(address.country, address.locale,v);
                    }
                });
            }
        }
        else
        {
            holder.addPlaceView.setText("Remove");
            holder.addPlaceView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    removePlace(address.locale, position);
                }
            });
        }
    }

    private void removePlace(final String locale, final int position)
    {
        mUtil.showIndeterminateProgressDialog("Removing...");
        ParseQuery<ParseObject> query = new ParseQuery<>(FavPlaces.TABLE);
        query.whereEqualTo(FavPlaces.USER_GOOGLE_ID, preferences.getString(Accounts.GOOGLE_ID, ""));
        query.whereEqualTo(FavPlaces.LOCALE, locale);
        query.getFirstInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(final ParseObject object, ParseException e)
            {
                object.deleteInBackground(new DeleteCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        mUtil.toast("Removed " + locale);
                        mUtil.hideProgressDialog();
                        removeAt(position);
                    }
                });
            }
        });
    }

    public void removeAt(int position)
    {
        mAddressList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mAddressList.size());
    }

    private void addPlace(String country, final String locale, final View button)
    {
        mUtil.showIndeterminateProgressDialog("Adding...");
        ParseObject friend = new ParseObject(FavPlaces.TABLE);
        friend.put(FavPlaces.COUNTRY, country);
        friend.put(FavPlaces.LOCALE, locale);
        friend.put(FavPlaces.USER_GOOGLE_ID, preferences.getString(Accounts.GOOGLE_ID, ""));
        friend.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e == null)
                {
                    mUtil.hideProgressDialog();
                    mUtil.toast("Added " + locale + " to your favorite List");
                    disableButton(button);
                }
            }
        });
    }

    public void disableButton(View button)
    {
        button.setEnabled(false);
        ((Button) button).setText("Added");
    }

    @Override
    public int getItemCount()
    {
        return mAddressList.size();
    }

    class PlaceSearchViewHolder extends RecyclerView.ViewHolder
    {
        Button addPlaceView;
        TextView countryNameView, localeNameView;

        public PlaceSearchViewHolder(View itemView)
        {
            super(itemView);
            addPlaceView = (Button) itemView.findViewById(R.id.addPlaceButton);
            countryNameView = (TextView) itemView.findViewById(R.id.country);
            localeNameView = (TextView) itemView.findViewById(R.id.localeName);
        }
    }
}