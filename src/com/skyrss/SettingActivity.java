package com.skyrss;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.skyrss.global.NightModeUpdate;
import com.skyrss.setting.AboutUs;
import com.skyrss.setting.MoreSetting;
import com.skyrss.setting.UserFeedbackActivity;
import com.skyrss.ui.SettingItemJump;
import com.skyrss.ui.SlideSwitch;
import com.skyrss.ui.SlideSwitch.SlideListener;
import com.skyrss.utils.ChangeAppBrightness;
import com.skyrss.utils.CheckUpdate;

public class SettingActivity extends Activity implements OnClickListener {

	private static final String TAG = null;

	SharedPreferences sp;

	// 设置夜间模式
	private SlideSwitch isnight;
	private TextView tv_nightdescription;

	private SettingItemJump userfeed;
	private SettingItemJump download;
	private SettingItemJump clearcache;
	private SettingItemJump moresettting;
	private SettingItemJump update;
	private SettingItemJump goodcomment;
	private SettingItemJump aboutus;
	private ImageButton ib_back;

	private int syslight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		// 夜间模式设置
		isnight = (SlideSwitch) findViewById(R.id.ss_setting_isnight);
		tv_nightdescription = (TextView) findViewById(R.id.tv_setting_nightdescription);
		Boolean nightstate = sp.getBoolean("nightmode", false);
		if (nightstate) {
			isnight.setState(nightstate);
			tv_nightdescription.setText("夜间模式已经开启");
		}
		isnight.setSlideListener(new SlideListener() {
			Editor et;

			@Override
			public void open() {
				tv_nightdescription.setText("夜间模式已经开启");
				et = sp.edit();
				et.putBoolean("nightmode", true);
				et.commit();
				ChangeAppBrightness.change(SettingActivity.this, 3);
				
			}

			@Override
			public void close() {
				tv_nightdescription.setText("夜间模式已经关闭");
				et = sp.edit();
				et.putBoolean("nightmode", false);
				et.commit();
				ChangeAppBrightness.change(SettingActivity.this, getScreenBrightness(SettingActivity.this));
			}
		});
		
		ib_back = (ImageButton) findViewById(R.id.ib_home_slidingmenu);
		download = (SettingItemJump) findViewById(R.id.si_setting_download);
		clearcache = (SettingItemJump) findViewById(R.id.si_setting_clearcache);
		moresettting = (SettingItemJump) findViewById(R.id.si_setting_moresetting);
		userfeed = (SettingItemJump) findViewById(R.id.si_setting_userfeed);
		update = (SettingItemJump) findViewById(R.id.si_setting_updade);
		goodcomment = (SettingItemJump) findViewById(R.id.si_setting_good);
		aboutus = (SettingItemJump) findViewById(R.id.si_setting_aboutus);
		
		ib_back.setOnClickListener(this);
		download.setOnClickListener(this);
		clearcache.setOnClickListener(this);
		moresettting.setOnClickListener(this);
		userfeed.setOnClickListener(this);
		update.setOnClickListener(this);
		goodcomment.setOnClickListener(this);
		aboutus.setOnClickListener(this);

