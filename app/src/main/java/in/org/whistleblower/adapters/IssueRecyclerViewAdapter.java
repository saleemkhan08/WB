package in.org.whistleblower.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import in.org.whistleblower.R;

public class IssueRecyclerViewAdapter extends RecyclerView.Adapter<IssueRecyclerViewAdapter.IssueViewHolder>
{
    LayoutInflater inflater;
    public IssueRecyclerViewAdapter(Context context)
    {
        inflater = LayoutInflater.from(context);
    }
    public IssueRecyclerViewAdapter(Context context, List data)
    {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public IssueViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.single_issue_layout, parent, false);
        IssueViewHolder holder = new IssueViewHolder(view);

        return null;
    }

    @Override
    public void onBindViewHolder(IssueViewHolder holder, int position)
    {

    }

    @Override
    public int getItemCount()
    {
        return 0;
    }

    class IssueViewHolder extends RecyclerView.ViewHolder
    {
        TextView description;
        ImageView issueImage;
        public IssueViewHolder(View itemView)
        {
            super(itemView);
        }
    }
}
