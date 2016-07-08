package com.jr.devicecontrol.ui;

import com.jr.devicecontrol.R;
import com.jr.devicecontrol.R.id;
import com.jr.devicecontrol.R.layout;
import com.jr.devicecontrol.R.menu;
import com.jr.devicecontrol.util.IPUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RouteSettingActivity extends BaseActivity implements OnClickListener{
	
	private Toolbar toolbar;
	
	private TextView textView;
	
	private EditText editText;
	
	private Button submitButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_setting);
		initToolbar();
		textView=(TextView)findViewById(R.id.current_lan_textview);
		textView.setText(getServerIP());
		submitButton=(Button)findViewById(R.id.submit_button);
		submitButton.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		EditText editText=(EditText)findViewById(R.id.route_setting_edittext);
		String ipStr=editText.getText().toString();
		if(!IPUtil.isIP(ipStr)){
			AlertDialog.Builder dialog=new AlertDialog.Builder(RouteSettingActivity.this);
			dialog.setTitle("警告");
			dialog.setMessage("ip地址格式不正确，请输入正确的ip地址");
			dialog.setCancelable(false);
			dialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
				}
			});
			dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub					
				}
			});
			dialog.show();
		}else{
			SharedPreferences.Editor editor=getSharedPreferences("ip", MODE_PRIVATE).edit();
			editor.putString("server_ip", ipStr);
			editor.commit();
			textView.setText(ipStr);
			Toast.makeText(RouteSettingActivity.this, "服务器地址设置成功！", Toast.LENGTH_SHORT).show();
			
		}
		
	}
	
	private void initToolbar() {
		// TODO Auto-generated method stub
		toolbar = (Toolbar) findViewById(R.id.tl_custom);
		toolbar.setTitle("路由设置");// 设置Toolbar标题
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
	
	private String getServerIP(){
		String HOST="192.168.10.189";
		SharedPreferences preferences=getSharedPreferences("ip", MODE_PRIVATE);
		String serverIP=preferences.getString("server_ip", HOST);
		return serverIP;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.route_setting, menu);
		return false;
	}


}
