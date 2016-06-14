package in.org.whistleblower.models;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationData implements Parcelable
{
    public String actionIntentTag;
    public String contentText;
    public String actionIntentText;
    public int actionIntentIcon;
    public String contentTitle;
    public int notificationId;
    public String contentIntentTag;
    public boolean onGoing = false;
    public String largeIconUrl;
    public boolean vibrate;
    public String notificationType;

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.actionIntentTag);
        dest.writeString(this.contentText);
        dest.writeString(this.actionIntentText);
        dest.writeInt(this.actionIntentIcon);
        dest.writeString(this.contentTitle);
        dest.writeInt(this.notificationId);
        dest.writeString(this.contentIntentTag);
        dest.writeByte(onGoing ? (byte) 1 : (byte) 0);
        dest.writeString(this.largeIconUrl);
        dest.writeByte(vibrate ? (byte) 1 : (byte) 0);
        dest.writeString(this.notificationType);
    }

    public NotificationData()
    {
    }

    protected NotificationData(Parcel in)
    {
        this.actionIntentTag = in.readString();
        this.contentText = in.readString();
        this.actionIntentText = in.readString();
        this.actionIntentIcon = in.readInt();
        this.contentTitle = in.readString();
        this.notificationId = in.readInt();
        this.contentIntentTag = in.readString();
        this.onGoing = in.readByte() != 0;
        this.largeIconUrl = in.readString();
        this.vibrate = in.readByte() != 0;
        this.notificationType = in.readString();
    }

    public static final Parcelable.Creator<NotificationData> CREATOR = new Parcelable.Creator<NotificationData>()
    {
        @Override
        public NotificationData createFromParcel(Parcel source)
        {
            return new NotificationData(source);
        }

        @Override
        public NotificationData[] newArray(int size)
        {
            return new NotificationData[size];
        }
    };
}
