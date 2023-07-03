package com.lexoff.lvivtransport;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState==null) {
            SplashScreen.installSplashScreen(this);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTransparentStatusBar();
        setLightNavigationBar();

        NavigationUtils.openMapFragment(this);
    }

    @Override
    public void onResume(){
        super.onResume();

        //need to check for locale in onResume
        //because if app is unloaded and then reloaded
        //locale will be set to phone's default
        //and onCreate will not be called
        assureCorrectLocale();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration configuration){
        super.onConfigurationChanged(configuration);

        //this method is here only to prevent activity's recreation
    }

    private void setTransparentStatusBar() {
        Window window=getWindow();

        if (window != null) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }

    private void setLightNavigationBar() {
        if (Build.VERSION.SDK_INT < 26) return;

        Window window=getWindow();

        if (window != null) {
            int flags=window.getDecorView().getSystemUiVisibility();

            window.getDecorView().setSystemUiVisibility(
                    flags
                    | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );

            window.setNavigationBarColor(Color.WHITE);
        }
    }

    private void assureCorrectLocale(){
        String current=Utils.getAppLocaleAsString(this);

        int lang=PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("lang", 1);

        switch (lang){
            case 2: {
                if (!current.toLowerCase().equals("uk")){
                    setLanguage();

                    //need to recreate Activity to apply new locale to view
                    //e.g. BottomNavigationView
                    recreateThisActivity();
                }

                break;
            }
            default: {
                if (!current.toLowerCase().equals("en")){
                    setLanguage();

                    recreateThisActivity();
                }

                break;
            }
        }
    }

    public void recreateThisActivity(){
        new Handler(Looper.getMainLooper()).post(MainActivity.this::recreate);
    }

    private void setLanguage(){
        int lang=PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("lang", 1);
        switch (lang){
            case 2: Utils.setAppLocale(this, "uk"); break;
            default: Utils.setAppLocale(this, "en"); break;
        }
    }

    public void resizeFragment(boolean minimize, boolean toRight){
        View container=findViewById(R.id.fragment_container);

        if (container==null) {
            return;
        }

        if (minimize) {
            if (toRight) {
                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.width = (int) (container.getMeasuredWidth() * 0.8);
                layoutParams.height = (int) (container.getMeasuredHeight() * 0.85);
                layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;

                container.setLayoutParams(layoutParams);
            }
        } else {
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            container.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        Fragment lastFragment = fragments.get(fragments.size() - 1);

        if (lastFragment instanceof BackPressableFragment) {
            if (((BackPressableFragment) lastFragment).onBackPressed())
                return;
        }

        this.finish();
    }
}
