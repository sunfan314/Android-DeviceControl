package com.jr.devicecontrol.ui;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.jr.devicecontrol.R;
import com.jr.devicecontrol.R.id;
import com.jr.devicecontrol.R.layout;
import com.jr.devicecontrol.R.menu;
import com.jr.devicecontrol.model.ActivityCollector;
import com.jr.devicecontrol.model.Status;
import com.jr.devicecontrol.util.BaseStatusAdapter;
import com.jr.devicecontrol.util.BasicConfig;
import com.jr.devicecontrol.util.ListViewForScrollView;
import com.jr.devicecontrol.util.LogInfoAdapter;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.ScrollView;

public class LogActivity extends BaseActivity {

	public static final int INIT_DATA = 0;
	public static final int UPDATE_DATA = 1;
	public static final int NET_ERROR = 2;

	private Toolbar toolbar;
	private ProgressBar progressBar;
	private ScrollView scrollView;
	private ListViewForScrollView logListView;
	private List<String> dataList = new ArrayList<String>();

	private String responseFromHttpRequest = "";// 存储从http请求中获取的xml数据

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INIT_DATA:// 为listview设置适配器
				setListViewAdapter();// 为listview设置适配器
				progressBar.setVisibility(View.GONE);
				scrollView.setVisibility(View.VISIBLE);
				new Thread(updateLog).start();
				break;
			case UPDATE_DATA:
				setListViewAdapter();
				break;
			case NET_ERROR:// 提示网络连接异常
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						LogActivity.this);
				dialog.setTitle("网络异常");
				dialog.setMessage("网络连接异常，是否重新启动应用");
				dialog.setCancelable(false);
				dialog.setPositiveButton("是",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								ActivityCollector.finishAll();
								Intent intent = new Intent(LogActivity.this,
										LogActivity.class);
								startActivity(intent);
							}
						});
				dialog.setNegativeButton("否",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
							}
						});
				dialog.show();
			default:
				break;
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		initToolbar();
		logListView = (ListViewForScrollView) findViewById(R.id.log_listview);
		progressBar = (ProgressBar) findViewById(R.id.progress_bar);
		scrollView = (ScrollView) findViewById(R.id.log_scrollview);
		sendRequest();
	}

	private void setListViewAdapter() {
		// TODO Auto-generated method stub
		// Log.d("daaaaaaa", "setAdapter");
		LogInfoAdapter log_adapter = new LogInfoAdapter(LogActivity.this,
				R.layout.log_item, dataList);
		logListView.setAdapter(log_adapter);
	}

	private void sendRequest() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String url = "http://" + getServerIP() + "/record.cgi";
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				httpGet.addHeader(new BasicHeader("Cookie",
						"ID=1234; status=log in"));
				try {
					HttpResponse httpResponse = httpClient.execute(httpGet);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = httpResponse.getEntity();
						responseFromHttpRequest = EntityUtils.toString(entity,
								"utf-8");
						// Log.d("data", responseFromHttpRequest);
						parseData(responseFromHttpRequest);// 解析获取的JSON数据
						Message msg = new Message();
						msg.what = INIT_DATA;
						handler.sendMessage(msg);

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Message message = new Message();
					message.what = NET_ERROR;
					handler.sendMessage(message);
					e.printStackTrace();
				}

			}

		}).start();
	}
	
	private void updateData() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String url = "http://" + getServerIP() + "/record.cgi";
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				httpGet.addHeader(new BasicHeader("Cookie",
						"ID=1234; status=log in"));
				try {
					HttpResponse httpResponse = httpClient.execute(httpGet);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = httpResponse.getEntity();
						responseFromHttpRequest = EntityUtils.toString(entity,
								"utf-8");
						// Log.d("data", responseFromHttpRequest);
						parseData(responseFromHttpRequest);// 解析获取的JSON数据
						Message msg = new Message();
						msg.what = UPDATE_DATA;
						handler.sendMessage(msg);

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}).start();
	}
	
	private Runnable updateLog =new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true){
				try {
					Thread.sleep(5*1000);
					updateData();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	private void initToolbar() {
		// TODO Auto-generated method stub
		toolbar = (Toolbar) findViewById(R.id.tl_custom);
		toolbar.setTitle("日志信息");// 设置Toolbar标题
		toolbar.setTitleTextColor(Color.parseColor("#ffffff")); // 设置标题颜色
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		getSupportActionBar().setHomeButtonEnabled(true); // 设置返回键可用
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	private void parseData(String responseFromHttpRequest) {// 从xml数据中获取日志信息
		// TODO Auto-generated method stub
		dataList.clear();
		int startIndex = responseFromHttpRequest.indexOf("<body>");
		int endIndex = responseFromHttpRequest.indexOf("</body>");
		String logInfo = responseFromHttpRequest.substring(startIndex + 6,
				endIndex);
		String[] logList = logInfo.split("<br />");
		for (String str : logList) {
			dataList.add(str);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log, menu);
		return false;
	}
	
	private String getServerIP(){
		String HOST="192.168.10.189";
		SharedPreferences preferences=getSharedPreferences("ip", MODE_PRIVATE);
		String serverIP=preferences.getString("server_ip", HOST);
		return serverIP;
	}

}
