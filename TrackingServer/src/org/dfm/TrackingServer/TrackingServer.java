package org.dfm.TrackingServer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.binary.Base64;

import java.net.URLDecoder;
import java.util.Vector;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * Servlet implementation class TrackingServer
 */
@WebServlet(description = "Extracts information fields from the URL and puts it into MySql database", urlPatterns = { "/TrackingServer" })
public class TrackingServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/**
	 * TODO  enable via logging class
	 */
	 static Boolean enableLogger =false;
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	static Vector<String> vec = new Vector<String>();
	private static String payload = null;
	private static String uid = null;
	private static String auth = null;
	private static String cookie = null;
	private static String json = null;
	private static String mid = null;
	private static String jsonurl = null;
	private static String ua = null;
	private static String libver = null;
	private static String iniref = null;
	private static String uname = null;
	private static String ename = null;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		payload = request.getPathInfo();
		decodeURL(payload);

	}

	private static void extractParts(String decodedurl) {
		String[] localurl = decodedurl.split("==");
		localurl[0] = localurl[0].replaceAll("\"", " ");
		localurl[0] = localurl[0].replace("/", " ").replace("?", " ")
				.replace("=", " ").replace("&", " ");
		String[] urlparts = localurl[0].split(" ");
		for (String x : urlparts) {
			if (!x.trim().equals("")) {
				vec.add(x);
			}
		}
		for (String x : vec) {
			int index = vec.indexOf(x);
			if (x.trim().equals("events")) {
				uid = vec.elementAt(index + 1);

			}
			if (x.trim().equals("auth")) {
				auth = vec.elementAt(index + 1);

			}
			if (x.trim().equals("uid")) {
				cookie = vec.elementAt(index + 1);

			}
			if (x.trim().equals("event")) {
				json = vec.elementAt(index + 1);

			}
			if (x.trim().equals("mid")) {
				mid = vec.elementAt(index + 1);

			}
		}
		base64utf();

	}

	private static void base64utf() {
		try {
			auth = new String(Base64.decodeBase64(auth), "UTF-8");
			cookie = new String(Base64.decodeBase64(cookie), "UTF-8");
			json = new String(Base64.decodeBase64(json), "UTF-8");
			decodeJSON();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @brief
	 * @param
	 */
	private static void decodeURL(String payload) {
		try {
			String temp = "127.0.0.1 - - [Tue, 21 May 2013 22:26:48 GMT] GET /events/markethealth?auth=VUEtMTIzNDM=&uid=MTNlYzkyZjQzODUtM2M4ZjQ4MGUtYWE2MC00MWJhLWFiYTAtZDkyZjBkYjg1N2Y2&event=eyIkcGFnZV9pbmZvIjp7InVybCI6Imh0dHA6Ly9hc3NldHMuZGVlcGZvcmVzdG1lZGlhLmNvbS9pbmRleC5odG1sIiwidWEiOiJNb3ppbGxhLzUuMCAoTWFjaW50b3NoOyBJbnRlbCBNYWMgT1MgWCAxMF84XzMpIEFwcGxlV2ViS2l0LzUzNy4zMSAoS0hUTUwsIGxpa2UgR2Vja28pIENocm9tZS8yNi4wLjE0MTAuNjUgU2FmYXJpLzUzNy4zMSJ9LCIkbGliX3ZlciI6IjAuOS4yIiwiaW5pdGlhbFJlZmVycmVyIjoiIiwidXNlcm5hbWUiOiJ0ZXN0ZXIiLCIkZXZlbnRfbmFtZSI6IiRwYWdldmlldyIsIiRwYWdlIjoiaHR0cDovL2Fzc2V0cy5kZWVwZm9yZXN0bWVkaWEuY29tL2luZGV4Lmh0bWwifQ==&_=MTM2OTE3NTIwODI1Mg== HTTP/1.1 200 2 http://assets.deepforestmedia.com/index.html Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31";
			String decodedurl = URLDecoder.decode(temp, "UTF-8");
			extractParts(decodedurl);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void decodeJSON() {
		JsonParser parser = new JsonParser();
		JsonObject obj = parser.parse(json).getAsJsonObject();
		String pageinfo = obj.get("$page_info").toString();
		JsonObject obj1 = parser.parse(pageinfo).getAsJsonObject();
		jsonurl = obj1.get("url").toString();
		ua = obj1.get("ua").toString();
		libver = obj.get("$lib_ver").toString();
		iniref = obj.get("initialReferrer").toString();
		uname = obj.get("username").toString();
		ename = obj.get("$event_name").toString();

	}

	private static void writetodb() {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		payload = request.getPathInfo();
		decodeURL(payload);
		// TODO Auto-generated method stub
	}

}
