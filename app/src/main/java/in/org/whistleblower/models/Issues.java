package in.org.whistleblower.models;

public class Issues
{
    public static final String TABLE = "Issues";
    public static final String ISSUE_ID = "issue_id";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String PLACE_NAME = "PLACE_NAME";
    public static final String IMAGE_URL = "FILE_URL";
    public static final String USER_DP_URL = "USER_DP_URL";
    public static final String USERNAME = "USERNAME";

    public static final String RADIUS = "RADIUS";
    public static final String AREA_TYPE = "AREA_TYPE";
    public static final String USER_ID = "userId";
    public static final String ANONYMOUS = "ANONYMOUS";
    public static final String STATUS = "STATUS";

    public static final String LONGITUDE = "LONGITUDE";
    public static final String LATITUDE = "LATITUDE";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;
    public static final String SPAM = "SPAM";

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
    public float longitude, latitude;

    public static final String TABLE_SCHEMA = "CREATE TABLE " + TABLE + "("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ISSUE_ID + " VARCHAR(255), "
            + DESCRIPTION + " VARCHAR(255), "
            + PLACE_NAME + " VARCHAR(255), "
            + IMAGE_URL + " VARCHAR(255), "
            + RADIUS + " INTEGER"
            + AREA_TYPE + " VARCHAR(255), "
            + USER_ID + " VARCHAR(255), "
            + LONGITUDE + " REAL, "
            + LATITUDE + " REAL );";

    public static final String ALTER_TABLE_SCHEMA = TABLE_SCHEMA;
    //TODO write a query to save the old data and just alter the table.

}
