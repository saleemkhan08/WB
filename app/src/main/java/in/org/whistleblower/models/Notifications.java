package in.org.whistleblower.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Notifications implements Parcelable
{
    public static final String UPDATE_NOTIFICATION_STATUS = "updateNotificationStatus";
    public static final String KEY_INITIATE_SHARE_LOCATION = "initiateShareLocation";
    public String senderEmail, senderName, message, senderLatitude, senderLongitude, senderPhotoUrl, type;
    public long timeStamp, id, serverNotificationId;
    public int status;

    public static final int READ = 2;
    public static final int UNREAD = 1;
    public static final int NOT_DOWNLOADED = 0;

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
    public static final String SENDER_LATITUDE = "senderLatitude";
    public static final String SENDER_LONGITUDE = "senderLongitude";
    public static final String TIME_STAMP = "timeStamp";
    public static final String RECEIVER_STATUS = "receiverStatus";
    public static final String TYPE = "type";
    public static final String SERVER_NOTIFICATION_ID = "serverNotificationId";

    public Notifications()
    {
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.senderEmail);
        dest.writeString(this.senderName);
        dest.writeString(this.message);
        dest.writeString(this.senderLatitude);
        dest.writeString(this.senderLongitude);
        dest.writeString(this.senderPhotoUrl);
        dest.writeString(this.type);
        dest.writeLong(this.timeStamp);
        dest.writeLong(this.id);
        dest.writeLong(this.serverNotificationId);
        dest.writeInt(this.status);
    }

    protected Notifications(Parcel in)
    {
        this.senderEmail = in.readString();
        this.senderName = in.readString();
        this.message = in.readString();
        this.senderLatitude = in.readString();
        this.senderLongitude = in.readString();
        this.senderPhotoUrl = in.readString();
        this.type = in.readString();
        this.timeStamp = in.readLong();
        this.id = in.readLong();
        this.serverNotificationId = in.readLong();
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
