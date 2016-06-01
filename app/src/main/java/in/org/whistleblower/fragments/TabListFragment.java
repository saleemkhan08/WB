package in.org.whistleblower.fragments;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.org.whistleblower.R;
import in.org.whistleblower.singletons.Otto;

public class TabListFragment extends Fragment
{
    public TabListFragment()
    {
    }

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_tab_list, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        swipeRefreshLayout.setEnabled(false);
        return parentView;
    }

    public static TabListFragment getInstance()
    {
        return new TabListFragment();
    }

}
