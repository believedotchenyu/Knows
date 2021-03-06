package com.java.g39.data;

import android.app.Activity;
import android.util.*;

import com.xyzlf.share.library.bean.ShareEntity;
import com.xyzlf.share.library.interfaces.ShareConstant;
import com.xyzlf.share.library.util.ShareUtil;

import org.json.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

import io.reactivex.Flowable;
import io.reactivex.annotations.*;
import io.reactivex.functions.*;

/**
 * Created by chenyu on 2017/9/7.
 * 新闻API相关操作
 */

class API {
    private API() {
    }

    /**
     * @param json_news Json格式的SimpleNews
     * @return DetailNews
     * @throws JSONException
     */
    static DetailNews GetDetailNewsFromJson(JSONObject json_news, boolean from_disk) throws JSONException {
        JSONArray list;
        DetailNews news = new DetailNews();
        news.plain_json = json_news.toString();
        news.from_disk = from_disk;

        news.Keywords = new ArrayList<DetailNews.WordWithScore>();
        list = json_news.getJSONArray("Keywords");
        for (int t = 0; t < list.length(); t++) {
            DetailNews.WordWithScore item = news.new WordWithScore();
            JSONObject jobj = list.getJSONObject(t);
            item.word = jobj.getString("word");
            item.score = jobj.getDouble("score");
            news.Keywords.add(item);
        }
        news.bagOfWords = new ArrayList<DetailNews.WordWithScore>();
        list = json_news.getJSONArray("bagOfWords");
        for (int t = 0; t < list.length(); t++) {
            DetailNews.WordWithScore item = news.new WordWithScore();
            JSONObject jobj = list.getJSONObject(t);
            item.word = jobj.getString("word");
            item.score = jobj.getDouble("score");
            news.bagOfWords.add(item);
        }
        news.crawl_Source = json_news.optString("crawl_Source");
        news.crawl_Time = json_news.optString("crawl_Time");
        news.inborn_KeyWords = json_news.optString("inborn_KeyWords");
        news.lang_Type = json_news.optString("lang_Type");
        news.locations = new ArrayList<DetailNews.WordWithCount>();
        list = json_news.getJSONArray("locations");
        for (int t = 0; t < list.length(); t++) {
            DetailNews.WordWithCount item = news.new WordWithCount();
            JSONObject jobj = list.getJSONObject(t);
            item.word = jobj.getString("word");
            item.count = jobj.getInt("count");
            news.locations.add(item);
        }
        news.newsClassTag = json_news.optString("newsClassTag");
        news.news_Author = json_news.optString("news_Author");
        news.news_Category = json_news.optString("news_Category");
        news.news_Content = json_news.optString("news_Content");
        news.news_ID = json_news.optString("news_ID");
        news.news_Journal = json_news.optString("news_Journal");
        news.news_Pictures = json_news.optString("news_Pictures");
        news.news_Source = json_news.optString("news_Source");
        news.news_Time = json_news.optString("news_Time");
        news.news_Title = json_news.optString("news_Title");
        news.news_URL = json_news.optString("news_URL");
        news.news_Video = json_news.optString("news_Video");
        news.organizations = new ArrayList<String>();
        list = json_news.getJSONArray("organizations");
        for (int t = 0; t < list.length(); t++)
            news.organizations.add(list.getString(t));
        news.persons = new ArrayList<DetailNews.WordWithCount>();
        list = json_news.getJSONArray("persons");
        for (int t = 0; t < list.length(); t++) {
            DetailNews.WordWithCount item = news.new WordWithCount();
            JSONObject jobj = list.getJSONObject(t);
            item.word = jobj.getString("word");
            item.count = jobj.getInt("count");
            news.persons.add(item);
        }
        news.repeat_ID = json_news.optString("repeat_ID");
        news.seggedPListOfContent = new ArrayList<String>();
        list = json_news.getJSONArray("seggedPListOfContent");
        for (int t = 0; t < list.length(); t++)
            news.seggedPListOfContent.add(list.getString(t));
        news.seggedTitle = json_news.optString("seggedTitle");
        news.wordCountOfContent = json_news.getInt("wordCountOfContent");
        news.wordCountOfTitle = json_news.getInt("wordCountOfTitle");
        return news;
    }

    /**
     * @param json_news Json格式的DetailNews
     * @return DetailNews
     * @throws JSONException
     */
    static SimpleNews GetNewsFromJson(JSONObject json_news, boolean from_disk) throws JSONException {
        SimpleNews news = new SimpleNews();
        news.plain_json = json_news.toString();
        news.from_disk = from_disk;

        news.lang_Type = json_news.optString("lang_Type");
        news.newsClassTag = json_news.optString("newsClassTag");
        news.news_Author = json_news.optString("news_Author");
        news.news_ID = json_news.optString("news_ID");
        news.news_Pictures = json_news.optString("news_Pictures");
        news.news_Source = json_news.optString("news_Source");
        news.news_Time = json_news.optString("news_Time"); // TODO format time
        news.news_Title = json_news.optString("news_Title");
        news.news_URL = json_news.optString("news_URL");
        news.news_Video = json_news.optString("news_Video");
        news.news_Intro = json_news.optString("news_Intro");
        return news;
    }

