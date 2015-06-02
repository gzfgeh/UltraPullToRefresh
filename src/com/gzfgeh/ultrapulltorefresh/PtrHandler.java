package com.gzfgeh.ultrapulltorefresh;
import android.view.View;

public interface PtrHandler {
	public boolean checkCanDoRefresh(final PtrFrameLayout frame, final View content, final View header);
	public void onRefreshBegin(final PtrFrameLayout frame);
}
