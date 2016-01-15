package in.org.whistleblower;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import in.org.whistleblower.adapters.UserSearchAdapter;
import in.org.whistleblower.models.Accounts;
import in.org.whistleblower.utilities.NavigationUtil;

public class SearchActivity extends AppCompatActivity
{
    String category;
    static RecyclerView searchResultView;
    static UserSearchAdapter mUserSearchAdapter;
    static List<Accounts> mUserList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchResultView = (RecyclerView) findViewById(R.id.searchResultView);
        mUserSearchAdapter = new UserSearchAdapter(this,mUserList);
        searchResultView.setAdapter(mUserSearchAdapter);
        searchResultView.setLayoutManager(new LinearLayoutManager(this));


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        category = getIntent().getStringExtra(NavigationUtil.KEY_CATEGORY);
        new SearchTask().execute(category);
    }

    private static class SearchTask extends AsyncTask<String,Void,Void>
    {

        @Override
        protected Void doInBackground(String... params)
        {
            switch (params[0])
            {
                case NavigationUtil.ADD_FRIEND :
                   loadUserList();
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            mUserSearchAdapter.notifyDataSetChanged();
        }

        private void loadUserList()
        {
            for(int i=0;i<10;i++){
                Accounts accounts = new Accounts();
                accounts.name = "User "+i;
                accounts.photo_url = "";
                mUserList.add(accounts);
            }
        }
    }



}
