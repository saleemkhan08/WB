package in.org.whistleblower.storage;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.utilities.MiscUtil;

public class RStorageObject extends StorageObject
{
    String mTableName;

    public RStorageObject(String tableName)
    {
        super(tableName);
        mTableName = tableName;
    }

    @Override
    public void store(final StorageListener listener)
    {
        ParseObject remoteData = new ParseObject(getString(TABLE_NAME));

        for (String key : data.keySet())
        {
            remoteData.put(key, data.get(key));
        }
        MiscUtil.log("Data Remote : " + remoteData);
        MiscUtil.log("Data : " + data);
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
        for (String key : data.keySet())
        {
            remoteData.put(key, data.get(key));
        }
        MiscUtil.log("Data Remote : " + remoteData);
        MiscUtil.log("Data : " + data);
        remoteData.saveInBackground();
    }

    @Override
    public void update(final StorageListener listener)
    {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(mTableName);
        query.whereEqualTo(Accounts.GOOGLE_ID, data.get(Accounts.GOOGLE_ID));
        query.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> objects, ParseException e)
            {
                if (e == null)
                {
                    ParseObject oldObject = objects.get(0);
                    for (String key : data.keySet())
                    {
                        oldObject.put(key, data.get(key));
                    }
                    oldObject.saveInBackground(new SaveCallback()
                    {
                        @Override
                        public void done(ParseException e)
                        {
                            if (e == null)
                            {
                                listener.onSuccess();
                            }
                            else
                            {
                                listener.onError(e.getMessage());
                            }
                        }
                    });
                }
                else
                {
                    listener.onError(e.getMessage());
                }
            }
        });
    }

    @Override
    public void update()
    {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(mTableName);
        query.whereEqualTo(Accounts.GOOGLE_ID, data.get(Accounts.GOOGLE_ID));
        query.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> objects, ParseException e)
            {
                if (e == null)
                {
                    ParseObject oldObject = objects.get(0);
                    for (String key : data.keySet())
                    {
                        oldObject.put(key, data.get(key));
                    }
                    oldObject.saveInBackground();
                }
                else
                {
                    MiscUtil.log("Couldn't find the Object to be updated");
                }
            }
        });
    }
}
