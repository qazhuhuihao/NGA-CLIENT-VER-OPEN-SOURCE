package sp.phone.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import gov.anzong.androidnga.R;
import gov.anzong.androidnga.base.util.ToastUtils;
import sp.phone.mvp.model.entity.ThreadPageInfo;

/**
 * Created by Justwen on 2017/11/19.
 */

public class TopicFavoriteFragment extends TopicSearchFragment implements View.OnLongClickListener {

    @Override
    protected void setTitle() {
        setTitle(R.string.bookmark_title);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ToastUtils.info("长按可删除收藏的帖子");
        mAdapter.setOnLongClickListener(this);
    }

    @Override
    public void removeTopic(int position) {
        mAdapter.removeItem(position);
    }

    @Override
    public void removeTopic(ThreadPageInfo pageInfo) {
        mAdapter.removeItem(pageInfo);
    }

    @Override
    public boolean onLongClick(final View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(this.getString(R.string.delete_favo_confirm_text))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ThreadPageInfo info = (ThreadPageInfo) view.getTag();
                        mPresenter.removeTopic(info, info.getPosition());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
        return true;
    }
}
