package in.org.whistleblower.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Issue implements Parcelable
{
    public String description,
            placeName,
            imgUrl,
            username,
            userDpUrl,
            userId,
            areaType,
            issueId,
            status;
    public int radius;
    public boolean anonymous;
    public String longitude, latitude;

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.description);
        dest.writeString(this.placeName);
        dest.writeString(this.imgUrl);
        dest.writeString(this.username);
        dest.writeString(this.userDpUrl);
        dest.writeString(this.userId);
        dest.writeString(this.areaType);
        dest.writeString(this.issueId);
        dest.writeString(this.status);
        dest.writeInt(this.radius);
        dest.writeByte(anonymous ? (byte) 1 : (byte) 0);
        dest.writeString(this.longitude);
        dest.writeString(this.latitude);
    }

    public Issue()
    {
    }

    protected Issue(Parcel in)
    {
        this.description = in.readString();
        this.placeName = in.readString();
        this.imgUrl = in.readString();
        this.username = in.readString();
        this.userDpUrl = in.readString();
        this.userId = in.readString();
        this.areaType = in.readString();
        this.issueId = in.readString();
        this.status = in.readString();
        this.radius = in.readInt();
        this.anonymous = in.readByte() != 0;
        this.longitude = in.readString();
        this.latitude = in.readString();
    }

    public static final Parcelable.Creator<Issue> CREATOR = new Parcelable.Creator<Issue>()
    {
        @Override
        public Issue createFromParcel(Parcel source)
        {
            return new Issue(source);
        }

        @Override
        public Issue[] newArray(int size)
        {
            return new Issue[size];
        }
    };
}