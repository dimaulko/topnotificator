package ua.improveit.topnotificator;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.behavior.SwipeDismissBehavior;

import java.lang.ref.WeakReference;

public class NotificationView extends CoordinatorLayout {

    private final long TIME_TO_SHOW = 5_000L;
    private final SwipeDismissBehavior<CardView> swipe = new SwipeDismissBehavior();
    private WeakReference<NotificationView> INSTANCE;
    private MyTimer timer;
    private View currentNotificationView;

    public NotificationView(@NonNull Context context) {
        super(context);
        init();
    }

    public NotificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NotificationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View.inflate(getContext(), R.layout.notification_view, this);
//        slideUp.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                if (timer != null) {
//                    timer.cancel();
//                }
//                if (currentNotificationView != null) {
//                    removeView(currentNotificationView);
////                }
////            }
////
////            @Override
////            public void onAnimationRepeat(Animation animation) {
////
////            }
////        });

        swipe.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
        swipe.setStartAlphaSwipeDistance(0.1f);
        swipe.setEndAlphaSwipeDistance(0.6f);
        swipe.setListener(new SwipeDismissBehavior.OnDismissListener() {
            @Override
            public void onDismiss(View view) {
                if (timer != null) {
                    timer.cancel();
                }
                if (currentNotificationView != null) {
                    removeView(currentNotificationView);
                }
            }

            @Override
            public void onDragStateChanged(int state) {
            }
        });
        INSTANCE = new WeakReference<>(this);
    }

    public void showMessage(String text) {
        Log.d("Child count", "count " + this.getChildCount());

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.notification_slide_up);
        Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.notification_slide_down);
        Animation slideDownInvalidated = AnimationUtils.loadAnimation(getContext(), R.anim.notification_slide_down_invalidated);


        View newView = constructMessageView();
        newView.setAlpha(0f);
        ((TextView) newView.findViewById(R.id.tv_message)).setText(text);
        newView.setId(ViewCompat.generateViewId());
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (currentNotificationView != null) {
            if (currentNotificationView.getAnimation() != null && currentNotificationView.getAnimation().hasStarted() && !currentNotificationView.getAnimation().hasEnded()) {
                slideDownInvalidated.setAnimationListener(null);
                currentNotificationView.animate().alpha(0f).setDuration(500).setListener(new MyAnimatorListener(INSTANCE.get(), currentNotificationView, true)).start();
            } else {
                currentNotificationView.animate().alpha(0f).translationYBy(800).setDuration(500).setListener(new MyAnimatorListener(INSTANCE.get(), currentNotificationView, true)).start();
//                slideDownInvalidated.setAnimationListener(new MyAnimDown(INSTANCE.get(), currentNotificationView, true));
//                currentNotificationView.startAnimation(slideDownInvalidated);
            }
        }


        LayoutParams coordinatorParams =
                (LayoutParams) newView.getLayoutParams();

        coordinatorParams.setBehavior(swipe);

        addView(newView);
//        slideDown.setFillAfter(true);
//        slideDown.setAnimationListener(new MyAnimDown(this, newView, false));
//        newView.startAnimation(slideDown);
        newView.animate().alpha(1f).translationYBy(500).setDuration(3000).setListener(new MyAnimatorListener(INSTANCE.get(), currentNotificationView, false)).start();
        currentNotificationView = newView;
//        currentNotificationView.animate().alpha(0).translationYBy(350).setDuration(500).setListener(new MyAnimatorListener(INSTANCE.get(), currentNotificationView, true)).start();
//        slideUp.setAnimationListener(new MyAnimDown(this, newView, true));
        timer = new MyTimer(currentNotificationView, slideUp, TIME_TO_SHOW);
        timer.start();
    }

    public CardView constructMessageView() {
        CardView view = (CardView) View.inflate(getContext(), R.layout.notification, null);
        LayoutParams params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.topMargin = dpToPx(0);
        params.leftMargin = dpToPx(16);
        params.rightMargin = dpToPx(16);
        params.bottomMargin = dpToPx(0);
        view.setLayoutParams(params);
        view.findViewById(R.id.tv_button_ok).setOnClickListener(v -> {

            try {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }

//                view.startAnimation(slideUp);

            } catch (Exception ex) {
                if (BuildConfig.DEBUG) {
                    Log.e(this.getClass().getSimpleName(), "Error", ex);
                }
            }
        });
        return view;
    }

    @Px
    public int dpToPx(@Dimension(unit = Dimension.DP) int dp) {
        final Resources resources = getContext().getResources();
        final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    class MyTimer extends CountDownTimer {

        WeakReference<View> _view;
        WeakReference<Animation> _anim;

        public MyTimer(View view, Animation anim, long period) {
            super(period, 300);
            _view = new WeakReference<>(view);
            _anim = new WeakReference<>(anim);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.e(this.getClass().getSimpleName(), "TIK " + millisUntilFinished);
        }

        @Override
        public void onFinish() {
//            if (_view != null && _view.get() != null && _anim != null && _anim.get() != null) {
            if (_view != null && _view.get() != null) {
                try {
                    _view.get().animate().alpha(0f).translationY(-350).setDuration(500).setListener(new MyAnimatorListener(INSTANCE.get(), _view.get(), true)).start();
//                    _view.get().startAnimation(_anim.get());
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(this.getClass().getSimpleName(), "Error", e);
                    }
                }
            }
        }
    }

    class MyAnimDown implements Animation.AnimationListener {

        WeakReference<View> _viewRef;
        WeakReference<ViewGroup> _viewGroupRef;
        boolean _removeAfter;

        public MyAnimDown(ViewGroup viewGroup, View view, boolean removeAfter) {
            _viewRef = new WeakReference<>(view);
            _viewGroupRef = new WeakReference<>(viewGroup);
            _removeAfter = removeAfter;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (_removeAfter && _viewGroupRef != null && _viewGroupRef.get() != null
                    && _viewRef != null && _viewRef.get() != null) {
                _viewRef.get().setVisibility(GONE);
                _viewGroupRef.get().removeView(_viewRef.get());
                Log.d("Removinng", "ID: " + _viewRef.get().getId());
            } else {
                Log.d("Removinng", "CantRemove view");
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    class MyAnimatorListener implements Animator.AnimatorListener {

        WeakReference<View> _viewRef;
        WeakReference<ViewGroup> _viewGroupRef;
        boolean _removeAfter;

        public MyAnimatorListener(ViewGroup viewGroup, View view, boolean removeAfter) {
            _viewRef = new WeakReference<>(view);
            _viewGroupRef = new WeakReference<>(viewGroup);
            _removeAfter = removeAfter;
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (_removeAfter && _viewGroupRef != null && _viewGroupRef.get() != null
                    && _viewRef != null && _viewRef.get() != null) {
                _viewGroupRef.get().removeView(_viewRef.get());
                Log.d("Removinng", "ID: " + _viewRef.get().getId());
            } else {
                Log.d("Removinng", "CantRemove view");
                Log.d("_viewGroupRef.get()", String.valueOf(_viewGroupRef.get() == null));
                Log.d("_viewRef.get()", String.valueOf(_viewRef.get() == null));
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}

