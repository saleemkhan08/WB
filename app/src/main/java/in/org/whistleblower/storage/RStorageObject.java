package in.org.whistleblower.storage;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

public class RStorageObject extends StorageObject
{
    public RStorageObject(String tableName)
    {
        super(tableName);
    }

    @Override
    public void store(final StorageListener listener)
    {
        ParseObject remoteData = new ParseObject(getString(TABLE_NAME));
        remoteData.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e != null)
                {
                    listener.onError(e.getMessage());
                }
                else
                {
                    listener.onSuccess();
                }
            }
        });
    }

    @Override
    public void store()
    {
        ParseObject remoteData = new ParseObject(getString(TABLE_NAME));
        remoteData.saveInBackground();
    }


}
