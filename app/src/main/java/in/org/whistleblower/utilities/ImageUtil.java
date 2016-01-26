package in.org.whistleblower.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import in.org.whistleblower.R;

public class ImageUtil
{
    public ImageLoader mImageLoader;
    Context mContext;
    DisplayImageOptions dpOptions, issueOptions;
    public ImageUtil(Context context)
    {
        mContext = context;
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
        issueOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        dpOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(100))
                .build();
    }

    public void displayImage(String photo_url, ImageView view, boolean isRounded)
    {
        if(isRounded)
        {
            mImageLoader.displayImage(photo_url, view, dpOptions);
        }
        else
        {
            mImageLoader.displayImage(photo_url, view, issueOptions);
        }
    }
}
