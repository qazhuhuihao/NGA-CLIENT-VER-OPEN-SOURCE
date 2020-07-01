package sp.phone.mvp.presenter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gov.anzong.androidnga.base.util.ThreadUtils;
import gov.anzong.androidnga.base.util.ToastUtils;
import sp.phone.mvp.model.BoardModel;
import sp.phone.param.TopicListParam;
import sp.phone.ui.fragment.TopicSearchFragment;
import sp.phone.http.OnHttpCallBack;
import sp.phone.mvp.contract.TopicListContract;
import sp.phone.mvp.model.TopicListModel;
import sp.phone.mvp.model.entity.ThreadPageInfo;
import sp.phone.mvp.model.entity.TopicListInfo;

/**
 *
 * @author Justwen
 * @date 2017/6/3
 */

public class TopicListPresenter extends BasePresenter<TopicSearchFragment, TopicListModel> implements TopicListContract.Presenter {

    // Following variables are for the 24 hour hot topic feature
    // How many pages we query for twenty four hour hot topic
    protected final int twentyFourPageCount = 5;
    // How many total topics we want to show
    protected final int twentyFourTopicCount = 50;
    protected int pageQueriedCounter = 0;
    protected int twentyFourCurPos = 0;
    protected TopicListInfo twentyFourList = new TopicListInfo();
    protected TopicListInfo twentyFourCurList = new TopicListInfo();

    private TopicListParam mRequestParam;

    private OnHttpCallBack<TopicListInfo> mCallBack = new OnHttpCallBack<TopicListInfo>() {
        @Override
        public void onError(String text) {
            if (isAttached()) {
                mBaseView.setRefreshing(false);
                mBaseView.showToast(text);
                mBaseView.hideLoadingView();
            }
        }

        @Override
        public void onSuccess(TopicListInfo data) {
            if (!isAttached()) {
                return;
            }
            mBaseView.clearData();
            mBaseView.scrollTo(0);
            setData(data);
            mBaseView.hideLoadingView();
        }
    };

    private OnHttpCallBack<TopicListInfo> mNextPageCallBack = new OnHttpCallBack<TopicListInfo>() {
        @Override
        public void onError(String text) {
            if (isAttached()) {
                mBaseView.setRefreshing(false);
                mBaseView.setNextPageEnabled(false);
                mBaseView.showToast(text);
            }
        }

        @Override
        public void onSuccess(TopicListInfo data) {
            if (!isAttached()) {
                return;
            }
            setData(data);
        }
    };

    /* callback for the twenty four hour hot topic list */
    private OnHttpCallBack<TopicListInfo> mTwentyFourCallBack = new OnHttpCallBack<TopicListInfo>() {
        @Override
        public void onError(String text) {
            if (isAttached()) {
                mBaseView.setRefreshing(false);
                mBaseView.setNextPageEnabled(false);
                mBaseView.showToast(text);
            }
        }

        @Override
        public void onSuccess(TopicListInfo data) {
            if (!isAttached()) {
                return;
            }
            /* Concatenate the pages */
            twentyFourList.getThreadPageList().addAll(data.getThreadPageList());
            pageQueriedCounter++;

            if (pageQueriedCounter == twentyFourPageCount) {
                twentyFourCurPos = 0;
                List<ThreadPageInfo> threadPageList = twentyFourList.getThreadPageList();
                threadPageList.removeIf(item -> (data.curTime - item.getPostDate() > 24 * 60 * 60));
                if (threadPageList.size() > twentyFourTopicCount) {
                    threadPageList.subList(twentyFourTopicCount, threadPageList.size());
                }
                Collections.sort(twentyFourList.getThreadPageList(), (o1, o2) -> Integer.compare(o2.getReplies(), o1.getReplies()));
                // We list 20 topics each time
                int endPos = twentyFourCurPos + 20 > twentyFourList.getThreadPageList().size() ?
                        twentyFourList.getThreadPageList().size() : (twentyFourCurPos + 20);
                twentyFourCurList.setThreadPageList(twentyFourList.getThreadPageList().subList(0, endPos));
                twentyFourCurPos = endPos;
                setData(twentyFourCurList);
                mBaseView.hideLoadingView();
            }
        }
    };

    public TopicListPresenter() {
    }

    public TopicListPresenter(TopicListParam requestParam) {
        mRequestParam = requestParam;
    }

    private void setData(TopicListInfo result) {
        mBaseView.setRefreshing(false);
        mBaseView.setData(result);
    }

    @Override
    protected TopicListModel onCreateModel() {
        return TopicListModel.getInstance();
    }

    @Override
    public void removeTopic(ThreadPageInfo info, final int position) {
        mBaseModel.removeTopic(info, new OnHttpCallBack<String>() {
            @Override
            public void onError(String text) {
                if (isAttached()) {
                    mBaseView.showToast(text);
                }
            }

            @Override
            public void onSuccess(String data) {
                if (isAttached()) {
                    mBaseView.showToast(data);
                    mBaseView.removeTopic(info);
                }
            }
        });
    }

    @Override
    public void removeCacheTopic(ThreadPageInfo info) {
        mBaseModel.removeCacheTopic(info, new OnHttpCallBack<String>() {
            @Override
            public void onError(String text) {
                if (isAttached()) {
                    ToastUtils.showToast("删除失败！");
                }
            }

            @Override
            public void onSuccess(String data) {
                if (isAttached()) {
                    ThreadUtils.runOnMainThread(() -> {
                        ToastUtils.showToast("删除成功！");
                        mBaseView.removeTopic(info);
                    });

                }
            }
        });

    }

    @Override
    public void loadPage(int page, TopicListParam requestInfo) {
        mBaseView.setRefreshing(true);
        if (requestInfo.twentyfour == 1) {
            // preload pages
            twentyFourList.getThreadPageList().clear();
            pageQueriedCounter = 0;
            mBaseView.clearData();
            mBaseView.scrollTo(0);
            mBaseModel.loadTwentyFourList(requestInfo, mTwentyFourCallBack, twentyFourPageCount);
        } else {
            mBaseModel.loadTopicList(page, requestInfo, mCallBack);
        }
    }

    @Override
    public void loadCachePage() {
        mBaseModel.loadCache(mCallBack);
    }

    @Override
    public void loadNextPage(int page, TopicListParam requestInfo) {
        mBaseView.setRefreshing(true);
        if (requestInfo.twentyfour == 1) {
            int endPos = twentyFourCurPos + 20 > twentyFourList.getThreadPageList().size() ?
                    twentyFourList.getThreadPageList().size() : (twentyFourCurPos + 20);
            twentyFourCurList.setThreadPageList(twentyFourList.getThreadPageList().subList(0, endPos));
            twentyFourCurPos = endPos;
            setData(twentyFourCurList);
        } else {
            mBaseModel.loadTopicList(page, requestInfo, mNextPageCallBack);
        }
    }

    @Override
    public boolean isBookmarkBoard(int fid, int stid) {
        return BoardModel.getInstance().isBookmark(fid, stid);
    }

    @Override
    public void addBookmarkBoard(int fid, int stid, String boardName) {
        BoardModel.getInstance().addBookmark(fid, stid, boardName);
    }

    @Override
    public void removeBookmarkBoard(int fid, int stid) {
        BoardModel.getInstance().removeBookmark(fid, stid);
    }

    @Override
    public void onViewCreated() {
        if (mRequestParam != null && mRequestParam.loadCache) {
            loadCachePage();
        } else {
            loadPage(1, mRequestParam);
        }
    }
}
