package com.skyrss.setting;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;

import com.skyrss.R;
import com.skyrss.global.NightModeUpdate;
import com.skyrss.ui.SlideSwitch;
import com.skyrss.ui.SlideSwitch.SlideListener;

public class MoreSetting extends Activity {
	private ImageButton ib_back;
	SlideSwitch ss;
	TextView tv_pushdesc;
	TextView tv_updatedesc;	
	SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_moresetting);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		ib_back = (ImageButton) findViewById(R.id.ib_back);
		ib_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		//推送功能设置
		ss = (SlideSwitch) findViewById(R.id.ss_moresetting_push);
		tv_pushdesc = (TextView) findViewById(R.id.tv_moresetting_pushdesc);
		Boolean pushstate = sp.getBoolean("JPUSH", true);
		if(pushstate){
			ss.setState(pushstate);	
			tv_pushdesc.setText("推送已经开启");//该处测试可能有bug,因为推送服务可能关闭
		}
		ss.setSlideListener(new SlideListener() {
			Editor et;

			@Override
			public void open() {
				tv_pushdesc.setText("推送已经开启");
				et = sp.edit();
				et.putBoolean("JPUSH", true);
				et.commit();
				JPushInterface.resumePush(getApplicationContext());
			}

			@Override
			public void close() {
				tv_pushdesc.setText("推送已经关闭");
				et = sp.edit();
				et.putBoolean("JPUSH", false);
				et.commit();
				JPushInterface.stopPush(getApplicationContext());
			}
		});
		
		//自动更新功能设置
		ss = (SlideSwitch) findViewById(R.id.ss_moresetting_update);
		tv_updatedesc = (TextView) findViewById(R.id.tv_moresetting_updatedesc);
		Boolean autoupdate = sp.getBoolean("autoupdate", true);
		if(autoupdate){
			ss.setState(autoupdate);	
			tv_updatedesc.setText("自动更新已经开启");//该处测试可能有bug,因为推送服务可能关闭
		}
		ss.setSlideListener(new SlideListener() {
			Editor et;

			@Override
			public void open() {
				tv_updatedesc.setText("自动更新已经开启");
				et = sp.edit();
				et.putBoolean("autoupdate", true);
				et.commit();
			}

			@Override
			public void close() {
				tv_updatedesc.setText("自动更新已经关闭");
				et = sp.edit();
				et.putBoolean("autoupdate", false);
				et.commit();
			}
		});
	}
	@Override
	protected void onResume() {
		NightModeUpdate.update(this);
		super.onResume();
	}
}
