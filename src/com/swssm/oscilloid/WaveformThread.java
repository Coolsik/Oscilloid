package com.swssm.oscilloid;

import android.graphics.*;
import android.util.*;
import android.view.*;

public class WaveformThread extends Thread {
	private SurfaceHolder holder;
	private WaveView plot_area;
	private boolean _run = false;
	
	public WaveformThread(SurfaceHolder surfaceHolder,WaveView view) {
		this.holder = surfaceHolder;
		this.plot_area = view;
	}
	public void setRunning(boolean run) {
		this._run = run;
	}
	public void run() {
		Canvas canvas;
		while(_run) {
			canvas = null;
			try {
				canvas = holder.lockCanvas(null);
				synchronized (holder) {
					plot_area.PlotPoints(canvas);
				}
			} catch(NullPointerException e) { 
				Log.d("Test","NullPointerException");
			} finally {
				if(canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}
}
