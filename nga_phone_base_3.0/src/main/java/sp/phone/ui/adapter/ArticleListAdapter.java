package sp.phone.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;

import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import gov.anzong.androidnga.R;
import gov.anzong.androidnga.arouter.ARouterConstants;
import gov.anzong.androidnga.base.util.DeviceUtils;
import sp.phone.http.bean.ThreadData;
import sp.phone.http.bean.ThreadRowInfo;
import sp.phone.common.PhoneConfiguration;
import sp.phone.common.UserManagerImpl;
import sp.phone.ui.fragment.dialog.AvatarDialogFragment;
import sp.phone.ui.fragment.dialog.BaseDialogFragment;
import sp.phone.rxjava.RxUtils;
import sp.phone.theme.ThemeManager;
import sp.phone.util.ActivityUtils;
import sp.phone.util.FunctionUtils;
import sp.phone.util.HtmlUtils;
import sp.phone.util.ImageUtils;
import sp.phone.util.StringUtils;
import sp.phone.view.webview.WebViewEx;

/**
 * 帖子详情列表Adapter
 */
public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ArticleViewHolder> {

    private static final String DEVICE_TYPE_IOS = "ios";

    private static final String DEVICE_TYPE_ANDROID = "android";

    private static final String DEVICE_TYPE_WP = "wp";

    private static final int VIEW_TYPE_WEB_VIEW = 0;

    private static final int VIEW_TYPE_NATIVE_VIEW = 1;

    private Context mContext;

    private FragmentManager mFragmentManager;

    private ThreadData mData;

    private LayoutInflater mLayoutInflater;

    private ThemeManager mThemeManager = ThemeManager.getInstance();

    private View.OnClickListener mOnClientClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            ThreadRowInfo row = (ThreadRowInfo) v.getTag();
            String fromClient = row.getFromClient();
            String clientModel = row.getFromClientModel();
            String deviceInfo;
            if (!StringUtils.isEmpty(clientModel)) {
                String clientAppCode;
                if (!fromClient.contains(" ")) {
                    clientAppCode = fromClient;
                } else {
                    clientAppCode = fromClient.substring(0,
                            fromClient.indexOf(' '));
                }
                switch (clientAppCode) {
                    case "1":
                        if (fromClient.length() <= 2) {
                            deviceInfo = "发送自Life Style苹果客户端 机型及系统:未知";
                        } else {
                            deviceInfo = "发送自Life Style苹果客户端 机型及系统:"
                                    + fromClient.substring(2);
                        }
                        break;
                    case "7":
                        if (fromClient.length() <= 2) {
                            deviceInfo = "发送自NGA苹果官方客户端 机型及系统:未知";
                        } else {
                            deviceInfo = "发送自NGA苹果官方客户端 机型及系统:"
                                    + fromClient.substring(2);
                        }
                        break;
                    case "8":
                        if (fromClient.length() <= 2) {
                            deviceInfo = "发送自NGA安卓客户端 机型及系统:未知";
                        } else {
                            String fromData = fromClient.substring(2);
                            if (fromData.startsWith("[")
                                    && fromData.contains("](Android")) {
                                deviceInfo = "发送自NGA安卓开源版客户端 机型及系统:"
                                        + fromData.substring(1).replace(
                                        "](Android", "(Android");
                            } else {
                                deviceInfo = "发送自NGA安卓官方客户端 机型及系统:" + fromData;
                            }
                        }
                        break;
                    case "9":
                        if (fromClient.length() <= 2) {
                            deviceInfo = "发送自NGA Windows Phone官方客户端 机型及系统:未知";
                        } else {
                            deviceInfo = "发送自NGA Windows Phone官方客户端 机型及系统:"
                                    + fromClient.substring(2);
                        }
                        break;
                    case "100":
                        if (fromClient.length() <= 4) {
                            deviceInfo = "发送自安卓浏览器 机型及系统:未知";
                        } else {
                            deviceInfo = "发送自安卓浏览器 机型及系统:"
                                    + fromClient.substring(4);
                        }
                        break;
                    case "101":
                        if (fromClient.length() <= 4) {
                            deviceInfo = "发送自苹果浏览器 机型及系统:未知";
                        } else {
                            deviceInfo = "发送自苹果浏览器 机型及系统:"
                                    + fromClient.substring(4);
                        }
                        break;
                    case "102":
                        if (fromClient.length() <= 4) {
                            deviceInfo = "发送自Blackberry浏览器 机型及系统:未知";
                        } else {
                            deviceInfo = "发送自Blackberry浏览器 机型及系统:"
                                    + fromClient.substring(4);
                        }
                        break;
                    case "103":
                        if (fromClient.length() <= 4) {
                            deviceInfo = "发送自Windows Phone客户端 机型及系统:未知";
                        } else {
                            deviceInfo = "发送自Windows Phone客户端 机型及系统:"
                                    + fromClient.substring(4);
                        }
                        break;
                    default:
                        if (!fromClient.contains(" ")) {
                            deviceInfo = "发送自未知浏览器 机型及系统:未知";
                        } else {
                            if (fromClient.length() == (fromClient.indexOf(' ') + 1)) {
                                deviceInfo = "发送自未知浏览器 机型及系统:未知";
                            } else {
                                deviceInfo = "发送自未知浏览器 机型及系统:"
                                        + fromClient.substring(fromClient
                                        .indexOf(' ') + 1);
                            }
                        }
                        break;
                }
                ActivityUtils.showToast(deviceInfo);
            }
        }
    };

    private View.OnClickListener mOnReplyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            ThreadRowInfo row = (ThreadRowInfo) view.getTag();

            (new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPostExecute(Void result) {
                    view.setEnabled(true);
                }

                @Override
                protected Void doInBackground(Void... params) {
                    Intent intent = new Intent();
                    StringBuilder postPrefix = new StringBuilder();
                    String mention = null;

                    final String quote_regex = "\\[quote\\]([\\s\\S])*\\[/quote\\]";
                    final String replay_regex = "\\[b\\]Reply to \\[pid=\\d+,\\d+,\\d+\\]Reply\\[/pid\\] Post by .+?\\[/b\\]";
                    String content = row.getContent();
                    final String name = row.getAuthor();
                    final String uid = String.valueOf(row.getAuthorid());
                    int page = (row.getLou() + 20) / 20;// 以楼数计算page
                    content = content.replaceAll(quote_regex, "");
                    content = content.replaceAll(replay_regex, "");
                    final String postTime = row.getPostdate();
                    final String tidStr = String.valueOf(row.getTid());
                    content = FunctionUtils.checkContent(content);
                    content = StringUtils.unEscapeHtml(content);
                    if (row.getPid() != 0) {
                        mention = name;
                        postPrefix.append("[quote][pid=");
                        postPrefix.append(row.getPid());
                        postPrefix.append(',');
                        if (tidStr != null) {
                            postPrefix.append(tidStr);
                            postPrefix.append(",");
                        }
                        if (page > 0)
                            postPrefix.append(page);
                        postPrefix.append("]");// Topic
                        postPrefix.append("Reply");
                        if (row.getISANONYMOUS()) {// 是匿名的人
                            postPrefix.append("[/pid] [b]Post by [uid=");
                            postPrefix.append("-1");
                            postPrefix.append("]");
                            postPrefix.append(name);
                            postPrefix.append("[/uid][color=gray](");
                            postPrefix.append(row.getLou());
                            postPrefix.append("楼)[/color] (");
                        } else {
                            postPrefix.append("[/pid] [b]Post by [uid=");
                            postPrefix.append(uid);
                            postPrefix.append("]");
                            postPrefix.append(name);
                            postPrefix.append("[/uid] (");
                        }
                        postPrefix.append(postTime);
                        postPrefix.append("):[/b]\n");
                        postPrefix.append(content);
                        postPrefix.append("[/quote]\n");
                    }
                    if (!StringUtils.isEmpty(mention))
                        intent.putExtra("mention", mention);
                    intent.putExtra("prefix",
                            StringUtils.removeBrTag(postPrefix.toString()));
                    if (tidStr != null)
                        intent.putExtra("tid", tidStr);
                    intent.putExtra("action", "reply");

                    if (UserManagerImpl.getInstance().getActiveUser() != null) {// 登入了才能发
                        intent.setClass(
                                view.getContext(),
                                PhoneConfiguration.getInstance().postActivityClass);
                    } else {
                        intent.setClass(
                                view.getContext(),
                                PhoneConfiguration.getInstance().loginActivityClass);
                    }
                    view.getContext().startActivity(intent);
                    return null;
                }
            }).execute();
        }
    };

    private View.OnClickListener mOnProfileClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ThreadRowInfo row = (ThreadRowInfo) view.getTag();

            if (row.getISANONYMOUS()) {
                ActivityUtils.showToast("这白痴匿名了,神马都看不到");
            } else {
                ARouter.getInstance()
                        .build(ARouterConstants.ACTIVITY_PROFILE)
                        .withString("mode", "username")
                        .withString("username", row.getAuthor())
                        .navigation();
            }
        }
    };

    private View.OnClickListener mOnAvatarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ThreadRowInfo row = (ThreadRowInfo) view.getTag();
            if (row.getISANONYMOUS()) {
                ActivityUtils.showToast("这白痴匿名了,神马都看不到");
            } else {
                Bundle bundle = new Bundle();
                bundle.putString("name", row.getAuthor());
                bundle.putString("url", FunctionUtils.parseAvatarUrl(row.getJs_escap_avatar()));
                BaseDialogFragment.show(mFragmentManager, bundle, AvatarDialogFragment.class);
                //FunctionUtils.Create_Avatar_Dialog(row, view.getContext(), null);
            }
        }
    };

    private View.OnClickListener mMenuTogglerListener;

    public class ArticleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_nickName)
        TextView nickNameTV;

        @BindView(R.id.wv_content)
        WebViewEx contentTV;

        @BindView(R.id.tv_floor)
        TextView floorTv;

        @BindView(R.id.tv_post_time)
        TextView postTimeTv;

        @BindView(R.id.iv_reply)
        ImageView replyBtn;

        @BindView(R.id.iv_avatar)
        ImageView avatarIv;

        @BindView(R.id.iv_client)
        ImageView clientIv;

        @BindView(R.id.tv_score)
        TextView scoreTv;

        @BindView(R.id.iv_more)
        ImageView menuIv;

        @BindView(R.id.fl_avatar)
        FrameLayout avatarPanel;

        @BindView(R.id.tv_detail)
        TextView detailTv;

        @BindView(R.id.tv_content)
        TextView contentTextView;

        public ArticleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public ArticleListAdapter(Context context, FragmentManager fm) {
        mContext = context;
        mFragmentManager = fm;
        if (HtmlUtils.hide == null) {
            HtmlUtils.initStaticStrings(mContext);
        }
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setData(ThreadData data) {
        mData = data;
    }

    public void setMenuTogglerListener(View.OnClickListener menuTogglerListener) {
        mMenuTogglerListener = menuTogglerListener;
    }

    @Override
    public int getItemViewType(int position) {
        ThreadRowInfo row = mData.getRowList().get(position);
        return TextUtils.isEmpty(row.getFormattedHtmlData()) ? VIEW_TYPE_NATIVE_VIEW : VIEW_TYPE_WEB_VIEW;
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.fragment_article_list_item, parent, false);
        ArticleViewHolder viewHolder = new ArticleViewHolder(view);
        ViewGroup.LayoutParams lp = viewHolder.avatarIv.getLayoutParams();
        lp.width = lp.height = PhoneConfiguration.getInstance().getAvatarSize();
        if (viewType == VIEW_TYPE_WEB_VIEW) {
            viewHolder.contentTextView.setVisibility(View.GONE);
            viewHolder.contentTV.setVisibility(View.VISIBLE);
            viewHolder.contentTV.setLocalMode();
        } else {
            viewHolder.contentTextView.setVisibility(View.VISIBLE);
            viewHolder.contentTV.setVisibility(View.GONE);
        }
        RxUtils.clicks(viewHolder.nickNameTV, mOnProfileClickListener);
        RxUtils.clicks(viewHolder.replyBtn, mOnReplyClickListener);
        RxUtils.clicks(viewHolder.clientIv, mOnClientClickListener);
        RxUtils.clicks(viewHolder.menuIv, mMenuTogglerListener);
        RxUtils.clicks(viewHolder.avatarPanel, mOnAvatarClickListener);
        viewHolder.contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, PhoneConfiguration.getInstance().getTopicContentSize());
        // viewHolder.contentTV.setTextSize(PhoneConfiguration.getInstance().getTopicContentSize());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ArticleViewHolder holder, final int position) {

        final ThreadRowInfo row = mData.getRowList().get(position);

        if (row == null) {
            return;
        }

        if (!PhoneConfiguration.getInstance().useSolidColorBackground()) {
            holder.itemView.setBackgroundResource(ThemeManager.getInstance().getBackgroundColor(position));
        }

        holder.replyBtn.setTag(row);
        holder.nickNameTV.setTag(row);
        holder.menuIv.setTag(row);
        holder.avatarPanel.setTag(row);

        onBindAvatarView(holder.avatarIv, row);
        onBindDeviceType(holder.clientIv, row);
        onBindContentView(holder, row);

        int fgColor = mThemeManager.getAccentColor(mContext);
        FunctionUtils.handleNickName(row, fgColor, holder.nickNameTV, mContext);

        holder.floorTv.setText(MessageFormat.format("[{0} 楼]", String.valueOf(row.getLou())));
        holder.postTimeTv.setText(row.getPostdate());
        holder.scoreTv.setText(MessageFormat.format("顶 : {0}", row.getScore()));

        holder.detailTv.setText(String.format("级别：%s   威望：%s   发帖：%s", row.getMemberGroup(), row.getReputation(), row.getPostCount()));

    }

    private void onBindContentView(ArticleViewHolder holder, ThreadRowInfo row) {
        String html = row.getFormattedHtmlData();
        if (html != null) {
            holder.contentTV.getWebViewClientEx().setImgUrls(row.getImageUrls());
            holder.contentTV.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        } else {
            holder.contentTextView.setText(row.getContent());
        }
    }

    private void onBindDeviceType(ImageView clientBtn, ThreadRowInfo row) {
        String deviceType = row.getFromClientModel();

        if (TextUtils.isEmpty(deviceType)) {
            clientBtn.setVisibility(View.GONE);
        } else {
            switch (deviceType) {
                case DEVICE_TYPE_IOS:
                    clientBtn.setImageResource(R.drawable.ic_apple_12dp);
                    break;
                case DEVICE_TYPE_WP:
                    clientBtn.setImageResource(R.drawable.ic_windows_12dp);
                    break;
                case DEVICE_TYPE_ANDROID:
                    clientBtn.setImageResource(R.drawable.ic_android_12dp);
                    break;
                default:
                    clientBtn.setImageResource(R.drawable.ic_smartphone_12dp);
                    break;
            }
            clientBtn.setTag(row);
            clientBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.getRowNum();
    }

    private void onBindAvatarView(ImageView avatarIv, ThreadRowInfo row) {
        final String avatarUrl = FunctionUtils.parseAvatarUrl(row.getJs_escap_avatar());
        final boolean downImg = DeviceUtils.isWifiConnected(mContext)
                || PhoneConfiguration.getInstance()
                .isDownAvatarNoWifi();

        ImageUtils.loadRoundCornerAvatar(avatarIv, avatarUrl, !downImg);
    }

}