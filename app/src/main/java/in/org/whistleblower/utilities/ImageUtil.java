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
                .showImageOnLoading(R.drawable.loading)
                .showImageForEmptyUri(R.drawable.loading)
                .showImageOnFail(R.drawable.loading)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        dpOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.user_accent_primary_o)
                .showImageForEmptyUri(R.mipmap.user_accent_primary_o)
                .showImageOnFail(R.mipmap.user_accent_primary_o)
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

    public static void displayImage(Context context,String photo_url, ImageView view, boolean isRounded)
    {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        DisplayImageOptions imageOptions;
        if(isRounded)
        {
            imageOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.anonymous_white_primary_dark)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new RoundedBitmapDisplayer(100))
                    .build();
        }
        else
        {
            imageOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.loading)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }
        imageLoader.displayImage(photo_url, view, imageOptions);
    }
}
