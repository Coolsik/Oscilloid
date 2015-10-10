package com.swssm.oscilloid;

public class Variable {
	public static int Angle_cnt = 0;
	public static int Trriger_val = 135;
	public static int FFT_Vertical_Range;
	boolean Run_Stop_flag = false;
	public static int draw_max = 1800;
	public static int sleep_time = 120;

	static int[] inputdraw = new int[3000];
	public static int Vertical_move = 0;
	public static int Horizontal_move = 600;
	public static int Center_position = 575;
	
	public static int Volt_div_cnt = 6 , Tim_div_cnt = 4;	// 2V , 10us // 500K , 1M
	public static byte Input_div;
	
	public static int Mode_Choice = 0;
	
	public static boolean hor_ver_check = false;
	public static boolean pos_sca_check = false;
	public static boolean trriger_check = false;
	
	public static double[] ADC_Pixel = new double[8];
	public static byte[] Volt_div = new byte[8];
	public static byte[] Tim_div = new byte[20];
	public static String[] Volt_div_str = new String[8];
	public static String[] Tim_div_str = new String[20];
	public static String[] FFT_Tim_div_str = new String[20];
	public static String[] CENTER_FFT_Tim_div_str = new String[20];
	public static int[] FREQ_CALC = new int[20];
	
	public static void Div_init() {
		ADC_Pixel[0] = 20;
		ADC_Pixel[1] = 10;
		ADC_Pixel[2] = 5;
		ADC_Pixel[3] = 4;
		ADC_Pixel[4] = 4;
		ADC_Pixel[5] = 3.6;
		ADC_Pixel[6] = 3.7;
		ADC_Pixel[7] = 3;
		
		Volt_div[0] = 7;			// 25mV
		Volt_div[1] = 7;			// 50mV			
		Volt_div[2] = 7;			// 100mV
		Volt_div[3] = 6;			// 250mV
		Volt_div[4] = 4;			// 500mV
		Volt_div[5] = 2;			// 1V
		Volt_div[6] = 1;			// 2V
		Volt_div[7] = 0;			// 5V

		Tim_div[0] = (byte) (((3 << 3) + 0) << 3);			// 500 ns / div
		Tim_div[1] = (byte) (((1 << 3) + 0) << 3);			// 1us / div
		Tim_div[2] = (byte) (((2 << 3) + 0) << 3);			// 2us / div
		Tim_div[3] = (byte) (((0 << 3) + 0) << 3);			// 5us / div
		Tim_div[4] = (byte) (((1 << 3) + 1) << 3);			// 10us / div
		Tim_div[5] = (byte) (((2 << 3) + 1) << 3);			// 20us / div
		Tim_div[6] = (byte) (((0 << 3) + 1) << 3);			// 50us / div
		Tim_div[7] = (byte) (((1 << 3) + 2) << 3);			// 100us / div
		Tim_div[8] = (byte) (((2 << 3) + 2) << 3);			// 200us / div
		Tim_div[9] = (byte) (((0 << 3) + 2) << 3);			// 500us / div
		Tim_div[10] = (byte) (((1 << 3) + 3) << 3);			// 1ms / div
		Tim_div[11] = (byte) (((2 << 3) + 3) << 3);			// 2ms / div
		Tim_div[12] = (byte) (((0 << 3) + 3) << 3);			// 5ms / div
		Tim_div[13] = (byte) (((1 << 3) + 4) << 3);			// 10ms / div
		Tim_div[14] = (byte) (((2 << 3) + 4) << 3);			// 20ms / div
		Tim_div[15] = (byte) (((0 << 3) + 4) << 3);			// 50ms / div
		Tim_div[16] = (byte) (((1 << 3) + 5) << 3);			// 100ms / div
		Tim_div[17] = (byte) (((2 << 3) + 5) << 3);			// 200ms / div
		Tim_div[18] = (byte) (((0 << 3) + 5) << 3);			// 500ms / div
		Tim_div[19] = (byte) (((1 << 3) + 6) << 3);			// 1s / div
		
		Volt_div_str[0] = "CH1 25.00 mV";
		Volt_div_str[1] = "CH1 50.00 mV";
		Volt_div_str[2] = "CH1 100.00 mV";
		Volt_div_str[3] = "CH1 250.00 mV";
		Volt_div_str[4] = "CH1 500.00 mV";
		Volt_div_str[5] = "CH1 1.00 V";
		Volt_div_str[6] = "CH1 2.00 V";
		Volt_div_str[7] = "CH1 5.00 V";

		Tim_div_str[0] = "M 250.0 ns";
		Tim_div_str[1] = "M 500.0 ns";
		Tim_div_str[2] = "M 1.0 us";
		Tim_div_str[3] = "M 2.5 us";
		Tim_div_str[4] = "M 5.0 us";
		Tim_div_str[5] = "M 10.0 us";
		Tim_div_str[6] = "M 25.0 us";
		Tim_div_str[7] = "M 50.0 us";
		Tim_div_str[8] = "M 100.0 us";
		Tim_div_str[9] = "M 250.0 us";
		Tim_div_str[10] = "M 500.0 us";
		Tim_div_str[11] = "M 1.0 ms";
		Tim_div_str[12] = "M 2.5 ms";
		Tim_div_str[13] = "M 5.0 ms";
		Tim_div_str[14] = "M 10.0 ms";
		Tim_div_str[15] = "M 25.0 ms";
		Tim_div_str[16] = "M 50.0 ms";
		Tim_div_str[17] = "M 100.0 ms";
		Tim_div_str[18] = "M 250.0 ms";
		Tim_div_str[19] = "M 500.0 ms";

		FFT_Tim_div_str[0] = "20 MHz";	//	20*10^6
		FFT_Tim_div_str[1] = "10 Mhz";
		FFT_Tim_div_str[2] = "5 MHz";
		FFT_Tim_div_str[3] = "2.5 MHz";
		FFT_Tim_div_str[4] = "1 MHz";
		FFT_Tim_div_str[5] = "500 KHz";
		FFT_Tim_div_str[6] = "250 KHz";
		FFT_Tim_div_str[7] = "100 KHz";
		FFT_Tim_div_str[8] = "50 KHz";
		FFT_Tim_div_str[9] = "25 KHz";
		FFT_Tim_div_str[10] = "10 KHz";
		FFT_Tim_div_str[11] = "5 KHz";
		FFT_Tim_div_str[12] = "2.5 KHz";
		FFT_Tim_div_str[13] = "1 KHz";
		FFT_Tim_div_str[14] = "500 Hz";
		FFT_Tim_div_str[15] = "250 Hz";
		FFT_Tim_div_str[16] = "100 Hz";
		FFT_Tim_div_str[17] = "50 Hz";
		FFT_Tim_div_str[18] = "25 Hz";
		FFT_Tim_div_str[19] = "10 Hz";
		
		CENTER_FFT_Tim_div_str[0] = "10 MHz";
		CENTER_FFT_Tim_div_str[1] = "5 Mhz";
		CENTER_FFT_Tim_div_str[2] = "2500 KHz";
		CENTER_FFT_Tim_div_str[3] = "1250 KHz";
		CENTER_FFT_Tim_div_str[4] = "500 KHz";
		CENTER_FFT_Tim_div_str[5] = "250 KHz";
		CENTER_FFT_Tim_div_str[6] = "125 KHz";
		CENTER_FFT_Tim_div_str[7] = "50 KHz";
		CENTER_FFT_Tim_div_str[8] = "25 KHz";
		CENTER_FFT_Tim_div_str[9] = "12.5 KHz";
		CENTER_FFT_Tim_div_str[10] = "5 KHz";
		CENTER_FFT_Tim_div_str[11] = "2500 Hz";
		CENTER_FFT_Tim_div_str[12] = "1250 Hz";
		CENTER_FFT_Tim_div_str[13] = "500 Hz";
		CENTER_FFT_Tim_div_str[14] = "250 Hz";
		CENTER_FFT_Tim_div_str[15] = "125 Hz";
		CENTER_FFT_Tim_div_str[16] = "50 Hz";
		CENTER_FFT_Tim_div_str[17] = "25 Hz";
		CENTER_FFT_Tim_div_str[18] = "12.5 Hz";
		CENTER_FFT_Tim_div_str[19] = "5 Hz";
		
		FREQ_CALC[0] = 20*1000000;
		FREQ_CALC[1] = 10*1000000;
		FREQ_CALC[2] = 5000*1000;
		FREQ_CALC[3] = 2500*1000;
		FREQ_CALC[4] = 1000*1000;
		FREQ_CALC[5] = 500*1000;
		FREQ_CALC[6] = 250*1000;
		FREQ_CALC[7] = 100*1000;
		FREQ_CALC[8] = 50*1000;
		FREQ_CALC[9] = 25*1000;
		FREQ_CALC[10] = 10*1000;
		FREQ_CALC[11] = 5000;
		FREQ_CALC[12] = 2500;
		FREQ_CALC[13] = 1000;
		FREQ_CALC[14] = 500;
		FREQ_CALC[15] = 250;
		FREQ_CALC[16] = 100;
		FREQ_CALC[17] = 50;
		FREQ_CALC[18] = 25;
		FREQ_CALC[19] = 10;
	}
}
