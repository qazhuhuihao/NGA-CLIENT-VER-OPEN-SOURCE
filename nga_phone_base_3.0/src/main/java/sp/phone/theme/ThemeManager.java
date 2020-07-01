package sp.phone.theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.TypedValue;

import gov.anzong.androidnga.R;
import gov.anzong.androidnga.common.PreferenceKey;
import sp.phone.common.ApplicationContextHolder;

public class ThemeManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    private int[] mAppThemes = {
            R.style.AppThemeDayNightBrown_NoActionBar,
            R.style.AppThemeDayNightGreen_NoActionBar,
            R.style.AppThemeDayNightBlack_NoActionBar,
    };

    private int[] mAppThemesActionBar = {
            R.style.AppThemeDayNightBrown,
            R.style.AppThemeDayNightGreen,
            R.style.AppThemeDayNightBlack,
    };

    private int mThemeIndex;

    private boolean mNightMode;

    private Context mContext;

    private WebViewTheme mWebViewTheme;

    private TypedValue mTypedValue = new TypedValue();

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equals(PreferenceKey.NIGHT_MODE)) {
            mNightMode = sp.getBoolean(key, false);
        } else if (key.equals(PreferenceKey.MATERIAL_THEME)) {
            mThemeIndex = Integer.parseInt(sp.getString(key, "0"));
        }
        mWebViewTheme = null;
    }

    private static class ThemeManagerHolder {

        private static ThemeManager sInstance = new ThemeManager();
    }

    private ThemeManager() {
        mContext = ApplicationContextHolder.getContext();
        SharedPreferences sp = mContext.getSharedPreferences(PreferenceKey.PERFERENCE, Context.MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(this);
        mNightMode = sp.getBoolean(PreferenceKey.NIGHT_MODE, false);
        mThemeIndex = Integer.parseInt(sp.getString(PreferenceKey.MATERIAL_THEME, "1"));
    }

    public static ThemeManager getInstance() {
        return ThemeManagerHolder.sInstance;
    }


    public void initializeWebTheme(Context context) {
        if (mWebViewTheme == null) {
            mWebViewTheme = new WebViewTheme(context);
        }
    }

    public int getForegroundColor() {
        return R.color.foreground_color;
    }

    public int getBackgroundColor() {
        return getBackgroundColor(0);
    }

    public int getBackgroundColor(int position) {
        return position % 2 == 1 ? R.color.background_color2 : R.color.background_color;
    }

    public boolean isNightMode() {
        return mNightMode;
    }

    public void setNightMode(boolean isNightMode){
        mContext = ApplicationContextHolder.getContext();
        SharedPreferences sp = mContext.getSharedPreferences(PreferenceKey.PERFERENCE, Context.MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(this);
        mNightMode = isNightMode;
        sp.edit().putBoolean(PreferenceKey.NIGHT_MODE,isNightMode).apply();
    }

    @ColorInt
    public int getPrimaryColor(Context context) {
        context.getTheme().resolveAttribute(android.R.attr.colorPrimary, mTypedValue, true);
        return ContextCompat.getColor(context, mTypedValue.resourceId);
    }

    @ColorInt
    public int getAccentColor(Context context) {
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, mTypedValue, true);
        return ContextCompat.getColor(context, mTypedValue.resourceId);
    }


    @ColorInt
    public int getWebTextColor() {
        return mWebViewTheme.getWebTextColor();
    }

    @ColorInt
    public int getWebQuoteBackgroundColor() {
        return mWebViewTheme.getQuoteBackgroundColor();
    }

    @StyleRes
    public int getTheme(boolean toolbarEnabled) {
        int index = isNightMode() ? 0 : mThemeIndex;
        return toolbarEnabled ? mAppThemes[index] : mAppThemesActionBar[index];
    }

    public void applyAboutTheme(AppCompatActivity activity) {
        activity.setTheme(ThemeConstants.THEME_ACTIVITY_ABOUT[isNightMode() ? 0 : mThemeIndex]);
        activity.getDelegate().setLocalNightMode(isNightMode() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void applyTheme(Activity activity) {

    }
}
