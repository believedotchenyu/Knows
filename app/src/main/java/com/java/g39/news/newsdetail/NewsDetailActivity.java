package com.java.g39.news.newsdetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.java.g39.R;
import com.java.g39.data.DetailNews;
import com.java.g39.data.ImageLoader;
import com.java.g39.data.Speech;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.util.Map;

public class NewsDetailActivity extends AppCompatActivity implements NewsDetailContract.View {

    public static final String NEWS_ID = "NEWS_ID";
    public static final String NEWS_TITLE = "NEWS_TITLE";
    public static final String NEWS_PICTURE_URL = "NEWS_PICTURE_URL";
    public static final String NEWS_IS_FAVORITED = "NEWS_IS_FAVORITED";

    private NewsDetailContract.Presenter mPresenter;
    private DetailNews mNews;
    private boolean mError;

    private TextView mTag, mDetail, mContent;
    private ImageView mImage;
    private FloatingActionButton mFab;
    private NestedScrollView mScrollView;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private View mBottomView, mFavoriteBtn, mSpeechBtn, mShareBtn;
    private Speech mSpeaker;

    private void onFavoite() {
        if (mNews != null) {
            mFab.setSelected(!mNews.is_favorite);
            mFavoriteBtn.setSelected(!mNews.is_favorite);
            if (mNews.is_favorite)
                mPresenter.unFavorite(mNews);
            else
                mPresenter.favorite(mNews);
        }
    }

    private void onSpeech() {
        if (mNews != null && mSpeaker != null) {
            switch (mSpeaker.getState()) {
                case ready:
                    mSpeaker.start();
                    break;
                case reading:
                    mSpeaker.stop();
                    break;
                default:
                    break;
            }
        }
    }