    /**
     * @param url 网页地址
     * @return 网页内容
     */
    static String GetBodyFromURL(String url) throws IOException {
        URL cs = new URL(url);
        URLConnection urlConn = cs.openConnection();
        urlConn.setConnectTimeout(10 * 1000);
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
        String inputLine, body = "";
        while ((inputLine = in.readLine()) != null)
            body = body + inputLine;
        in.close();
        return body;
    }

    /**
     * 测试是否可以访问
     * @param url 地址
     * @return 网络不可用是返回null
     */
    static Boolean TestBaikeConnection(String url) {
        try {
            URL cs = new URL(url);
            URLConnection conn = cs.openConnection();
            conn.connect();

            int code = 0;
            if (cs.getProtocol().toLowerCase().equals("http")) {
                code = ((HttpURLConnection)conn).getResponseCode();
            } else if (cs.getProtocol().toLowerCase().equals("https")) {
                code = ((HttpsURLConnection)conn).getResponseCode();
            } else {
                return null;
            }

            return code == 200 && !conn.getURL().toString().endsWith("error.html");
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取最近的新闻，不设置subscribeOn，只设置网络获取的字段
     *
     * @param pageNo   页码
     * @param pageSize 每页新闻数量
     * @param category 分类，0表示不设置
     * @return 新闻列表
     */
    static List<SimpleNews> GetSimpleNews(final int pageNo, final int pageSize, final int category) throws IOException, JSONException {
        String URL_String = new String(String.format("http://166.111.68.66:2042/news/action/query/latest?pageNo=%d&pageSize=%d", pageNo, pageSize));
        if (category > 0)
            URL_String = URL_String + String.format("&category=%d", category);
        String body = GetBodyFromURL(URL_String);
        if(body.equals("")) {
            Log.d("warning"," In GetSimpleNews body=\"\"");
            return new ArrayList<SimpleNews>();
        }
        List<SimpleNews> result = new ArrayList<SimpleNews>();
        JSONObject allData = new JSONObject(body);
        JSONArray list = allData.getJSONArray("list");
        for (int t = 0; t < list.length(); t++) {
            JSONObject json_news = list.getJSONObject(t);
            result.add(GetNewsFromJson(json_news, false));
        }

        return result;
    }

    /**
     * 获取最近的新闻，不设置subscribeOn，只设置网络获取的字段
     *
     * @param pageNo   页码
     * @param pageSize 每页新闻数量
     * @return 新闻列表
     */
    public static List<SimpleNews> GetSimpleNews(int pageNo, int pageSize) throws IOException, JSONException {
        return GetSimpleNews(pageNo, pageSize, 0);
    }

    /**
     * 搜索新闻，不设置subscribeOn，只设置网络获取的字段
     *
     * @param keyword  关键字
     * @param pageNo   页码
     * @param pageSize 每页新闻数量
     * @param category 分类，0表示不设置
     * @return 新闻列表
     */
    static List<SimpleNews> SearchNews(final String keyword, final int pageNo, final int pageSize, final int category) throws IOException, JSONException {
        String URL_String = new String(String.format("http://166.111.68.66:2042/news/action/query/search?keyword=%s&pageNo=%d&pageSize=%d",
                URLEncoder.encode(keyword, "UTF-8"), pageNo, pageSize));
        if (category > 0)
            URL_String = URL_String + String.format("&category=%d", category);
        String body = GetBodyFromURL(URL_String);

        List<SimpleNews> result = new ArrayList<SimpleNews>();
        JSONObject allData;
        allData = new JSONObject(body);
        JSONArray list = allData.getJSONArray("list");
        for (int t = 0; t < list.length(); t++) {
            JSONObject json_news = list.getJSONObject(t);
            result.add(GetNewsFromJson(json_news, false));
        }

        return result;
    }

    /**
     * 搜索新闻，不设置subscribeOn，只设置网络获取的字段
     *
     * @param keyword  关键字
     * @param pageNo   页码
     * @param pageSize 每页新闻数量
     * @return 新闻列表
     */
    static List<SimpleNews> SearchNews(String keyword, int pageNo, int pageSize) throws IOException, JSONException {
        return SearchNews(keyword, pageNo, pageSize, 0);
    }

    /**
     * 获取新闻详情，不设置subscribeOn，只设置网络获取的字段
     *
     * @param newsId ID
     * @return 新闻详情
     */
    static DetailNews GetDetailNews(final String newsId) throws IOException, JSONException {
        String URL_String = new String(String.format("http://166.111.68.66:2042/news/action/query/detail?newsId=%s", newsId));
        String body = GetBodyFromURL(URL_String);

        JSONObject allData;
        allData = new JSONObject(body);
        return GetDetailNewsFromJson(allData, false);
    }

    /**
     * 分享
     * @param activity 调用者
     * @param title 标题
     * @param text 文本内容
     * @param url 分享链接
     * @param imgUrl 图片链接
     */
    public static void ShareNews(Activity activity, String title, String text, String url, String imgUrl)
    {
        ShareEntity testBean = new ShareEntity(title, text);
        testBean.setUrl(url);
        testBean.setImgUrl(imgUrl);
        ShareUtil.showShareDialog(activity, testBean, ShareConstant.REQUEST_CODE);
    }
}
