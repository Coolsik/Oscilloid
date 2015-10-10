package com.swssm.oscilloid;

import android.view.*;
import android.widget.*;

public class ButtonEvent {
	public static boolean check_run(View v) {
		WaveView wv = null;
		boolean Run_Stop_flag;
		
		if(((Button)v).getText().toString().equals("Run")) {
			((Button)v).setText("Stop");
			((Button)v).setSelected(false);
			Run_Stop_flag = false;
		} else {
			((Button)v).setText("Run");
			((Button)v).setSelected(true);
			Run_Stop_flag = true;
		}
		return Run_Stop_flag;
	}
	public static boolean Hor_Ver_Check(View v) {
		if(((Button)v).getText().toString().equals("Vertical")) {
			((Button)v).setText("Horizontal");
			((Button)v).setSelected(false);
			return false;
		} else {
			((Button)v).setText("Vertical");
			((Button)v).setSelected(true);
			return true;
		}
	}
	public static boolean Pos_Sca_Check(View v) {
		if(((Button)v).getText().toString().equals("Scale")) {
			((Button)v).setText("Position");
			((Button)v).setSelected(false);
			return false;
		} else {
			((Button)v).setText("Scale");
			((Button)v).setSelected(true);
			return true;
		}
	}
	public static boolean Trigger_Check(View v) {
		if(((Button)v).getText().toString().equals(" Trigger ")) {
			((Button)v).setText("Trigger");
			((Button)v).setSelected(false);
			return false;
		} else {
			((Button)v).setText(" Trigger ");
			((Button)v).setSelected(true);
			return true;
		}
	}
	public static int Vertical_Position_Up(int up) {
		Variable vari = null;
		
		vari.Vertical_move += up;
		
		//Stop_Move();
		//Stop_Move2();
		
		return up;
	}
	public static int Vertical_Position_Down(int down) {
		Variable vari = null;
		vari.Vertical_move -= down;
		
		//Stop_Move();
		//Stop_Move2();
		
		return -down;
	}
	public static int Horizontal_Position_Up(int up) {
		Variable vari = null;
		WaveView wv = null;
		vari.Horizontal_move += up;
		vari.Center_position+=up;
		
		if(vari.Horizontal_move > 1180) vari.Horizontal_move = 1180;
		
    	if(vari.Center_position > 1150) wv.image2_x = 1150;
    	else if(vari.Center_position < 0) wv.image2_x = 0;
    	else wv.image2_x = vari.Center_position;
    	wv.image2_x = (1150 - WaveView.image2_x);
		
		return up;
	}
	public static int Horizontal_Position_Down(int down) {
		Variable vari = null;
		WaveView wv = null;
		vari.Horizontal_move -= down;
		vari.Center_position-=down;

		if(vari.Horizontal_move < 20) vari.Horizontal_move = 20;
		// Stop Move
		//Stop_Move();
		//Stop_Move2();
		
    	if(vari.Center_position > 1150) wv.image2_x = 1150;
    	else if(vari.Center_position < 0) wv.image2_x = 0;
    	else wv.image2_x = vari.Center_position;
    	wv.image2_x = (1150 - WaveView.image2_x);
    	
		return down;
	}
	/*
	public static void Stop_Move() {
		WaveView wv = null;
		// Stop Move
		draw_max = (wv.WIDTH+MainActivity.Horizontal_move);
		if(MainActivity.Run_Stop_flag == false) {
			for(int k=MainActivity.Horizontal_move, p=0 ; k<draw_max ; k++,p++) {
				if(draw_max > 2400) draw_max = 2400;
				else if(draw_max < 1200) draw_max = 1200;
	    	wv.ch1_inputdata[p] = MainActivity.inputdraw[k] + MainActivity.Vertical_move;
		  	}
			wv.set_data(wv.ch1_inputdata);
		}
	}*/
	public static int Stop_Move2(int add) {
		WaveView wv = null;
		Variable vari = null;
		
		vari.Vertical_move += add;
		
		vari.draw_max = (wv.WIDTH+vari.Horizontal_move);
		for(int k=vari.Horizontal_move, p=0 ; k<vari.draw_max ; k++,p++) {
			if(vari.draw_max > 2400) vari.draw_max = 2400;
			else if(vari.draw_max < 1200) vari.draw_max = 1200;
    		wv.ch1_inputdata[p] = vari.inputdraw[k] + vari.Vertical_move;
	  	}
		wv.set_data(wv.ch1_inputdata);
		/*
		WaveView wv = null;
		draw_max = (wv.WIDTH+MainActivity.Horizontal_move);
		for(int k=MainActivity.Horizontal_move, p=0 ; k<draw_max ; k++,p++) {
			if(draw_max > 2400) draw_max = 2400;
			else if(draw_max < 1200) draw_max = 1200;
			wv.ch1_inputdata[p] += add;
	  	}
		wv.set_data(wv.ch1_inputdata);*/
		return add;
	}
	public static int Stop_Move3(int add) {
		WaveView wv = null;
		Variable vari = null;
		
		vari.Horizontal_move += add;
		vari.Center_position += add;
		
		if(vari.Horizontal_move > 1180) vari.Horizontal_move = 1180;
		else if(vari.Horizontal_move < 20) vari.Horizontal_move = 20;
		
		if(vari.Center_position > 1150) vari.Center_position=wv.image2_x = 1150;
    	else if(vari.Center_position < 0) vari.Center_position=wv.image2_x = 0;
    	else wv.image2_x = vari.Center_position;
    	wv.image2_x = (1150 - WaveView.image2_x);
    	
    	vari.draw_max = (wv.WIDTH+vari.Horizontal_move);
		for(int k=vari.Horizontal_move, p=0 ; k<vari.draw_max ; k++,p++) {
			//if(draw_max > 2400) draw_max = 2400;
			//else if(draw_max < 1200) draw_max = 1200;
    		wv.ch1_inputdata[p] = vari.inputdraw[k] + vari.Vertical_move;
	  	}
		wv.set_data(wv.ch1_inputdata);
		return add;
	}
}