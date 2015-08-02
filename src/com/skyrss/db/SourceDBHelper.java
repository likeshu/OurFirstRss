package com.skyrss.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SourceDBHelper extends SQLiteOpenHelper {

	public SourceDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String createstr = "create table sourceinfo (sid integer primary key autoincrement,sname varchar(30),surl varchar(200),sischoosed integer);";
		String str1 = "insert into sourceinfo values(1,'Engadget中国','http://cn.engadget.com/rss.xml',1);";
		String str2 = "insert into sourceinfo values(2,'知乎精选','http://www.zhihu.com/rss',1);";
		String str3 = "insert into sourceinfo values(3,'36氪','http://www.36kr.com/feed/',1);";
		String str4 = "insert into sourceinfo values(4,'爱范儿','http://www.ifanr.com/feed',1);";
		String str5 = "insert into sourceinfo values(5,'极客公园','http://www.geekpark.net/rss',1);";
		String str6 = "insert into sourceinfo values(6,'科学松鼠会','http://songshuhui.net/feed',0);";
		String str7 = "insert into sourceinfo values(7,'谷奥','http://each.fm/live/53c6d726d01983e704000008/rss.xml',0);";
		String str8 = "insert into sourceinfo values(8,'微软亚洲研究院','http://blog.sina.com.cn/rss/1286528122.xml',0);";
		String str9 = "insert into sourceinfo values(9,'善用佳软','http://feed.xbeta.info/',0);";
		String str10 = "insert into sourceinfo values(10,'月光博客','http://feed.williamlong.info/',0);";
		String str11 = "insert into sourceinfo values(11,'博客园','http://feed.cnblogs.com/blog/sitehome/rss',0);";
		String str12 = "insert into sourceinfo values(12,'虎嗅','http://www.huxiu.com/rss/1.xml',0);";
		String str13 = "insert into sourceinfo values(13,'泛科学','http://pansci.tw/feed',0);";
		String str14 = "insert into sourceinfo values(14,'战隼学习探索','http://www.read.org.cn/feed',0);";
		String str15 = "insert into sourceinfo values(15,'同步控','http://www.syncoo.com/feed',0);";
		String str16 = "insert into sourceinfo values(16,'锋客网','http://www.phonekr.com/feed/',0);";
		String str17 = "insert into sourceinfo values(17,'WPDang','http://www.wpdang.com/feed',0);";
		String str18 = "insert into sourceinfo values(18,'什么值得买','http://feed.smzdm.com/',0);";
		String str19 = "insert into sourceinfo values(19,'看上去很猛','http://blog.sina.com.cn/rss/1609953534.xml',0);";
		String str20 = "insert into sourceinfo values(20,'优设','http://www.uisdc.com/feed',0);";
		db.execSQL(createstr);
		db.execSQL(str1);
		db.execSQL(str2);
		db.execSQL(str3);
		db.execSQL(str4);
		db.execSQL(str5);
		db.execSQL(str6);
		db.execSQL(str7);
		db.execSQL(str8);
		db.execSQL(str9);
		db.execSQL(str10);
		db.execSQL(str11);
		db.execSQL(str12);
		db.execSQL(str13);
		db.execSQL(str14);
		db.execSQL(str15);
		db.execSQL(str16);
		db.execSQL(str17);
		db.execSQL(str18);
		db.execSQL(str19);
		db.execSQL(str20);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
