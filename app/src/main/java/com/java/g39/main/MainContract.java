package com.java.g39.main;

import com.java.g39.BaseView;
import com.java.g39.BasePresenter;
import com.java.g39.data.Config;

/**
 * Created by equation on 9/7/17.
 */

public interface MainContract {

    interface View extends BaseView<MainContract.Presenter> {

        /**
         * 切换到新闻页面
         */
        void switchToNews();

        /**
         * 切换到收藏页面
         */
        void switchToFavorites();

        /**
         * 切换到设置页面
         */
        void switchToSettings();

        /**
         * 切换到关于页面
         */
        void switchToAbout();
    }

    interface Presenter extends BasePresenter {

        /**
         * 是否夜间模式
         * @return
         */
        boolean isNightMode();

        void setConfigNightModeChangeListener(Config.NightModeChangeListener listener);

        /**
         * 切换页面
         *
         * @param id 页面 ID
         */
        void switchNavigation(int id);

        /**
         * 获得当前页面
         * @return 页面 ID
         */
        int getCurrentNavigation();
    }
}
