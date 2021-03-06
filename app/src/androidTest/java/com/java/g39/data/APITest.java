package com.java.g39.data;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.java.g39.data.DetailNews;
import com.java.g39.data.SimpleNews;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.*;

import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class APITest {
    @Test
    public void TestGetSimpleNews() throws Exception {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                List<SimpleNews> news = null;
                try {
                    news = API.GetSimpleNews(1, 10, -1);
                } catch (Exception e) {
                    e.printStackTrace();
                    assertTrue(false);
                }
                String[] answer = {"创事记 微博 作者： 广州阿超","环球网","中国网","","环球网","中国新闻网","环球网","京华时报","新浪网",""};
                int tot=0;
                for(SimpleNews s : news) {
                    if(s.news_Author.equals("来源：北京晚报"))
                        break;
                    assertEquals(answer[tot++], s.news_Author);
                }
            }
        });
        t.start();
        t.join();
    }
    @Test
    public void TestSearchNews() throws Exception {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] keyWords={"北京","杭州","清华","北大"};
                int[] pageNumber={1,2,3};
                int[] pageSize={4,5,10};
                for(String a1 : keyWords)
                    for(int a2 : pageNumber)
                        for(int a3 : pageSize) {
                            List<SimpleNews> news = null;
                            try {
                                news = API.SearchNews(a1,a2,a3,-1);
                            } catch (Exception e) {
                                e.printStackTrace();
                                assertTrue(false);
                            }
                            int tot = 0;
                            for (SimpleNews s : news) {
                                for(int j=0;j<a1.length();j++)
                                    assertTrue(String.format("arg:(%s,%d,%d) tot:%d id:%s title:%s",a1,a2,a3,tot,s.news_ID,s.news_Title),s.news_Title.contains(a1.substring(j,j+1)));
                            }
                        }
            }
        });
        t.start();
        t.join();
    }
    @Test
    public void TestGetDetailNews() throws Exception {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                DetailNews news = null;
                try {
                    news = API.GetDetailNews("201608090432c815a85453c34d8ca43a591258701e9b");
                } catch (Exception e) {
                    e.printStackTrace();
                    assertTrue(false);
                }
                int tot = 0;
                assertEquals("德媒：俄柔道运动员里约夺金与普京密切相关", news.news_Title);
            }
        });
        t.start();
        t.join();
    }
}
