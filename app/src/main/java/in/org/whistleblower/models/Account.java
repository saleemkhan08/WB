package in.org.whistleblower.models;

import android.content.Context;

public class Account
{
    protected String email;
    protected String name;
    protected String googleId;
    protected String photo_url;
    protected boolean login_status;


    public static final String TABLE = "UserAccount";
    public static final String EMAIL = "email";
    public static final String NAME = "name";
    public static final String GOOGLE_ID = "google_id";
    public static final String PHOTO_URL = "photo_url";
    public static final String LOGIN_STATUS = "login_status";
    public static final String USER_ID = "userId";

    public Account(String email, String name, String googleId, String photo_url, boolean login_status)
    {
        this.email = email;
        this.name = name;
        this.googleId = googleId;
        this.photo_url = photo_url;
        this.login_status = login_status;
    }

    public void saveInPreference(Context context, String preferenceName)
    {
        context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).edit()
                .putBoolean(LOGIN_STATUS, true)
                .putString(EMAIL, email)
                .putString(NAME, name)
                .putString(GOOGLE_ID, googleId)
                .putString(PHOTO_URL, photo_url)
                .commit();
    }
}
