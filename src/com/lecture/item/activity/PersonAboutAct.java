package com.lecture.item.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class PersonAboutAct extends Activity {
	String s = "\n    1.����Ʒ����ѧϰ�������Ͻ�������ҵ��;\n\n"+
               "    2.�ҵ���ϵ��ʽ995204127@163.com���������bug���кõĽ������������ϵ��\n\n"+
               "    3.������״̬���û򲥷Ų����������������غ��ٹۿ�\n\n"
			;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO �Զ����ɵķ������
		super.onCreate(savedInstanceState);
		TextView t = new TextView(this);
		t.setTextIsSelectable(true);
		t.setTextSize(17);
		t.setText(s);
		setContentView(t);
	}
}
