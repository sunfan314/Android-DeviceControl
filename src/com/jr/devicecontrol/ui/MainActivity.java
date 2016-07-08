package com.jr.devicecontrol.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.jr.devicecontrol.model.ActivityCollector;
import com.jr.devicecontrol.R;
import com.jr.devicecontrol.R.id;
import com.jr.devicecontrol.R.layout;
import com.jr.devicecontrol.R.menu;
import com.jr.devicecontrol.model.Status;
import com.jr.devicecontrol.util.BaseStatusAdapter;
import com.jr.devicecontrol.util.BasicConfig;
import com.jr.devicecontrol.util.ListViewForScrollView;

import android.R.integer;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;

public class MainActivity extends BaseActivity implements OnItemClickListener {
	private final int GET_DATA=0;

	/*
	 * toolbar和drawerlayout中的组件
	 */
	private Toolbar toolbar;
	private DrawerLayout mDrawerLayout;
	private boolean drawerOpen = false;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView lvLeftMenu;
	private String[] lvs = { "统计信息", "日志信息", "路由设置" };
	private ArrayAdapter arrayAdapter;

	private String responseFromHttpRequest = "";// 存储从http请求中获取的json数据
	private ListViewForScrollView netListView;
	private ListViewForScrollView baseBandListView;
	private ListViewForScrollView radioFrequencyListView;
	private ListViewForScrollView ethernetListView;
	private ListViewForScrollView httpListView;
	private ListViewForScrollView snmpListView;
	private List<Status> netStatusList = new ArrayList<Status>();
	private List<Status> baseBandStatusList = new ArrayList<Status>();
	private List<Status> radioFrequencyStatusList = new ArrayList<Status>();
	private List<Status> ethernetStatusList = new ArrayList<Status>();
	private List<Status> httpStatusList = new ArrayList<Status>();
	private List<Status> snmpStatusList = new ArrayList<Status>();

