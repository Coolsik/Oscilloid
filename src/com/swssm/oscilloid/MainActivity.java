package com.swssm.oscilloid;

import java.io.*;
import java.text.*;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
	Menu myMenu;
    final int MENU_FORMAT = Menu.FIRST;
    final int MENU_CLEAN = Menu.FIRST+1;
    final String[] formatSettingItems = {"DC(Direct Current)","AC(Alternating Current)",
    										"DC FFT"};
    final int FORMAT_DC = 0;
    final int FORMAT_AC = 1;
    final int FORMAT_FFT = 2;
    
	int inputFormat = FORMAT_DC;
    
	int save_mid;
	
    final int NOT_AUTOSET = 0;
    final int AUTOSET_VOL = 1;
    final int AUTOSET_TIM = 2; 
    final int MAX_CHECK = 4;
    
    int Autoset_Check = NOT_AUTOSET;
    int[] result = new int[512];
    int past_len=0;

    int sendCount=0 , readCount=0;
	boolean click = false;
	static boolean Run_Stop_flag = false;
	StringBuffer readSB = new StringBuffer();
    
	/* thread to read the data */
	public handler_thread handlerThread;

	/* declare a FT311 UART interface variable */
	public FT311UARTInterface uartInterface;

	/* local variables */
	byte[] writeBuffer;
	byte[] readBuffer;
	char[] readBufferToChar;
	int[] actualNumBytes;
	
	//static int[] inputdraw;
	int inputdata,Draw_cnt=0;
	//public static int Vertical_move = 355;
	
	//Volt Autoset Val
	int Autoset_Volt_div_cnt=0,Autoset_ADCPixel=0,Autoset_Vol_Max=0;
	int erase_val1=0;
	int erase_max=0,erase_min=3000;
	
	//Tim Autoset Val
	int Autoset_Tim_div_cnt=0 , Tim_width_cnt=0 , Autoset_Tim_Max=0,Autoset_Tim_Save=2;
	boolean Vol_Autoset_flag = false,Tim_Autoset_flag= false,One_Mega_flag=true;
	
	//Autoset Val
	int Auto_max=0,Auto_min=3000,Auto_mid;
	boolean rising_check = true;
	int rising_cnt = 0;
	
	int Count_Autoset_volt=0;

	String addtext = "";
	
	WaveView wv = null;
	Logger logger;
	
	// 지우기 테스트영
	int[] index=new int[20];
	//
	
	int numBytes;
	byte count;
	byte status;
	byte writeIndex = 0;
	byte readIndex = 0;

	int baudRate; /* baud rate */
	byte stopBit; /* 1:1stop bits, 2:2 stop bits */
	byte dataBit; /* 8:8bit, 7: 7bit */
	byte parity; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
	byte flowControl; /* 0:none, 1: flow control(CTS,RTS) */
	
	public Context global_context;
	public boolean bConfiged = false;
	public SharedPreferences sharePrefSettings;
	public String act_string;
	
	TextView CH_info,Freq_info,checkasdkf , Center_frq_btn , Last_frq;
	Button Autoset_btn,Func2_btn;
    
    Variable vari = null;
    Layout_Change L_change = null;
	ButtonEvent btnevent = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.autoset_button).setOnClickListener(Listener);
		findViewById(R.id.run_stop_button).setOnClickListener(Listener);
		findViewById(R.id.func1).setOnClickListener(Listener);
		findViewById(R.id.func2).setOnClickListener(Listener);
		findViewById(R.id.func3).setOnClickListener(Listener);
		
		logger.addRecordToLog("oncreate");

		Autoset_btn = (Button)findViewById(R.id.autoset_button);
		Func2_btn = (Button)findViewById(R.id.func2);
		CH_info = (TextView) findViewById(R.id.ch_info);
		Freq_info = (TextView) findViewById(R.id.freq_info);
		Last_frq = (TextView)findViewById(R.id.freq_info_last);
		
		//
		checkasdkf =(TextView)findViewById(R.id.appnametext);
		//
		L_change = new Layout_Change(Autoset_btn,CH_info,Freq_info,Last_frq,Func2_btn);

		RotaryKnobView jogView = (RotaryKnobView)findViewById(R.id.jogView);
		jogView.setKnobListener(new RotaryKnobView.RotaryKnobListener() {
			@Override
			public void onKnobChanged(float delta,float angle) {
				if(delta > 0) {
					//Log.d("Test","Rotate Right");
					jog_right();
				} else {
					//Log.d("Test","Rotate Left");
					jog_left();
				}
			}
		});
		sharePrefSettings = getSharedPreferences("UARTLBPref", 0);
		global_context = this;
		
		/* allocate buffer */
		writeBuffer = new byte[64];
		readBuffer = new byte[4096];
		readBufferToChar = new char[4096]; 
		actualNumBytes = new int[1];
		//inputdraw = new int[3000];
		
		for(int i=0;i<3000;i++) vari.inputdraw[i]=485;
		
		vari.Div_init();

		baudRate = 57600;
		stopBit = 1;
		dataBit = 8;
		parity = 0;
		flowControl = 0;
		vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);

		act_string = getIntent().getAction();
		if( -1 != act_string.indexOf("android.intent.action.MAIN")){
			restorePreference();
		}
		else if( -1 != act_string.indexOf("android.hardware.usb.action.USB_ACCESSORY_ATTACHED")){
			cleanPreference();
		}
		uartInterface = new FT311UARTInterface(this, sharePrefSettings);

		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		handlerThread = new handler_thread(handler);
		handlerThread.start();
	}
	protected void cleanPreference(){
		SharedPreferences.Editor editor = sharePrefSettings.edit();
		editor.remove("configed");
		editor.remove("baudRate");
		editor.remove("stopBit");
		editor.remove("dataBit");
		editor.remove("parity");
		editor.remove("flowControl");
		editor.commit();
	}
	protected void savePreference() {
		if(true == bConfiged){
			sharePrefSettings.edit().putString("configed", "TRUE").commit();
			sharePrefSettings.edit().putInt("baudRate", baudRate).commit();
			sharePrefSettings.edit().putInt("stopBit", stopBit).commit();
			sharePrefSettings.edit().putInt("dataBit", dataBit).commit();
			sharePrefSettings.edit().putInt("parity", parity).commit();			
			sharePrefSettings.edit().putInt("flowControl", flowControl).commit();			
		}
		else{
			sharePrefSettings.edit().putString("configed", "FALSE").commit();
		}
	}
	protected void restorePreference() {
		String key_name = sharePrefSettings.getString("configed", "");
		if(true == key_name.contains("TRUE")){
			bConfiged = true;
		}
		else{
			bConfiged = false;
        }
		baudRate = sharePrefSettings.getInt("baudRate", 57600);
		stopBit = (byte)sharePrefSettings.getInt("stopBit", 1);
		dataBit = (byte)sharePrefSettings.getInt("dataBit", 8);
		parity = (byte)sharePrefSettings.getInt("parity", 0);
		flowControl = (byte)sharePrefSettings.getInt("flowControl", 0);
	}
	public void onHomePressed() {
		onBackPressed();
	}	
	public void onBackPressed() {
	    super.onBackPressed();
	}	
	@Override
	protected void onResume() {
		super.onResume();
		if(2 == uartInterface.ResumeAccessory()) {
			cleanPreference();
			restorePreference();
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onStop() {
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		uartInterface.DestroyAccessory(bConfiged);
		super.onDestroy();
	}
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			for(int i=0; i<actualNumBytes[0]; i++) {
				readBufferToChar[i] = (char)readBuffer[i];
			}
			appendData(readBufferToChar, actualNumBytes[0]);
		}
	};
	/* usb input data handler */
	private class handler_thread extends Thread {
		Handler mHandler;
		/* constructor */
		handler_thread(Handler h) {
			mHandler = h;
		}
		public void run() {
			Message msg;
			while (true) {
				try {
					Thread.sleep(vari.sleep_time);
				} catch (InterruptedException e) {
					
				} 
				status = uartInterface.ReadData(4096, readBuffer,actualNumBytes);
				if (status == 0x00 && actualNumBytes[0] > 0) {
					msg = mHandler.obtainMessage();
					mHandler.sendMessage(msg);
					sendCount = 0;
					if(!click){
						click=true;
					}
				}
				if(status == 0x01 && click) {
					byte[] buffer = {(byte) vari.Mode_Choice,vari.Input_div,(byte)vari.Trriger_val};
					uartInterface.SendData(3, buffer);
				}
			}
		}
	}
    public boolean onCreateOptionsMenu(Menu menu) {
        myMenu = menu;
        myMenu.add(0, MENU_FORMAT, 0, "Format - DC");
        myMenu.add(0, MENU_CLEAN, 0, "Go Center");
        return super.onCreateOptionsMenu(myMenu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case MENU_FORMAT:
        	new AlertDialog.Builder(global_context).setTitle("Mode")
			.setItems(formatSettingItems, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {	
					MenuItem item = myMenu.findItem(MENU_FORMAT);
					if(0 == which) {
						vari.Mode_Choice = inputFormat = FORMAT_DC;
					    item.setTitle("Format - "+ formatSettingItems[0]);
					    L_change.DC_Layout();
			    		readCount = 0;
					    //msgToast("FORMAT_DC",Toast.LENGTH_SHORT);
					} else if(1 == which) {
						vari.Mode_Choice = inputFormat = FORMAT_AC;
						item.setTitle("Format - "+ formatSettingItems[1]);
					    L_change.AC_Layout();		
						readCount = 0;
					    //msgToast("FORMAT_AC",Toast.LENGTH_SHORT);
					} else if(2 == which) {
						vari.Mode_Choice = inputFormat = FORMAT_FFT;
						item.setTitle("Format - "+ formatSettingItems[2]);
						L_change.DC_FFT_Layout();
						
						//msgToast("FORMAT_FFT",Toast.LENGTH_SHORT);
			        	readSB.delete(0, readSB.length());
					}
				    char[] ch = new char[1];
				    appendData(ch, 0);	//지워보기
				}
			}).show();           	
        	break;
        case MENU_CLEAN:
        	wv.image_y = 465;
        	wv.image2_x = 575;
        	vari.Trriger_val = (int) (255 - WaveView.image_y / 3.6);
			vari.Trriger_val -= (int)(vari.Vertical_move / 3.8);
        	vari.Vertical_move = 0;
        	vari.Horizontal_move = 600;
        	vari.draw_max = (wv.WIDTH+vari.Horizontal_move);
    		for(int k=vari.Horizontal_move, p=0 ; k<vari.draw_max ; k++,p++) {
    			if(vari.draw_max > 2400) vari.draw_max = 2400;
    			else if(vari.draw_max < 1200) vari.draw_max = 1200;
        		wv.ch1_inputdata[p] = vari.inputdraw[k] + vari.Vertical_move;
    	  	}
    		wv.set_data(wv.ch1_inputdata);
        	break;
        default:        	
        	readSB.delete(0, readSB.length());
        	break;
        }
        return super.onOptionsItemSelected(item);
    }
	public void writeData(String destStr){
		numBytes = destStr.length();
		for (int i = 0; i < numBytes; i++) {
			writeBuffer[i] = (byte)destStr.charAt(i);
		}
		uartInterface.SendData(numBytes, writeBuffer);
	}
	public void appendData(char[] data, int len) {
		//msgToast("start1",Toast.LENGTH_SHORT);
    	if(len >= 1) {
    		switch(inputFormat) {
    		case FORMAT_DC:
    		case FORMAT_AC:
    			if(vari.Mode_Choice != 1) {
        			readSB.delete(0, readSB.length());
        			readSB.append(String.copyValueOf(data, 0, len));
    			}
    			break;
    		case FORMAT_FFT:
    			if(len == 512 && vari.Mode_Choice == 1) {
    	            Complex[] x = new Complex[512];
    	            for (int i = 0; i < 512; i++) {
    	               x[i] = new Complex((double)(data[i] & 0xff), 0);
    	            }
    	            Complex[] y = FFT.fft(x);
    	            for(int i=0; i<512; i++){
    	               double re = y[i].re();
    	               double im = y[i].im();
    	               int temp = (int)Math.sqrt((re*re)+(im*im));
    	               result[i] = temp / 20;
    	            }
    			}
    			readSB.delete(0, readSB.length());
    			readSB.append(String.copyValueOf(data, 0, len));
    		}
		}
    	//checkasdkf.setText(""+len);
    		switch(inputFormat)	{
        	case FORMAT_DC:
        	case FORMAT_AC:
        		if(len < 300) AC_DC_func();
        		break;
        	case FORMAT_FFT:
        		AC_DC_FFT_func();
        		break;
        	}
        	switch(inputFormat)	{
        	case FORMAT_DC:
        	case FORMAT_AC:
            	Draw_AC_DC_Oscilloscope();
        		break;
        	case FORMAT_FFT:
        		Draw_AC_DC_FFT_Oscilloscope();
        		break;
        	default:
        		break;
        	}
    }
	public void AC_DC_func() {
		char[] ch = readSB.toString().toCharArray();
		String temp;
		StringBuilder tmpSB = new StringBuilder();
		
		readCount = 0;
		
		boolean Div_3_flag = (vari.Tim_div_cnt > 0) && ((vari.Tim_div_cnt % 3) == 0);
		for(int i = 0; i < ch.length; i++) {   	
			temp = Integer.toString((int)(ch[i] & 0xff));
			for(int j = 0; j < (3 - temp.length()); j++) {
				tmpSB.append("0");
			}
			tmpSB.append(temp);
			if(i+1 < ch.length && Run_Stop_flag) {
				inputdata = (int)(ch[i] & 0xff);
				erase_val1 = (int)(ch[i] & 0xff);
				if(inputdata > 124) inputdata = 485+ (int)((inputdata-124) * vari.ADC_Pixel[vari.Volt_div_cnt]);
  				else inputdata = 485 - (int)((124 - inputdata) * vari.ADC_Pixel[vari.Volt_div_cnt]);

				if(Autoset_Check==NOT_AUTOSET) {
					if(rising_check && (Auto_mid < inputdata)) {
						rising_check = false;
					}
					if((rising_check == false) && (Auto_mid > inputdata)) {
						rising_cnt++;
					} else if((rising_check == false) && (Auto_mid < inputdata) && (rising_cnt > 3)) {
						rising_check = true;
						if(rising_cnt>100 || rising_cnt < 5){
							checkasdkf.setText("?");
						} else{
							int add = (vari.FREQ_CALC[vari.Tim_div_cnt] / rising_cnt);
							add /= 1000;
							if(add==0){
								String pattern = "#.##";
								DecimalFormat dformat = new DecimalFormat(pattern);
								double add2 = (vari.FREQ_CALC[vari.Tim_div_cnt] / (double)rising_cnt);
								dformat.format(add2);
								
								checkasdkf.setText(dformat.format(add2));
							} else{
								checkasdkf.setText(add+" KHz");
							}
						}
						rising_cnt=0;
					}
				}
				
				//if(inputdata > 970) inputdata = 970;
				//else if(inputdata < 0) inputdata = 0;
				
				switch(Autoset_Check) {
				case NOT_AUTOSET:
					if(Div_3_flag) {
						for(int k=0;k<10;k++) {
							vari.inputdraw[readCount] = inputdata;
			  				readCount++;
			  			}
					} else {
			  			for(int k=0;k<12;k++) {
			  				if(readCount==3000){
			  					readCount=2999;
			  				}
			  				vari.inputdraw[readCount] = inputdata;
			  				readCount++;
			  			}
					}
					break;
				case AUTOSET_VOL:
					//if(i > 50) {
					//if(vari.Volt_div_cnt==6) addtext += (" "+inputdata);
					if(Auto_max < inputdata) Auto_max = inputdata;
					if(Auto_min > inputdata) Auto_min = inputdata;
					//Log.d("Test",i+" : " + erase_val1+"");	
					if(erase_max < erase_val1) erase_max= erase_val1;
					if(erase_min > erase_val1) erase_min = erase_val1;
					//}
					break;
				case MAX_CHECK:
					if(Auto_max < inputdata) Auto_max = inputdata;
					if(Auto_min > inputdata) Auto_min = inputdata;
					break;
				case AUTOSET_TIM:
					if(rising_check && (Auto_mid < inputdata)) {
						rising_check = false;
					}
					if((rising_check == false) && (Auto_mid > inputdata)) {
						rising_cnt++;
					} else if((rising_check == false) && (Auto_mid < inputdata) && (rising_cnt > 3)) {
						rising_check = true;
					}
					break;
				}
			}
		}
		tmpSB.delete(0, tmpSB.length());		
	}
	public void AC_DC_FFT_func() {
		
	}
	public void Draw_AC_DC_Oscilloscope() {
		switch(Autoset_Check) {
		case NOT_AUTOSET:
			if(Run_Stop_flag && vari.Mode_Choice != 3) {
				for(int k=vari.Horizontal_move, p=0 ; k<vari.draw_max ; k++,p++) {
					if(vari.draw_max > 2400) vari.draw_max = 2400;
					else if(vari.draw_max < 1200) vari.draw_max = 1200;
	    			wv.ch1_inputdata[p] = vari.inputdraw[k] + vari.Vertical_move;
	    		}
	        	wv.set_data(wv.ch1_inputdata);
				addtext = "";
			}
			break;
		case AUTOSET_VOL:
			if(vari.Volt_div_cnt == 7) {
    			Last_frq.setText(""+Autoset_Volt_div_cnt+" : "+ Autoset_ADCPixel);
    			Auto_mid = Auto_max - Auto_min;
    			//Log.d("Test","max : " + Auto_max + " min : " + Auto_min + " , mid : " + Auto_mid);
    			if(Auto_mid < 5) {
    				if(Auto_max < 777) {
    					if(Autoset_Vol_Max < Auto_max) {
    						Autoset_Vol_Max = Auto_max;
        					Autoset_ADCPixel = vari.Volt_div_cnt;
    					}
    				}
    			} else if(Auto_mid < 291 && Auto_max < 777){//  && Auto_max > 600) {
    				if(Autoset_Vol_Max < Auto_max) {
						Autoset_Vol_Max = Auto_max;
    					Autoset_ADCPixel = vari.Volt_div_cnt;
					}
    			}
    			vari.Volt_div_cnt = Autoset_ADCPixel;
    			//CH_info.setText(vari.Volt_div_str[vari.Volt_div_cnt]);
    			Auto_max=0;
    			Auto_min=3000;
    			
    			erase_max=0;
    			erase_min=3000;
    			if(Autoset_Vol_Max == 0) vari.Volt_div_cnt = 7;
    			Autoset_Check = MAX_CHECK;
    			Autoset_Vol_Max = 0;
    			vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);    			
    		} else {
    			//Log.d("Test","max : " + Auto_max + " min : " + Auto_min + " , mid : " + Auto_mid);
    			Last_frq.setText(""+Autoset_Volt_div_cnt+" : "+ Autoset_ADCPixel);
    			Auto_mid = Auto_max - Auto_min;
    			if(Auto_mid < 5) {
    				if(Auto_max < 777) {
    					if(Autoset_Vol_Max < Auto_max) {
    						Autoset_Vol_Max = Auto_max;
        					Autoset_ADCPixel = vari.Volt_div_cnt;
    					}
    				}
    			} else if(Auto_mid < 291 && Auto_max < 777 ) {// && (MAX_VOL_CHECK < Autoset_Volt_div_cnt)) {
    				if(Autoset_Vol_Max < Auto_max) {
						Autoset_Vol_Max = Auto_max;
    					Autoset_ADCPixel = vari.Volt_div_cnt;
					}
    				if(Autoset_ADCPixel < vari.Volt_div_cnt) {
    					Autoset_Vol_Max = Auto_max;
    					Autoset_ADCPixel = vari.Volt_div_cnt;
    				}
    			}
    			erase_max= Auto_max=0;
    			erase_min= Auto_min=3000;
            	Autoset_Volt_div_cnt++;
        		vari.Volt_div_cnt = Autoset_Volt_div_cnt;
            	vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);
    		}
			break;
		case MAX_CHECK:
			vari.Tim_div_cnt = 0;
			Autoset_Tim_div_cnt = 2;
			Autoset_Tim_Save=Autoset_Tim_Max = 0;
			Autoset_Check = AUTOSET_TIM;
			Auto_mid = ((Auto_max - Auto_min) / 2) + Auto_min;
			break;
		case AUTOSET_TIM:
			if(vari.Tim_div_cnt == 12) {
				if(Autoset_Tim_Max==0) Autoset_Tim_Save=2;
				if(One_Mega_flag) {
					vari.Tim_div_cnt = 2;
					vari.Volt_div_cnt = 6;
					//CH_info.setText(vari.Volt_div_str[vari.Volt_div_cnt]);
					Auto_mid = save_mid = 582;
				} else {
					vari.Tim_div_cnt = Autoset_Tim_Save;
				}
				Log.d("Test",Autoset_Tim_div_cnt + " : "+rising_cnt);
				Log.d("Test",""+vari.Tim_div_cnt);
				CH_info.setText(vari.Volt_div_str[vari.Volt_div_cnt]);
				vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);
				Freq_info.setText(vari.Tim_div_str[vari.Tim_div_cnt]);
				Last_frq.setText("");
				wv.image_y = wv.HEIGHT - save_mid;
				wv.image_y -= (vari.Vertical_move);
				vari.Trriger_val = (int) (255 - wv.image_y / 3.6);
				vari.Trriger_val -= (int)(vari.Vertical_move / 3.8);
				//vari.Trriger_val += (vari.Vertical_move);

    			Autoset_Check = NOT_AUTOSET;
				rising_cnt = 0;
				rising_check = true;
				One_Mega_flag = true;
				vari.sleep_time = 90;
			} else {
				Log.d("Test",Autoset_Tim_div_cnt + " : "+rising_cnt);
				if(Autoset_Tim_div_cnt < 5 && One_Mega_flag) {
					if(rising_cnt < 10 && rising_cnt > 0) One_Mega_flag = true;
					else One_Mega_flag = false;
				}
				if(rising_cnt < 30) {
					if(rising_cnt >= Autoset_Tim_Max) {
						Autoset_Tim_Max = rising_cnt;
						Autoset_Tim_Save = Autoset_Tim_div_cnt;
						save_mid = Auto_mid;
					}
				}
				rising_cnt = 0;
				rising_check = true;
				Last_frq.setText(""+vari.Tim_div_cnt + " : " + index[vari.Tim_div_cnt]);
				Autoset_Tim_div_cnt++;
				vari.Tim_div_cnt = Autoset_Tim_div_cnt;
            	vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);
			}
			break;
		}
	}
	public void Draw_AC_DC_FFT_Oscilloscope() {
		double match = 4.72;
		for(int k=0; k<1200;k++) {
			wv.ch1_inputdata[k] = result[(int)((k/match)+1)];
		}
    	wv.set_data(wv.ch1_inputdata);
	}
	Button.OnClickListener Listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.run_stop_button:
				Run_Stop_flag = btnevent.check_run(v);
				if(Run_Stop_flag) {
					if(false == bConfiged) {
						bConfiged = true;
						uartInterface.SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
						savePreference();
					}
				} else {
					
				}
				break;
			case R.id.autoset_button:
				//if(FORMAT_FFT != 3) {
				if(inputFormat != 2) {
					vari.sleep_time = 120;
					Autoset_Check = AUTOSET_VOL;
					Autoset_ADCPixel = Autoset_Vol_Max = Autoset_Volt_div_cnt = vari.Volt_div_cnt = 0;
					vari.Tim_div_cnt = 4;
					vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);
				}
				break;
			case R.id.func1:
				vari.hor_ver_check = btnevent.Hor_Ver_Check(v);
				break;
			case R.id.func2:
				vari.pos_sca_check = L_change.Layout_Mode_Func2(inputFormat,v);
				break;
			case R.id.func3:
				vari.trriger_check = btnevent.Trigger_Check(v);
				break;
			}
		}
	};
	public void jog_right() {
		if(vari.trriger_check) {
			WaveView.image_y--;
			if(WaveView.image_y < 0) WaveView.image_y=0;
			vari.Trriger_val = (int) (255 - WaveView.image_y / 3.6);
			vari.Trriger_val -= (int)(vari.Vertical_move / 3.8);
		} else {
			if(vari.pos_sca_check) {
				// Scale
				// vertical and horizontal scale up 조절
				// horizontal == false , vertical == true
				vari.Angle_cnt++;
				if(vari.Angle_cnt == 30) {
					vari.Angle_cnt = 0;
					if(vari.hor_ver_check) {
						//vertical up
						vari.Volt_div_cnt++;
						if(vari.Volt_div_cnt > 7) vari.Volt_div_cnt = 7;
						vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);
						L_change.Layout_Mode_Choose(inputFormat);
					} else {
						//horizontal up
						vari.Tim_div_cnt++;
						if(vari.Tim_div_cnt > 19) vari.Tim_div_cnt = 19;
						vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);
						L_change.Layout_Mode_Choose(inputFormat);
					}
				}
			} else {
				// Position
				if(vari.hor_ver_check) {
					vari.Vertical_move++;
					//Stop_Horizontal_Move();
				}
				else {
					vari.Horizontal_move++;
					vari.Center_position++;

					if(vari.Horizontal_move > 1180) vari.Horizontal_move = 1180;
					else {
				    	if(vari.Center_position > 1150) {
				    		vari.Center_position = wv.image2_x = 1150;
				    	}
				    	else wv.image2_x = vari.Center_position;
				    	wv.image2_x = (1150 - WaveView.image2_x);
					}
					vari.draw_max = (wv.WIDTH+vari.Horizontal_move);
				}
				Stop_Horizontal_Move();
			}
		}
	}
	public void jog_left() {
		if(vari.trriger_check) {
			WaveView.image_y++;
			if(WaveView.image_y > 920) wv.image_y = 920;
			vari.Trriger_val = (int) (255 - wv.image_y / 3.6);
			vari.Trriger_val -= (int)(vari.Vertical_move / 3.8);
		}
		else {
			if(vari.pos_sca_check) {
				// Scale
				// vertical and horizontal scale down 조절
				// horizontal == false , vertical == true
				vari.Angle_cnt++;
				if(vari.Angle_cnt == 30) {
					vari.Angle_cnt=0;
					
					if(vari.hor_ver_check) {
						//vertical down
						vari.Volt_div_cnt--;
						if(vari.Volt_div_cnt < 0) vari.Volt_div_cnt = 0;
						vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);
						L_change.Layout_Mode_Choose(inputFormat);
					} else {
						//horizontal down
						vari.Tim_div_cnt--;
						if(vari.Tim_div_cnt < 0) vari.Tim_div_cnt = 0;
						vari.Input_div = (byte) (vari.Volt_div[vari.Volt_div_cnt] + vari.Tim_div[vari.Tim_div_cnt]);
						L_change.Layout_Mode_Choose(inputFormat);
					}
				}
			} else {
				// Position
				if(vari.hor_ver_check) {
					vari.Vertical_move--;
					//Stop_Horizontal_Move();
				} else {
					vari.Horizontal_move--;
					vari.Center_position--;
	        		if(vari.Horizontal_move < 20) vari.Horizontal_move = 20;
	        		else {
				    	if(vari.Center_position < 0) {
				    		vari.Center_position = wv.image2_x = 0;
				    	}
				    	else wv.image2_x = vari.Center_position;
				    	wv.image2_x = (1150 - WaveView.image2_x);
				    	//Log.d("Test","Center_position : " + vari.Center_position + " wv.image2_x  : " + wv.image2_x + "vari.Horizontal_move : " + vari.Horizontal_move);
	        		}
	        		vari.draw_max = (wv.WIDTH+vari.Horizontal_move);
				}
				Stop_Horizontal_Move();
			}
		}
	}
	public void Stop_Horizontal_Move() {
		if(Run_Stop_flag == false) {
			for(int k=vari.Horizontal_move, p=0 ; k<vari.draw_max ; k++,p++) {
				if(vari.draw_max > 2400) vari.draw_max = 2400;
				else if(vari.draw_max < 1200) vari.draw_max = 1200;
	    		wv.ch1_inputdata[p] = vari.inputdraw[k] + vari.Vertical_move;
	    	}
	       	wv.set_data(wv.ch1_inputdata);
		}
	}
	void msgToast(String str, int showTime) {
    	Toast.makeText(global_context, str, showTime).show();
    }
}