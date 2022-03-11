package ua.improveit.topnotificator;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
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

public class NotificationView extends CoordinatorLayout {

    private final long TIME_TO_SHOW = 12_000L;
    Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.top_notification_slide_up);
    Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.top_notification_slide_down);

    private CountDownTimer timer;
    private View currentView;

    private SwipeDismissBehavior<CardView> swipe= new SwipeDismissBehavior();




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
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(timer !=null){
                    timer.cancel();
                }
                if (currentView != null) {
                    removeView(currentView);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        swipe.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
        swipe.setStartAlphaSwipeDistance(0.1f);
        swipe.setEndAlphaSwipeDistance(0.6f);
        swipe.setListener(new SwipeDismissBehavior.OnDismissListener() {
            @Override public void onDismiss(View view) {
                if(timer !=null){
                    timer.cancel();
                }
                if (currentView != null) {
                    removeView(currentView);
                }
            }

            @Override public void onDragStateChanged(int state) {}
        });
    }

    public void showMessage(String text) {
        if (slideUp.hasStarted() && !slideUp.hasEnded()) {
            slideUp.cancel();
        }
        if (currentView != null) {
            currentView.clearAnimation();
            removeView(currentView);
        }

        View newView = constructMessageView();

        ((TextView) newView.findViewById(R.id.tv_message)).setText(text);

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        LayoutParams coordinatorParams =
            (LayoutParams) newView.getLayoutParams();

        coordinatorParams.setBehavior(swipe);

        addView(newView);
        currentView = newView;
        newView.startAnimation(slideDown);
        timer = createTimer(TIME_TO_SHOW, newView).start();
    }

    public CardView constructMessageView() {
        CardView view = (CardView) View.inflate(getContext(), R.layout.notification, null);
        view.setId(ViewCompat.generateViewId());
        LayoutParams params = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.topMargin = dpToPx(36);
        params.leftMargin = dpToPx(16);
        params.rightMargin = dpToPx(16);
        params.bottomMargin = dpToPx(36);
        view.setLayoutParams(params);
        view.findViewById(R.id.tv_button_ok).setOnClickListener(v -> {

            try {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }

                view.startAnimation(slideUp);

            } catch (Exception ex) {
                if (BuildConfig.DEBUG) {
                    Log.e(this.getClass().getSimpleName(), "Error", ex);
                }
            }
        });
        return view;
    }

    private CountDownTimer createTimer(long period, View view){

        return new CountDownTimer(period, 500) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (view != null) {
                    try {
                        view.startAnimation(slideUp);
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG) {
                            Log.e(this.getClass().getSimpleName(), "Error", e);
                        }
                    }
                }
            }
        };
    }



    @Px
    public int dpToPx(@Dimension(unit = Dimension.DP) int dp) {
        final Resources resources = getContext().getResources();
        final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }
}
