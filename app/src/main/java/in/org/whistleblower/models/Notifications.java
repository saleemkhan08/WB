package in.org.whistleblower.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Notifications implements Parcelable
{


    public String userEmail, name, message, latitude, longitude, photoUrl, type;
    public long timeStamp, id;
    public int status;
//    (senderEmail, senderName, senderPhotoUrl, receiverEmail, message, type, latitude, longitude, timeStamp, status)


    public static final int READ = 3;
    public static final int UNREAD = 2;
    public static final int DOWNLOAD = 1;
    public static final int NOT_DOWNLOAD = 0;

    public static final String TYPE_NOTIFY_LOCATION = "TYPE_NOTIFY_LOCATION";
    public static final String TYPE_SHARE_LOCATION_ONCE = "TYPE_SHARE_LOCATION_ONCE";
    public static final String TYPE_SHARE_LOCATION_CONTINUOUSLY = "TYPE_SHARE_LOCATION_CONTINUOUSLY";

    public static final String ID = "id";
    public static final String TABLE = "Notifications";
    public static final String SENDER_NAME = "senderName";
    public static final String SENDER_EMAIL = "senderEmail";
    public static final String SENDER_PHOTO_URL = "senderPhotoUrl";
    public static final String RECEIVER_EMAIL = "receiverEmail";
    public static final String MESSAGE = "message";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String TIME_STAMP = "timeStamp";
    public static final String STATUS = "status";
    public static final String TYPE = "type";

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.userEmail);
        dest.writeString(this.name);
        dest.writeString(this.message);
        dest.writeString(this.latitude);
        dest.writeString(this.longitude);
        dest.writeString(this.photoUrl);
        dest.writeString(this.type);
        dest.writeLong(this.timeStamp);
        dest.writeLong(this.id);
        dest.writeInt(this.status);
    }

    public Notifications()
    {
    }

    protected Notifications(Parcel in)
    {
        this.userEmail = in.readString();
        this.name = in.readString();
        this.message = in.readString();
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.photoUrl = in.readString();
        this.type = in.readString();
        this.timeStamp = in.readLong();
        this.id = in.readLong();
        this.status = in.readInt();
    }

    public static final Parcelable.Creator<Notifications> CREATOR = new Parcelable.Creator<Notifications>()
    {
        @Override
        public Notifications createFromParcel(Parcel source)
        {
            return new Notifications(source);
        }

        @Override
        public Notifications[] newArray(int size)
        {
            return new Notifications[size];
        }
    };
}
