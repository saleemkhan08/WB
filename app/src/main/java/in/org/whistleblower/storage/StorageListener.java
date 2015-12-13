package in.org.whistleblower.storage;

/**
 * Created by Saleem Khan on 12/1/2015.
 */
public interface StorageListener
{
    public void onSuccess();
    public void onError(String e);
}
