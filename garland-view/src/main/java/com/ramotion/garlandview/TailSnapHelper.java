package com.ramotion.garlandview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.view.View;

public class TailSnapHelper extends SnapHelper {

    private static final int MAX_SCROLL_ON_FLING_DURATION = 300; // ms
    private static final float MILLISECONDS_PER_INCH = 100f;

    private RecyclerView mRecyclerView;

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        super.attachToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager lm, @NonNull View targetView) {
        final int[] out = new int[] {0 ,0};

        if (lm.canScrollHorizontally()) {
            final int targetStart = lm.getDecoratedLeft(targetView);
            out[0] = targetStart - ((TailLayoutManager)lm).getItemStart();
        }

        return out;
    }

    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager lm) {
        return ((TailLayoutManager)lm).getCenterView();
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager lm, int velocityX, int velocityY) {
        final int itemCount = lm.getItemCount();
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION;
        }

        final int childCount = lm.getChildCount();
        if (childCount == 0) {
            return RecyclerView.NO_POSITION;
        }

        final int start = ((TailLayoutManager)lm).getItemStart();

        int absClosest = Integer.MAX_VALUE;
        View closestChild = null;
        for (int i = 0; i < childCount; i++) {
            final View child = lm.getChildAt(i);
            final int childStart = lm.getDecoratedLeft(child);
            int absDistance = Math.abs(childStart - start);

            if (absDistance < absClosest) {
                absClosest = absDistance;
                closestChild = child;
            }
        }

        if (closestChild == null) {
            return RecyclerView.NO_POSITION;
        }

        final int currentPosition = lm.getPosition(closestChild);
        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION;
        }

        final boolean leftDirection = velocityX > 0;

        if (leftDirection) {
            return Math.min(itemCount - 1, currentPosition + 1);
        } else {
            return Math.max(0, currentPosition - 1);
        }
    }

    @Nullable
    @Override
    protected LinearSmoothScroller createSnapScroller(RecyclerView.LayoutManager layoutManager) {
        return new LinearSmoothScroller(mRecyclerView.getContext()) {
            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                int[] snapDistances = calculateDistanceToFinalSnap(mRecyclerView.getLayoutManager(),
                        targetView);
                final int dx = snapDistances[0];
                final int dy = snapDistances[1];
                final int time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator);
                }
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }

            @Override
            protected int calculateTimeForScrolling(int dx) {
                return Math.min(MAX_SCROLL_ON_FLING_DURATION, super.calculateTimeForScrolling(dx));
            }
        };
    }
}