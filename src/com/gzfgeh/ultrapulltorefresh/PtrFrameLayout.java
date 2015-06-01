package com.gzfgeh.ultrapulltorefresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class PtrFrameLayout extends ViewGroup {
	private int headID, contentID;
	private float ptrResistence;
	private float ratioHeaderToRefresh;
	private int durationToClose, durationToCloseHeader;
	private boolean isPullToRefresh, isPullToRefreshKeepHeader;
	private View headView, contentView;
	
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
		}
	}

	/*
	 * View中所有子控件映射成xml后，调用此函数
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
					
				}
					
			}
			
		}
		
		
		super.onFinishInflate();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

	}

	

}
