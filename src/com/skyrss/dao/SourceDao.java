package com.skyrss.dao;


import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.skyrss.bean.SourceBean;
import com.skyrss.db.SourceDBHelper;

public class SourceDao {
	
	
	SQLiteDatabase db ;
	private SourceDBHelper helper;
	
	public SourceDao(Context ctx){
		helper = new SourceDBHelper(ctx, "source.db", null, 1);
		this.db = helper.getReadableDatabase();
	}
	
	public List<SourceBean> getAllSource(){//单元测试无BUG
		//获取所有数据库中的数据
		List<SourceBean> sourcelist = new ArrayList<SourceBean>();
		
		Cursor cursor = db.query("sourceinfo", new String[]{"sid","sname","surl","sischoosed"}, null, null, null, null, null);
	
		while (cursor.moveToNext()) {
			int id = cursor.getInt(0);
			String name = cursor.getString(1);
			String url = cursor.getString(2);
			int intchoose = cursor.getInt(3);
			boolean ischoosed ;
			if (intchoose == 0) {
				ischoosed = false;//在home中没有
			}else {
				ischoosed = true;//home中已经存在
			}
			SourceBean source = new SourceBean(id,name, url, ischoosed);
			sourcelist.add(source);
		}
		return sourcelist;	
	}
	
	public List<SourceBean> getAllChecked(){//单元测试成功
		List<SourceBean> checkedlist = new ArrayList<SourceBean>();
		
		Cursor cursor = db.query("sourceinfo", new String[]{"sid","sname","surl","sischoosed"}, null, null, null, null, null);
	
		while (cursor.moveToNext()) {
			int id = cursor.getInt(0);
			String name = cursor.getString(1);
			String url = cursor.getString(2);
			int intchoose = cursor.getInt(3);
			if (intchoose == 1) {
				SourceBean source = new SourceBean(id,name, url, true);
				checkedlist.add(source);
			}
			
		}
		return checkedlist;
	}
	
	public boolean IsChoosed(String name){//单元测试无bug
		//判断该name的源是否已经在home中显示
		boolean flag = false ;
		Cursor cursor = db.query("sourceinfo", new String[]{"sname","surl","sischoosed"}, "sname = ?", new String[]{name}, null, null, null);
		cursor.moveToNext();//这句不能漏掉。。。
		if(cursor.getInt(2) == 1){
			flag = true;
		}
		return flag;
		
	}
	
	public String getUrlById(int id){//单元测试OK
		
		Cursor cursor = db.query("sourceinfo", new String[]{"surl"}, "sid = ?", new String[]{id+""}, null, null, null);
		cursor.moveToNext();
		String url = cursor.getString(0);
		return url;
	}
	
	public void addtohome(String name){//单元测试无bug
		ContentValues values = new ContentValues();
		values.put("sischoosed", 1);//
		db.update("sourceinfo", values, "sname = ?", new String[]{name});
	}
	
	public void removefromhome(String name){//
		ContentValues values = new ContentValues();
		values.put("sischoosed", 0);//将选中状态改为0，则去掉
		db.update("sourceinfo", values, "sname = ?", new String[]{name});
	}
	
	public void selfadd(String name,String url){//单元测试无bug
		//用户手动添加的源，判断合法后添加
		ContentValues values = new ContentValues();
		values.put("sname", name);
		values.put("surl", url);
		values.put("sischoosed", 1);//默认将用户自己手动添加的源添加到home中,true为1
		
		db.insert("sourceinfo", null, values);
	}
	

}
