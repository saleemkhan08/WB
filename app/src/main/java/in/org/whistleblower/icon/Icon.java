package in.org.whistleblower.icon;

import java.io.Serializable;

public interface Icon extends Serializable
{

    public TypefaceManager.IconicTypeface getIconicTypeface();

    public int getIconUtfValue();

}
