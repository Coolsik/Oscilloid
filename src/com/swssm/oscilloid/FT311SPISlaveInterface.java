package com.swssm.oscilloid;
//User must modify the below package with their package name

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/******************************FT311 GPIO interface class******************************************/
public class FT311SPISlaveInterface extends Activity
{
	
	private static final String ACTION_USB_PERMISSION =    "com.SPISlaveDemo.USB_PERMISSION";
	public UsbManager usbmanager;
	public UsbAccessory usbaccessory;
	public PendingIntent mPermissionIntent;
	public ParcelFileDescriptor filedescriptor;
	public FileInputStream inputstream;
	public FileOutputStream outputstream;
	public boolean mPermissionRequestPending = false;
	public boolean READ_ENABLE = true;
	public boolean accessory_attached = false;
	public boolean terminate_read = false;
	public handler_thread handlerThread;
	
	private byte [] usbdata;
	private byte []	writeusbdata;
	private byte [] readBuffer;
	private int totalRxBytes;
	private int writeIndex;
	private int readIndex;
    private int readcount;
    private byte status;
    private byte  maxnumbytes = (byte) 64; /*maximum data bytes, except command*/
    private byte maxtxmitsize = 64; /*this could be max, 256*/
    public boolean datareceived = false;
	
    public Context global_context;
   
    public static String ManufacturerString = "mManufacturer=FTDI";
    public static String ModelString = "mModel=FTDISPISlaveDemo";
    public static String VersionString = "mVersion=1.0";
		
		/*constructor*/
	 public FT311SPISlaveInterface(Context context){
		 	super();
                        global_context = context;
			/*shall we start a thread here or what*/
			usbdata = new byte[maxtxmitsize];
			/*
			 * this shoudl be higher buffer,as this is the main
			 * 	receive buffer. But here its only 128 bytes 
			 */
			readBuffer = new byte [maxnumbytes];
			
	        writeusbdata = new byte[maxtxmitsize];
	        
	        readIndex = 0;
	        writeIndex = 0;
	        totalRxBytes = 0;
	        
	        /***********************USB handling******************************************/
			
	        usbmanager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
	       // Log.d("LED", "usbmanager" +usbmanager);
	        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
	        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
	       filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
	       context.registerReceiver(mUsbReceiver, filter);
	        
	       inputstream = null;
	       outputstream = null;
		}
	 
	 	/*reset method*/
	 	public void Reset()
	 	{
	 		/*create the packet*/
	 		writeusbdata[0] = (byte) 0x54;
			/*send the packet over the USB*/
			SendPacket(1);
	 	}
	 	
	 	
	 	public void SetConfig(byte clockPhase,byte dataOrder)
	 	{
	 		
	 		writeusbdata[0] = (byte) 0x51;
	 		writeusbdata[1] = clockPhase;
	 		writeusbdata[2] = dataOrder;
	 		
	 		SendPacket((int)3);;
	 		
	 	}
	 		
	 	
	 	/*write data*/
	 	public byte SendData(byte numBytes, char[] buffer, byte [] numReadBytes)
	 	{
	 		
	 		status = 0x00; /*error by default*/
	 		/*
	 		 * if num bytes are more than maximum limit
	 		 */
	 		if(numBytes < 1){
	 			/*return the status with the error in the command*/
	 			return status;
	 		}
	 		
	 		/*check for maximum limit*/
	 		if(numBytes > (maxtxmitsize-1)){
	 			numBytes = (byte) (maxtxmitsize-1);
		
	 		}
	 		
	 		
	 		
	 		
	 		/*prepare the packet to be sent*/
	 		for(int count = 0;count<numBytes;count++)
	 		{
	 			writeusbdata[count+1] = (byte)buffer[count];
	 		}
	 		
	 		/*prepare the usbpacket*/
	 		writeusbdata[0] = (byte) 0x52;
	 		SendPacket((int)(numBytes+1));
	 		
	 		datareceived = false;
	 		/*wait while the data is received*/
	 		/*FIXME, may be create a thread to wait on , but any
	 		 * way has to wait in while loop
	 		 */
	 		while(true){
	 			
	 			if(datareceived == true){
	 				break;
	 			}
	 		}
	 		
	 		/*success by default*/
	 		status = 0x00;
	 		
	 		if(usbdata[0] == 0x52)
	 		{
	 			/*update the received length*/
	 			numReadBytes[0] = (byte)usbdata[1];
	 		}	
	 		/*read the next usb data*/
	 		datareceived = false;
	 			
	 		return status;
	 		
	 	}
	 	
	 	
	 	/*read data*/
	 	public byte ReadData(byte numBytes,char[] buffer, byte [] actualNumBytes)
	 	{
	 			status = 0x00; /*success by default*/
	 			
	 			/*should be at least one byte to read*/
	 			if((numBytes < 1) || (totalRxBytes == 0)){
	 				actualNumBytes[0] = 0x00;
	 				status = 0x01;
	 				return status;
	 			}
	 			
	 			/*check for max limit*/
	 			if(numBytes > maxtxmitsize){
	 				numBytes = maxtxmitsize;
	 			}
	 			
	 			
	 			if(numBytes > totalRxBytes)
	 				numBytes = (byte)totalRxBytes;
	 			
	 					
	 			
	 			/*update the number of bytes available*/
	 			totalRxBytes -= numBytes;
	 				
	 			actualNumBytes[0] = numBytes;	
	 				
	 			/*copy to the user buffer*/	
 				for(int count = 0; count<numBytes;count++)
 				{
 					buffer[count] = (char)readBuffer[readIndex];
 					readIndex++;
 					/*shouldnt read more than what is there in the buffer,
 					 * 	so no need to check the overflow
 					 */
 					readIndex %= maxnumbytes;
 				}
 				return status;
	 	}
	 		 	
