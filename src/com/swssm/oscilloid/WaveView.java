package com.swssm.oscilloid;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class WaveView extends SurfaceView implements SurfaceHolder.Callback {
	private WaveformThread plot_thread;
	private Variable vari = null;
	private final String TAG = "Test";
	private float MainLineStroke = (float) 7.0;
	private float LineStroke = (float) 7.0;
	private int x=300;
	private int y=300;
	private int Current_dis,Last_Dis;
	private boolean Zoom_Check = false;
	
	TextView CH_info;
	TextView Freq_info;
	
	private int image_x = 1155;
	public static int image_y = 460;
	public static int image2_x = 575;
	private int image2_y = 0;
	
	public static int WIDTH;
	public static int HEIGHT;
	public static int cnt;

	Bitmap h_bitmap,resize_h_Bitmap;
	Bitmap w_bitmap,resize_w_Bitmap;
	
	ButtonEvent btnevent = null;
	
	boolean flag=false,imgflag=false;
	static int tmp_y;
	int tmp_x;
	int sum=0;
	int comp_y;
	int comp_x;
	int sub_y;
	int sub_x;
	
	static int[] ch1_data = new int[1200];
	static int[] ch1_inputdata = new int[3000];
	private static int ch1_val = 485;
	
	public static Paint ch1_color = new Paint();
	private Paint grid_paint = new Paint();
	private Paint cross_paint = new Paint();
	private Paint outline_paint = new Paint();
	
	public WaveView(Context context,AttributeSet attr) {
		super(context,attr);
		getHolder().addCallback(this);
		
		CH_info = (TextView) findViewById(R.id.ch_info);
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		WIDTH = 1200;
		HEIGHT = (int)(metrics.heightPixels * 0.9);
		
		for(int i=0;i<1200;i++) {
			ch1_data[i] = ch1_val;
			ch1_inputdata[i] = ch1_val;
		}
		//
		for(int i=1200;i<3000;i++) ch1_inputdata[i] = ch1_val;
		//
		plot_thread = new WaveformThread(getHolder(), this);

		ch1_color.setColor(Color.YELLOW);
		ch1_color.setStrokeWidth(MainLineStroke);
		grid_paint.setColor(Color.rgb(100, 100, 100));
		grid_paint.setStrokeWidth(LineStroke);
		cross_paint.setColor(Color.rgb(70, 100, 70));
		cross_paint.setStrokeWidth(LineStroke);
		outline_paint.setColor(Color.DKGRAY);
		outline_paint.setStrokeWidth(LineStroke);
	}
	public static void setStroke(float stroke){
		ch1_color.setStrokeWidth(stroke);
	}
	public static void setLineColor(int color){
		ch1_color.setColor(color);
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		BitmapFactory.Options option = new BitmapFactory.Options();
		option.inSampleSize = 5;
		option.inPurgeable = true;
		option.inDither = true;
		
		h_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.height_center_triangle,option);
		resize_h_Bitmap = h_bitmap.createScaledBitmap(h_bitmap, 50,50, true);
		w_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.width_center_triangle2,option);
		resize_w_Bitmap = w_bitmap.createScaledBitmap(w_bitmap, 50,40, true);
		
		
		plot_thread.setRunning(true);
		plot_thread.start();
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) throws NullPointerException {
		boolean retry = true;
		plot_thread.setRunning(false);
		while(retry) {
			try {
				plot_thread.join();
				retry = false;
			} catch(InterruptedException e) {
				Log.d(TAG,"InterruptException");
			} catch(Exception e) {
				Log.d(TAG,"Exception");
			}
		}
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int keyAction = event.getAction();
		x = (int)event.getX(0);
		y = (int)event.getY(0);
		
		switch(keyAction & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if(x > 1100 && x < 1250) {
				imgflag = true;
				image_y = y-resize_h_Bitmap.getHeight()/2;	
			} else imgflag=false;
			
			int max=0,min=1920;
			for(int i=0;i<WIDTH;i++) {
				if(ch1_data[i] < min) min=ch1_data[i];
				if(ch1_data[i] > max) max=ch1_data[i];
			}
			if(max > 1100) max=1100;
			if(y >= min && y <= max && x < 1100) {
				flag=true;
				tmp_y = y;
				tmp_x = x;
			}
			else flag=false;
			sum=0;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			Current_dis = Calc_distance(event);
			Last_Dis = Calc_distance(event);
			flag = false;
			break;
		case MotionEvent.ACTION_MOVE:
			

			if(vari.Mode_Choice == 1 || vari.Mode_Choice == 3) {
				Log.d("Test","FFT Mode");
			} else {
				if(vari.pos_sca_check) Zoom_check(event);
				if(imgflag) {
					image_y = y-resize_h_Bitmap.getHeight()/2;
					if(image_y > 920) image_y = 920;
					if(image_y < 0) image_y = 0;
					vari.Trriger_val = (int) (255 - image_y / 3.6);
					vari.Trriger_val -= (int)(vari.Vertical_move / 3.8);
				}
				// Horizontal 체크해야함
				if((Zoom_Check==false) && flag) {
					if(vari.hor_ver_check) {
						// vertical
						Calc_Onetouch_distance();
						vari.Trriger_val = (int) (255 - image_y / 3.6);
						vari.Trriger_val -= (int)(vari.Vertical_move / 3.8);
					} else {
						// horizontal
						Calc_Onetouch_distance2();
					}
				}
				// Zoom Check
			}
			break;
		}
		return true;
	}
	public void onDraw(Canvas canvas) {
		PlotPoints(canvas);
	}
	public static void set_data(int[] data) {
		for(int x=0;x<WIDTH;x++) {
			ch1_data[x] = HEIGHT - data[x] + 1;
		}
	}
	public void PlotPoints(Canvas canvas) {
		canvas.drawColor(Color.rgb(20, 20, 20));
		
		// draw vertical grids
	    for(int vertical = 1; vertical<10; vertical++){
	    	canvas.drawLine(vertical*(WIDTH/10)+1, 1,
	    					vertical*(WIDTH/10)+1, HEIGHT+1,
	    					grid_paint);
	    }
	    // draw horizontal grids
	    for(int horizontal = 1; horizontal<10; horizontal++){
	    	canvas.drawLine(1, horizontal*(HEIGHT/10)+1,
	    					WIDTH+1, horizontal*(HEIGHT/10)+1,
	    					grid_paint);
	    }
	    // draw center cross
	 	canvas.drawLine(0, (HEIGHT/2)+1, WIDTH+1, (HEIGHT/2)+1, cross_paint);
	 	canvas.drawLine((WIDTH/2)+1, 0, (WIDTH/2)+1, HEIGHT+1, cross_paint);
	    
	    // draw outline
 		canvas.drawLine(0, 0, (WIDTH+1), 0, outline_paint);	// top
 		canvas.drawLine((WIDTH+1), 0, (WIDTH+1), (HEIGHT+1), outline_paint); //right
 		canvas.drawLine(0, (HEIGHT+1), (WIDTH+1), (HEIGHT+1), outline_paint); // bottom
 		canvas.drawLine(0, 0, 0, (HEIGHT+1), outline_paint); //left
 		
 		canvas.drawBitmap(resize_h_Bitmap,image_x,image_y,null);
 		canvas.drawBitmap(resize_w_Bitmap,image2_x ,image2_y ,null);
 		
 		for(int x=0;x<(WIDTH-1);x++) {
 			canvas.drawLine(x+1, ch1_data[x], x+2, ch1_data[x+1], ch1_color);
 		}
	}
	public void Calc_Onetouch_distance() {
		int add_val = 15;
		int y_position = tmp_y - y;
		int move_check = (y_position + sum*-1);
		
		if(move_check < 15 && move_check > -15) add_val = 0;
		else add_val =15;
		
		if(y_position > sum) {
			sum += btnevent.Stop_Move2(add_val);
		} else if(y_position < sum) {
			sum += btnevent.Stop_Move2(add_val*-1);
		}
	}
	public void Calc_Onetouch_distance2() {
		int add_val = 7;
		int x_position = tmp_x - x;
		int move_check = (x_position + sum*-1);
		
		if(move_check < 7 && move_check > -7) add_val = 0;
		else add_val = 7;
		
		if(x_position > sum) {
			sum += btnevent.Stop_Move3(add_val);
		} else if(x_position < sum) {
			sum += btnevent.Stop_Move3(add_val*-1);
		}
	}
	public static int Calc_distance(MotionEvent event) {
		float x_position = event.getX(0) - event.getX(1);
		float y_position = event.getY(0) - event.getY(1);
		return (int)Math.sqrt(x_position*x_position + y_position + y_position);
	}
	public void Zoom_check(MotionEvent event) {
		if(event.getPointerCount() >= 2) {
			Current_dis = Calc_distance(event);
			Zoom_Check = false;
			if(Current_dis - Last_Dis > 160) {
				Zoom_Check = true;
				//Log.d("Test","Zoom in");
				if(vari.hor_ver_check) {
					//vertical down
					vari.Volt_div_cnt--;
					if(vari.Volt_div_cnt < 0) vari.Volt_div_cnt = 0;
					vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);
					CH_info = (TextView) ((Activity)getContext()).findViewById(R.id.ch_info);
					CH_info.setText(vari.Volt_div_str[vari.Volt_div_cnt]);
				} else {
					//horizontal down
					vari.Tim_div_cnt--;
					if(vari.Tim_div_cnt < 0) vari.Tim_div_cnt = 0;
					vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);
					Freq_info = (TextView) ((Activity)getContext()).findViewById(R.id.freq_info);
					Freq_info.setText(vari.Tim_div_str[vari.Tim_div_cnt]);
				}
				Last_Dis = Current_dis;
			} else if(Last_Dis - Current_dis > 160) {
				Zoom_Check = true;
				//Log.d("Test","Zoom out");
				if(vari.hor_ver_check) {
					//vertical up
					vari.Volt_div_cnt++;
					if(vari.Volt_div_cnt > 7) vari.Volt_div_cnt = 7;
					vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);
					CH_info = (TextView) ((Activity)getContext()).findViewById(R.id.ch_info);
					CH_info.setText(vari.Volt_div_str[vari.Volt_div_cnt]);
				} else {
					//horizontal up
					vari.Tim_div_cnt++;
					if(vari.Tim_div_cnt > 19) vari.Tim_div_cnt = 19;
					vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);
					Freq_info = (TextView) ((Activity)getContext()).findViewById(R.id.freq_info);
					Freq_info.setText(vari.Tim_div_str[vari.Tim_div_cnt]);
				}
				Last_Dis = Current_dis;
			}
		}
	}
}
