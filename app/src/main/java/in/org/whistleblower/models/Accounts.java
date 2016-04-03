package in.org.whistleblower.models;

public class Accounts
{
    public String email;
    public String name;
    public String googleId;
    public String photo_url;
    public String timeStamp;
    public boolean login_status;
    public boolean relation;


    public static final String TABLE = "UserAccount";
    public static final String EMAIL = "email";
    public static final String NAME = "name";
    public static final String GOOGLE_ID = "google_id";
    public static final String PHOTO_URL = "photo_url";
    public static final String TIME_STAMP = "time_stamp";
    public static final String RELATION = "relation";

}
