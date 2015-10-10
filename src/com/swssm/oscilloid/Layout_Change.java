package com.swssm.oscilloid;

import android.graphics.*;
import android.view.*;
import android.widget.*;

public class Layout_Change {
	ButtonEvent btnevent;
	Variable vari;
	WaveView wv;
	
	Button Autoset_btn , Func2_btn;
	TextView CH_info,Freq_info , Last_frq_btn;
	
	public Layout_Change(Button Autoset_btn,TextView CH_info,TextView Freq_info,TextView Last_frq_btn,Button Func2_btn) {
		// TODO Auto-generated constructor stub
		this.Autoset_btn = Autoset_btn;
		this.CH_info = CH_info;
		this.Freq_info = Freq_info;
		this.Last_frq_btn = Last_frq_btn;
		this.Func2_btn = Func2_btn;
		
		btnevent = null;
		vari = null;
		wv = null;
	}
	public void DC_Layout() {
		Autoset_btn.setText("Auto Set");
		Autoset_btn.setTextColor(Color.BLACK);
		Autoset_btn.setTypeface(null,Typeface.NORMAL);
		
		CH_info.setText(vari.Volt_div_str[vari.Volt_div_cnt]);
		CH_info.setTextColor(Color.YELLOW);
		Freq_info.setText(vari.Tim_div_str[vari.Tim_div_cnt]);
		Last_frq_btn.setText("");
		vari.Mode_Choice = 0;
		
		vari.sleep_time = 75;
		
		wv.setLineColor(Color.YELLOW);
		wv.setStroke((float) 7.0);
	}
	public void AC_Layout() {
		Autoset_btn.setText("Auto Set");
		Autoset_btn.setTextColor(Color.BLACK);
		//Autoset_btn.setTypeface(null,Typeface.NORMAL);
		
		CH_info.setText(vari.Volt_div_str[vari.Volt_div_cnt]);
		CH_info.setTextColor(Color.YELLOW);
		Freq_info.setText(vari.Tim_div_str[vari.Tim_div_cnt]);
		Last_frq_btn.setText("");
		vari.Mode_Choice = 2;
		
		vari.sleep_time = 75;
		
		wv.setLineColor(Color.RED);
		wv.setStroke((float) 7.0);
	}
	public void DC_FFT_Layout() {
		Autoset_btn.setText("FFT Mode");
		Autoset_btn.setTextColor(Color.RED);
		//Autoset_btn.setTypeface(null,Typeface.BOLD);
		
		CH_info.setText("0 Hz");
		CH_info.setTextColor(Color.WHITE);
		Freq_info.setText(vari.CENTER_FFT_Tim_div_str[vari.Tim_div_cnt]);
		Freq_info.setTextColor(Color.WHITE);
		Last_frq_btn.setText(vari.FFT_Tim_div_str[vari.Tim_div_cnt]);
		Last_frq_btn.setTextColor(Color.WHITE);
		
		Func2_btn.setText("Scale");
		Func2_btn.setSelected(true);
		vari.pos_sca_check = true;
		
		vari.Mode_Choice = 1;
		
		vari.sleep_time = 150;
		
		wv.setLineColor(Color.GREEN);
		wv.setStroke((float) 3.0);
	}
	public void AC_FFT_Layout() {
		Autoset_btn.setText("FFT Mode");
		Autoset_btn.setTextColor(Color.RED);
		Autoset_btn.setTypeface(null,Typeface.BOLD);
		
		CH_info.setText("0 Hz");
		CH_info.setTextColor(Color.WHITE);
		Freq_info.setText(vari.CENTER_FFT_Tim_div_str[vari.Tim_div_cnt]);
		Freq_info.setTextColor(Color.WHITE);
		Last_frq_btn.setText(vari.FFT_Tim_div_str[vari.Tim_div_cnt]);
		Last_frq_btn.setTextColor(Color.WHITE);
		
		Func2_btn.setText("Scale");
		Func2_btn.setSelected(true);
		vari.pos_sca_check = true;
		
		vari.Mode_Choice = 3;
		
		wv.setLineColor(Color.GREEN);
		wv.setStroke((float) 3.0);
	}
	public void Layout_Mode_Choose(int inputFormat) {
		switch(inputFormat) {
		case 0:
		case 1:
			CH_info.setText(vari.Volt_div_str[vari.Volt_div_cnt]);
			Freq_info.setText(vari.Tim_div_str[vari.Tim_div_cnt]);
			Last_frq_btn.setText("");
			break;
		case 2:
		case 3:
			CH_info.setText("0 Hz");
			Freq_info.setText(vari.CENTER_FFT_Tim_div_str[vari.Tim_div_cnt]);
			Last_frq_btn.setText(vari.FFT_Tim_div_str[vari.Tim_div_cnt]);
			break;
		}
	}
	public boolean Layout_Mode_Func2(int inputFormat,View v) {
		switch(inputFormat) {
		case 0:
		case 1:
			vari.pos_sca_check = btnevent.Pos_Sca_Check(v);
			break;
		case 2:
		case 3:
			vari.pos_sca_check = true;
			break;
		}
		return vari.pos_sca_check;
	}
}
