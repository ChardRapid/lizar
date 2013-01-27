package com.lizar.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.lizar.util.StringHelper;

public class Http {

	public static boolean is_IE(HttpServletRequest request) {
		String user_agent = request.getHeader("user-agent");
		if (user_agent.toLowerCase().indexOf("msie") != -1) {
			return true;
		}
		return false;
	}

	public static String getParameter(HttpServletRequest request, String name,
			String encode_type, boolean need_trans_code, String defaultValue)
			throws UnsupportedEncodingException {
		String value = "";
		if (need_trans_code) {
			try {
				value = Http.escape(new String(request.getParameter(name)
						.getBytes("ISO-8859-1"), encode_type));
			} catch (Exception e) {
				return defaultValue;
			}
		} else {
			value = Http.escape(request.getParameter(name));
		}
		if (StringHelper.isNull(value))
			value = defaultValue;
		return value;
	}

	public static String url_internal_indicator_filter(String url) {
		if (StringHelper.isNull(url))
			return url;
		int pos = url.indexOf("#");
		if (pos == -1)
			return url;
		return url.substring(0, pos);
	}

	public static String getParameter(HttpServletRequest request, String name,
			String encode_type, String defaultValue) {
		String param = request.getParameter(name);
		if (param == null || param.trim().equals("")) {
			return defaultValue;
		}
		return escape(param);
	}

	public static String getParameter(HttpServletRequest request, String name,
			String defaultValue) {
		String param = request.getParameter(name);
		if (param == null || param.trim().equals("")) {
			return defaultValue;
		}
		return escape(param);
	}

	public static String encode_url_chinese(String url, String encode_type)
			throws UnsupportedEncodingException {
		if (StringHelper.isNull(url))
			return url;
		if (StringHelper.isNull(encode_type))
			encode_type = "UTF-8";
		StringBuilder s = new StringBuilder(url.length());
		for (int i = 0; i < url.length(); i++) {
			char c = url.charAt(i);
			if (c >= '\u4e00' && c <= '\u9fa5') {
				s.append(URLEncoder.encode(String.valueOf(c), encode_type));
			} else {
				s.append(c);
			}
		}
		return s.toString();
	}

	public static String get_encode_type(String content_type,
			String default_content_type) {
		if (StringHelper.isNull(content_type))
			return default_content_type;
		int pos = content_type.toLowerCase().indexOf("charset=") + 8;
		int last_pos = content_type.indexOf(";", pos);
		if (last_pos == -1)
			return content_type.substring(pos, content_type.length()).trim();
		return content_type.substring(pos, last_pos).trim();
	}

	public static boolean is_local_url(String domain) {
		if (domain.startsWith("10.") || domain.startsWith("192.168."))
			return true;
		if (domain.startsWith("172.")) {
			int end = domain.indexOf(".", 4);
			String range = domain.substring(4, end);
			if (StringHelper.isInteger(range)) {
				int i = Integer.parseInt(range);
				if (i < 32 && i > 15)
					return true;
			}
		}
		if (domain.equals("localhost"))
			return true;
		if (domain.equals("127.0.0.1"))
			return true;
		return false;
	}

	public static boolean is_local_ip(String ip) {
		if (ip.startsWith("10.") || ip.startsWith("192.168."))
			return true;
		if (ip.startsWith("172.")) {
			int end = ip.indexOf(".", 4);
			String range = ip.substring(4, end);
			if (StringHelper.isInteger(range)) {
				int i = Integer.parseInt(range);
				if (i < 32 && i > 15)
					return true;
			}
		}
		if (ip.equals("127.0.0.1"))
			return true;
		return false;
	}

	public static String escape(String param) {
		if (StringHelper.isNull(param))
			return "";
		return param.replaceAll("'", "\"");
	}

	public static String _p_flter(String content) {
		content = content.replaceAll("<p>", "");
		content = content.replaceAll("</p>", "");
		return content;
	}

	public static String getWebPath(HttpServletRequest request, String path) {
		StringBuilder result = new StringBuilder(request.getScheme());
		result.append("://").append(request.getServerName()).append(":")
				.append(request.getServerPort()).append(
						request.getContextPath()).append("/").append(path);
		return path.toString();

	}

	public static Object getAttribute(HttpServletRequest request, String name,
			Object defaultValue) {
		Object param = request.getAttribute(name);
		if (param == null) {
			return defaultValue;
		}
		return param;
	}

	public static int getParameter(HttpServletRequest request, String name,
			int defaultValue) {
		int value = 0;
		try {
			value = Integer.parseInt(request.getParameter(name));
		} catch (Exception e) {
			value = defaultValue;
		}
		return value;
	}

