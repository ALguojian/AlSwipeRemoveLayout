package com.alguojian.alswiperemovelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义viewGroup
 * 2018/5/7
 *
 * @author alguojian
 */

public class ALSwipeRemoveLayout extends ViewGroup {

    private final List<View> childrens = new ArrayList<>(1);
    private State result;
    private int mLeftViewId;
    private int mRightViewId;
    private int mContentId;
    private View mLeftView;
    private View mRightView;
    private View mContentView;
    private MarginLayoutParams mMarginLayoutParams;
    private boolean isSwipe;
    private PointF mLastPointf;
    private PointF mFirstPointf;
    private float mSwipeDistance;
    private boolean mOpenLeft = true;
    private boolean mOpenRight = false;
    private int mScaledTouchSlop;
    private Scroller mScroller;
    private ALSwipeRemoveLayout mALSwipeRemoveLayout;
    private State mState;
    private float distancex;
    private float finallyDiatancex;

    public ALSwipeRemoveLayout(Context context) {
        this(context, null);

    }

    public ALSwipeRemoveLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public ALSwipeRemoveLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mScaledTouchSlop = viewConfiguration.getScaledTouchSlop();
        mScroller = new Scroller(context);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.alswiperemovelayout, defStyleAttr, 0);

        try {

            int indexCount = typedArray.getIndexCount();

            for (int i = 0; i < indexCount; i++) {

                int attr = typedArray.getIndex(i);

                if (attr == R.styleable.alswiperemovelayout_leftLayout) {
                    mLeftViewId = typedArray.getResourceId(R.styleable.alswiperemovelayout_leftLayout, -1);

                } else if (attr == R.styleable.alswiperemovelayout_rightLayout) {
                    mRightViewId = typedArray.getResourceId(R.styleable.alswiperemovelayout_rightLayout, -1);

                } else if (attr == R.styleable.alswiperemovelayout_contentLayout) {
                    mContentId = typedArray.getResourceId(R.styleable.alswiperemovelayout_contentLayout, -1);

                } else if (attr == R.styleable.alswiperemovelayout_openLeft) {
                    mOpenLeft = typedArray.getBoolean(R.styleable.alswiperemovelayout_openLeft, true);

                } else if (attr == R.styleable.alswiperemovelayout_openRight) {
                    mOpenRight = typedArray.getBoolean(R.styleable.alswiperemovelayout_openRight, true);

                } else if (attr == R.styleable.alswiperemovelayout_swipeDistance) {
                    mSwipeDistance = typedArray.getFloat(R.styleable.alswiperemovelayout_swipeDistance, 0.5f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("----" + e.getMessage());
        } finally {
            typedArray.recycle();
        }
    }

    public State getStateCache() {
        return mState;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                isSwipe = false;
                if (mLastPointf == null) {
                    mLastPointf = new PointF();
                }
                mLastPointf.set(ev.getRawX(), ev.getRawY());
                if (mFirstPointf == null) {
                    mFirstPointf = new PointF();
                }
                mFirstPointf.set(ev.getRawX(), ev.getRawY());
                if (mALSwipeRemoveLayout != null) {
                    if (mALSwipeRemoveLayout != this) {
                        mALSwipeRemoveLayout.handlerSwipeMenu(State.CLOSE);
                    }
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float distanceX = mLastPointf.x - ev.getRawX();
                float distanceY = mLastPointf.y - ev.getRawY();
                if (Math.abs(distanceY) > mScaledTouchSlop && Math.abs(distanceY) > Math.abs(distanceX)) {
                    break;
                }

                scrollBy((int) (distanceX), 0);//滑动使用scrollBy
                //越界修正
                if (getScrollX() < 0) {
                    if (!mOpenRight || mLeftView == null) {
                        scrollTo(0, 0);
                    } else {//左滑
                        if (getScrollX() < mLeftView.getLeft()) {

                            scrollTo(mLeftView.getLeft(), 0);
                        }

                    }
                } else if (getScrollX() > 0) {
                    if (!mOpenLeft || mRightView == null) {
                        scrollTo(0, 0);
                    } else {
                        if (getScrollX() > mRightView.getRight() - mContentView.getRight() - mMarginLayoutParams.rightMargin) {
                            scrollTo(mRightView.getRight() - mContentView.getRight() - mMarginLayoutParams.rightMargin, 0);
                        }
                    }
                }
                //当处于水平滑动时，禁止父类拦截
                if (Math.abs(distanceX) > mScaledTouchSlop
//                        || Math.abs(getScrollX()) > mScaledTouchSlop
                        ) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                mLastPointf.set(ev.getRawX(), ev.getRawY());


                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                //     System.out.println(">>>>dispatchTouchEvent() ACTION_CANCEL OR ACTION_UP");

                finallyDiatancex = mFirstPointf.x - ev.getRawX();
                if (Math.abs(finallyDiatancex) > mScaledTouchSlop) {
                    //  System.out.println(">>>>P");

                    isSwipe = true;
                }
                result = isShouldOpen(getScrollX());
                handlerSwipeMenu(result);


                break;
            }
            default: {
                break;
            }
        }

        return super.dispatchTouchEvent(ev);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                //滑动时拦截点击时间
                if (Math.abs(finallyDiatancex) > mScaledTouchSlop) {
                    // 当手指拖动值大于mScaledTouchSlop值时，认为应该进行滚动，拦截子控件的事件
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                //滑动后不触发contentView的点击事件
                if (isSwipe) {
                    isSwipe = false;
                    finallyDiatancex = 0;
                    return true;
                }

            default:
                break;

        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this == mALSwipeRemoveLayout) {
            mALSwipeRemoveLayout.handlerSwipeMenu(mState);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (this == mALSwipeRemoveLayout) {
            mALSwipeRemoveLayout.handlerSwipeMenu(State.CLOSE);
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int count = getChildCount();

        int left = getPaddingLeft();
        int right = getPaddingLeft();
        int top = getPaddingTop();
        int button = getPaddingBottom();

        for (int i = 0; i < count; i++) {
            View childAt = getChildAt(i);

            if (mLeftView == null && childAt.getId() == mLeftViewId) {

                mLeftView = childAt;
                mLeftView.setClickable(true);
            } else if (mRightView == null && childAt.getId() == mRightViewId) {
                // Log.i(TAG, "找到右边按钮view");
                mRightView = childAt;
                mRightView.setClickable(true);
            } else if (mContentView == null && childAt.getId() == mContentId) {
                // Log.i(TAG, "找到内容View");
                mContentView = childAt;
                mContentView.setClickable(true);
            }
        }

        //布局contentView
        int cRight = 0;
        if (mContentView != null) {
            mMarginLayoutParams = (MarginLayoutParams) mContentView.getLayoutParams();
            int cTop = top + mMarginLayoutParams.topMargin;
            int cLeft = left + mMarginLayoutParams.leftMargin;
            cRight = left + mMarginLayoutParams.leftMargin + mContentView.getMeasuredWidth();
            int cBottom = cTop + mContentView.getMeasuredHeight();
            mContentView.layout(cLeft, cTop, cRight, cBottom);
        }
        if (mLeftView != null) {
            MarginLayoutParams leftViewLp = (MarginLayoutParams) mLeftView.getLayoutParams();
            int lTop = top + leftViewLp.topMargin;
            int lLeft = 0 - mLeftView.getMeasuredWidth() + leftViewLp.leftMargin + leftViewLp.rightMargin;
            int lRight = 0 - leftViewLp.rightMargin;
            int lBottom = lTop + mLeftView.getMeasuredHeight();
            mLeftView.layout(lLeft, lTop, lRight, lBottom);
        }
        if (mRightView != null) {
            MarginLayoutParams rightViewLp = (MarginLayoutParams) mRightView.getLayoutParams();
            int lTop = top + rightViewLp.topMargin;
            int lLeft = mContentView.getRight() + mMarginLayoutParams.rightMargin + rightViewLp.leftMargin;
            int lRight = lLeft + mRightView.getMeasuredWidth();
            int lBottom = lTop + mRightView.getMeasuredHeight();
            mRightView.layout(lLeft, lTop, lRight, lBottom);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    /**
     * 自动设置状态
     *
     * @param result
     */

    private void handlerSwipeMenu(State result) {
        if (result == State.OPEN_LEFT) {
            mScroller.startScroll(getScrollX(), 0, mLeftView.getLeft() - getScrollX(), 0);
            mALSwipeRemoveLayout = this;
            mState = result;
        } else if (result == State.OPEN_RIGHT) {
            mALSwipeRemoveLayout = this;
            mScroller.startScroll(getScrollX(), 0, mRightView.getRight() - mContentView.getRight() - mMarginLayoutParams.rightMargin - getScrollX(), 0);
            mState = result;
        } else {
            mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0);
            mALSwipeRemoveLayout = null;
            mState = null;

        }
        invalidate();
    }

    /**
     * 根据当前的scrollX的值判断松开手后应处于何种状态
     *
     * @param
     * @param scrollX
     * @return
     */
    private State isShouldOpen(int scrollX) {
        if (!(mScaledTouchSlop < Math.abs(finallyDiatancex))) {
            return mState;
        }
        if (finallyDiatancex < 0) {
            //➡滑动
            //1、展开左边按钮
            //获得leftView的测量长度
            if (getScrollX() < 0 && mLeftView != null) {
                if (Math.abs(mLeftView.getWidth() * mSwipeDistance) < Math.abs(getScrollX())) {
                    return State.OPEN_LEFT;
                }
            }
            //2、关闭右边按钮

            if (getScrollX() > 0 && mRightView != null) {
                return State.CLOSE;
            }
        } else if (finallyDiatancex > 0) {
            //滑动
            //3、开启右边菜单按钮
            if (getScrollX() > 0 && mRightView != null) {

                if (Math.abs(mRightView.getWidth() * mSwipeDistance) < Math.abs(getScrollX())) {
                    return State.OPEN_RIGHT;
                }

            }
            //关闭左边
            if (getScrollX() < 0 && mLeftView != null) {
                return State.CLOSE;
            }
        }

        return State.CLOSE;

    }

    @Override
    public void computeScroll() {
        //判断Scroller是否执行完毕：
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //通知View重绘-invalidate()->onDraw()->computeScroll()
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //获取children的个数
        setClickable(true);
        int count = getChildCount();

        final boolean measureMatchParentChildren = MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY
                || MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;

        childrens.clear();

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        //遍历childrenViews

        for (int i = 0; i < count; i++) {
            View childAt = getChildAt(i);

            if (childAt.getVisibility() != GONE) {

                measureChildWithMargins(childAt, widthMeasureSpec, 0, heightMeasureSpec, 0);
                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) childAt.getLayoutParams();
                maxWidth = Math.max(maxWidth, childAt.getMeasuredWidth() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin);

                maxHeight = Math.max(maxHeight, childAt.getMeasuredHeight() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin);

                childState = combineMeasuredStates(childState, childAt.getMeasuredState());

                if (measureMatchParentChildren) {
                    if (marginLayoutParams.width == LayoutParams.MATCH_PARENT ||
                            marginLayoutParams.height == LayoutParams.MATCH_PARENT) {
                        childrens.add(childAt);
                    }
                }
            }
        }

        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState), resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));

        count = childrens.size();

        if (count > 1) {

            for (int i = 0; i < count; i++) {
                final View child = childrens.get(i);
                final MarginLayoutParams marginLayoutParams = (MarginLayoutParams) child.getLayoutParams();

                final int childWidthMeasureSpec;

                if (marginLayoutParams.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, getMeasuredWidth() - marginLayoutParams.leftMargin - marginLayoutParams.rightMargin);

                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                } else {

                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, marginLayoutParams.leftMargin + marginLayoutParams.rightMargin, marginLayoutParams.width);
                }

                final int childHeightMeasureSpec;
                if (marginLayoutParams.height == LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, getMeasuredHeight() - marginLayoutParams.topMargin - marginLayoutParams.bottomMargin);

                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, marginLayoutParams.topMargin + marginLayoutParams.bottomMargin, marginLayoutParams.height);
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    public void resetStatus() {
        if (mALSwipeRemoveLayout != null) {
            if (mState != null && mState != State.CLOSE && mScroller != null) {
                mScroller.startScroll(mALSwipeRemoveLayout.getScrollX(), 0, -mALSwipeRemoveLayout.getScrollX(), 0);
                mALSwipeRemoveLayout.invalidate();
                mALSwipeRemoveLayout = null;
                mState = null;
            }
        }
    }

    public float getSwipeDistance() {
        return mSwipeDistance;
    }

    public void setSwipeDistance(float mSwipeDistance) {
        this.mSwipeDistance = mSwipeDistance;
    }

    public boolean isOpenLeft() {
        return mOpenLeft;
    }

    public void setOpenLeft(boolean mOpenLeft) {
        this.mOpenLeft = mOpenLeft;
    }

    public boolean isOpenRight() {
        return mOpenRight;
    }

    public void setOpenRight(boolean mOpenRight) {
        this.mOpenRight = mOpenRight;
    }

    public ALSwipeRemoveLayout getViewCache() {
        return mALSwipeRemoveLayout;
    }

    private boolean isLeftToRight() {
        if (distancex < 0) {
            //➡滑动
            return true;
        } else {
            return false;
        }
    }

}
