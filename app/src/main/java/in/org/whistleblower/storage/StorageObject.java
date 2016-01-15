package in.org.whistleblower.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class StorageObject
{
    protected Map<String, Object> data;

    public String getPrimaryKey()
    {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey)
    {
        this.primaryKey = primaryKey;
    }

    protected String primaryKey;
    public static final String TABLE_NAME = "tableName";
    public Map<String, Object> getData()
    {
        return data;
    }

    public Set<String> keySet()
    {
        return data.keySet();
    }

    public void setData(Map<String, Object> data)
    {
        this.data = data;
    }

    public StorageObject(String tableName)
    {
        data = new HashMap<>();
        data.put(TABLE_NAME, tableName);
    }

    public void put(String key, Object value)
    {
        data.put(key, value);
    }

    public void put(String key, boolean value)
    {
        data.put(key, value);
    }

    public void put(String key, int value)
    {
        data.put(key, value);
    }

    public void put(String key, long value)
    {
        data.put(key, value);
    }

    public void put(String key, float value)
    {
        data.put(key, value);
    }

    public void put(String key, double value)
    {
        data.put(key, value);
    }

    public void put(String key, char value)
    {
        data.put(key, value);
    }

    public void put(String key, String value)
    {
        data.put(key, value);
    }


    public Object get(String key)
    {
        return data.get(key);
    }

    public boolean getBoolean(String key)
    {
        return (boolean) data.get(key);
    }

    public int getInt(String key)
    {
        return (int) data.get(key);
    }

    public long getLong(long key)
    {
        return (long) data.get(key);
    }

    public float getFloat(String key)
    {
        return (float) data.get(key);
    }

    public double getDouble(double key)
    {
        return (double) data.get(key);
    }

    public int getChar(String key)
    {
        return (char) data.get(key);
    }

    public String getString(String key)
    {
        return data.get(key).toString();
    }

    abstract public void store(final StorageListener listener);
    abstract public void store();

    abstract public void update(final StorageListener listener);
    abstract public void update();
}
