package com.skyrss;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.skyrss.global.NightModeUpdate;
import com.skyrss.utils.SetCssUtils;


public class DetailsActivity extends Activity{
	
	private TextView tv_title;
	private TextView tv_data;
	private WebView wv_showdetails;
	private WebSettings size_setting;
	private int checkItem;
	private PopupWindow popmenu;
	private Button bt1;
	private Button bt2;
	private View popview;
	
	public static final int SIZE_SMALLEST = 0;
	public static final int SIZE_SMALLER = 1;
	public static final int SIZE_NOMAL = 2;
	public static final int SIZE_LARGER = 3;
	public static final int SIZE_LARGEST = 4;
	
	public static final int VISIBLE = 5;
	public static final int INVISIBLE = 6;
	
	private int startX;
	private int stopX;

	public TextSize[] textSizes ={TextSize.SMALLEST,TextSize.SMALLER,TextSize.NORMAL,TextSize.LARGER,TextSize.LARGEST};
	
	public  float currentLight;
	public  int currentVisible ;
	private ImageView iv_visible;
	private ScrollView sv_webview;
	private LinearLayout ll_bottom;
	private RelativeLayout rl_title;
	private SharedPreferences sp;
	private LinearLayout ll_details;
	private TextView tv_loading;
	private float scale;
	private String url;
	private String date;
	private String title;
	private String sourcename;
	private int widthPixels;
	private String[] needParseHtmls;
	private boolean needParse = false;
	private String[] needGBKHtmls;
	private boolean needGbk = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		needParseHtmls = new String[] {"博客园","科学松鼠会","同步控","WPDang","软件小子"};
		widthPixels = getResources().getDisplayMetrics().widthPixels;
		initView();
		initPopView();
		initData();
	}

	public void initView(){
		ll_details = (LinearLayout) findViewById(R.id.ll_details);
		tv_loading = (TextView) findViewById(R.id.tv_loading);
		rl_title = (RelativeLayout) findViewById(R.id.ll_details_title);
		tv_title = (TextView) findViewById(R.id.tv_details_title);
		tv_data = (TextView) findViewById(R.id.tv_details_date);
		wv_showdetails = (WebView) findViewById(R.id.wv_details_showcontent);
		//这里删了一行设置背景颜色的代码，webview正常显示
		sv_webview = (ScrollView) findViewById(R.id.sv_details_webview);
		ll_bottom = (LinearLayout) findViewById(R.id.ll_details_bottom);
		iv_visible = (ImageView) findViewById(R.id.bt_details_visible);

	}
	public void initPopView(){
		
		popview = this.getLayoutInflater().inflate(R.layout.details_popmenu,null);
		bt1 = (Button) popview.findViewById(R.id.bt_pop_bt1);
		bt2 = (Button) popview.findViewById(R.id.bt_pop_bt2);

	}
	
	public void initData(){
		//从listView中获取数据
		
		scale = this.getResources().getDisplayMetrics().density;

		Intent intent = getIntent();
		title = intent.getStringExtra("title");
		date = intent.getStringExtra("date");
		url = intent.getStringExtra("url");
		sourcename = intent.getStringExtra("sourcename");
		tv_title.setText(title);
		tv_data.setText(sourcename+"  "+date);
		processFlag();
		
		wv_showdetails.getSettings().setLoadWithOverviewMode(true);
		
		wv_showdetails.getSettings().setJavaScriptEnabled(true);//加载js，否则部分list无法显示
		wv_showdetails.getSettings().setBlockNetworkImage(true); //图片下载阻塞
		
		wv_showdetails.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
            		view.loadUrl(url);
                    return true;
            }
			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				//网页加载完毕
				tv_loading.setVisibility(view.INVISIBLE);
				sv_webview.setVisibility(view.VISIBLE);
				wv_showdetails.getSettings().setBlockNetworkImage(false);//开始图片下载
				super.onPageFinished(view, url);
			}
		});
		if (needParse) {
			getHtmlStringData(url);
			
		}else {
			wv_showdetails.loadUrl(url);
		}
		wv_showdetails.setOnTouchListener(new MyOnTouchListener());

		size_setting = wv_showdetails.getSettings();
		size_setting.setTextSize(TextSize.NORMAL);
		
		sp = getSharedPreferences("details", MODE_PRIVATE);
		
		//sp中取出数据并进行回显
		checkItem = sp.getInt("checkItem", 2);
		size_setting.setTextSize(textSizes[checkItem]);
		
		//默认工具栏是显示的
		currentVisible = sp.getInt("currentVisible", 5);
		if (currentVisible == VISIBLE) {
			ll_bottom.setPadding(0, 0, 0, 0);
		}else if(currentVisible == INVISIBLE){
			Toast.makeText(this, "点击屏幕或菜单键呼出工具栏。", 0).show();
			ll_bottom.setPadding(0, 0, 0, -getBottomHeight());
		}
		
		//获取当前的屏幕亮度
		currentLight = sp.getFloat("currentLight", getScreenBrightness(this));
		lightrefresh();
		
	}

	private void processFlag() {
		for (String s : needParseHtmls) {
			if (sourcename.contains(s)) {
				needParse  = true;
			}
		}
	}
	
	private void getHtmlStringData(String url) {
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				// TODO Auto-generated method stub
				String html = SetCssUtils.setCssToHtml(arg0.result, widthPixels);
				wv_showdetails.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
				try {
					FileOutputStream fos = new FileOutputStream(new File(getCacheDir()+"/text.txt"));
					fos.write(html.getBytes());
					fos.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				needParse = false;
			}
		});
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		Editor editor = sp.edit();
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			ll_bottom.setPadding(0, 0, 0, 0);
			currentVisible = VISIBLE;
			editor.putInt("currentVisible", currentVisible);
			editor.commit();
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}

		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		
		int height_title = getTitleHeight();
		int width_display = getDisplayWidth() / 2;
		Editor editor = sp.edit();
		System.out.println("height = "+height_title);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			
				
			startX = (int) event.getRawX();
			break;
		case MotionEvent.ACTION_MOVE:
			
			break;
		case MotionEvent.ACTION_UP:
			if (startX == (int)event.getRawX()) {
				if (popmenu != null && popmenu .isShowing()) {
					popmenu.dismiss();
				}else 
				if (currentVisible == VISIBLE) {
					//当前为visible，则点击之后得显示invisible
					ll_bottom.setPadding(0, 0, 0, -getBottomHeight());
					currentVisible = INVISIBLE;
					editor.putInt("currentVisible", currentVisible);
					editor.commit();
					
				}else if(currentVisible == INVISIBLE){
					//当前为invisible，则点击之后
					ll_bottom.setPadding(0, 0, 0, 0);
					currentVisible = VISIBLE;
					editor.putInt("currentVisible", currentVisible);
					editor.commit();
					}
			}


			break;
		default:
			break;
		}
		
		
		return true;
	}
	


	class MyOnTouchListener implements OnTouchListener{


		private int startX;
		private int stopX;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			
			int height_title = getTitleHeight();
			int width_display = getDisplayWidth() / 2;
			Editor editor = sp.edit();
			System.out.println("height = "+height_title);
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startX = (int) event.getRawX();
				break;
			case MotionEvent.ACTION_MOVE:
				
				break;
			case MotionEvent.ACTION_UP:
				if (startX == (int) event.getRawX()) {
					if (popmenu != null && popmenu.isShowing()) {
						popmenu.dismiss();
					} else if (currentVisible == VISIBLE) {
						// 当前为visible，则点击之后得显示invisible
						ll_bottom.setPadding(0, 0, 0, -getBottomHeight());
						currentVisible = INVISIBLE;
						editor.putInt("currentVisible", currentVisible);
						editor.commit();

					} else if (currentVisible == INVISIBLE) {
						// 当前为invisible，则点击之后
						ll_bottom.setPadding(0, 0, 0, 0);
						currentVisible = VISIBLE;
						editor.putInt("currentVisible", currentVisible);
						editor.commit();
					}
				}



				break;
			default:
				break;
			}

			return true;
		}
		
	}
	
	public int getTitleHeight(){
		rl_title.measure(0, 0);
		
		int height = rl_title.getMeasuredHeight();
		
		return height;
	}
	
	public int getBottomHeight(){
		ll_bottom.measure(0, 0);
		int height = ll_bottom.getMeasuredHeight();
		return height; 
	}
	
	public int getDisplayWidth(){
		WindowManager wm = getWindowManager();
		int width = wm.getDefaultDisplay().getWidth();
		return width;
	}
	public void back(View v){
		//可以将finish的过程优化
		finish();
	}
	public void changesize(View v){
		if (popmenu!= null && popmenu.isShowing()) {
			popmenu.dismiss();
		}
		popmenu = new PopupWindow(popview, (int) (95 * scale), (int) (50 * scale));
		/*bt1.setText("增大");
		bt2.setText("减小");*/
		//字体点击改变
		bt1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sizemax();
			}
		});
		//字体点击变小
		bt2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sizemin();
			}
		});
		popmenu.showAsDropDown(v);
		popmenu.showAtLocation(findViewById(R.id.bt_details_size), Gravity.CENTER, 20, 20);
		
	}
	public void changevisible(View v){
		//功能切换为隐藏工具栏功能
		Editor editor = sp.edit();
		if (currentVisible == VISIBLE) {
			//当前为visible，则点击之后得显示invisible
			ll_bottom.setPadding(0, 0, 0, -getBottomHeight());
			currentVisible = INVISIBLE;
			editor.putInt("currentVisible", currentVisible);
			editor.commit();
			Toast.makeText(this, "工具栏隐藏，点击菜单项或点击屏幕重新呼出", 0).show();
			
		}else if(currentVisible == INVISIBLE){
			//当前为invisible，按钮无法点击到
		}

	}
	public void changelight(View v){
		if (popmenu!= null && popmenu.isShowing()) {
			popmenu.dismiss();
		}
		popmenu = new PopupWindow(popview, (int) (95 * scale), (int) (50 * scale));
		/*bt1.setText("变亮");
		bt2.setText("变暗");*/
		bt1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				lightmax();
			}
		});
		bt2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				lightmin();
			}
		});
		popmenu.showAsDropDown(v);
		popmenu.showAtLocation(findViewById(R.id.bt_details_light), Gravity.CENTER, 20, 20);
	}
	public void share(View v){
		showShare();
	}
	
	private void showShare() {
		 ShareSDK.initSDK(this);
		 OnekeyShare oks = new OnekeyShare();
		 //关闭sso授权
		 oks.disableSSOWhenAuthorize(); 
		 
		// 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
		 //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
		 // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		 oks.setTitle(title);
		 // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		 oks.setTitleUrl(url);
		 // text是分享文本，所有平台都需要这个字段
		 oks.setText("我正在看:"+title);
		 // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		 oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
		 // url仅在微信（包括好友和朋友圈）中使用
		 oks.setUrl("http://sharesdk.cn");
		 // comment是我对这条分享的评论，仅在人人网和QQ空间使用
		 oks.setComment("评论");
		 // site是分享此内容的网站名称，仅在QQ空间使用
		 oks.setSite(sourcename);
		 // siteUrl是分享此内容的网站地址，仅在QQ空间使用
		 oks.setSiteUrl(url);
		 
		// 启动分享GUI
		 oks.show(this);
		 }
	
	public void sizemax(){
		//字体变大
		/*size_setting.setTextSize(TextSize.NORMAL);
		checkItem = 2;*/
		Editor editor = sp.edit();
		if (checkItem < 4) {
			checkItem++;
			size_setting.setTextSize(textSizes[checkItem]);
		}else {
			Toast.makeText(this, "抱歉，字体已是最大", 0).show();
		}
		editor.putInt("checkItem", checkItem);
		editor.commit();
		
	}
	public void sizemin(){
		//字体变小
		Editor editor = sp.edit();
		if (checkItem > 0) {
			checkItem--;
			size_setting.setTextSize(textSizes[checkItem]);
		}else {
			Toast.makeText(this, "抱歉，字体已是最小", 0).show();
		}
		editor.putInt("checkItem", checkItem);
		editor.commit();
	}
	
	public void lightmax(){
		//变亮
		Editor editor = sp.edit();
		if (currentLight < 236) {
			currentLight += 20;
			lightrefresh();
		}else {
			Toast.makeText(this, "抱歉，屏幕亮度已是最大", 0).show();
		}
		editor.putFloat("currentLight", currentLight);
		editor.commit();
	}
	
	public void lightmin(){
		//屏幕变小
		Editor editor = sp.edit();
		if (currentLight >= 20) {
			currentLight -= 20;
			lightrefresh();
		}else {
			Toast.makeText(this, "抱歉，屏幕亮度已是最小", 0).show();
		}
		editor.putFloat("currentLight", currentLight);
		editor.commit();
	}
	
	public void lightrefresh(){
		WindowManager.LayoutParams params = getWindow().getAttributes();
	    params.screenBrightness = currentLight / 255f;
	    getWindow().setAttributes(params);
	}
	
	public static int getScreenBrightness(Activity activity) {
	    int value = 0;
	    ContentResolver cr = activity.getContentResolver();
	    try {
	        value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
	    } catch (SettingNotFoundException e) {
	        
	    }
	    return value;
	}
	
	@Override
	protected void onResume() {
		NightModeUpdate.update(this);
		super.onResume();
	}


}
