package in.org.whistleblower.storage;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RstorageQuery<T extends StorageObject>
{
    String mTableName;
    public RstorageQuery(String tableName)
    {
        mTableName = tableName;
    }

    public void getWhereEqualTo(String key, Object value, final QueryResultListener<StorageObject> listener)
    {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(mTableName);
        query.whereEqualTo(key, value);
        query.findInBackground(new FindCallback<ParseObject>()
        {
            public void done(List<ParseObject> userList, ParseException e)
            {
                List<StorageObject> resultList = new ArrayList<>();
                StorageObject resultObject = new RStorageObject(mTableName);
                if (e != null)
                {
                    listener.onError(e.getMessage());
                }
                else
                {
                    for (ParseObject object : userList)
                    {
                        Set<String> keys = object.keySet();
                        for (String key : keys)
                        {
                            resultObject.put(key, object.get(key));
                        }
                        resultList.add(resultObject);
                        resultObject = new RStorageObject(mTableName);
                    }
                    listener.onResult(resultList);
                }
            }
        });
    }
}
