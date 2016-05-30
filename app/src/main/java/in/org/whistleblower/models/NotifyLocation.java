package in.org.whistleblower.models;

import android.os.Parcel;
import android.os.Parcelable;

public class NotifyLocation implements Parcelable
{
    public static final String EMAIL = "email";
    public static final String TABLE = "NotifyLocation";
    public static final String NAME = "name";
    public static final String USER_EMAIL = "UserEmail";
    public static final String PHOTO_URL = "photoUrl";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String MESSAGE = "message";
    public static final String RADIUS = "radius";

    public static final String FRAGMENT_TAG = "FRAGMENT_TAG";

    public String name, email, userEmail, latitude, longitude, photoUrl, message;
    public int radius;

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
        dest.writeString(this.latitude);
        dest.writeString(this.longitude);
        dest.writeString(this.photoUrl);
        dest.writeString(this.message);
        dest.writeInt(this.radius);
    }

    public NotifyLocation()
    {
    }

    protected NotifyLocation(Parcel in)
    {
        this.name = in.readString();
        this.email = in.readString();
        this.userEmail = in.readString();
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.photoUrl = in.readString();
        this.message = in.readString();
        this.radius = in.readInt();
    }

    public static final Parcelable.Creator<NotifyLocation> CREATOR = new Parcelable.Creator<NotifyLocation>()
    {
        @Override
        public NotifyLocation createFromParcel(Parcel source)
        {
            return new NotifyLocation(source);
        }

        @Override
        public NotifyLocation[] newArray(int size)
        {
            return new NotifyLocation[size];
        }
    };
}
