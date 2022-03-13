package ua.improveit.topnotificator;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
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

    private final long MIN_TIME_TO_SHOW = 2_000L;
    private final long MAX_TIME_TO_SHOW = 10_000L;

    private final long DEBUG_TIME_TO_SHOW = 5_000L;
    private final long RELEASE_TIME_TO_SHOW = 4_000L;
    private long timeAnimationIn = MIN_TIME_TO_SHOW;
    private long timeAnimationOut = MIN_TIME_TO_SHOW;
    private long timeAnimationOutDirtyView = MIN_TIME_TO_SHOW;
    @Dimension(unit = Dimension.DP)
    private int marginForAppearingView = 24;
    @Dimension(unit = Dimension.DP)
    private int marginForDisappearingView = 16;
    private long timeShowMessage = DEBUG_TIME_TO_SHOW;
    private WeakReference<NotificationView> INSTANCE;
    private MyTimer timer;
    private View currentNotificationView;

    public NotificationView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public NotificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public NotificationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        View.inflate(getContext(), R.layout.notification_view, this);

        INSTANCE = new WeakReference<>(this);

        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.NotificationView, 0, 0);
            try {
                timeShowMessage = ta.getInt(R.styleable.NotificationView_timeToShowMessage, -1);
                timeAnimationIn = ta.getInt(R.styleable.NotificationView_timeAnimationIn, -1);
                timeAnimationOut = ta.getInt(R.styleable.NotificationView_timeAnimationOut, -1);
                timeAnimationOutDirtyView = ta.getInt(R.styleable.NotificationView_timeAnimationOutDirtyView, -1);
                marginForAppearingView = ta.getLayoutDimension(R.styleable.NotificationView_marginForAppearingView, 24);
                marginForDisappearingView = ta.getLayoutDimension(R.styleable.NotificationView_marginForDisappearingView, 16);
            } finally {
                ta.recycle();
                validateData();
            }
        }
    }

    public void showMessage(String text) {

        View newView = constructMessageView();
        newView.setAlpha(0f);
        ((TextView) newView.findViewById(R.id.tv_message)).setText(text);
        newView.setId(ViewCompat.generateViewId());
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (currentNotificationView != null) {
            createSlideDownImmediatelyAnimationSet(currentNotificationView, new MyAnimatorListener(INSTANCE.get(), currentNotificationView, true)).start();
        }

        LayoutParams lp =
                (LayoutParams) newView.getLayoutParams();

        lp.setBehavior(createSwipeDismissBehavior(newView));

        addView(newView);
        createSlideDownAnimationSet(newView).start();
        currentNotificationView = newView;
        timer = new MyTimer(currentNotificationView, DEBUG_TIME_TO_SHOW);
        timer.start();
    }

    public CardView constructMessageView() {
        CardView view = (CardView) View.inflate(getContext(), R.layout.notification_message, null);
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

                createSlideUpAnimationSet(view, new MyAnimatorListener(INSTANCE.get(), view, true)).start();

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

    private AnimatorSet createSlideDownAnimationSet(View view) {
        int t = marginForAppearingView;
        ObjectAnimator animX = ObjectAnimator.ofFloat(view, "alpha", 1f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY", t);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
        animSetXY.setDuration(timeAnimationIn);
        return animSetXY;
    }

    private AnimatorSet createSlideUpAnimationSet(View view, Animator.AnimatorListener listener) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(view, "alpha", 0f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY", -350);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
        animSetXY.setDuration(timeAnimationOut);
        animSetXY.addListener(listener);
        return animSetXY;
    }

    private AnimatorSet createSlideDownImmediatelyAnimationSet(View view, Animator.AnimatorListener listener) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(view, "alpha", 0f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY", marginForDisappearingView + marginForAppearingView + view.getHeight());
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
        animSetXY.setDuration(timeAnimationOutDirtyView);
        animSetXY.addListener(listener);
        return animSetXY;
    }

    private SwipeDismissBehavior createSwipeDismissBehavior(View _view) {
        SwipeDismissBehavior swipe = new SwipeDismissBehavior();
        swipe.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
        swipe.setStartAlphaSwipeDistance(0.1f);
        swipe.setEndAlphaSwipeDistance(0.6f);
        swipe.setListener(new SwipeDismissBehavior.OnDismissListener() {
            @Override
            public void onDismiss(View view) {
                if (timer != null) {
                    timer.cancel();
                }
                if (_view != null) {
                    removeView(_view);
                }
            }

            @Override
            public void onDragStateChanged(int state) {
            }
        });
        swipe.setListener(new MySwipeToDismiss(INSTANCE.get(), _view));
        return swipe;
    }

    private void validateData() {

        if (timeShowMessage < MIN_TIME_TO_SHOW || timeShowMessage > MAX_TIME_TO_SHOW) {
            timeShowMessage = BuildConfig.DEBUG ? DEBUG_TIME_TO_SHOW : RELEASE_TIME_TO_SHOW;
        }

        if (timeAnimationIn < 350 || timeAnimationIn > 3_000) {
            timeAnimationIn = 550;
        }

        if (timeAnimationOut < 350 || timeAnimationOut > 3_000) {
            timeAnimationOut = 350;
        }

        if (timeAnimationOutDirtyView < 350 || timeAnimationOutDirtyView > 1_000) {
            timeAnimationOutDirtyView = 450;
        }

        if (marginForAppearingView < 24 || marginForAppearingView > 128) {
            marginForAppearingView = 64;
        }

        if (marginForDisappearingView < 12 || marginForDisappearingView > 64) {
            marginForDisappearingView = 32;
        }
    }

    class MySwipeToDismiss implements SwipeDismissBehavior.OnDismissListener {

        WeakReference<View> weakViewReference;
        WeakReference<ViewGroup> weakViewGroupReference;

        MySwipeToDismiss(ViewGroup _viewGroup, View _view) {
            weakViewReference = new WeakReference<>(_view);
            weakViewGroupReference = new WeakReference<>(_viewGroup);
        }

        @Override
        public void onDismiss(View view) {
            if (timer != null) {
                timer.cancel();
            }
            if (weakViewGroupReference != null && weakViewGroupReference.get() != null
                    && weakViewReference != null && weakViewReference.get() != null) {
                weakViewGroupReference.get().removeView(weakViewReference.get());
            }
        }

        @Override
        public void onDragStateChanged(int state) {
        }
    }

    class MyTimer extends CountDownTimer {

        WeakReference<View> _view;

        public MyTimer(View view, long period) {
            super(period, 250);
            _view = new WeakReference<>(view);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (BuildConfig.DEBUG) {
                Log.e(this.getClass().getSimpleName(), "TIK " + millisUntilFinished);
            }
        }

        @Override
        public void onFinish() {
            if (_view != null && _view.get() != null) {
                try {
                    createSlideUpAnimationSet(_view.get(), new MyAnimatorListener(INSTANCE.get(), _view.get(), true)).start();
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
            } else {
                Log.d("Removing", "CantRemove view");
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
            } else {
                Log.d("Removing", "CantRemove view");
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

