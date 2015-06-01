package com.gzfgeh.ultrapulltorefresh;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

public class MainActivity extends Activity {
	final String[] mStringList = {"Alibaba", "TMALL 11-11"};
	private String mTitlePre;
	private PtrFrameLayout frameLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = (PtrFrameLayout) findViewById(R.id.store_house_ptr_frame);
        
        final StoreHouseHeader header = new StoreHouseHeader(this);
        header.setPadding(0, 30, 0, 30);
        header.initWithString(mStringList[0]);
        
        frameLayout.addPtrUIHandler(new PtrUIHandler() {
        	private int mLoadTime = 0;
        	
			@Override
			public void onUIReset(PtrFrameLayout arg0) {
				mLoadTime++;
                String string = mStringList[mLoadTime % mStringList.length];
                header.initWithString(string);
			}
			
			@Override
			public void onUIRefreshPrepare(PtrFrameLayout arg0) {
				String string = mStringList[mLoadTime % mStringList.length];
                //setHeaderTitle(mTitlePre + string);
			}
			
			@Override
			public void onUIRefreshComplete(PtrFrameLayout arg0) {
				
			}
			
			@Override
			public void onUIRefreshBegin(PtrFrameLayout arg0) {
				
			}

			@Override
			public void onUIPositionChange(PtrFrameLayout frame,
					boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
				// TODO Auto-generated method stub
				
			}
		});
        
        
        frameLayout.setDurationToCloseHeader(3000);
        frameLayout.setHeaderView(header);
        frameLayout.addPtrUIHandler(header);
        frameLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
            	frameLayout.autoRefresh(false);
            }
        }, 100);

        frameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return true;
            }

            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        frame.refreshComplete();
                    }
                }, 2000);
            }
        });
        
    }
    
   
    
}
