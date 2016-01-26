package in.org.whistleblower.models;

public class UserConnections
{
    public String userGoogleId, friendName, friendGoogleId, friendUrl;
    public static final String TABLE = "UserConnections";
    public static final String FRIEND_NAME = "DESCRIPTION";
    public static final String FRIEND_GOOGLE_ID = "PLACE_NAME";
    public static final String FRIEND_DP_URL = "USER_DP_URL";
    public static final String USER_GOOGLE_ID = "USERNAME";
}
