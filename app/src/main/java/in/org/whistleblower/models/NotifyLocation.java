package in.org.whistleblower.models;

public class NotifyLocation
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

    public String name, email, userEmail, latitude, longitude, photoUrl, message;
    public int radius;
}
