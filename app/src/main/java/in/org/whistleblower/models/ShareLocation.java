package in.org.whistleblower.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ShareLocation implements Parcelable
{
    public static final String SENDER_EMAIL = "senderEmail";
    public static final String TABLE = "ShareLocation";
    public static final String SENDER_NAME = "senderName";
    public static final String RECEIVER_EMAIL = "receiverEmail";
    public static final String SENDER_PHOTO_URL = "senderPhotoUrl";
    public static final String SENDER_LATITUDE = "senderLatitude";
    public static final String SENDER_LONGITUDE = "senderLongitude";
    public static final String SERVER_NOTIFICATION_ID = "serverNotificationId";
    public static final String LOCATION = "location";

    public String senderName, senderEmail, receiverEmail, senderPhotoUrl;
    public long serverNotificationId;

    public ShareLocation()
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
        dest.writeString(this.senderName);
        dest.writeString(this.senderEmail);
        dest.writeString(this.receiverEmail);
        dest.writeString(this.senderPhotoUrl);
        dest.writeLong(this.serverNotificationId);
    }

    protected ShareLocation(Parcel in)
    {
        this.senderName = in.readString();
        this.senderEmail = in.readString();
        this.receiverEmail = in.readString();
        this.senderPhotoUrl = in.readString();
        this.serverNotificationId = in.readLong();
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
