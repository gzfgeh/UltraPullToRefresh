package com.gzfgeh.ultrapulltorefresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

public class PtrFrameLayout extends ViewGroup {
	// status enum
    public final static byte PTR_STATUS_INIT = 1;
    public final static byte PTR_STATUS_PREPARE = 2;
    public final static byte PTR_STATUS_LOADING = 3;
    public final static byte PTR_STATUS_COMPLETE = 4;
	// auto refresh status
    private static byte FLAG_AUTO_REFRESH_AT_ONCE = 0x01;
    private static byte FLAG_AUTO_REFRESH_BUT_LATER = 0x01 << 1;
    private static byte FLAG_ENABLE_NEXT_PTR_AT_ONCE = 0x01 << 2;
    private static byte FLAG_PIN_CONTENT = 0x01 << 3;
    private int mFlag = 0x00;
    private byte mStatus = PTR_STATUS_INIT;
    private static byte MASK_AUTO_REFRESH = 0x03;
	private int headID, contentID;
	private float ptrResistence;
	private float ratioHeaderToRefresh;
	private int durationToClose, durationToCloseHeader;
	private boolean isPullToRefresh, isPullToRefreshKeepHeader;
	private View headView, contentView;
	private int headerHeight;
	private int mPagingTouchSlop;
	private PtrIndicator ptrIndicator;
	private boolean mHasSendCancelEvent = false;
	private MotionEvent mDownEvent;
    private MotionEvent mLastMoveEvent;
    // disable when detect moving horizontally
    private boolean mPreventForHorizontal = false;
    private boolean mDisableWhenHorizontalMove = false;
    private PtrHandler ptrHandler;
    private PtrUIHandlerHolder mPtrUIHandlerHolder = PtrUIHandlerHolder.create();
    private boolean mPullToRefresh = false;
    private long mLoadingStartTime = 0;
    
	public PtrFrameLayout(Context context) {
		this(context, null);
	}
	
