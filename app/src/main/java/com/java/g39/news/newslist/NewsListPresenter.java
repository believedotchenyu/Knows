package com.java.g39.news.newslist;

import android.content.Intent;
import android.util.Log;

import com.java.g39.data.Manager;
import com.java.g39.data.SimpleNews;
import com.java.g39.news.newsdetail.NewsDetailActivity;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * Created by chenyu on 2017/9/7.
 */

public class NewsListPresenter implements NewsListContract.Presenter {

    private NewsListContract.View mView;
    private int mCategory;
    private int mPageNo = 1;

    public NewsListPresenter(NewsListContract.View view, int category) {
        this.mView = view;
        this.mCategory = category;

        view.setPresenter(this);
    }

    @Override
    public void subscribe() {
        refreshNews();
    }

    @Override
    public void unsubscribe() {
        // nothing
    }

    @Override
    public void requireMoreNews() {
        mPageNo ++;
        fetchNews();
    }

    @Override
    public void refreshNews() {
        mPageNo = 1;
        fetchNews();
    }

    @Override
    public void openNewsDetailUI(SimpleNews news) {
        Intent intent = new Intent(mView.context(), NewsDetailActivity.class);
        intent.putExtra(NewsDetailActivity.NEWS_ID, news.news_ID);
        mView.start(intent);
    }

    private void fetchNews() {
        final long start = System.currentTimeMillis();
        Manager.I.fetchSimpleNews(mPageNo, 20, mCategory)
                .subscribe(new Consumer<List<SimpleNews>>() {
                    @Override
                    public void accept(List<SimpleNews> simpleNewses) throws Exception {
                        System.out.println(System.currentTimeMillis() - start + " | " + mCategory);
                        mView.onSuccess(simpleNewses.size() == 0); // TODO check if load completed
                        // TODO onError
                        if (mPageNo == 1) mView.setNewsList(simpleNewses);
                        else mView.appendNewsList(simpleNewses);
                    }
                });
    }
}