	private ProgressBar progressBar;
	private ScrollView scrollView;

	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initToolbarDrawerLayout();// 初始化toolbar和drawerlayout
		getViews();// 获取布局中的控件
		new Thread(getDataRunnable).start();

	}

	@Override
	protected void onPostResume() {
		// TODO Auto-generated method stub
		super.onPostResume();
		if (drawerOpen) {// 如果抽屉打开则关闭
			mDrawerLayout.closeDrawers();
		}

	}

	/*
	 * 初始化工具栏和左侧的抽屉
	 */
	private void initToolbarDrawerLayout() {
		// TODO Auto-generated method stub
		toolbar = (Toolbar) findViewById(R.id.tl_custom);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_left);
		lvLeftMenu = (ListView) findViewById(R.id.lv_left_menu);
		toolbar.setTitle("设备状态");// 设置Toolbar标题
		toolbar.setTitleTextColor(Color.parseColor("#ffffff")); // 设置标题颜色
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true); // 设置返回键可用
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// 创建返回键，并实现打开关/闭监听
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
				R.string.open, R.string.close) {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				drawerOpen = true;
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				drawerOpen = false;
			}
		};
		mDrawerToggle.syncState();
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		// 设置菜单列表
		arrayAdapter = new ArrayAdapter(this,
				android.R.layout.simple_list_item_1, lvs);
		lvLeftMenu.setAdapter(arrayAdapter);
		lvLeftMenu.setOnItemClickListener(this);
	}

	/*
	 * 获取布局中的控件
	 */
	private void getViews() {
		// TODO Auto-generated method stub
		netListView = (ListViewForScrollView) findViewById(R.id.net_status_listView);
		baseBandListView = (ListViewForScrollView) findViewById(R.id.baseBand_status_listView);
		radioFrequencyListView = (ListViewForScrollView) findViewById(R.id.radioFrequency_status_listView);
		ethernetListView = (ListViewForScrollView) findViewById(R.id.ethernet_status_listView);
		httpListView = (ListViewForScrollView) findViewById(R.id.http_status_listView);
		snmpListView = (ListViewForScrollView) findViewById(R.id.snmp_status_listView);
		progressBar = (ProgressBar) findViewById(R.id.progress_bar);
		scrollView = (ScrollView) findViewById(R.id.scrollview);
	}

	private String getServerIP() {
		String HOST = "192.168.10.189";
		SharedPreferences preferences = getSharedPreferences("ip", MODE_PRIVATE);
		String serverIP = preferences.getString("server_ip", HOST);
		return serverIP;
	}

	private String removeLastComma(String str) {// 删json字符串中最后多余的逗号
		String tmp = str.substring(0, str.length() - 2);
		String result = tmp + "}";
		return result;
	}

	private void setListViewAdapter() {
		// TODO Auto-generated method stub
		BaseStatusAdapter netStatus_adapter = new BaseStatusAdapter(
				MainActivity.this, R.layout.status_item, netStatusList);
		BaseStatusAdapter baseBandStatus_adapter = new BaseStatusAdapter(
				MainActivity.this, R.layout.status_item, baseBandStatusList);
		BaseStatusAdapter radioFrequencyStatus_adapter = new BaseStatusAdapter(
				MainActivity.this, R.layout.status_item,
				radioFrequencyStatusList);
		BaseStatusAdapter ethernetStatus_adapter = new BaseStatusAdapter(
				MainActivity.this, R.layout.status_item, ethernetStatusList);
		BaseStatusAdapter httpStatus_adapter = new BaseStatusAdapter(
				MainActivity.this, R.layout.status_item, httpStatusList);
		BaseStatusAdapter snmpStatus_adapter = new BaseStatusAdapter(
				MainActivity.this, R.layout.status_item, snmpStatusList);
		netListView.setAdapter(netStatus_adapter);
		baseBandListView.setAdapter(baseBandStatus_adapter);
		radioFrequencyListView.setAdapter(radioFrequencyStatus_adapter);
		ethernetListView.setAdapter(ethernetStatus_adapter);
		httpListView.setAdapter(httpStatus_adapter);
		snmpListView.setAdapter(snmpStatus_adapter);
	}

	private void parseJSON(String jsonStr) {// 从JSONObject中取出数据放入List中
		netStatusList.clear();
		baseBandStatusList.clear();
		radioFrequencyStatusList.clear();
		ethernetStatusList.clear();
		httpStatusList.clear();
		snmpStatusList.clear();
		try {
			JSONObject jsonObject = new JSONObject(jsonStr);
			netStatusList.add(new Status("IP地址", jsonObject
					.getString("ip_address")));
			netStatusList.add(new Status("子网掩码", jsonObject
					.getString("subnet_mask")));
			netStatusList.add(new Status("默认网关", jsonObject
					.getString("default_gateway")));
			netStatusList.add(new Status("MAC地址", jsonObject
					.getString("mac_address")));

			String link_status = "未知";
			if (jsonObject.getString("link_status").equals("Link Down")) {
				link_status = "断开";
			} else if (jsonObject.getString("link_status").equals("Link UP")) {
				link_status = "连接";
			}
			baseBandStatusList.add(new Status("链路状态", link_status));
			baseBandStatusList.add(new Status("LDBC错误数", jsonObject
					.getString("radio_temp")));

			String txpll = "未知";
			if (jsonObject.getString("txpll").equals("on")) {
				txpll = "正常";
			} else if (jsonObject.getString("txpll").equals("off")) {
				txpll = "异常";
			}
			String rxpll = "未知";
			if (jsonObject.getString("rxpll").equals("on")) {
				rxpll = "正常";
			} else if (jsonObject.getString("rxpll").equals("off")) {
				rxpll = "异常";
			}
			radioFrequencyStatusList.add(new Status("发送频率", jsonObject
					.getString("txfreq") + "MHz"));
			radioFrequencyStatusList.add(new Status("发送功率", jsonObject
					.getString("txpwr") + "dBm"));
			radioFrequencyStatusList.add(new Status("调制模式", jsonObject
					.getString("txmode")));
			radioFrequencyStatusList.add(new Status("发送机状态", txpll));
			radioFrequencyStatusList.add(new Status(" ", " "));
			radioFrequencyStatusList.add(new Status("接收频率", jsonObject
					.getString("rxfreq") + "MHz"));
			radioFrequencyStatusList.add(new Status("接收功率", jsonObject
					.getString("rxpwr") + "dBm"));
			radioFrequencyStatusList.add(new Status("解调模式", jsonObject
					.getString("rxmode")));
			radioFrequencyStatusList.add(new Status("LNA", jsonObject
					.getString("lna")));
			radioFrequencyStatusList.add(new Status("VGA", jsonObject
					.getString("vga")));
			radioFrequencyStatusList.add(new Status("接收机状态", rxpll));
			radioFrequencyStatusList.add(new Status("接收信号能量", jsonObject
					.getString("rxrecpwr") + "dBm"));
			radioFrequencyStatusList.add(new Status("信号信噪比", jsonObject
					.getString("snr")));

			String DPAN = "未知";
			if (jsonObject.getString("DPAN").equals("enable")) {
				DPAN = "开启";
			} else if (jsonObject.getString("DPAN").equals("disable")) {
				DPAN = "关闭";
			}
			String DPAS = "未知";
			if (jsonObject.getString("DPAS").equals("enable")) {
				DPAS = "开启";
			} else if (jsonObject.getString("DPAS").equals("disable")) {
				DPAS = "关闭";
			}
			String DPS;
			if (jsonObject.getString("DPS").equals("100Mbps-Duplex")) {
				DPS = "100Mbps 全双工";
			} else {
				DPS = "1000Mbps 全双工";
			}
			String MDTM = "未知";
			if (jsonObject.getString("MDTM").equals("In-Band")) {
				MDTM = "带内";
			}
			ethernetStatusList.add(new Status("数据端口自协商状态", DPAN));
			ethernetStatusList.add(new Status("数据端口自动翻转状态", DPAS));
			ethernetStatusList.add(new Status("数据端口状态", DPS));
			ethernetStatusList.add(new Status("管理数据传送模式", MDTM));

			String login_auth = "未知";
			if (jsonObject.getString("login_auth").equals("enable")) {
				login_auth = "开启";
			} else if (jsonObject.getString("login_auth").equals("disable")) {
				login_auth = "关闭";
			}
			httpStatusList.add(new Status("端口", jsonObject
					.getString("http_port")));
			httpStatusList.add(new Status("登录验证", login_auth));

			String trap_event = "未知";
			if (jsonObject.getString("trap_event").equals("enable")) {
				trap_event = "使能";
			} else if (jsonObject.getString("trap_event").equals("disable")) {
				trap_event = "禁用";
			}
			snmpStatusList.add(new Status("SNMP协议端口号", jsonObject
					.getString("snmp_port")));
			snmpStatusList.add(new Status("SNMP协议共同体", jsonObject
					.getString("snmp_community")));
			snmpStatusList.add(new Status("Trap版本号", jsonObject
					.getString("trap_version")));
			snmpStatusList.add(new Status("Trap事件", trap_event));
			snmpStatusList.add(new Status("Trap IP地址", jsonObject
					.getString("trap_ip")));
			snmpStatusList.add(new Status("Trap端口号", jsonObject
					.getString("trap_port")));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}

	/*
	 * 侧滑菜单点击事件的实现
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (arg2) {
		case 0:
			intent = new Intent(MainActivity.this, StatisticsActivity.class);
			startActivity(intent);
			break;
		case 1:
			intent = new Intent(MainActivity.this, LogActivity.class);
			startActivity(intent);
			break;
		case 2:
			intent = new Intent(MainActivity.this, RouteSettingActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}

	}

	private Runnable getDataRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String url = "http://" + getServerIP()
					+ "/getstatus.cgi?getstatus=getstatus&random="
					+ String.valueOf(Math.random());
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader(new BasicHeader("Cookie",
					"ID=1234; status=log in"));
			try {
				HttpResponse httpResponse = httpClient.execute(httpGet);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = httpResponse.getEntity();
					responseFromHttpRequest = EntityUtils.toString(entity);
					responseFromHttpRequest = removeLastComma(responseFromHttpRequest);
					Log.d("ss", "ss");
					Log.d("data", responseFromHttpRequest);
					parseJSON(responseFromHttpRequest);// 解析获取的JSON数据
					handler.postDelayed(initListViewRunnable, 1000);

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				handler.post(netErrorRunnable);
				e.printStackTrace();
			}
		}
	};

	private Runnable updateDataRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String url = "http://" + getServerIP()
					+ "/getstatus.cgi?getstatus=getstatus&random="
					+ String.valueOf(Math.random());
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader(new BasicHeader("Cookie",
					"ID=1234; status=log in"));
			try {
				HttpResponse httpResponse = httpClient.execute(httpGet);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = httpResponse.getEntity();
					responseFromHttpRequest = EntityUtils.toString(entity);
					responseFromHttpRequest = removeLastComma(responseFromHttpRequest);
					parseJSON(responseFromHttpRequest);// 解析获取的JSON数据
					handler.postDelayed(updateListViewRunnable, 2000);

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private Runnable initListViewRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			setListViewAdapter();
			progressBar.setVisibility(View.GONE);
			scrollView.setVisibility(View.VISIBLE);
			new Thread(updateDataRunnable).start();
		}
	};

	private Runnable updateListViewRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			setListViewAdapter();
			new Thread(updateDataRunnable).start();
		}
	};

	private Runnable netErrorRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					MainActivity.this);
			dialog.setTitle("网络异常");
			dialog.setMessage("网络连接异常，是否重新启动应用");
			dialog.setCancelable(false);
			dialog.setPositiveButton("是",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							ActivityCollector.finishAll();
							Intent intent = new Intent(MainActivity.this,
									MainActivity.class);
							startActivity(intent);
						}
					});
			dialog.setNegativeButton("否",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					});
			dialog.show();
		}
	};
}