    private void onShare() {
        if (mNews != null) {
            mPresenter.shareNews(this, mNews);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String news_ID = getIntent().getStringExtra(NEWS_ID);
        String news_Title = getIntent().getStringExtra(NEWS_TITLE);
        String news_picture_url = getIntent().getStringExtra(NEWS_PICTURE_URL);
        boolean news_is_favorited = getIntent().getBooleanExtra(NEWS_IS_FAVORITED, false);

        mPresenter = new NewsDetailPresenter(this, news_ID);
        mError = false;

        setContentView(R.layout.activity_news_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener((View view) -> onFavoite());

        mBottomView = findViewById(R.id.bottom_view);
        mScrollView = (NestedScrollView) findViewById(R.id.scroll_view);
        mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            private boolean isBottomShow = true;

            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY - oldScrollY > 0 && isBottomShow) { // 下移隐藏
                    isBottomShow = false;
                    mBottomView.animate().translationY(mBottomView.getHeight());
                } else if (scrollY - oldScrollY < 0 && !isBottomShow) { // 上移出现
                    isBottomShow = true;
                    mBottomView.animate().translationY(0);
                }
            }
        });

        mFavoriteBtn = findViewById(R.id.bottom_favorite);
        mSpeechBtn = findViewById(R.id.bottom_speech);
        mShareBtn = findViewById(R.id.bottom_share);
        mFavoriteBtn.setOnClickListener((View view) -> onFavoite());
        mSpeechBtn.setOnClickListener((View view) -> onSpeech());
        mShareBtn.setOnClickListener((View view) -> onShare());

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbarLayout.setTitle(news_Title);

        mTag = (TextView) findViewById(R.id.text_tag);
        mDetail = (TextView) findViewById(R.id.text_detail);
        mContent = (TextView) findViewById(R.id.text_content);
        mImage = (ImageView) findViewById(R.id.image_view);

        mFab.setSelected(news_is_favorited);
        mFavoriteBtn.setSelected(news_is_favorited);
        if (news_picture_url != null) {
            ImageLoader.displayImage(news_picture_url, mImage);
        }

        findViewById(R.id.button_reload).setOnClickListener((View view) -> mPresenter.subscribe());

        mPresenter.subscribe();
    }

    @Override
    protected void onResume() {
        findViewById(R.id.progress_bar).setVisibility(mNews != null || mError ? View.INVISIBLE : View.VISIBLE);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_news, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mNews != null) {
            menu.findItem(R.id.action_favorite).setTitle(
                    getString(mNews.is_favorite ? R.string.action_unfavorite : R.string.action_favorite));
            menu.findItem(R.id.action_speech).setTitle(
                    getString(mSpeaker.getState() == Speech.State.ready ? R.string.action_speech : R.string.action_stop_speech));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_favorite:
                onFavoite();
                return true;
            case R.id.action_speech:
                onSpeech();
                return true;
            case R.id.action_share:
                onShare();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onBackPressed() {
        mFab.setVisibility(View.INVISIBLE);
        if (mSpeaker != null) mSpeaker.stop();

        if (mNews != null) {
            Intent intent = new Intent();
            intent.putExtra("IS_FAVORITED", mNews.is_favorite);
            setResult(RESULT_OK, intent);
        }

        super.onBackPressed();
    }

    @Override
    public void setPresenter(NewsDetailContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void start(Intent intent, Bundle options) {
        startActivity(intent, options);
    }

    @Override
    public Context context() {
        return this;
    }

    @Override
    public void setNewsDetail(DetailNews news) {
        mNews = news;
        mError = false;
        mTag.setText(news.newsClassTag);
        mDetail.setText((news.news_Author.isEmpty() ? news.news_Source : news.news_Author) + "　" + news.formatTime());
        mFab.setSelected(news.is_favorite);
        mFavoriteBtn.setSelected(news.is_favorite);
        news.single_picture_url
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        ImageLoader.displayImage(s, mImage);
                    }
                });

        String content = TextUtils.join("\n\n　　", news.news_Content.trim().split("　　"));
        mContent.setText(content);
        long links_start = System.currentTimeMillis();
        mNews.links.observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Map<String, String>>() {
            @Override
            public void accept(Map<String, String> links) throws Exception {
                System.out.println("links : " + (System.currentTimeMillis() - links_start));
                SpannableString sp = new SpannableString(content);
                for (Map.Entry<String, String> e : links.entrySet()) {
                    String word = e.getKey(), url = e.getValue();
                    int pos = content.indexOf(word);
                    if (pos >= 0)
                        sp.setSpan(new URLSpan(url), pos, pos + word.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                mContent.setText(sp);
                mContent.setMovementMethod(LinkMovementMethod.getInstance());
            }
        });

        mSpeaker = new Speech(this, mNews.news_Title + "。" + mNews.news_Content, null);
        mSpeaker.setStateChangeListener(() -> {
            switch (mSpeaker.getState()) {
                case stoped:
                    mSpeechBtn.setSelected(false);
                    break;
                case reading:
                    mSpeechBtn.setSelected(true);
                    break;
                default:
                    break;
            }
        });

        mFab.setClickable(true);
        mFavoriteBtn.setClickable(true);
        mSpeechBtn.setClickable(true);
        mShareBtn.setClickable(true);
        findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
        findViewById(R.id.layout_error).setVisibility(View.INVISIBLE);
        findViewById(R.id.layout_content).setVisibility(View.VISIBLE);
    }

    @Override
    public void onShowToast(String title) {
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartLoading() {
        findViewById(R.id.layout_error).setVisibility(View.INVISIBLE);
        findViewById(R.id.layout_content).setVisibility(View.INVISIBLE);
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
    }

    @Override
    public void onError() {
        mError = true;
        mFab.setClickable(false);
        mFavoriteBtn.setClickable(false);
        mSpeechBtn.setClickable(false);
        mShareBtn.setClickable(false);
        findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
        findViewById(R.id.layout_content).setVisibility(View.INVISIBLE);
        findViewById(R.id.layout_error).setVisibility(View.VISIBLE);
    }

    @Override
    public void setImageVisible(boolean visible) {
        findViewById(R.id.image_layout).setVisibility(visible && !mError ? View.VISIBLE : View.GONE);
    }
}
