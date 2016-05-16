package in.org.whistleblower.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ShareLocation implements Parcelable
{
    public static final String EMAIL = "email";
    public static final String TABLE = "ShareLocation";
    public static final String NAME = "name";
    public static final String USER_EMAIL = "UserEmail";
    public static final String PHOTO_URL = "photoUrl";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String LOCATION = "location";

    public String name, email, userEmail, photoUrl;

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.name);
        dest.writeString(this.email);
        dest.writeString(this.userEmail);
        dest.writeString(this.photoUrl);
    }

    public ShareLocation()
    {
    }

    protected ShareLocation(Parcel in)
    {
        this.name = in.readString();
        this.email = in.readString();
        this.userEmail = in.readString();
        this.photoUrl = in.readString();
    }

    public static final Parcelable.Creator<ShareLocation> CREATOR = new Parcelable.Creator<ShareLocation>()
    {
        @Override
        public ShareLocation createFromParcel(Parcel source)
        {
            return new ShareLocation(source);
        }

        @Override
        public ShareLocation[] newArray(int size)
        {
            return new ShareLocation[size];
        }
    };
}