		//动态获取系统版本号
		update.changeDescription("当前版本为"+CheckUpdate.getVersion(this));

	}

	@Override
	protected void onResume() {
		NightModeUpdate.update(this);
		super.onResume();
	}
	

	
	public static int getScreenBrightness(Activity activity) {
	    int value = 0;
	    ContentResolver cr = activity.getContentResolver();
	    try {
	        value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
	    } catch (SettingNotFoundException e) {
	        
	    }
	    
	    System.out.println("setting light : "+ value);
	    return value;
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.si_setting_download:
			Toast.makeText(this, "离线下载功能正在开发中,请期待2.0版本", Toast.LENGTH_SHORT).show();
			break;
		case R.id.si_setting_clearcache:
			deleteCache();
			break;
		case R.id.si_setting_moresetting:
			startActivity(new Intent().setClass(this, MoreSetting.class));
			break;
		case R.id.si_setting_userfeed:
			Intent intent = new Intent(this, UserFeedbackActivity.class);
			startActivity(intent);
			break;
		case R.id.si_setting_updade:
			checkupdate();
			break;
		case R.id.si_setting_good:
			try {
				Uri uri = Uri.parse("market://details?id=" + getPackageName());
				Intent goodcomments = new Intent(Intent.ACTION_VIEW, uri);
				goodcomments.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(goodcomments);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(this, "出现错误 !", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.si_setting_aboutus:
			startActivity(new Intent().setClass(this, AboutUs.class));
			break;

		case R.id.ib_home_slidingmenu:
			// 返回按钮
			back();
			break;
		default:
			Toast.makeText(this, "没有找到", Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private void deleteCache() {
		File directory = new File(getCacheDir() + "");
		double size = getCacheSize(directory);
		Boolean success = deleteDir(directory);
		if (success) {
			if (size == 0.0) {
				Toast.makeText(this, "当前没有缓存,请在使用一段时间后再清理", Toast.LENGTH_SHORT).show();
			} else {
				long space = new Double(size).longValue();
				Toast.makeText(this,
						"成功清理缓存" + Formatter.formatFileSize(this, space), Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			Toast.makeText(this, "出现错误，请反馈", Toast.LENGTH_SHORT).show();
		}
	}

	private double getCacheSize(File file) {
		if (file.exists()) {
			if (!file.isFile()) {
				File[] fl = file.listFiles();
				double ss = 0;
				for (File f : fl)
					ss += getCacheSize(f);
				return ss;
			} else {
				double ss = (double) file.length() / 1024 / 1024;
				return ss;
			}
		} else {
			return 0.0;
		}
	}

	private boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] filelist = dir.list();
			// 递归删除目录中的子目录下
			for (int i = 0; i < filelist.length; i++) {
				boolean success = deleteDir(new File(dir, filelist[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}



	private void checkupdate() {
		HttpUtils utils = new HttpUtils();
		String url = "http://skyrssreader.sinaapp.com/version.json";
		utils.send(HttpMethod.GET, url, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				Toast.makeText(SettingActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				try {
					JSONObject object = new JSONObject(arg0.result);
					String newversion = object.getString("version");
					String oldversion = CheckUpdate.getVersion(SettingActivity.this);
					Log.d(TAG, "newversion," + newversion + "," + oldversion);
					if (oldversion.equals(newversion)) {
						Toast.makeText(SettingActivity.this, "已经是最新版本，不需要更新",  Toast.LENGTH_SHORT)
								.show();
					} else {
						String description = object.getString("description");
						String ApkUrl = object.getString("ApkUrl");
						AlertDialog.Builder dialog = new AlertDialog.Builder(
								SettingActivity.this);
						dialog.setTitle("检测到新版本:" + newversion);
						dialog.setMessage(description);
						dialog.setPositiveButton("升级", new myupgradelistener(
								ApkUrl));
						dialog.setNegativeButton("取消", null);
						dialog.show();
					}

				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(SettingActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
				}

			}
		});

	}
	
	private void back() {
		finish();
		overridePendingTransition(R.anim.enteranim, R.anim.exitanim);
	}

	/**
	 * 升级时的动作类
	 * 
	 * @author Administrator
	 *
	 */
	class myupgradelistener implements DialogInterface.OnClickListener {
		String url;

		public myupgradelistener() {
			super();
		}

		public myupgradelistener(String url) {
			super();
			this.url = url;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			HttpUtils utils = new HttpUtils();
			utils.send(HttpMethod.GET, url, new RequestCallBack<Byte>() {

				@Override
				public void onFailure(HttpException arg0, String arg1) {
					Toast.makeText(SettingActivity.this, "连接服务器失败",  Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onSuccess(ResponseInfo<Byte> arg0) {
					File file = new File(getFilesDir() + "/SkyRssReader.apk");
					try {
						FileOutputStream fos = new FileOutputStream(file);
						fos.write(arg0.result);
						fos.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Intent intent = new Intent();
					intent.setAction("android.intent.action.VIEW");
					intent.addCategory("android.intent.category.DEFAULT");
					intent.setDataAndType(Uri.fromFile(file),
							"application/vnd.android.package-archive");
					startActivityForResult(intent, 100);
				}
			});
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		back();
	}

}
