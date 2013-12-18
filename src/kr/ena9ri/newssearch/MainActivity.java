package kr.ena9ri.newssearch;

import java.io.*;
import java.net.*;
import java.util.*;

import org.xmlpull.v1.*;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.google.ads.*;

public class MainActivity extends Activity
{
	private EditText editSearch;
	private Button btnSearch;
	private ListView listNews;
	private TextView tvSearch;
	private ArrayAdapter<NewsBean> adapter;
	private ArrayList<NewsBean> newsList = new ArrayList<NewsBean>();
	private LinearLayout layout;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		editSearch = (EditText) findViewById(R.id.edit_search);
		btnSearch = (Button) findViewById(R.id.btn_search);
		listNews = (ListView) findViewById(R.id.list_news);
		tvSearch = (TextView) findViewById(R.id.tv_search);
		
		new NewAsyncTask().execute("");
		
		adapter = new ArrayAdapter<NewsBean>(MainActivity.this, android.R.layout.simple_list_item_1, newsList);
		listNews.setAdapter(adapter);
		listNews.setOnItemClickListener(new OnItemClickListener()
		{
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				String link = null;
				link = ((NewsBean) listNews.getAdapter().getItem(position)).getLink();
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
				startActivity(intent);
			}
		});
		btnSearch.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				tvSearch.setVisibility(View.INVISIBLE);
				listNews.setVisibility(View.VISIBLE);
				adapter.clear();
				new MyAsyncTask().execute(editSearch.getText().toString());
			}
		});
	}
	
	public class NewAsyncTask extends AsyncTask<String, String, String>
	{
		@Override
		protected String doInBackground(String... params)
		{
			String rank = null;
			try
			{
				Log.w("MainActivity", "AsyncTask");
				URL searchlink = new URL("http://openapi.naver.com/search?key=d1d43c0ae82a4f256007d539105c4944&query=nexearch&target=rank");
				XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
				XmlPullParser parser = parserCreator.newPullParser();
				
				parser.setInput(searchlink.openStream(), null);
				
				int parseEvent = parser.getEventType();
				int count = 1;
				boolean inItem = false, inK = false;
				while (parseEvent != XmlPullParser.END_DOCUMENT)
				{
					switch (parseEvent)
					{
						case XmlPullParser.START_TAG://2
							if (parser.getName().contains("R"))
								inItem = true;
							if (parser.getName().equals("K"))
								inK = true;
							break;
						
						case XmlPullParser.TEXT://4
							if (inItem)
							{
								if (inK)
								{
									if (!TextUtils.isEmpty(parser.getText()))
										rank = parser.getText();
									Log.w("MainActivity", " " + rank);
									inK = false;
								}
							}
							break;
						case XmlPullParser.END_TAG://3
							if (parser.getName().contains("R"))
							{
								inItem = false;
								inK = false;
								tvSearch.setText(tvSearch.getText() + "\n" + count + "." + rank);
								count++;
							}
							break;
					
					}
					parseEvent = parser.next();
					
				}
				
			} catch (Exception e)
			{
				Log.w("NET", "Parsing Failed! " + e.toString());
			}
			return rank;
		}
		
		
		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
		}
	}
	
	public class MyAsyncTask extends AsyncTask<String, String, ArrayList<NewsBean>>
	{
		@Override
		protected ArrayList<NewsBean> doInBackground(String... params)
		{
			newsList = new ArrayList<NewsBean>();
			String searchText = "";
			try
			{
				Log.w("NET", "Parsing..." + params[0]);
				searchText = URLEncoder.encode(params[0], "UTF-8");
			} catch (UnsupportedEncodingException e1)
			{
				e1.printStackTrace();
			}
			
			try
			{
				
				URL text = new URL("http://openapi.naver.com/search?key=" + "d1d43c0ae82a4f256007d539105c4944" + "&query=" + searchText + "&display=50" + "&start=1&target=news");
				XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
				XmlPullParser parser = parserCreator.newPullParser();
				
				parser.setInput(text.openStream(), null);
				
				int parseEvent = parser.getEventType();
				boolean inItem = false, inTitle = false, inLink = false;
				String title = null, link = null;
				
				while (parseEvent != XmlPullParser.END_DOCUMENT)
				{
					switch (parseEvent)
					{
						case XmlPullParser.START_TAG:
							if (parser.getName().equals("item"))
								inItem = true;
							else if (parser.getName().equals("title"))
								inTitle = true;
							else if (parser.getName().equals("originallink"))
								inLink = true;
							break;
						case XmlPullParser.TEXT:
							if (inItem)
							{
								if (inTitle)
								{
									if (!TextUtils.isEmpty(parser.getText()))
										title = parser.getText();
									inTitle = false;
								}
								else if (inLink)
								{
									if (!TextUtils.isEmpty(parser.getText()))
										link = parser.getText();
									inLink = false;
								}
							}
							break;
						case XmlPullParser.END_TAG:
							if (parser.getName().equals("item"))
							{
								inItem = false;
								inLink = false;
								NewsBean bean = new NewsBean();
								bean.setTitle(title);
								bean.setLink(link);
								newsList.add(bean);
							}
							break;
					}
//					Log.w("NaverParser | GetNewsBean()", "content : " + title);
					parseEvent = parser.next();
				}
				Log.w("NET", "End...");
			} catch (Exception e)
			{
				Log.w("NET", "Parsing Failed! " + e.toString());
			}
			return newsList;
		}
		
		
		@Override
		protected void onPostExecute(ArrayList<NewsBean> result)
		{
			super.onPostExecute(result);
			adapter.addAll(newsList);
			
		}
	}
	
}
