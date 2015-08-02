package com.skyrss;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.skyrss.bean.Item;
import com.skyrss.bean.News;
import com.skyrss.bean.NewsDetail;
import com.skyrss.bean.SourceBean;
import com.skyrss.bean.NewsDetail.NewsItem;
import com.skyrss.bean.NewsDetailForOther.NewsEntry;
import com.skyrss.bean.NewsDetailForOther;
import com.skyrss.global.NightModeUpdate;
import com.skyrss.utils.ExtractImgUrlUtil;
import com.skyrss.utils.PubDateFormatUtils;
import com.skyrss.utils.XmlparserUtils;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.skyrss.bean.Item;
import com.skyrss.bean.News;
import com.skyrss.bean.NewsDetail;
import com.skyrss.bean.NewsDetail.NewsItem;
import com.skyrss.bean.NewsDetailForOther;
import com.skyrss.bean.NewsDetailForOther.NewsEntry;
import com.skyrss.bean.SourceBean;
import com.skyrss.dao.SourceDao;
import com.skyrss.global.NightModeUpdate;
import com.skyrss.utils.ExtractImgUrlUtil;
import com.skyrss.utils.PubDateFormatUtils;
import com.skyrss.utils.XmlparserUtils;
import com.viewpagerindicator.CirclePageIndicator;

public class HomeActivity extends Activity {

	private ViewPager vp_home_rollnews;

	private String[] imgUrls = new String[4];
	private String[] titles = new String[4];
	private TextView tv_home_rolltitles;
	private CirclePageIndicator cpi_home_rollnews;
	private int randomSourceId;
	private ArrayList<NewsItem> choosedItems = new ArrayList<NewsDetail.NewsItem>();
	private ArrayList<NewsEntry> choosedEntries = new ArrayList<NewsDetailForOther.NewsEntry>();
	private NewsDetail detail;
	private NewsItem newsItem;
	private News news;
	private NewsDetailForOther detailForOther;
	private NewsEntry newsEntry;

	private List<SourceBean> chooseList;
	
	private int[] iconlist = {R.drawable.ic_engadget,R.drawable.ic_zhihu,R.drawable.ic_36kr,R.drawable.ic_ifanr,R.drawable.ic_geekpark,};
	
	private SourceDao dao;

	private GridView gv_home_sourcelist;
	private int selectedPosition;

	private MyNewsSourceAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		initdbSource();
		gv_home_sourcelist = (GridView) findViewById(R.id.gv_home_sourcelist);
		vp_home_rollnews = (ViewPager) findViewById(R.id.vp_home_rollnews);
		tv_home_rolltitles = (TextView) findViewById(R.id.tv_home_rolltitles);
		cpi_home_rollnews = (CirclePageIndicator) findViewById(R.id.cpi_home_rollnews);

