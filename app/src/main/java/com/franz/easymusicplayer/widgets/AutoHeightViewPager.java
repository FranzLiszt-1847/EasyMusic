package com.franz.easymusicplayer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class AutoHeightViewPager extends ViewPager {

    private int mHeight = 0;

    /** 已经获取到的高度下标 ： 当前的高度 */
    private int mCurPosition = 0;

    /** 当前显示下标 */
    private int mPosition = 0;

    /** 按下标存储View历史高度 */
    private HashMap<Integer, Integer> mChildrenViews = new LinkedHashMap<Integer, Integer>();
    /** 记录页面是否存储了高度 */
    private HashMap<Integer, Boolean> indexList = new LinkedHashMap<Integer, Boolean>();

    /** 做自适应高度，必须先进行初始化标记 */
    public void initIndexList(int size) {
        mHeight = 0;
        mCurPosition = 0;
        mPosition = 0;
        for (int i = 0; i < size; i++) {
            /** 初始化高度存储状态 */
            indexList.put(i, false);
        }
    }

    public AutoHeightViewPager( Context context) {
        super(context);
    }

    public AutoHeightViewPager( Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;
        if (indexList.size() > 0) {
            if (indexList.get(mPosition)) {
                height = mChildrenViews.get(mPosition);
                Log.d("ViewHeight","if-高度="+height+"-"+mPosition);
            } else {
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                    int h = child.getMeasuredHeight();
                    Log.d("ViewHeight",i+"-高度="+height);
                    if (h > height) {
                        height = h;
                    }
                }
                mHeight = height;
            }
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 在viewpager 切换的时候进行更新高度
     */
    public void updateHeight(int current) {
        this.mPosition = current;
        if (indexList.size() > 0) {
            saveIndexData();
            if (indexList.get(current)) {
                int height = 0;
                if (mChildrenViews.get(current) != null) {
                    height = mChildrenViews.get(current);
                    Log.d("ViewHeight","update ture 高度="+height);
                }
                Log.d("ViewHeight","update false 高度");
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) getLayoutParams();
                if (layoutParams == null) {
                    layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, height);
                } else {
                    layoutParams.height = height;
                }
                setLayoutParams(layoutParams);
            }
            this.mCurPosition = current;
        }
    }

    /**
     * 保存已经测绘好的高度
     */
    private void saveIndexData() {
        if (!indexList.get(mCurPosition)) {
            /** 没保存高度时，保存 */
            indexList.put(mCurPosition, true);
            mChildrenViews.put(mCurPosition, mHeight);
        }
    }
}
