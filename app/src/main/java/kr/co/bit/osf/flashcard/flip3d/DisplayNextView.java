package kr.co.bit.osf.flashcard.flip3d;

import android.view.View;
import android.view.animation.Animation;

// http://www.inter-fuser.com/2009/08/android-animations-3d-flip.html
public class DisplayNextView implements Animation.AnimationListener {
    private boolean mCurrentView;
    View view1;
    View view2;

    public DisplayNextView(boolean currentView, View view1, View view2) {
        mCurrentView = currentView;
        this.view1 = view1;
        this.view2 = view2;
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        view1.post(new SwapViews(mCurrentView, view1, view2));
    }

    public void onAnimationRepeat(Animation animation) {
    }
}
