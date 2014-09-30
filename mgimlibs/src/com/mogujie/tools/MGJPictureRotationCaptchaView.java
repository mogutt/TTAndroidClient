/**
* Filename:    MGJPictureRotationCaptchaView.java  
* Description:   
* @author:     shangqu  
* @version:    1.0  
* Create at:   2013-8-23 下午2:54:31   
*/

package com.mogujie.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.mogujie.im.libs.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;



/**
 * @author blank_ken
 *
 */
public class MGJPictureRotationCaptchaView extends LinearLayout {
	
	private View pictureBoard;
	private LayoutInflater inflater;
	@SuppressWarnings("unused")
	private Context context;
	private ImageView image1;
	private ImageView image2;
	private ImageView image3;
	private ImageView image4;
	
	// degree
	private int degree1 = 0;
	private int degree2 = 0;
	private int degree3 = 0;
	private int degree4 = 0;
	
	// click times
	private int clickTimes1 = 0;
	private int clickTimes2 = 0;
	private int clickTimes3 = 0;
	private int clickTimes4 = 0;
	
	// bitmap
	Bitmap imageSource1;
	Bitmap imageSource2;
	Bitmap imageSource3;
	Bitmap imageSource4;
	
	// 请求数据
	@SuppressWarnings("unused")
	private OnLoadFinishListener onLoadFinishListener;
	private HttpClient httpClient;
	String deviceId;
	private String captkey;
	
	// const
	private static final String BASE_URL = "http://www.mogujie.com/app_mgjtuan_v200_";
	@SuppressWarnings("unused")
	private static final String BASE_SURL = "https://www.mogujie.com/app_mgjtuan_v200_";
	private static final String UTILS_CAPTCHA_URL = BASE_URL + "util/captchaimg";
	private static final String PARAM_ATYPE_ANDROID = "_atype=android";
	private static final String PARAM_DID = "_did=";
	private final int UPDATE_UI = 100000;
	private final int NETWORK_ERROR = 200000;

	// 请求数据完成的接口
	private Handler imageGetHandler = new Handler(){
		
		

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_UI:
				image1.setImageBitmap(imageSource1);
				image2.setImageBitmap(imageSource2);
				image3.setImageBitmap(imageSource3);
				image4.setImageBitmap(imageSource4);
				image1.clearAnimation();
				image2.clearAnimation();
				image3.clearAnimation();
				image4.clearAnimation();
				break;
			case NETWORK_ERROR:
				Toast.makeText(getContext(), "验证码获取失败，请检查网络连接~", Toast.LENGTH_LONG).show();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	private interface OnLoadFinishListener{
		public void getData(Object data);
	}
	
	/**
	 * @param context
	 */
	public MGJPictureRotationCaptchaView(Context context) {
		this(context, null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public MGJPictureRotationCaptchaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		inflater = LayoutInflater.from(context);
		pictureBoard = inflater.inflate(R.layout.picutre_borad, this);
		image1 = (ImageView) pictureBoard.findViewById(R.id.image1);
		image2 = (ImageView) pictureBoard.findViewById(R.id.image2);
		image3 = (ImageView) pictureBoard.findViewById(R.id.image3);
		image4 = (ImageView) pictureBoard.findViewById(R.id.image4);
		
		image1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				((MGJRotatableImageView)v).setAngle((degree1 += 90)%360);
				runImageAnimation(v, degree1);
				degree1 = (degree1 + 90) % 360;
				clickTimes1++;
			}
		});
		image2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				((MGJRotatableImageView)v).setAngle((degree2 += 90)%360);
				runImageAnimation(v, degree2);
				degree2 = (degree2 + 90) % 360;
				clickTimes2++;
			}
		});
		image3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				((MGJRotatableImageView)v).setAngle((degree3 += 90)%360);
				runImageAnimation(v, degree3);
				degree3 = (degree3 + 90) % 360;
				clickTimes3++;
			}
		});
		image4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				((MGJRotatableImageView)v).setAngle((degree4 += 90)%360);
				runImageAnimation(v, degree4);
				degree4 = (degree4 + 90) % 360;
				clickTimes4++;
			}
		});
