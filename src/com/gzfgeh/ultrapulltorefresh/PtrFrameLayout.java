package com.gzfgeh.ultrapulltorefresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class PtrFrameLayout extends ViewGroup {
	private int headID, contentID;
	//private float 
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
			
			
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

	}

	

}