	public static long getParameter(HttpServletRequest request, String name,
			long defaultValue) {
		long value = 0;
		try {
			value = Long.parseLong(request.getParameter(name).toString());
		} catch (Exception e) {
			value = defaultValue;
		}
		return value;
	}

	public static int getAttribute(HttpServletRequest request, String name,
			int defaultValue) {
		int value = 0;
		try {
			value = Integer.parseInt(request.getAttribute(name).toString());
		} catch (Exception e) {
			value = defaultValue;
		}
		return value;
	}

	public static long getAttribute(HttpServletRequest request, String name,
			long defaultValue) {
		long value = 0;
		try {
			value = Long.parseLong(request.getAttribute(name).toString());
		} catch (Exception e) {
			value = defaultValue;
		}
		return value;
	}

	public static char getParameter(HttpServletRequest request, String name,
			char defaultValue) {
		char value = 0;
		try {
			value = request.getParameter(name).charAt(0);
		} catch (Exception e) {
			value = defaultValue;
		}
		return value;
	}

	public static String getToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		String token = null;
		if (cookies == null) {
			return null;
		}
		for (Cookie c : cookies) {
			if (StringHelper.equals(c.getName(), "token")) {
				token = c.getValue();
			}
		}

		return token;
	}

	public static String getRemoteIP(HttpServletRequest request) {
		return request.getRemoteAddr();
	}

	public static String encode(String s) {
		if (s == null)
			return "";
		try {
			return URLEncoder.encode(s, "UTF-8").replace("+", "%20").replace(
					"*", "%2A").replace("%7E", "~").replace("#", "%23");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	public static String decode(String s) {
		if (s == null)
			return "";
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	public static String getWholeDomain(String url) {
		if (StringHelper.isNull(url))
			return "";
		if (!url.startsWith("http://") && !url.startsWith("https://")
				&& !url.startsWith("ftp://") && !url.startsWith("sftp://"))
			return "";
		String result = url.substring(url.indexOf("://") + 3, url.length());
		int rest_point = result.indexOf("/");
		if (rest_point == -1) {
			if (!StringHelper
					.is_normal_char(result.charAt(result.length() - 1)))
				return result.substring(0, result.length() - 1);
			return result;
		}
		result = result.substring(0, rest_point);
		if (result.indexOf(".") == result.lastIndexOf(".")) {
			result = "www." + result;
		}
		if (!StringHelper.is_normal_char(result.charAt(result.length() - 1)))
			return result.substring(0, result.length() - 1);
		return result;
	}

	public static String get_whole_domain_without_auto_www(String url) {
		if (StringHelper.isNull(url))
			return "";
		if (!url.startsWith("http://") && !url.startsWith("https://")
				&& !url.startsWith("ftp://") && !url.startsWith("sftp://"))
			return "";
		String result = url.substring(url.indexOf("://") + 3, url.length());
		int rest_point = result.indexOf("/");
		if (rest_point == -1) {
			if (!StringHelper
					.is_normal_char(result.charAt(result.length() - 1)))
				return result.substring(0, result.length() - 1);
			return result;
		}
		result = result.substring(0, rest_point);
		if (!StringHelper.is_normal_char(result.charAt(result.length() - 1)))
			return result.substring(0, result.length() - 1);
		return result;
	}

	public static String htmlFiler(String html) {
		Pattern pattern = Pattern.compile("<[\\/\\!]*?[^<>]*?>|[(\r\n)|\r]"); // \s
																				// 空白字符：[
																				// \t\n\x0B\f\r]
																				// 可以说是一个空格
		Matcher matcher = pattern.matcher(html);
		html = matcher.replaceAll(" ");

		pattern = Pattern.compile("&lt;");
		matcher = pattern.matcher(html);
		html = matcher.replaceAll("<");

		pattern = Pattern.compile("&gt;");
		matcher = pattern.matcher(html);
		html = matcher.replaceAll(">");

		pattern = Pattern.compile("&nbsp;");
		matcher = pattern.matcher(html);
		html = matcher.replaceAll(" ");
		return html.replaceAll(" ", " ").replaceAll(" +", " ");
	}

	public static String get(String url) throws IOException {
		StringBuilder content = new StringBuilder();
		URL netUrl = new URL(url);
		HttpURLConnection httpConn = (HttpURLConnection) netUrl
				.openConnection();
		httpConn.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpConn.getInputStream()));
		String temp;
		while ((temp = reader.readLine()) != null) {
			content.append(temp);
		}
		reader.close();
		httpConn.disconnect();
		return content.toString();
	}

	public static Map<String, List<String>> head(String u) throws IOException {
		URL url = new URL(u);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("HEAD");
		return conn.getHeaderFields();
	}

	public static String get(String url, int timeout) throws IOException {
		StringBuilder content = new StringBuilder();
		URL netUrl = new URL(url);
		HttpURLConnection httpConn = (HttpURLConnection) netUrl
				.openConnection();
		httpConn.setReadTimeout(timeout);
		httpConn.setConnectTimeout(timeout);
		System
				.setProperty("sun.net.client.defaultConnectTimeout", timeout
						+ "");
		System.setProperty("sun.net.client.defaultReadTimeout", timeout + "");
		httpConn.connect();
		BufferedReader reader = null;
		try {
			InputStreamReader inputs = new InputStreamReader(httpConn
					.getInputStream(), get_encode_type(httpConn
					.getHeaderField("content-type"), "utf-8"));
			reader = new BufferedReader(inputs);
		} catch (Exception e) {
			reader = new BufferedReader(new InputStreamReader(httpConn
					.getInputStream()));
		}
		String temp;
		while ((temp = reader.readLine()) != null) {
			content.append(temp);
		}
		reader.close();
		httpConn.disconnect();
		return content.toString();
	}

	public static String get(String url, String encode) throws IOException {
		StringBuilder content = new StringBuilder();
		URL netUrl = new URL(url);
		HttpURLConnection httpConn = (HttpURLConnection) netUrl
				.openConnection();
		httpConn.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				httpConn.getInputStream(), encode));
		String temp;
		while ((temp = reader.readLine()) != null) {
			content.append(temp);
		}
		reader.close();
		httpConn.disconnect();
		return content.toString();
	}

	public static String check_domain(String domain) {
		if (StringHelper.isNull(domain))
			return "domain is null";
		char start = domain.charAt(0);
		char end = domain.charAt(domain.length() - 1);
		if (!((start <= '9' && start >= '0') || (start <= 'z' && start >= 'a') || (start <= 'Z' && start >= 'A'))) {
			return "wrong args";
		}
		if (!((end <= '9' && end >= '0') || (end <= 'z' && end >= 'a') || (end <= 'Z' && end >= 'A'))) {
			return "wrong args";
		}
		for (int i = 1; i < domain.length() - 1; i++) {
			start = domain.charAt(i);
			if (!((start <= '9' && start >= '0')
					|| (start <= 'z' && start >= 'a')
					|| (start <= 'Z' && start >= 'A') || start == '.')) {
				return "wrong args";
			}
		}
		return null;
	}

	public static String post(String url, Map<String, String> param, int timeout)
			throws IOException {
		URL _url = new URL(url);
		StringBuilder content = new StringBuilder();
		URLConnection conn = _url.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		out.write(getSerialParameters(param, true));
		out.flush();
		out.close();
		InputStream in = conn.getInputStream();
		byte[] b = new byte[1024];
		while (in.read(b) >= 0) {
			content.append(new String(b, "UTF-8").trim());
		}
		return content.toString();
	}

	public static String post(String url, String data, int timeout)
			throws IOException {
		URL _url = new URL(url);
		StringBuilder content = new StringBuilder();
		URLConnection conn = _url.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		out.write(data);
		out.flush();
		out.close();
		InputStream in = conn.getInputStream();
		byte[] b = new byte[1024];
		while (in.read(b) >= 0) {
			content.append(new String(b, "UTF-8").trim());
		}
		return content.toString();
	}

	private static String getSerialParameters(Map<String, String> parameters,
			boolean onlySerialValue) throws UnsupportedEncodingException {
		String str = "";
		int i = 0;
		for (Entry<String, String> e : parameters.entrySet()) {
			if (onlySerialValue) {
				str += e.getKey() + "="
						+ URLEncoder.encode(e.getValue(), "UTF-8");

			} else {
				str += e.getKey() + "=" + e.getValue();
			}
			if (i < parameters.size() - 1) {
				str += "&";
			}
			i++;
		}
		if (!onlySerialValue) {
			str = URLEncoder.encode(str, "UTF-8");
		}
		return str;
	}

	public static String avoid_tag(String html) {
		if (StringHelper.isNotNull(html)) {
			html = html.replaceAll("\n", " ");
			html = html.replaceAll("\t", " ");
			html = html.replaceAll("<", "&lt;");
			html = html.replaceAll(">", "&gt;");
			html = html.replaceAll("'", "&apos;");
			html = html.replaceAll("\"", "&qout;");
		}
		return html;
	}

	public static boolean from_android(HttpServletRequest request) {
		// TODO Auto-generated method stub
		String user_agent=request.getHeader("User-Agent");
		System.out.println("user agent "+user_agent);
		if(user_agent.toLowerCase().contains("android"))return true;
		return false;
	}

	public static void main(String[] args) throws IOException {

	}

}
