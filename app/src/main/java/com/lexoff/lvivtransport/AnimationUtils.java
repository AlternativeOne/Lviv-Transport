package com.lexoff.lvivtransport;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class AnimationUtils {

    //TODO: fix flickering
    public static void expandAdditionalControls(final View v) {
        float density=v.getContext().getResources().getDisplayMetrics().density;

        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(Utils.dpToPx(density, 400), View.MeasureSpec.EXACTLY));
        final int targetHeight = v.getMeasuredHeight();

        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                if (interpolatedTime%2==0) v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (targetHeight / density / 2));
        v.startAnimation(a);
    }

    public static void collapseAdditionalControls(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density / 2));
        v.startAnimation(a);
    }


}
