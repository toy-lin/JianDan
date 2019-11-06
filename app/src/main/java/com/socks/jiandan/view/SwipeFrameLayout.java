package com.socks.jiandan.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class SwipeFrameLayout extends FrameLayout {
    public static String TAG = SwipeFrameLayout.class.getSimpleName();
    private Scroller mScroller;
    private int heightThreshold;
    private int mTouchSlop;
    private int screenHeight;
    private int downX;
    private int downY;
    private int tempY;
    private View child;
    private SwipeListener lis;
    private boolean finishingView;
    private boolean shouldIntercept = true;

    public interface SwipeListener {
        void onSwipe(float offset);
    }

    public SwipeFrameLayout(Context context) {
        super(context);
    }

    public SwipeFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        heightThreshold = dm.heightPixels / 6;
        screenHeight = dm.heightPixels;
        mScroller = new Scroller(context);
    }

    public void setSwipeListener(SwipeListener lis) {
        this.lis = lis;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        child = getChildAt(0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (finishingView)
            return true;

        if (ev.getPointerCount() >= 2) // 如果用户发起多点触摸，则在用户抬起所有手指之前不再拦截事件
            shouldIntercept = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = tempY = (int) ev.getRawY();
                downX = (int) ev.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (shouldIntercept) {
                    int moveY = (int) ev.getRawY();
                    int moveX = (int) ev.getRawX();
                    int distanceY = Math.abs(moveY - downY);
                    int distanceX = Math.abs(moveX - downX);
                    if (distanceY > mTouchSlop && distanceX < distanceY / 2) {
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                shouldIntercept = true;
                break;
        }
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (finishingView)
            return true;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) ev.getRawY();
                int dy = moveY - tempY;

                child.scrollBy(0, -dy);
                if (lis != null)
                    lis.onSwipe(Math.abs(moveY - downY));
                tempY = moveY;
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(tempY - downY) > heightThreshold)
                    closeView(tempY > downY);
                else
                    scrollToOrigin();
                break;
        }
        return true;
    }

    private void closeView(boolean scrollUp) {
        int y = child.getScrollY();
        int toY = scrollUp ? -screenHeight : screenHeight;
        mScroller.startScroll(0, y, 0, toY - y, Math.abs(toY - y) / 2);
        finishingView = true;
        postInvalidate();
    }

    private void scrollToOrigin() {
        int y = child.getScrollY();
        mScroller.startScroll(0, y, 0, -y, Math.abs(y));
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int toY = mScroller.getCurrY();
            child.scrollTo(child.getScrollX(), toY);
            if (lis != null)
                lis.onSwipe(toY);
            if (finishingView) {
                int y = child.getScrollY();
                int height = child.getHeight();
                if (y <= -(screenHeight + height) / 2 || y >= (screenHeight + height) / 2) {
                    mScroller.abortAnimation();
                    ((Activity) getContext()).finish();
                }
            }
            if (!mScroller.isFinished())
                postInvalidate();
        }
        super.computeScroll();
    }
}