//		init();
	}
	
	public void init(){
		new Thread(requestRunnable).start();
	}
	
	public void refreshCode(){
		degree1 = 0;
		degree2 = 0;
		degree3 = 0;
		degree4 = 0;
		clickTimes1 = 0;
		clickTimes2 = 0;
		clickTimes3 = 0;
		clickTimes4 = 0;
		new Thread(requestRunnable).start();
	}
	
	public int[] getClickCount(){
		int[] array = new int[4];
		array[0] = clickTimes1%4;
		array[1] = clickTimes2%4;
		array[2] = clickTimes3%4;
		array[3] = clickTimes4%4;
		return array;
	}
	
	public int getClickTime(){
		return clickTimes1 + clickTimes2 + clickTimes3 + clickTimes4;
	}
	
	public String getClickString(){
		int[] array = getClickCount();
		return "" + array[0] + array[1] + array[2] + array[3];
	}
	
	private void runImageAnimation(View view, float fromDegrees){
		float centerX = view.getWidth()/2;
		RotateAnimation animation = null;
		animation = new RotateAnimation(fromDegrees, fromDegrees+90, centerX, centerX);
		animation.setDuration(150);
		animation.setFillAfter(true);
		view.setAnimation(animation);
		view.startAnimation(animation);
	}
	
	Runnable requestRunnable = new Runnable() {
		
		@Override
		public void run() {
			requestImageFromHttp();
		}
	};
	
	
	@SuppressWarnings("deprecation")
	private void requestImageFromHttp(){
		httpClient = new DefaultHttpClient();
		TelephonyManager tm = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
		deviceId = tm.getDeviceId();
		if(null != deviceId && deviceId.length() > 0) {
			deviceId = URLEncoder.encode(deviceId);
		}else {
			//没取到-取mac地址-
			String mac = getMacAddress(getContext());
			if(null != mac && mac.length() > 0){
				deviceId = "mac" + mac;
			}else{
				//取不到-给个默认值-
				deviceId = "mgj_tuan";
			}
		}
		String url = UTILS_CAPTCHA_URL + "?" + PARAM_ATYPE_ANDROID + "&" + PARAM_DID + deviceId;
		
		HttpGet httpGet = new HttpGet(url);
		Bitmap bitmap = null;
		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				Header[] headers = httpResponse.getHeaders("captkey");
				captkey = headers[0].getValue();
				HttpEntity entity = httpResponse.getEntity();
				InputStream inputStream = entity.getContent();
				bitmap = BitmapFactory.decodeStream(inputStream);
				if (null != bitmap) {
					cutPictureIn4(bitmap);
				}
				else {
					//不能在非UI线程里面调用toast，必须要用handler发出去
					imageGetHandler.sendEmptyMessage(NETWORK_ERROR);
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private void cutPictureIn4(Bitmap bitmap){
		int totalWidth = bitmap.getWidth();
		int singleWidth = totalWidth / 4;
		imageSource1 = Bitmap.createBitmap(bitmap, 0, 0, singleWidth, singleWidth);
		imageSource2 = Bitmap.createBitmap(bitmap, singleWidth, 0, singleWidth, singleWidth);
		imageSource3 = Bitmap.createBitmap(bitmap, singleWidth * 2, 0, singleWidth, singleWidth);
		imageSource4 = Bitmap.createBitmap(bitmap, singleWidth * 3, 0, singleWidth, singleWidth);
		imageGetHandler.sendEmptyMessage(UPDATE_UI);
	}
	
	private String getMacAddress(Context ctx){
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		return wifiInfo.getMacAddress().replaceAll(":", "");
	}
	
//	public void sendCaptchCode(){
//		if (null == captkey){
//			return ;
//		}
//		String captchCode = getClickString();
////		String url = UTILS_CAPTCHA_URL + "?" + PARAM_ATYPE_ANDROID + "&" + PARAM_DID + deviceId;
//	}
	
	public String getCaptkey(){
		return captkey;
	}
	
}
