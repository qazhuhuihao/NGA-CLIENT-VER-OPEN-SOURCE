package gov.anzong.androidnga.base.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

/**
 * Created by Justwen on 2017/6/24.
 */
@SuppressLint("CheckResult")
public class PermissionUtils {

    private PermissionUtils() {

    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCombined(AppCompatActivity activity, @Nullable Consumer<? super Permission> consumer, String... permissions) {
        consumer = createIfNull(consumer);
        new RxPermissions(activity).requestEachCombined(permissions).subscribe(consumer);
    }

    public static void requestEach(AppCompatActivity activity, @Nullable Consumer<? super Permission> consumer, String... permissions) {
        consumer = createIfNull(consumer);
        new RxPermissions(activity).requestEach(permissions).subscribe(consumer);
    }

    public static void request(AppCompatActivity activity, @Nullable Consumer<Boolean> consumer, String permission) {
        consumer = createIfNull(consumer);
        new RxPermissions(activity).request(permission).subscribe(consumer);
    }

    public static void requestCombined(Fragment fragment, @Nullable Consumer<? super Permission> consumer, String... permissions) {
        consumer = createIfNull(consumer);
        new RxPermissions(fragment).requestEachCombined(permissions).subscribe(consumer);
    }

    public static void requestEach(Fragment fragment, @Nullable Consumer<? super Permission> consumer, String... permissions) {
        consumer = createIfNull(consumer);
        new RxPermissions(fragment).requestEach(permissions).subscribe(consumer);
    }

    public static void request(Fragment fragment, @Nullable Consumer<Boolean> consumer, String permission) {
        consumer = createIfNull(consumer);
        new RxPermissions(fragment).request(permission).subscribe(consumer);
    }

    private static <T> Consumer<T> createIfNull(Consumer<T> consumer) {
        if (consumer == null) {
            consumer = t -> {

            };
        }
        return consumer;
    }
}
