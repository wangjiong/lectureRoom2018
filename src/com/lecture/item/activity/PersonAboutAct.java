package com.lecture.item.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class PersonAboutAct extends Activity {
	String s = "\n    1.本产品仅供学习交流，严禁用于商业用途\n\n"+
               "    2.我的联系方式995204127@163.com，如果发现bug或有好的建议可以邮箱联系我\n\n"+
               "    3.若网络状态不好或播放不流畅，建议先下载后再观看\n\n"
			;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		TextView t = new TextView(this);
		t.setTextIsSelectable(true);
		t.setTextSize(17);
		t.setText(s);
		setContentView(t);
	}
}
