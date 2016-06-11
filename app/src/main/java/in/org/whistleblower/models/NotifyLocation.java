package in.org.whistleblower.models;

import android.os.Parcel;
import android.os.Parcelable;

public class NotifyLocation implements Parcelable
{
    public static final String TABLE = "NotifyLocation";
    public static final String SENDER_EMAIL = "senderEmail";
    public static final String SENDER_NAME = "senderName";
    public static final String SENDER_PHOTO_URL = "senderPhotoUrl";
    public static final String RECEIVER_EMAIL = "receiverEmail";
    public static final String RECEIVER_NAME = "receiverName";
    public static final String RECEIVER_PHOTO_URL = "receiverPhotoUrl";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String MESSAGE = "message";
    public static final String RADIUS = "radius";
    public static final String STATUS = "status";


    public static final String FRAGMENT_TAG = "FRAGMENT_TAG";

    public String senderName, senderPhotoUrl, senderEmail, receiverName, receiverPhotoUrl, receiverEmail, latitude, longitude, message;
    public int radius, status;

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.senderName);
        dest.writeString(this.senderPhotoUrl);
        dest.writeString(this.senderEmail);
        dest.writeString(this.receiverName);
        dest.writeString(this.receiverPhotoUrl);
        dest.writeString(this.receiverEmail);
        dest.writeString(this.latitude);
        dest.writeString(this.longitude);
        dest.writeString(this.message);
        dest.writeInt(this.radius);
        dest.writeInt(this.status);
    }

    public NotifyLocation()
    {
    }

    protected NotifyLocation(Parcel in)
    {
        this.senderName = in.readString();
        this.senderPhotoUrl = in.readString();
        this.senderEmail = in.readString();
        this.receiverName = in.readString();
        this.receiverPhotoUrl = in.readString();
        this.receiverEmail = in.readString();
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.message = in.readString();
        this.radius = in.readInt();
        this.status = in.readInt();
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
