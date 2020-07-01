package sp.phone.mvp.contract;

import sp.phone.param.TopicListParam;
import sp.phone.http.OnHttpCallBack;
import sp.phone.mvp.model.entity.ThreadPageInfo;
import sp.phone.mvp.model.entity.TopicListInfo;

/**
 * Created by Justwen on 2017/6/3.
 */

public interface TopicListContract {

    interface Presenter {

        void removeTopic(ThreadPageInfo info, int position);

        void removeCacheTopic(ThreadPageInfo info);

        void loadPage(int page, TopicListParam requestInfo);

        void loadCachePage();

        void loadNextPage(int page, TopicListParam requestInfo);

        boolean isBookmarkBoard(int fid, int stid);

        void addBookmarkBoard(int fid, int stid, String boardName);

        void removeBookmarkBoard(int fid, int stid);
    }

    interface View {

        void setRefreshing(boolean refreshing);

        boolean isRefreshing();

        void setData(TopicListInfo result);

        void clearData();

        void scrollTo(int position);

        void setNextPageEnabled(boolean enabled);

        void removeTopic(int position);

        void removeTopic(ThreadPageInfo pageInfo);

        void hideLoadingView();

    }

    interface Model {

        void loadCache(OnHttpCallBack<TopicListInfo> callBack);

        void removeTopic(ThreadPageInfo info, OnHttpCallBack<String> callBack);

        void loadTopicList(int page, TopicListParam param, OnHttpCallBack<TopicListInfo> callBack);

        void loadTwentyFourList(TopicListParam param, OnHttpCallBack<TopicListInfo> callBack, int pageCount);

        void removeCacheTopic(ThreadPageInfo info, OnHttpCallBack<String> callBack);
    }

}
