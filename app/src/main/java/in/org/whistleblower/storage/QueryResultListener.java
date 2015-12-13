package in.org.whistleblower.storage;

import java.util.List;

public interface QueryResultListener<Object extends StorageObject>
{
    public void onResult(List<Object> result);
    public void onError(String e);
}
