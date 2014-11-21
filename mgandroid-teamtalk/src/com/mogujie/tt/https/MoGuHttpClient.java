package com.mogujie.tt.https;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.utils.FileUtil;

public class MoGuHttpClient {

	@SuppressWarnings("unused")
	private static int TIMEOUT = 30 * 1000;

	private static Logger logger = Logger.getLogger(MoGuHttpClient.class);

	private final int UPLOAD_STATUS_SUCCESS_MIN = 300;
	private final int UPLOAD_STATUS_SUCCESS_MAX = 307;
	private final String CONTENT_TYPE = "multipart/form-data";
	String BOUNDARY = UUID.randomUUID().toString();
	String PREFIX = "--", LINE_END = "\r\n";
	private DefaultHttpClient httpClient = null;
	private int nTimeOut = 30 * 1000;

	/**
	 * Op Http get request
	 * 
	 * @param url
	 * @param mapSelectedList
	 *            Values to request
	 * @return
	 */

	public HttpResponse get(String url, HashMap<String, String> mapParam,
			HashMap<String, String> mapHeader) {

		int i = 0;
		for (Map.Entry<String, String> entry : mapParam.entrySet()) {

			// logger.i(entry.getKey() + "=>" + entry.getValue());
			if (i == 0) {
				url = url + "?" + entry.getKey() + "=" + entry.getValue();
			} else {
				url = url + "&" + entry.getKey() + "=" + entry.getValue();
			}

			i++;

		}
		return post(url, null, mapHeader);

	}

