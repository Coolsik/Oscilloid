package com.swssm.oscilloid;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class RotaryKnobView extends ImageView {
	private float angle = 0f;
	private float theta_old = 0f;
	
	Variable vari = null;
	
	private RotaryKnobListener listener;
	
	public interface RotaryKnobListener {
		public void onKnobChanged(float delta,float angle);
	}
	public void setKnobListener(RotaryKnobListener lis) {
		listener = lis;
	}
	public RotaryKnobView(Context context) {
		super(context);
	}
	public RotaryKnobView(Context context,AttributeSet attrs) {
		super(context,attrs);
		initialize();
	}
	public RotaryKnobView(Context context,AttributeSet attrs,int defStyle) {
		super(context,attrs,defStyle);
	}
	private float getTheta(float x,float y) {
		float sx = x - (getWidth() / 2.0f);
		float sy = y - (getHeight() / 2.0f);
		
		float length = (float)Math.sqrt(sx*sx+sy*sy);
		float nx = sx / length;
		float ny = sy / length;
		float theta = (float)Math.atan2(ny, nx);
		
		final float rad2deg = (float)(180.0 / Math.PI);
		float thetaDeg = theta * rad2deg;
		
		return (thetaDeg < 0) ? thetaDeg+360.0f : thetaDeg;
	}
	public void initialize() {
		this.setImageResource(R.drawable.jog);
		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				float x = event.getX(0);
				float y = event.getY(0);
				float theta = getTheta(x, y);
				//Log.d("Test","x : " + x + " y : " + y + " theta : " + theta);
				switch(event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_POINTER_DOWN:
					theta_old = theta;
					break;
				case MotionEvent.ACTION_DOWN:
					vari.Angle_cnt =0;
					break;
				case MotionEvent.ACTION_UP:
					break;
				case MotionEvent.ACTION_MOVE:
					invalidate();
					float delta_theta = theta - theta_old;
					theta_old = theta;
					angle = theta-270;
					notifyListener(delta_theta,(theta+90)%360);
					break;
				}
				return true;
			}
		});
	}
	private void notifyListener(float delta,float angle) {
		if(listener != null) listener.onKnobChanged(delta,angle);
	}
	protected void onDraw(Canvas c) {
		c.rotate(angle, getWidth()/2, getHeight()/2);
		super.onDraw(c);
	}
}
