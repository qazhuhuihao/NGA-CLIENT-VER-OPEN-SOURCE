package com.justwen.androidnga.cloud.lean;

import android.content.Context;
import android.os.Build;

import com.justwen.androidnga.cloud.BuildConfig;
import com.justwen.androidnga.cloud.R;
import com.justwen.androidnga.cloud.base.ICloudDataBase;
import com.justwen.androidnga.cloud.base.VersionBean;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVObject;

public class LeanDataBase implements ICloudDataBase {

    private VersionBean mVersionBean;

    public LeanDataBase(VersionBean versionBean) {
        mVersionBean = versionBean;
    }

    @Override
    public void init(Context context) {
        AVOSCloud.initialize(context, context.getString(R.string.lean_cloud_app_id), context.getString(R.string.lean_cloud_app_key));
        if (BuildConfig.DEBUG) {
            AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
        }
    }

    private void uploadAndroidVersionInfo() {
        AVObject obj = new AVObject("androidVersion");
        obj.put("versionName", mVersionBean.versionName);
        obj.put("androidVersion", Build.VERSION.SDK_INT);
        obj.increment("count");
        obj.saveInBackground().subscribe(new EmptyObserver<>());
    }


    @Override
    public void uploadVersionInfo() {
//        AVObject obj = new AVObject("version");
//        obj.put("versionName", mVersionBean.versionName);
//        obj.increment("count");
//        obj.saveInBackground().subscribe(new EmptyObserver<>());
        uploadAndroidVersionInfo();
    }

    @Override
    public void checkUpgrade() {
//        AVQuery<AVObject> query = new AVQuery<>("versionUpgrade");
//        query.whereGreaterThan("versionCode", mVersionBean.versionCode);
//        query.findInBackground().subscribe(new Observer<List<AVObject>>() {
//            @Override
//            public void onSubscribe(Disposable disposable) {
//            }
//
//            @Override
//            public void onNext(List<AVObject> objects) {
//                if (objects != null && !objects.isEmpty()) {
//                    PreferenceUtils.putData(PreferenceKey.KEY_CHECK_UPGRADE_STATE, false);
//                    ToastUtils.show("新版本已发布， 关于界面可以查看最新版本！");
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });
    }


}