	/**
	 * post请求
	 * 
	 * @param urlString
	 * @param params
	 * @return
	 */
	public HttpResponse post(String url, HashMap<String, String> map,
			HashMap<String, String> mapHeader) {

		// DefaultHttpClient httpClient = getNewHttpClient();
		httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, nTimeOut);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				nTimeOut);
		HttpPost post = new HttpPost(url);
		if (null != mapHeader) {
			for (Map.Entry<String, String> entry : mapHeader.entrySet()) {
				post.addHeader(entry.getKey(), entry.getValue());
			}
		}

		ArrayList<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
		if (null != map) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				BasicNameValuePair pair = new BasicNameValuePair(
						entry.getKey(), entry.getValue());
				pairList.add(pair);
			}
		}

		try {
			HttpEntity entity = new UrlEncodedFormEntity(pairList, HTTP.UTF_8);
			post.setEntity(entity);

			HttpResponse response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return response;
			}
		} catch (OutOfMemoryError e) {
			logger.e(e.getMessage() + post.toString());
		} catch (Exception e) {
			logger.e(e.getMessage() + post.toString());
		}
		return null;
	}

	@SuppressWarnings("unused")
	private DefaultHttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new SSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			logger.e(e.getMessage());
			return new DefaultHttpClient();
		}
	}

	public InputStream download(String strUrl) throws MalformedURLException {
		URL url = new URL(strUrl);
		HttpURLConnection conn = null;
		InputStream inputStream = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setReadTimeout(SysConstant.HTTP_TIME_OUT);
			inputStream = conn.getInputStream();
		} catch (Exception e) {
			logger.e(e.getMessage());
		}
		return inputStream;
	}

	public String uploadImage(String strUrl, byte[] fileBytes, String fileName,
			String strdao) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(strUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setReadTimeout(SysConstant.HTTP_TIME_OUT);
			conn.setConnectTimeout(SysConstant.HTTP_TIME_OUT);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Accept-Encoding", "");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Cookie", "__dao=" + strdao);
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
					+ BOUNDARY);
			long total = 0;
			StringBuffer sbf = getBodyHead(fileName);
			total += sbf.toString().getBytes().length;
			total += fileBytes.length;
			total += LINE_END.getBytes().length;
			total += (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes().length;
			conn.setRequestProperty("Content-Length", String.valueOf(total));

			if (fileBytes.length > 0) {
				OutputStream outputSteam = conn.getOutputStream();
				DataOutputStream dos = new DataOutputStream(outputSteam);
				StringBuffer sBuffer = getBodyHead(fileName);
				dos.write(sBuffer.toString().getBytes());
				dos.write(fileBytes, 0, fileBytes.length);
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
						.getBytes();
				dos.write(end_data);
				dos.flush();
				int res = conn.getResponseCode();
				if (res >= UPLOAD_STATUS_SUCCESS_MIN
						&& res <= UPLOAD_STATUS_SUCCESS_MAX) {
					String location = conn.getHeaderField("Location");
					String path = location
							.substring(location.lastIndexOf("=") + 1);
					return path;
				}
			}
		} catch (MalformedURLException e) {
			logger.e(e.getMessage());
		} catch (IOException e) {
			logger.e(e.getMessage());
		} finally {
			if (null != conn) {
				conn.disconnect();
				conn = null;
			}
		}
		return null;
	}

	public String uploadImage3(String strUrl, byte[] bytes, String fileName) {
		logger.d("pic#uploadImage3 strUlr:%s", strUrl);
		List<String> list = new ArrayList<String>(); // 要上传的文件名,如：d:\haha.doc.你要实现自己的业务。我这里就是一个空list.
		list.add(fileName);
		try {
			String BOUNDARY = "---------7d4a6d158c9"; // 定义数据分隔线
			URL url = new URL(strUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			conn.setRequestProperty("Charsert", "UTF-8");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY);

			OutputStream out = new DataOutputStream(conn.getOutputStream());
			byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线
			int leng = list.size();
			for (int i = 0; i < leng; i++) {
				String fname = list.get(i);
				File file = new File(fname);
				StringBuilder sb = new StringBuilder();
				sb.append("--");
				sb.append(BOUNDARY);
				sb.append("\r\n");
				sb.append("Content-Disposition: form-data;name=\"file" + i
						+ "\";filename=\"" + file.getName() + "\"\r\n");
				sb.append("Content-Type:application/octet-stream\r\n\r\n");

				byte[] data = sb.toString().getBytes();
				out.write(data);
				out.write(bytes);
				out.write("\r\n".getBytes()); // 多个文件时，二个文件之间加入这个
			}
			out.write(end_data);
			out.flush();
			out.close();

			// 定义BufferedReader输入流来读取URL的响应
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				logger.d("pic#line:%s", line);

				// todo eric
				/*
				 * {"error_code":0,"error_msg":
				 * "成功","path":"g0/000/000/1410706133246550_140184328214.jpg"
				 * ,"url":
				 * "http://122.225.68.125:8001/g0/000/000/1410706133246550_140184328214.jpg"
				 * }
				 */
				JSONObject root = new JSONObject(line);
				return root.getString("url");
			}

		} catch (Exception e) {
			System.out.println("pic#发送POST请求出现异常！" + e);
			e.printStackTrace();
		}

		return "";
	}

	public String uploadImage2(String strUrl, byte[] fileBytes, String fileName) {
		logger.d("chat#uploadImage2 strUrl:%s, fileBytes:%d, fileName:%s",
				strUrl, fileBytes.length, fileName);

		HttpURLConnection conn = null;
		try {
			URL url = new URL(strUrl);
			conn = (HttpURLConnection) url.openConnection();
			if (conn == null) {
				logger.e("chat#openConnection failed");
				return null;
			}

			conn.setInstanceFollowRedirects(false);
			// conn.setReadTimeout(SysConstant.HTTP_TIME_OUT);
			// conn.setConnectTimeout(SysConstant.HTTP_TIME_OUT);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Accept-Encoding", "");
			conn.setRequestProperty("Connection", "Keep-Alive");
			// conn.setRequestProperty("Cookie", "__dao=" + strdao);
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
					+ BOUNDARY);
			long total = 0;
			StringBuffer sbf = getBodyHead2(fileName);
			total += sbf.toString().getBytes().length;
			total += fileBytes.length;
			total += LINE_END.getBytes().length;
			total += (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes().length;
			conn.setRequestProperty("Content-Length", String.valueOf(total));

			if (fileBytes.length > 0) {
				OutputStream outputSteam = conn.getOutputStream();
				DataOutputStream dos = new DataOutputStream(outputSteam);
				StringBuffer sBuffer = getBodyHead(fileName);
				dos.write(sBuffer.toString().getBytes());
				dos.write(fileBytes, 0, fileBytes.length);
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
						.getBytes();
				dos.write(end_data);
				dos.flush();
				int res = conn.getResponseCode();
				logger.d("chat#resCode:%d", res);

				// if (res >= UPLOAD_STATUS_SUCCESS_MIN
				// && res <= UPLOAD_STATUS_SUCCESS_MAX) {
				// String location = conn.getHeaderField("Location");
				// String path = location
				// .substring(location.lastIndexOf("=") + 1);
				// return path;
				// }
			}
		} catch (MalformedURLException e) {
			logger.e(e.getMessage());
		} catch (IOException e) {
			logger.e(e.getMessage());
		} finally {
			if (null != conn) {
				conn.disconnect();
				conn = null;
			}
		}
		return null;
	}

	public String uploadImage(String strUrl, String filePath, String strdao) {
		HttpURLConnection conn = null;
		File file = new File(filePath);
		try {
			URL url = new URL(strUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setReadTimeout(SysConstant.HTTP_TIME_OUT);
			conn.setConnectTimeout(SysConstant.HTTP_TIME_OUT);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Accept-Encoding", "");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Cookie", "__dao=" + strdao);
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
					+ BOUNDARY);
			long total = 0;
			StringBuffer sbf = getBodyHead(file.getName());
			total += sbf.toString().getBytes().length;
			total += FileUtil.getFileLen(file);
			total += LINE_END.getBytes().length;
			total += (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes().length;
			conn.setRequestProperty("Content-Length", String.valueOf(total));

			if (null != file) {
				OutputStream outputSteam = conn.getOutputStream();
				DataOutputStream dos = new DataOutputStream(outputSteam);
				StringBuffer sBuffer = getBodyHead(file.getName());
				dos.write(sBuffer.toString().getBytes());
				InputStream is = new FileInputStream(file);
				byte[] bytes = new byte[1024];
				int len = 0;

				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
						.getBytes();
				dos.write(end_data);
				dos.flush();
				int res = conn.getResponseCode();
				if (res >= UPLOAD_STATUS_SUCCESS_MIN
						&& res <= UPLOAD_STATUS_SUCCESS_MAX) {
					String location = conn.getHeaderField("Location");
					String path = location
							.substring(location.lastIndexOf("=") + 1);
					return path;
				}
			}
		} catch (MalformedURLException e) {
			logger.e(e.getMessage());
		} catch (IOException e) {
			logger.e(e.getMessage());
		} finally {
			if (null != conn) {
				conn.disconnect();
				conn = null;
			}
		}
		return null;
	}

	private StringBuffer getBodyHead2(String fileName) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(PREFIX + BOUNDARY);

		sBuffer.append(LINE_END);
		sBuffer.append("content-disposition: form-data; name=\"image\"; filename=\""
				+ fileName + "\"" + LINE_END);
		sBuffer.append("Content-Type: image/jpeg" + LINE_END);
		sBuffer.append(LINE_END);
		return sBuffer;
	}

	private StringBuffer getBodyHead(String fileName) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(PREFIX);
		sBuffer.append(BOUNDARY);
		sBuffer.append(LINE_END);
		sBuffer.append("content-disposition: form-data; name=\"type\""
				+ LINE_END);
		sBuffer.append(LINE_END);
		sBuffer.append("im_image" + LINE_END);
		sBuffer.append(PREFIX + BOUNDARY);

		sBuffer.append(LINE_END);
		sBuffer.append("content-disposition: form-data; name=\"image\"; filename=\""
				+ fileName + "\"" + LINE_END);
		sBuffer.append("Content-Type: image/jpeg" + LINE_END);
		sBuffer.append(LINE_END);
		return sBuffer;
	}

	public void setTimeout(int nSeconds) {
		nTimeOut = nSeconds * 1000;
	}
}
