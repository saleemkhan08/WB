package in.org.whistleblower.models;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationAlarm implements Parcelable
{
    public static final String TABLE = "LocationAlarm";
    public static final String ADDRESS = "address";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String RADIUS = "radius";
    public static final String STATUS = "status";
    public static final int ALARM_ON = 1;
    public static final int ALARM_OFF = 0;
    public static final String ALARM = "alarm";

    public String latitude, longitude, address;
    public int radius, status;

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
        dest.writeString(this.address);
        dest.writeInt(this.radius);
        dest.writeInt(this.status);
    }

    public LocationAlarm()
    {
    }

    protected LocationAlarm(Parcel in)
    {
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.address = in.readString();
        this.radius = in.readInt();
        this.status = in.readInt();
    }

    public static final Parcelable.Creator<LocationAlarm> CREATOR = new Parcelable.Creator<LocationAlarm>()
    {
        @Override
        public LocationAlarm createFromParcel(Parcel source)
        {
            return new LocationAlarm(source);
        }

        @Override
        public LocationAlarm[] newArray(int size)
        {
            return new LocationAlarm[size];
        }
    };
}
