package com.skyrss.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.skyrss.bean.News;
import com.skyrss.bean.NewsDetail;
import com.skyrss.bean.NewsDetail.NewsImage;
import com.skyrss.bean.NewsDetail.NewsItem;
import com.skyrss.bean.NewsDetailForOther.NewsEntry;
import com.skyrss.bean.NewsDetailForOther;

import android.util.Log;
import android.util.Xml;

public class XmlparserUtils {
	public static News Xmlparse(String data) {
		NewsDetail detail = null;
		NewsItem item = null;
		NewsDetailForOther detailForOther = null;
		NewsEntry entry = null; 
		XmlPullParser parser = Xml.newPullParser();
		try {
			//将字符串转换成流，然后解析。
			InputStream is = new ByteArrayInputStream(data.getBytes());
			parser.setInput(is, "utf-8");
			int eventType = parser.getEventType();
			
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					if ("channel".equals(parser.getName())) {
						detail = new NewsDetail();
						detail.image = new NewsImage();
						detail.items = new ArrayList<NewsItem>();
					}
					if ("feed".equals(parser.getName())) {
						detailForOther = new NewsDetailForOther();
						detailForOther.entries = new ArrayList<NewsEntry>();
						eventType = parser.next();
						while (true) {
							if (eventType == XmlPullParser.START_TAG) {
								if ("title".equals(parser.getName())) {
									detailForOther.title = parser.nextText();
								}
								if ("entry".equals(parser.getName())) {
									entry = new NewsEntry();
									eventType = parser.next();
									while (true) {
										if (eventType == XmlPullParser.START_TAG) {
											if ("id".equals(parser.getName())) {
												entry.id = parser.nextText();
											}
											if ("title".equals(parser.getName())) {
												entry.title = parser.nextText().trim();
											}
											if ("author".equals(parser.getName())) {
												parser.next();
												if ("name".equals(parser.getName())) {
													entry.author = parser.nextText();
												}
											}
											if ("published".equals(parser.getName())) {
												entry.published = parser.nextText();
											}
											if ("content".equals(parser.getName())) {
												entry.content = parser.nextText();
											}
										}
										if (eventType == XmlPullParser.END_TAG) {
											if ("entry".equals(parser.getName())) {
												detailForOther.entries.add(entry);
												entry = null;
												break;
											}
										}
										eventType = parser.next();
									}
								}
							}
							if (eventType == XmlPullParser.END_TAG) {
								if ("feed".equals(parser.getName())) {
									return detailForOther;
								}
							}
							
							eventType = parser.next();	
							}
						}
					}
					
					if ("title".equals(parser.getName())) {
						detail.title = parser.nextText();
					}
					if ("link".equals(parser.getName())) {
						detail.link = parser.nextText();
					}
					if ("description".equals(parser.getName())) {
						detail.description = parser.nextText();
					}

					if ("image".equals(parser.getName())) {
						eventType = parser.next();
						while (true) {
							if (eventType == XmlPullParser.START_TAG) {
								if ("url".equals(parser.getName())) {
									detail.image.url = parser.nextText();
								}
								if ("title".equals(parser.getName())) {
									detail.image.title = parser.nextText();
								}
								if ("link".equals(parser.getName())) {
									detail.image.link = parser.nextText();
								}
							}

							if (eventType == XmlPullParser.END_TAG) {
								if ("image".equals(parser.getName())) {
									break;
								}
							}
							eventType = parser.next();
						}
					}

					if ("item".equals(parser.getName())) {
						item = new NewsItem();
						eventType = parser.next();
						while (true) {
							if (eventType == XmlPullParser.START_TAG) {
								if ("title".equals(parser.getName())) {
									item.title = parser.nextText().trim();
								}
								if ("link".equals(parser.getName())) {
									item.link = parser.nextText();
								}
								if ("description".equals(parser.getName())) {
									item.description = parser.nextText();
								}
								/*
								 * It looks like you are not on the START_TAG
								 * event when you call parser.nextText(). Check
								 * that you are on a START_TAG event when you
								 * call parser.nextText() with the
								 * parser.getEventType(). 解析时报错。
								 */
								if ("creator".equals(parser.getName())) {
									item.creator = parser.nextText();
								}
								if ("pubDate".equals(parser.getName())) {
									item.pubDate = parser.nextText();
								}
								if ("image".equals(parser.getName())) {
									item.image = parser.nextText();
								}
								if ("author".equals(parser.getName())) {
									item.author = parser.nextText();
								}
								if ("encoded".equals(parser.getName())) {
									item.encoded = parser.nextText();
								}
							}
							if (eventType == XmlPullParser.END_TAG) {
								if ("item".equals(parser.getName())) {
									detail.items.add(item);
									item = null;
									break;
								}
							}

							eventType = parser.next();
						}

					}
				

				eventType = parser.next();
			}

		} catch (XmlPullParserException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return detail;
	}
}
