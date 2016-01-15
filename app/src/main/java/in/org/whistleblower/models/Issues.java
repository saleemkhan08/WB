package in.org.whistleblower.models;

public class Issues
{
    public static final String TABLE = "Issues";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String PLACE_NAME = "PLACE_NAME";
    public static final String FILE_URL = "FILE_URL";
    public static final String USER_DP_URL = "USER_DP_URL";
    public static final String USERNAME = "USERNAME";

    public static final String RADIUS = "RADIUS";
    public static final String AREA_TYPE = "AREA_TYPE";
    public static final String USER_ID = "userId";
    public static final String ANONYMOUS = "ANONYMOUS";

    public static final String LONGITUDE = "LONGITUDE";
    public static final String LATITUDE = "LATITUDE";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;

    String description,
            placeName,
            imgUrl,
            username,
            userDpUrl,
            userId,
            zone;

    int radius;
    boolean anonymous;
    float longitude, latitude;

    public static final String TABLE_SCHEMA = "CREATE TABLE " + TABLE + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DESCRIPTION + " VARCHAR(255), "
            + PLACE_NAME + " VARCHAR(255), "
            + FILE_URL + " VARCHAR(255), "
            + RADIUS + " INTEGER"
            + AREA_TYPE + " VARCHAR(255), "
            + USER_ID + " VARCHAR(255), "
            + LONGITUDE + " REAL, "
            + LATITUDE + " REAL );";

    public static final String ALTER_TABLE_SCHEMA = TABLE_SCHEMA;
    //TODO write a query to save the old data and just alter the table.

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getPlaceName()
    {
        return placeName;
    }

    public void setPlaceName(String placeName)
    {
        this.placeName = placeName;
    }

    public String getImgUrl()
    {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl)
    {
        this.imgUrl = imgUrl;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getUserDpUrl()
    {
        return userDpUrl;
    }

    public void setUserDpUrl(String userDpUrl)
    {
        this.userDpUrl = userDpUrl;
    }

    public String getZone()
    {
        return zone;
    }

    public void setZone(String zone)
    {
        this.zone = zone;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public int getRadius()
    {
        return radius;
    }

    public void setRadius(int radius)
    {
        this.radius = radius;
    }

    public boolean isAnonymous()
    {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous)
    {
        this.anonymous = anonymous;
    }

    public float getLongitude()
    {
        return longitude;
    }

    public void setLongitude(float longitude)
    {
        this.longitude = longitude;
    }

    public float getLatitude()
    {
        return latitude;
    }

    public void setLatitude(float latitude)
    {
        this.latitude = latitude;
    }
}
