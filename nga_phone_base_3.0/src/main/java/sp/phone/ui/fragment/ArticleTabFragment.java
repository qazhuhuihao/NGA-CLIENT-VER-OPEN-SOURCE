package sp.phone.ui.fragment;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gov.anzong.androidnga.R;
import gov.anzong.androidnga.Utils;
import gov.anzong.androidnga.base.widget.TabLayoutEx;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import sp.phone.rxjava.BaseSubscriber;
import sp.phone.theme.ThemeManager;
import sp.phone.ui.adapter.ArticlePagerAdapter;
import sp.phone.common.PhoneConfiguration;
import sp.phone.common.UserManagerImpl;
import sp.phone.param.ArticleListParam;
import sp.phone.param.ParamKey;
import sp.phone.ui.fragment.dialog.GotoDialogFragment;
import sp.phone.rxjava.RxBus;
import sp.phone.rxjava.RxEvent;
import sp.phone.task.BookmarkTask;
import sp.phone.util.ActivityUtils;
import sp.phone.util.FunctionUtils;
import sp.phone.util.StringUtils;
import sp.phone.view.behavior.ScrollAwareFamBehavior;

/**
 * 帖子详情Fragment
 * Created by Justwen on 2017/7/9.
 */

public class ArticleTabFragment extends BaseRxFragment {

    @BindView(R.id.pager)
    public ViewPager mViewPager;

    private ArticlePagerAdapter mPagerAdapter;

    private ArticleListParam mRequestParam;

    @BindView(R.id.tabs)
    public TabLayoutEx mTabLayout;

    private static final String GOTO_TAG = "goto";

    @BindView(R.id.fab_menu)
    public FloatingActionsMenu mFam;

    private int mReplyCount;

