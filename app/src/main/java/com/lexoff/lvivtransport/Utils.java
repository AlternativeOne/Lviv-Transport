package com.lexoff.lvivtransport;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.RouteInfo;

import java.io.IOException;
import java.util.Locale;

public class Utils {
    public static String LANG_KEY="lang";

    public static Locale getAppLocale(Context context){
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        return configuration.getLocales().get(0);
    }

    public static String getAppLocaleAsString(Context context) {
        return getAppLocale(context).getLanguage();
    }

    public static void setAppLocale(Context context, String localeCode) {
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(localeCode.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void fixLayoutTopPadding(View view) {
        view.setPadding(view.getPaddingStart(), view.getPaddingTop() + getStatusBarHeight(view.getContext()), view.getPaddingEnd(), view.getPaddingBottom());
    }

    public static boolean isNullOrEmpty(CharSequence s){
        return s==null || s.length()==0;
    }

    public static boolean isNetworkRelated(Throwable throwable) {
        return hasAssignableCause(throwable, IOException.class);
    }

    private static boolean hasAssignableCause(Throwable throwable, Class... causesToCheck) {
        return hasCause(throwable, true, causesToCheck);
    }

    private static boolean hasCause(Throwable throwable, boolean checkSubtypes, Class... causesToCheck) {
        if (throwable == null) {
            return false;
        }

        // Check if throwable is a subtype of any of the causes to check
        for (Class causeClass : causesToCheck){
            if (checkSubtypes){
                if (causeClass.isAssignableFrom(throwable.getClass())){
                    return true;
                }
            } else {
                if (causeClass.equals(throwable.getClass())){
                    return true;
                }
            }
        }

        Throwable currentCause = throwable.getCause();
        // Check if cause is not pointing to the same instance, to avoid infinite loops.
        if (!throwable.equals(currentCause)) {
            return hasCause(currentCause, checkSubtypes, causesToCheck);
        }

        return false;
    }

    public static String getShortRouteNameFromInfo(Info info){
        if (!(info instanceof RouteInfo)) return "";

        RouteInfo rInfo=(RouteInfo) info;
        String result=rInfo.getStaticRouteInfo().getRouteShortName();

        return result==null ? "" : result;
    }

    public static String getLongRouteNameFromInfo(Info info){
        if (!(info instanceof RouteInfo)) return "";

        RouteInfo rInfo=(RouteInfo) info;
        String result=rInfo.getStaticRouteInfo().getRouteLongName();

        return result==null ? "" : result;
    }

    public static void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //inverseDirection is hack to return correct values even for routes with inverted routes
    //but it seems there is no need in it for now atleast
    public static String getRouteArrow(int direction, boolean inverseDirection){
        if (inverseDirection){
            if (direction==0) direction=1;
            else if (direction==1) direction=0;
        }

        return direction==1 ? "↓" : "↑";
    }

    public static String getRouteColorCode(int index, boolean useDarkColors){
        String[] colorCodes=new String[]{"#0336FF", "#EE02BE", "#02DFEE", "#EED502", "#EE0220", "#02EE6E", "#EE4602"};
        String[] darkColorCodes=new String[]{"#0336FF", "#C300B3", "#00B8DA", "#EDCA00", "#EE0219", "#00B925", "#EE4602"};

        while (index>=colorCodes.length) index-=colorCodes.length;

        return useDarkColors ? darkColorCodes[index] : colorCodes[index];
    }

    public static int getRouteColor(int index, boolean useDarkColors){
        return Color.parseColor(getRouteColorCode(index, useDarkColors));
    }

    //TODO: rework?
    public static void animateClickOnItem(View v, Runnable callback){
        //drawable instead of tint list
        //because tint list has no effect in grid layout
        final Drawable startDrw=v.getForeground();

        v.setForeground(new ColorDrawable(Color.parseColor("#99F5F5F5")));
        v.postDelayed(() -> {
            v.setForeground(startDrw);

            callback.run();
        }, 10);
    }

    public static Bitmap drawableToBitmap(Drawable drawable){
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static void animateClickOnImageButton(View v, Runnable callback) {
        if (!(v instanceof ImageView) && !(v instanceof ImageButton)) {
            return;
        }

        final ImageView imgView=(ImageView) v;

        final Drawable drawable=imgView.getDrawable();
        Bitmap bitmap=drawableToBitmap(drawable);

        Bitmap resized=Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth()*0.85), (int) (bitmap.getHeight()*0.85), false);
        imgView.setImageBitmap(resized);

        imgView.postDelayed(() -> {
            imgView.setImageDrawable(drawable);

            callback.run();
        }, 100);
    }

    public static int dpToPx(float density, int dp){
        return (int)(density*dp);
    }

}