		ImageButton ib_home_addsource = (ImageButton) findViewById(R.id.ib_home_addsource);
		ib_home_addsource.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent jump = new Intent(HomeActivity.this,
						FindmoreActivity.class);
				startActivity(jump);
				overridePendingTransition(R.anim.enteranim, R.anim.exitanim);
			}

		});
		ImageButton ib_home_setting=(ImageButton) findViewById(R.id.ib_home_slidingmenu);
		ib_home_setting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent jump=new Intent(HomeActivity.this,
						SettingActivity.class);
				startActivity(jump);
				overridePendingTransition(R.anim.pre_enteranim, R.anim.pre_exitanim);
			}
		});
		adapter = new MyNewsSourceAdapter();
		gv_home_sourcelist.setAdapter(adapter);
		gv_home_sourcelist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				selectedPosition = position;
				Intent intent = new Intent(HomeActivity.this,
						SourceActivity.class);
				intent.putExtra("source", chooseList.get(position).getId());
				//修改成功
				intent.putExtra("sourcename", chooseList.get(position).getName());
				
				startActivity(intent);
				overridePendingTransition(R.anim.enteranim, R.anim.exitanim);
			}
		});
		
		initOneSource();
		
		ConnectivityManager conMana=(ConnectivityManager) getSystemService(this.
				CONNECTIVITY_SERVICE);
		State wifi=conMana.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if(wifi.toString().equals("DISCONNECTED")){
			Toast.makeText(this, "检测到您没有连接wifi，请连接wifi节省流量并可以获得较好的使用体验", Toast.LENGTH_LONG).show();
		}
		
	}
	
	private void initdbSource(){
		chooseList = new ArrayList<SourceBean>();
		dao = new SourceDao(this);
		chooseList = dao.getAllChecked();
	}

	/**
	 * 在进入主页面的时候随机选择一个源加载，并随机获取它的某4条图片新闻，显示到 viewpager
	 */
	private void initOneSource() {
		// TODO Auto-generated method stub

		Random random = new Random();
		randomSourceId = random.nextInt(5);
		String url = dao.getUrlById(randomSourceId+1);
		
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.configCurrentHttpCacheExpiry(1000 * 10);// 设置超时时间
		httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				// TODO Auto-generated method stub
				arg0.printStackTrace();
				Toast.makeText(getApplicationContext(), "网络连接失败", 0).show();
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				// TODO Auto-generated method stub
				//获取到了数据，拿去解析
				news = XmlparserUtils.Xmlparse(arg0.result);
				if (news instanceof NewsDetail) {
					detail = (NewsDetail) news;
				} else {
					detailForOther = (NewsDetailForOther) news;
				}
				//随机获取4条新闻
				initRollNews();
				vp_home_rollnews.setAdapter(new MyImgPageAdapter());
				
				
				//默认显示第一条新闻的标题
				tv_home_rolltitles.setText(titles[0]);
				

				cpi_home_rollnews.setViewPager(vp_home_rollnews);
				cpi_home_rollnews.setOnPageChangeListener(new MyRollPageChangedListener());
				cpi_home_rollnews.setSnap(true);
				
				
			}

		});

	}

	private void initRollNews() {
		Random random = new Random();
		String temp = "";
		//填充4个图片url，注意：直接产生4个随机数的话可能会重复。。
		//所以通过代码，避免重复url。
		for (int i = 0; i < imgUrls.length; i++) {
			boolean flag = true;
			while (temp.isEmpty() || flag) {
				flag = false;
				int itemId = random.nextInt(detail==null?detailForOther.entries.size():detail.items.size());
				if (detail != null) {
					newsItem = detail.items.get(itemId);
				} else {
					newsEntry = detailForOther.entries.get(itemId);
				}
				if (newsItem != null) {
					if (newsItem.image == null) {
						temp = getImgUrl(newsItem);
						for (String url : imgUrls) {
							if (temp.equals(url)) {
								flag = true;
							}
						}
					} else {
						break;
					}
				} else {
					temp = getImgUrl(newsEntry);
				}
			}
			if (newsItem != null) {
				if (newsItem.image == null) {
					imgUrls[i] = temp;
				} else {
					imgUrls[i] = newsItem.image;
				}
				titles[i] = newsItem.title;
				choosedItems.add(newsItem);
			} else if (newsEntry != null) {
				imgUrls[i] = temp;
				titles[i] = newsEntry.title;
				choosedEntries.add(newsEntry);
			}
		}
		
		
	}

	/**
	 * 将description中的img标签通过字符串处理提取出它的url。。
	 * @param item
	 * @return
	 */
	private String getImgUrl(Item item) {
		if (item instanceof NewsEntry) {
			newsEntry = (NewsEntry) item;
		} else {
			newsItem = (NewsItem) item;
		}
		String imgUrl = "";
		if (newsItem != null) {
			if (newsItem.encoded == null) {
				imgUrl = ExtractImgUrlUtil.extractUrl(newsItem.description);
			} else {
				imgUrl = ExtractImgUrlUtil.extractUrl(newsItem.encoded);
			}
		} else {
			imgUrl = ExtractImgUrlUtil.extractUrl(newsEntry.content);
		}
		return imgUrl;
	}

	class MyImgPageAdapter extends PagerAdapter {

		private BitmapUtils bitmapUtils;

		public MyImgPageAdapter() {
			bitmapUtils = new BitmapUtils(HomeActivity.this);
			bitmapUtils.configDefaultLoadingImage(R.drawable.home_pic);
			bitmapUtils.configDefaultLoadFailedImage(R.drawable.home_pic);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 4;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			// TODO Auto-generated method stub
			ImageView iv = new ImageView(HomeActivity.this);
			iv.setScaleType(ScaleType.FIT_XY);
			if (!"".equals(imgUrls[position])) {
				bitmapUtils.display(iv, imgUrls[position]);
			}
			
			iv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String date = PubDateFormatUtils
							.formatDate(detail==null?choosedEntries.get(position).published:choosedItems.get(position).pubDate);
					Intent intent = new Intent(HomeActivity.this, DetailsActivity.class);
					intent.putExtra("date", date);
					intent.putExtra("title", detail==null?choosedEntries.get(position).title:choosedItems.get(position).title);
					intent.putExtra("url", detail==null?choosedEntries.get(position).id:choosedItems.get(position).link);
					intent.putExtra("sourcename",detail==null?
							detailForOther.title:detail.title);
					startActivity(intent);
					
				}
			});
			container.addView(iv);
			return iv;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			container.removeView((View) object);
		}

	}
	
	
	class MyRollPageChangedListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			tv_home_rolltitles.setText(titles[arg0]);
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

	}
	
	class MyNewsSourceAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return chooseList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = View.inflate(HomeActivity.this, R.layout.items_sourcelist,
					null);
			TextView tv_sourcelist_name = (TextView) v.findViewById(R.id.tv_sourcelist_name);
			ImageView iv_sourcelist_logo = (ImageView) v.findViewById(R.id.iv_sourcelist_logo);
			tv_sourcelist_name.setText((String) chooseList.get(position).getName());
			
			if (chooseList.get(position).getId() <6) {
				iv_sourcelist_logo.setImageResource(iconlist[chooseList.get(position).getId()-1]);
			}else {
				iv_sourcelist_logo.setImageResource(R.drawable.ic_default);
			}
			return v;
		}
	}
	
	@Override
	protected void onResume() {
		initdbSource();
		adapter.notifyDataSetChanged();
		NightModeUpdate.update(this);
		super.onResume();
	}
	
}
