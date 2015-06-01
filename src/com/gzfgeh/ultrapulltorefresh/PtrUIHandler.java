package com.gzfgeh.ultrapulltorefresh;

public interface PtrUIHandler {
	/*
	 * Call when Before Refresh
	 */
	public void onUIRefreshPrepare(PtrFrameLayout frameLayout);
	
	/*
	 * Call when will Refresh
	 */
	public void onUIRefreshBegin(PtrFrameLayout frameLayout);
	
	/*
	 * Call when Refreshing
	 */
	public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator);

	/*
	 * Call when Refresh end
	 */
	public void onUIRefreshComplete(PtrFrameLayout frame);
	
	/*
     * When the content view has reached top and refresh has been completed, view will be reset.
     */
    public void onUIReset(PtrFrameLayout frame);
}