	public PtrFrameLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PtrFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PtrFrameLayout, 0, 0);
		if (array != null){
			headID = array.getResourceId(R.styleable.PtrFrameLayout_ptr_header, headID);
			contentID = array.getResourceId(R.styleable.PtrFrameLayout_ptr_content, contentID);
			ptrResistence = array.getFloat(R.styleable.PtrFrameLayout_ptr_resistance, ptrResistence);
			durationToClose = array.getInt(R.styleable.PtrFrameLayout_ptr_duration_to_close, durationToClose);
			durationToCloseHeader = array.getInt(R.styleable.PtrFrameLayout_ptr_duration_to_close_header, durationToCloseHeader);
			ratioHeaderToRefresh = array.getFloat(R.styleable.PtrFrameLayout_ptr_ratio_of_header_height_to_refresh, ratioHeaderToRefresh);
			isPullToRefresh = array.getBoolean(R.styleable.PtrFrameLayout_ptr_pull_to_fresh, isPullToRefresh);
			isPullToRefreshKeepHeader = array.getBoolean(R.styleable.PtrFrameLayout_ptr_keep_header_when_refresh, isPullToRefreshKeepHeader);
			array.recycle();
			
			ptrIndicator = new PtrIndicator();
		}
	}

	/*
	 * call when the child control of View all inflated to xml
	 */
	@Override
	protected void onFinishInflate() {
		int childrenCnt = getChildCount();
		if (childrenCnt > 2){
			throw new IllegalAccessError("PtrFrameLayout only can host 2 elements");
		}else if (childrenCnt == 2){
			if (headID != 0 && headView == null)
				headView = findViewById(headID);
			if (contentID != 0 && contentView == null)
				contentView = findViewById(contentID);
			
			if (headView == null || contentView == null){
				View child1 = getChildAt(0);
				View child2 = getChildAt(1);
				
				if (child1 instanceof PtrUIHandler){
					headView = child1;
					contentView = child2;
				}else if (child2 instanceof PtrUIHandler){
					headView = child2;
					contentView = child1;
				}else {
					//both two are not specific
					if (headView == null && contentView == null){
						headView = child1;
						contentView = child2;
					}else if (headView == null){
						headView = contentView == child1 ? child2 : child1;
					}else {
						contentView = headView == child1 ? child2 : child1;
					}
				}
			}
		}else if (childrenCnt == 1){
			contentView = getChildAt(0);
		}else{
			TextView errorView = new TextView(getContext());
            errorView.setClickable(true);
            errorView.setTextColor(0xffff6600);
            errorView.setGravity(Gravity.CENTER);
            errorView.setTextSize(20);
            errorView.setText("The content view in PtrFrameLayout is empty. Do you forget to specify its id in xml layout file?");
            contentView = errorView;
            addView(contentView);
		}
		
		//remove from parent view and add to top of the parent view
		if (headView != null)
			headView.bringToFront();
		
		super.onFinishInflate();
	}

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if (headView != null){
			measureChildWithMargins(headView, widthMeasureSpec, 0, heightMeasureSpec, 0);
			MarginLayoutParams lParams = (MarginLayoutParams) headView.getLayoutParams();
			headerHeight = headView.getMeasuredHeight() + lParams.topMargin + lParams.bottomMargin;
			ptrIndicator.setHeaderHeight(headerHeight);
		}
		
		if (contentView != null){
			measureChildWithMargins(contentView, widthMeasureSpec, 0, heightMeasureSpec, 0);
			
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		layoutChildren();
	}

	private void layoutChildren() {
		int offsetX = ptrIndicator.getCurrentPosY();
		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		
		if (headView != null){
			MarginLayoutParams layoutParams = (MarginLayoutParams) headView.getLayoutParams();
			int left = paddingLeft + layoutParams.leftMargin;
			int right = left + headView.getMeasuredWidth();
			int top = paddingTop + layoutParams.topMargin + offsetX - headerHeight;
			int bottom = top + headView.getMeasuredHeight();
			headView.layout(left, top, right, bottom);
		}
		
		if (contentView != null){
			MarginLayoutParams layoutParams = (MarginLayoutParams) contentView.getLayoutParams();
			int left = paddingLeft + layoutParams.leftMargin;
			int right = left + contentView.getMeasuredWidth();
			int top = paddingTop + layoutParams.topMargin + offsetX;
			int bottom = top + contentView.getMeasuredHeight();
			contentView.layout(left, top, right, bottom);
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!isEnabled() || contentView == null || headView == null)
			return super.dispatchTouchEvent(ev);
		
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mHasSendCancelEvent = false;
			mDownEvent = ev;
			ptrIndicator.onPressDown(ev.getX(), ev.getY());
			
			mPreventForHorizontal = false;
            if (ptrIndicator.hasLeftStartPosition()) {
                // do nothing, intercept child event
            } else {
            	super.dispatchTouchEvent(ev);
            }
            return true;

		case MotionEvent.ACTION_MOVE:
			mLastMoveEvent = ev;
			ptrIndicator.onMove(ev.getX(), ev.getY());
			float offsetX = ptrIndicator.getOffsetX();
			float offsetY = ptrIndicator.getOffsetY();
			
			if (mDisableWhenHorizontalMove && !mPreventForHorizontal && (Math.abs(offsetX) > mPagingTouchSlop || Math.abs(offsetX) > 3 * Math.abs(offsetY))) {
                if (ptrIndicator.isInStartPosition()) {
                    mPreventForHorizontal = true;
                }
            }
            if (mPreventForHorizontal) {
                return super.dispatchTouchEvent(ev);
            }
            
            boolean moveDown = offsetY > 0;
            boolean moveUp = !moveDown;
            boolean canMoveUp = ptrIndicator.hasLeftStartPosition();
            
            if (moveDown && ptrHandler != null && !ptrHandler.checkCanDoRefresh(this, contentView, headView))
            	return super.dispatchTouchEvent(ev);
            
            if ((moveUp && canMoveUp) || moveDown){
            	movePos(offsetY);
            	return true;
            }
			break;
			
		default:
			break;
		}
		
		
		return super.dispatchTouchEvent(ev);
	}

	public boolean isPinContent() {
        return (mFlag & FLAG_PIN_CONTENT) > 0;
    }
	
	/*
	 * if deltaY>0, move the content down
	 */
	private void movePos(float deltaY){
		if ((deltaY < 0) && ptrIndicator.isInStartPosition()){
			return;
		}
		
		int to = ptrIndicator.getCurrentPosY() + (int)deltaY;
		
		//over top
		if (ptrIndicator.willOverTop(to)){
			to = ptrIndicator.POS_START;
		}
		
		ptrIndicator.setCurrentPos(to);
		int change = to - ptrIndicator.getLastPosY();
		updatePos(change);
	}
	
	private void updatePos(int change) {
        if (change == 0) {
            return;
        }

        boolean isUnderTouch = ptrIndicator.isUnderTouch();

        // once moved, cancel event will be sent to child
        if (isUnderTouch && !mHasSendCancelEvent && ptrIndicator.hasMovedAfterPressedDown()) {
            mHasSendCancelEvent = true;
            sendCancelEvent();
        }

        // leave initiated position or just refresh complete
        if ((ptrIndicator.hasJustLeftStartPosition() && mStatus == PTR_STATUS_INIT) ||
                (ptrIndicator.goDownCrossFinishPosition() && mStatus == PTR_STATUS_COMPLETE && isEnabledNextPtrAtOnce())) {

            mStatus = PTR_STATUS_PREPARE;
            mPtrUIHandlerHolder.onUIRefreshPrepare(this);
        }

        // back to initiated position
        if (ptrIndicator.hasJustBackToStartPosition()) {
            tryToNotifyReset();

            // recover event to children
            if (isUnderTouch) {
                sendDownEvent();
            }
        }

        // Pull to Refresh
        if (mStatus == PTR_STATUS_PREPARE) {
            // reach fresh height while moving from top to bottom
            if (isUnderTouch && !isAutoRefresh() && mPullToRefresh
                    && ptrIndicator.crossRefreshLineFromTopToBottom()) {
                tryToPerformRefresh();
            }
            // reach header height while auto refresh
            if (performAutoRefreshButLater() && ptrIndicator.hasJustReachedHeaderHeightFromTopToBottom()) {
                tryToPerformRefresh();
            }
        }

        headView.offsetTopAndBottom(change);
        if (!isPinContent()) {
            contentView.offsetTopAndBottom(change);
        }
        invalidate();

        if (mPtrUIHandlerHolder.hasHandler()) {
            mPtrUIHandlerHolder.onUIPositionChange(this, isUnderTouch, mStatus, ptrIndicator);
        }
        //onPositionChange(isUnderTouch, mStatus, ptrIndicator);
    }
	
	private void sendCancelEvent() {
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }
	
	public boolean isEnabledNextPtrAtOnce() {
        return (mFlag & FLAG_ENABLE_NEXT_PTR_AT_ONCE) > 0;
    }
	
	/**
     * If at the top and not in loading, reset
     */
    private boolean tryToNotifyReset() {
        if ((mStatus == PTR_STATUS_COMPLETE || mStatus == PTR_STATUS_PREPARE) && ptrIndicator.isInStartPosition()) {
            if (mPtrUIHandlerHolder.hasHandler()) {
                mPtrUIHandlerHolder.onUIReset(this);
            }
            mStatus = PTR_STATUS_INIT;
            clearFlag();
            return true;
        }
        return false;
    }
    
    private void clearFlag() {
        // remove auto fresh flag
        mFlag = mFlag & ~MASK_AUTO_REFRESH;
    }
    
    private void sendDownEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }
    
    public boolean isAutoRefresh() {
        return (mFlag & MASK_AUTO_REFRESH) > 0;
    }
    
    private boolean tryToPerformRefresh() {
        if (mStatus != PTR_STATUS_PREPARE) {
            return false;
        }

        //
        if ((ptrIndicator.isOverOffsetToKeepHeaderWhileLoading() && isAutoRefresh()) || ptrIndicator.isOverOffsetToRefresh()) {
            mStatus = PTR_STATUS_LOADING;
            performRefresh();
        }
        return false;
    }
    
    private void performRefresh() {
        mLoadingStartTime = System.currentTimeMillis();
        if (mPtrUIHandlerHolder.hasHandler()) {
            mPtrUIHandlerHolder.onUIRefreshBegin(this);
        }
        if (ptrHandler != null) {
        	ptrHandler.onRefreshBegin(this);
        }
    }
    
    private boolean performAutoRefreshButLater() {
        return (mFlag & MASK_AUTO_REFRESH) == FLAG_AUTO_REFRESH_BUT_LATER;
    }
    
}
