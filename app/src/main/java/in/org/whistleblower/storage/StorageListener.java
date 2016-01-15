package in.org.whistleblower.storage;

public interface StorageListener
{
    void onSuccess();
    void onError(String e);
}