    private ScrollAwareFamBehavior mBehavior;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mRequestParam = getArguments().getParcelable(ParamKey.KEY_PARAM);
        }
    }

    @Override
    protected void accept(@NonNull RxEvent rxEvent) {
        switch (rxEvent.what) {
            case RxEvent.EVENT_ARTICLE_TAB_UPDATE:
                mReplyCount = rxEvent.arg;
                int count = mReplyCount / 20;
                if (mReplyCount % 20 != 0) {
                    count++;
                }
                if (count > mPagerAdapter.getCount()) {
                    mPagerAdapter.setCount(count);
                    mTabLayout.setTabOnScreenLimit(count <= 5 ? count : 0);
                    mTabLayout.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (mConfig.isShowBottomTab()) {
            return inflater.inflate(R.layout.fragment_article_tab_bottom, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_article_tab, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        updateFloatingMenu();
        mPagerAdapter = new ArticlePagerAdapter(getChildFragmentManager(), mRequestParam);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mBehavior.animateIn(mFam);
                super.onPageSelected(position);
            }
        });

        mTabLayout.setTabOnScreenLimit(1);
        mTabLayout.setUpWithViewPager(mViewPager);

        mFam.getAddFloatingActionButton().setOnLongClickListener(v -> {
            mBehavior.animateOut(mFam);
            return true;
        });
        super.onViewCreated(view, savedInstanceState);
    }

    private void updateFloatingMenu() {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mFam.getLayoutParams();
        mBehavior = (ScrollAwareFamBehavior) lp.getBehavior();
        if (mConfig.isLeftHandMode()) {
            lp.gravity = Gravity.START | Gravity.BOTTOM;
            mFam.setExpandDirection(FloatingActionsMenu.EXPAND_UP, FloatingActionsMenu.LABELS_ON_RIGHT_SIDE);
            mFam.setLayoutParams(lp);
        }
    }

    @Override
    public void onResume() {
        if (mFam != null) {
            mFam.collapse();
        }
        registerRxBus(FragmentEvent.PAUSE);
        super.onResume();
    }

    @OnClick(R.id.fab_post)
    public void reply() {
        Intent intent = new Intent();
        String tid = String.valueOf(mRequestParam.tid);
        intent.putExtra("prefix", "");
        intent.putExtra("tid", tid);
        intent.putExtra("action", "reply");
        if (!StringUtils.isEmpty(UserManagerImpl.getInstance().getUserName())) {// 登入了才能发
            intent.setClass(getContext(),
                    PhoneConfiguration.getInstance().postActivityClass);
        } else {
            intent.setClass(getContext(),
                    PhoneConfiguration.getInstance().loginActivityClass);
        }
        startActivityForResult(intent, ActivityUtils.REQUEST_CODE_TOPIC_POST);
    }

    @OnClick(R.id.fab_refresh)
    public void refresh() {
        mRequestParam.page = mViewPager.getCurrentItem() + 1;
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_ARTICLE_UPDATE, mViewPager.getCurrentItem() + 1, mRequestParam));
        mFam.collapse();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_bookmark:
                BookmarkTask.execute(mRequestParam.tid);
                break;
            case R.id.menu_goto_floor:
                createGotoDialog();
                break;
            case R.id.menu_share:
                share();
                break;
            case R.id.menu_copy_url:
                copyUrl();
                break;
            case R.id.menu_nightmode:
                ThemeManager.getInstance().setNightMode(true);
                Observable.timer(0, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<Long>() {
                            @Override
                            public void onNext(Long aLong) {
                                getActivity().recreate();
                            }
                        });
                break;
            case R.id.menu_daymode:
                ThemeManager.getInstance().setNightMode(false);
                Observable.timer(0, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<Long>() {
                            @Override
                            public void onNext(Long aLong) {
                                getActivity().recreate();
                            }
                        });
                break;
            case R.id.menu_download:
                mRequestParam.page = mViewPager.getCurrentItem() + 1;
                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_CACHE_TOPIC_CONTENT, mViewPager.getCurrentItem() + 1, mRequestParam));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void copyUrl() {
        StringBuilder builder = new StringBuilder();
        builder.append(Utils.getNGAHost()).append("read.php?");
        if (mRequestParam.pid != 0) {
            builder.append("pid=").append(mRequestParam.pid);
        } else {
            builder.append("tid=").append(mRequestParam.tid);
        }
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            ClipData clipData = ClipData.newPlainText("text", builder.toString());
            clipboardManager.setPrimaryClip(clipData);
            showToast("已经复制至粘贴板");
        }

    }

    private void share() {
        String title = getString(R.string.share);
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(getActivity().getTitle())) {
            builder.append("《").append(getActivity().getTitle()).append("》 - 艾泽拉斯国家地理论坛，地址：");
        }
        builder.append(Utils.getNGAHost()).append("read.php?");
        if (mRequestParam.pid != 0) {
            builder.append("pid=").append(mRequestParam.pid).append(" (分享自NGA安卓客户端开源版)");
        } else {
            builder.append("tid=").append(mRequestParam.tid).append(" (分享自NGA安卓客户端开源版)");
        }
        FunctionUtils.share(getContext(), title, builder.toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.article_list_option_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_goto_floor).setVisible(mReplyCount != 0);

        if (ThemeManager.getInstance().isNightMode()) {
            menu.findItem(R.id.menu_nightmode).setVisible(false);
            menu.findItem(R.id.menu_daymode).setVisible(true);
        } else {
            menu.findItem(R.id.menu_nightmode).setVisible(true);
            menu.findItem(R.id.menu_daymode).setVisible(false);
        }

        if (mRequestParam.pid != 0) {
            menu.findItem(R.id.menu_download).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    private void createGotoDialog() {

        Bundle args = new Bundle();
        args.putInt("page", mPagerAdapter.getCount());
        args.putInt("floor", mReplyCount);

        DialogFragment df = new GotoDialogFragment();
        df.setArguments(args);
        df.setTargetFragment(this, ActivityUtils.REQUEST_CODE_JUMP_PAGE);

        FragmentManager fm = getActivity().getSupportFragmentManager();

        Fragment prev = fm.findFragmentByTag(GOTO_TAG);
        if (prev != null) {
            fm.beginTransaction().remove(prev).commit();
        }
        df.show(fm, GOTO_TAG);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityUtils.REQUEST_CODE_TOPIC_POST && resultCode == Activity.RESULT_OK) {
            if (mViewPager.getCurrentItem() == mPagerAdapter.getCount() - 1) {
                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_ARTICLE_UPDATE, mViewPager.getCurrentItem()));
            }
        } else if (requestCode == ActivityUtils.REQUEST_CODE_JUMP_PAGE) {
            if (data.hasExtra("page")) {
                mViewPager.setCurrentItem(data.getIntExtra("page", 0));
            } else {
                int floor = data.getIntExtra("floor", 0);
                mViewPager.setCurrentItem(floor / 20);
                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_ARTICLE_GO_FLOOR, mViewPager.getCurrentItem(), floor % 20));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

}
