package in.org.whistleblower.models;

import android.os.Parcel;
import android.os.Parcelable;

public class FavPlaces implements Parcelable
{
    public String latitude;
    public String longitude;
    public String addressLine;
    public String placeType;
    public int radius;
    public int placeTypeIndex;

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.latitude);
        dest.writeString(this.longitude);
        dest.writeString(this.addressLine);
        dest.writeString(this.placeType);
        dest.writeInt(this.radius);
        dest.writeInt(this.placeTypeIndex);
    }

    public FavPlaces()
    {
    }

    protected FavPlaces(Parcel in)
    {
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.addressLine = in.readString();
        this.placeType = in.readString();
        this.radius = in.readInt();
        this.placeTypeIndex = in.readInt();
    }

    public static final Parcelable.Creator<FavPlaces> CREATOR = new Parcelable.Creator<FavPlaces>()
    {
        @Override
        public FavPlaces createFromParcel(Parcel source)
        {
            return new FavPlaces(source);
        }

        @Override
        public FavPlaces[] newArray(int size)
        {
            return new FavPlaces[size];
        }
    };
}
