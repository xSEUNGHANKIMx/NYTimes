package com.example.nytimes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nytimes.Article;
import com.example.nytimes.ArticleClickListener;
import com.example.nytimes.CustomScrollListener;
import com.example.nytimes.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    public static final String API_KEY = "a2541c22984e4d05a2eb374f67bbf34b";
    public static final String SEARCH_URL = "http://api.nytimes.com/svc/search/v2/articlesearch.json";

    RecyclerView mRecyclerView;
    ArrayList<Article> mArticleList;
    ArticleAdapter mAdapter;
    String searchStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupViews();
    }

    public void setupViews() {
        mArticleList = new ArrayList<>();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new ArticleAdapter(mArticleList);
        mRecyclerView.setAdapter(mAdapter);

        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.addOnItemTouchListener(
                new ArticleClickListener(this, mRecyclerView,
                        new ArticleClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Intent i = new Intent(getApplicationContext(), DetailActivity.class);
                                Article article = mArticleList.get(position);
                                i.putExtra("article", article);
                                startActivity(i);
                            }

                            @Override
                            public void onItemLongClick(View view, int position) {
                                // ...
                            }
                        }));
        mRecyclerView.setOnScrollListener(new CustomScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadMoreArticles(page);
            }
        });

        searchForArticles("Top Stories");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchForArticles(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void searchForArticles(String str) {
        searchStr = str;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(SEARCH_URL,
                getSearchQueryParams(str, 0),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        JSONArray articleJsonResults = null;
                        try {
                            JSONObject responseObj = response.getJSONObject("response");
                            articleJsonResults = responseObj.getJSONArray("docs");
                            mAdapter.swap(Article.fromJsonArray(articleJsonResults));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void loadMoreArticles(int page) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(SEARCH_URL, getSearchQueryParams(searchStr, page),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        JSONArray articleJsonResults = null;
                        try {
                            articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                            mArticleList.addAll(Article.fromJsonArray(articleJsonResults));
                            int curSize = mAdapter.getItemCount();
                            mAdapter.notifyItemRangeInserted(curSize, mArticleList.size() - 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private RequestParams getSearchQueryParams(String query, int page) {
        RequestParams params = new RequestParams();
        params.put("q", query);
        params.put("page", page);
        params.put("api-key", API_KEY);
        return params;
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView headline;

        public Holder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.ivImage);
            headline = (TextView) itemView.findViewById(R.id.tvTitle);
        }
    }

    class ArticleAdapter extends RecyclerView.Adapter<Holder>  {
        private List<Article> mArticles;
        private Context context;
        public ArticleAdapter(List<Article> articles) {
            mArticles = articles;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View articleView = inflater.inflate(R.layout.article_item_layout, parent, false);
            Holder viewHolder = new Holder(articleView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(Holder viewHolder, int position) {
            Article article = mArticles.get(position);
            viewHolder.headline.setText(article.getHeadline());

            String thumbnail = article.getThumbnail();
            if (!TextUtils.isEmpty(thumbnail)) {
                Picasso.with(context).load(thumbnail).into(viewHolder.thumbnail);
            } else {
                Picasso.with(context).load(R.mipmap.noimage).into(viewHolder.thumbnail);
            }
        }

        public void swap(List<Article> articles){
            mArticles.clear();
            mArticles.addAll(articles);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mArticles.size();
        }
    }
}

