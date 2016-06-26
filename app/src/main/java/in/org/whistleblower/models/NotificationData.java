package in.org.whistleblower.models;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationData implements Parcelable
{
    public String contentText;
    public Notifications notification;
    public String action1IntentTag;
    public String action1IntentText;
    public int action1IntentIcon;

    public String action2IntentTag;
    public String action2IntentText;
    public int action2IntentIcon;

    public String contentTitle;
    public long notificationId;
    public String contentIntentTag;
    public boolean onGoing = false;
    public String largeIconUrl;
    public boolean vibrate;
    public String notificationType;
    public int priority = 0;

    public NotificationData()
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
        dest.writeString(this.contentText);
        dest.writeParcelable(this.notification, flags);
        dest.writeString(this.action1IntentTag);
        dest.writeString(this.action1IntentText);
        dest.writeInt(this.action1IntentIcon);
        dest.writeString(this.action2IntentTag);
        dest.writeString(this.action2IntentText);
        dest.writeInt(this.action2IntentIcon);
        dest.writeString(this.contentTitle);
        dest.writeLong(this.notificationId);
        dest.writeString(this.contentIntentTag);
        dest.writeByte(onGoing ? (byte) 1 : (byte) 0);
        dest.writeString(this.largeIconUrl);
        dest.writeByte(vibrate ? (byte) 1 : (byte) 0);
        dest.writeString(this.notificationType);
        dest.writeInt(this.priority);
    }

    protected NotificationData(Parcel in)
    {
        this.contentText = in.readString();
        this.notification = in.readParcelable(Notifications.class.getClassLoader());
        this.action1IntentTag = in.readString();
        this.action1IntentText = in.readString();
        this.action1IntentIcon = in.readInt();
        this.action2IntentTag = in.readString();
        this.action2IntentText = in.readString();
        this.action2IntentIcon = in.readInt();
        this.contentTitle = in.readString();
        this.notificationId = in.readInt();
        this.contentIntentTag = in.readString();
        this.onGoing = in.readByte() != 0;
        this.largeIconUrl = in.readString();
        this.vibrate = in.readByte() != 0;
        this.notificationType = in.readString();
        this.priority = in.readInt();
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
