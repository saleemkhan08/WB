package in.org.whistleblower.icon;

import java.io.Serializable;

import in.org.whistleblower.util.TypefaceManager;

public interface Icon extends Serializable
{

    public TypefaceManager.IconicTypeface getIconicTypeface();

    public int getIconUtfValue();

}
