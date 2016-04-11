package in.org.whistleblower.utilities;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TouchableWrapper extends FrameLayout
{
    private OnMapTouchListener onMapTouchListener;

    public TouchableWrapper(Context context, OnMapTouchListener listener)
    {
        super(context);
        try
        {
            onMapTouchListener = listener;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() + " must implement OnMapTouchListener");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                onMapTouchListener.onActionDown();
                break;

            case MotionEvent.ACTION_UP:
                onMapTouchListener.onActionUp();
                break;

            case MotionEvent.ACTION_CANCEL:
                onMapTouchListener.onActionUp();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public interface OnMapTouchListener
    {
        void onActionUp();

        void onActionDown();
    }
}