		/*method to send on USB*/
		private void SendPacket(int numBytes)
		{
			
			
			try {
				if(outputstream != null){
					outputstream.write(writeusbdata, 0,numBytes);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		/*resume accessory*/
		public void ResumeAccessory()
		{
			// Intent intent = getIntent();
			if (inputstream != null && outputstream != null) {
				return;
			}
			
			UsbAccessory[] accessories = usbmanager.getAccessoryList();
			if(accessories != null)
			{
				Toast.makeText(global_context, "Accessory Attached", Toast.LENGTH_SHORT).show();
			}		
			else
			{
				accessory_attached = false;
				return;
			}
			
			UsbAccessory accessory = (accessories == null ? null : accessories[0]);
			if (accessory != null) {
				if( -1 == accessory.toString().indexOf(ManufacturerString))
				{
					Toast.makeText(global_context, "Manufacturer is not matched!", Toast.LENGTH_SHORT).show();
					return;
				}

				if( -1 == accessory.toString().indexOf(ModelString))
				{
					Toast.makeText(global_context, "Model is not matched!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if( -1 == accessory.toString().indexOf(VersionString))
				{
					Toast.makeText(global_context, "Version is not matched!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Toast.makeText(global_context, "Manufacturer, Model & Version are matched!", Toast.LENGTH_SHORT).show();
				accessory_attached = true;
				
				if (usbmanager.hasPermission(accessory)) {
					OpenAccessory(accessory);
				} 
				else
				{
					synchronized (mUsbReceiver) {
						if (!mPermissionRequestPending) {
							Toast.makeText(global_context, "Request USB Permission", Toast.LENGTH_SHORT).show();
							usbmanager.requestPermission(accessory,
									mPermissionIntent);
							mPermissionRequestPending = true;
						}
				}
			}
			} else {}

		}
		
		/*destroy accessory*/
		public void DestroyAccessory(){
			global_context.unregisterReceiver(mUsbReceiver);
			CloseAccessory();
		}
		
		public void EndHandleThread()
		{
			READ_ENABLE = false;
			terminate_read = true;
		}
		
/*********************helper routines*************************************************/		
		
		public void OpenAccessory(UsbAccessory accessory)
		{
			filedescriptor = usbmanager.openAccessory(accessory);
			if(filedescriptor != null){
				usbaccessory = accessory;
				FileDescriptor fd = filedescriptor.getFileDescriptor();
				inputstream = new FileInputStream(fd);
				outputstream = new FileOutputStream(fd);
				/*check if any of them are null*/
				if(inputstream == null || outputstream==null){
					return;
				}
			}
			
			handlerThread = new handler_thread(inputstream);
			handlerThread.start();
		}
		
		private void CloseAccessory()
		{
			try{
				if(filedescriptor != null)
					filedescriptor.close();
				
			}catch (IOException e){}
			
			try {
				if(inputstream != null)
						inputstream.close();
			} catch(IOException e){}
			
			try {
				if(outputstream != null)
						outputstream.close();
				
			}catch(IOException e){}
			/*FIXME, add the notfication also to close the application*/
			
			filedescriptor = null;
			inputstream = null;
			outputstream = null;
		
			System.exit(0);
		
		}
		
				
		/***********USB broadcast receiver*******************************************/
	    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				String action = intent.getAction();
				if (ACTION_USB_PERMISSION.equals(action)) 
				{	
					synchronized (this)
					{
						UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
						if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
						{
							Toast.makeText(global_context, "Allow USB Permission", Toast.LENGTH_SHORT).show();
							OpenAccessory(accessory);
						} 
						else 
						{
							Toast.makeText(global_context, "Deny USB Permission", Toast.LENGTH_SHORT).show();
							Log.d("LED", "permission denied for accessory " + accessory);
							
						}
						mPermissionRequestPending = false;
					}
				} 
				else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))
				{
						CloseAccessory();
				}else
				{
					Log.d("LED", "....");
				}
			}	
		};
	    

		
		/*usb input data handler*/
		private class handler_thread  extends Thread {
			FileInputStream instream;
			
			handler_thread(FileInputStream stream ){
				instream = stream;
			}
			
			public void run()
			{
				
				while(READ_ENABLE == true)
				{
					
					try
					{
						/*dont overwrite the previous buffer*/
						if((instream != null) && (datareceived==false))
						{
							readcount = instream.read(usbdata,0,64);
							if(readcount > 0)
							{
								datareceived = true;
								
								if(usbdata[0] == 0x53)
								{
									/*since first byte is command, start the count with 1*/
									for(int count = 1;count<readcount;count++)
									{
					    			
										readBuffer[writeIndex] = usbdata[count];
										/*move to the next write location*/
										writeIndex++;
										writeIndex %= maxnumbytes;
										/*FIXME,check for overflow*/
										//if(writeIndex == readIndex){
					    				
										//}
					    			
									}
					    		
						    		/*caluclate the available bytes to read*/
						    		if(writeIndex >= readIndex)
						    			totalRxBytes = writeIndex-readIndex;
						    		else
						    			totalRxBytes = (maxnumbytes-readIndex)+writeIndex;
						    		
						    		/*go and read next bytes*/
						    		datareceived = false;
								}
							}
						
						}
					}catch (IOException e){}
				}
				
				if(terminate_read == true)
				{
				        Log.e(">>@@", "terminate handler_thread");
					DestroyAccessory();
				}
			}
		}
	}