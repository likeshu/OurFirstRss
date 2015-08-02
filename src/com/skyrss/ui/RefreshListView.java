package com.skyrss.ui;

import java.text.SimpleDateFormat;
import java.util.Date;








import com.skyrss.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshListView extends  ListView{

	private int mHeaderViewHeight;
	int rawstartY ;
	int rawendY ;
	private View mHeaderView;

	
	private static final int STATE_PULL_REFRESH = 0;// ����ˢ��
	private static final int STATE_RELEASE_REFRESH = 1;// �ɿ�ˢ��
	private static final int STATE_REFRESHING = 2;// ����ˢ��
	private int mCurrrentState = STATE_PULL_REFRESH;// ��ǰ״̬
	
	private ImageView iv_refreshhead_arr;
	private TextView tv_refreshhead_title;
	private onRefreshListener mlistener;
	
	private View mFooterView;
	private int mFooterViewHeight;
	private Animation downAnimation;
	private Animation upAnimation;
	private ImageView iv_refreshhead_dot1;
	private ImageView iv_refreshhead_dot2;
	private ImageView iv_refreshhead_dot3;


	public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initHeaderView();
		initFooterView();
 	}




	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHeaderView();
		initFooterView();
		initAnimation();

		// TODO Auto-generated constructor stub
	}

	public RefreshListView(Context context) {
		super(context);
		initHeaderView();
		initFooterView();
		
		// TODO Auto-generated constructor stub
	}
	
	

	private void initHeaderView() {
		mHeaderView = View.inflate(getContext(), R.layout.refreshheader_newstitlelist, null);
		
		this.addHeaderView(mHeaderView);
		
		mHeaderView.measure(0, 0);
		mHeaderViewHeight = mHeaderView.getMeasuredHeight();
		mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);

		iv_refreshhead_arr = (ImageView)mHeaderView.findViewById(R.id.iv_refreshheader_arr);
		tv_refreshhead_title = (TextView) mHeaderView.findViewById(R.id.tv_refreshheader_title);
		
		ll_refreshheader_dots = (LinearLayout) mHeaderView.findViewById(R.id.ll_refreshheader_dots);
		iv_refreshhead_dot1 = (ImageView)mHeaderView.findViewById(R.id.iv_refreshheader_dot1);
		iv_refreshhead_dot2 = (ImageView)mHeaderView.findViewById(R.id.iv_refreshheader_dot2);
		iv_refreshhead_dot3 = (ImageView)mHeaderView.findViewById(R.id.iv_refreshheader_dot3);
	}
	
	private void initAnimation() {
		upAnimation = new RotateAnimation(0f, -180f,
		        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
		        0.5f);
		upAnimation.setDuration(500);
		upAnimation.setFillAfter(true); 
		
		downAnimation = new RotateAnimation(-180f, -360f,
		        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
		        0.5f);
		downAnimation.setDuration(500);
		downAnimation.setFillAfter(true); 
		
		dotAnimation1 = (AlphaAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.anim_refreshdot);
		dotAnimation2 = (AlphaAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.anim_refreshdot);
		dotAnimation2.setStartOffset(100);
		dotAnimation3 = (AlphaAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.anim_refreshdot);
		dotAnimation3.setStartOffset(200);
	}
	
	private void initFooterView() {
		// TODO Auto-generated method stub
		mFooterView = View.inflate(getContext(), R.layout.refreshfooter_newstitlelist, null);
		
		this.addFooterView(mFooterView); //�ӵ�listview�������
		
		mFooterView.measure(0, 0);
		mFooterViewHeight = mFooterView.getMeasuredHeight();
		mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);// ���ؽŲ���
		
		this.setOnScrollListener(new MyOnScrollListener());
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub


		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			 rawstartY = (int) ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			
			if (rawstartY == -1) {
				rawstartY = (int) ev.getRawY();
			}

			rawendY = (int) ev.getRawY();

			int dy = rawendY-rawstartY;
			
			
			if (getFirstVisiblePosition() > 0) {
				rawstartY = rawendY;
			}

			if (dy>0 &&  getFirstVisiblePosition()==0) {
				
				
				int padding =  dy-mHeaderViewHeight  ;       //-(mHeaderViewHeight-dy);

				if (mCurrrentState==STATE_REFRESHING) {
					break;
				}
				
				mHeaderView.setPadding(0, padding, 0, 0);
				
				if (padding>0&&mCurrrentState!=STATE_RELEASE_REFRESH) {
					mCurrrentState = STATE_RELEASE_REFRESH;
					refreshState();
				}
				else if (padding<0&& mCurrrentState!= STATE_PULL_REFRESH) {
					mCurrrentState = STATE_PULL_REFRESH;
					refreshState();

				} 
				
			} 
			
			break;
		case MotionEvent.ACTION_UP:
			rawstartY = -1;
			
	        if (mCurrrentState==STATE_RELEASE_REFRESH) {
	        	mCurrrentState=STATE_REFRESHING;
				mHeaderView.setPadding(0, 0, 0, 0);

				refreshState();
				
			}else if(mCurrrentState==STATE_PULL_REFRESH){
				mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);

			}
			break;

		default:
			break;
		}
		
		
		return super.onTouchEvent(ev);
	}
	


	private void refreshState() {
		// TODO Auto-generated method stub
		switch (mCurrrentState) {
		case STATE_PULL_REFRESH:
			tv_refreshhead_title.setText("下拉刷新");
			iv_refreshhead_arr.setVisibility(View.VISIBLE);
			ll_refreshheader_dots.setVisibility(View.INVISIBLE);
			iv_refreshhead_arr.startAnimation(downAnimation);
			break;
		case STATE_RELEASE_REFRESH:
			tv_refreshhead_title.setText("释放刷新");
			iv_refreshhead_arr.setVisibility(View.VISIBLE);
			ll_refreshheader_dots.setVisibility(View.INVISIBLE);
			iv_refreshhead_arr.startAnimation(upAnimation);
			break;
		case STATE_REFRESHING:
			tv_refreshhead_title.setText("正在刷新");
			iv_refreshhead_arr.clearAnimation();//不加这句箭头不会消失。。
			iv_refreshhead_arr.setVisibility(View.INVISIBLE);
			ll_refreshheader_dots.setVisibility(View.VISIBLE);
			iv_refreshhead_dot1.startAnimation(dotAnimation1);
			iv_refreshhead_dot2.startAnimation(dotAnimation2);
			iv_refreshhead_dot3.startAnimation(dotAnimation3);
			
            if (mlistener!=null) {
				mlistener.onRrefesh();
			}
			break;
		default:
			break;
		}
	}
	
	
	public void setOnRefreshListner(onRefreshListener listener){	
		 mlistener= listener;
	}
	
	public interface onRefreshListener{	
		public void onRrefesh();
		public void onLoadmore(int lastPostion);
	}
	
	
	public String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = format.format(new Date());
		
		return time ;
	}
	
	
	public void onRefreshComplete() {
		if (isLoadingMore) {
			mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);
			isLoadingMore=false;
		}else {
			mCurrrentState = STATE_PULL_REFRESH;
			tv_refreshhead_title.setText("下拉刷新");
			iv_refreshhead_arr.setVisibility(View.VISIBLE);
			ll_refreshheader_dots.setVisibility(View.INVISIBLE);
			mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
		}
		
 
	}
	
	private boolean isLoadingMore = false;
	private AlphaAnimation dotAnimation1;
	private AlphaAnimation dotAnimation2;
	private AlphaAnimation dotAnimation3;
	
	private LinearLayout ll_refreshheader_dots;
	
	class MyOnScrollListener implements OnScrollListener{

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			if((scrollState== SCROLL_STATE_IDLE ||scrollState== SCROLL_STATE_FLING )&& !isLoadingMore){
				if (getLastVisiblePosition()==getCount()-1) {//
					
					
					mFooterView.setPadding(0, 0, 0, 0);// 
					setSelection(getCount()-1);
					isLoadingMore=true;
					if (mlistener != null) {
						mlistener.onLoadmore(getLastVisiblePosition());
					}
				}
				
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
		}
		
		
	}
	

}
