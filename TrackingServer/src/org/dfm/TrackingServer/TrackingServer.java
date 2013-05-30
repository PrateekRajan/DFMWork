package org.dfm.TrackingServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.binary.Base64;

import java.net.URLDecoder;
import java.sql.*;
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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	JsonParser parser = new JsonParser();
	private Vector<String> vec = new Vector<String>();
	private Boolean writtentodb = false;
	private String payload = null;
	private String decodedurl = null;
	private String uid = null;
	private String auth = null;
	private String cookie = null;
	private String json = null;
	private String mid = null;
	private String jsonurl = null;
	private String ua = null;
	private String libver = null;
	private String iniref = null;
	private String uname = null;
	private String ename = null;

	/**
	 * @author Prateek
	 * @breif Gets the request from the browser and forwards it for parsing
	 * @param HTTP
	 *            request
	 * @param HTTP
	 *            response
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		TrackingServer ts = null;
		if (ts == null) {
			ts = new TrackingServer();
		}
		payload = request.getQueryString();
		decodedurl = ts.decodeURL(payload);
		ts.extractParts(decodedurl);
		ts.base64utf();
		ts.decodeJSON();
		writtentodb = ts.writetodb();
		if (writtentodb.booleanValue() == true) {
			OutputStreamWriter writer = new OutputStreamWriter(
					response.getOutputStream(), "UTF-8");
			String browser_output = "<html><head></head><body><h1>Success<h1><p> The data has been added to the database</p></body></html>";
			writer.write(browser_output);
			writer.flush();
			ts.clear();

		} else {
			System.out.println("There was an error writing to the database");
		}

	}

	/**
	 * @author Prateek
	 * @param decodedurl
	 */
	private void extractParts(String decodedurl) {
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

	}

	/**
	 * @author Prateek
	 * @brief Clears the variables after the necessary information is uploaded
	 *        into the database
	 */
	private void clear() {
		payload = null;
		decodedurl = null;
		uid = null;
		auth = null;
		cookie = null;
		json = null;
		mid = null;
		jsonurl = null;
		ua = null;
		libver = null;
		iniref = null;
		uname = null;
		ename = null;
	}

	/**
	 * @author Prateek
	 * @brief Converts the base64 string to UTF-8 format for later processing
	 * @param auth
	 * @param cookie
	 * @param json
	 */
	private void base64utf() {
		try {
			auth = new String(Base64.decodeBase64(auth), "UTF-8");
			cookie = new String(Base64.decodeBase64(cookie), "UTF-8");
			json = new String(Base64.decodeBase64(json), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @author Prateek
	 * @brief Decodes the URL (Removes the URL encoding introduced in the URL
	 *        definition)
	 * @param payload
	 * @return decodedurl
	 */
	private String decodeURL(String payload) {
		try {
			// String temp =
			// "127.0.0.1 - - [Tue, 21 May 2013 22:26:48 GMT] GET /events/markethealth?auth=VUEtMTIzNDM=&uid=MTNlYzkyZjQzODUtM2M4ZjQ4MGUtYWE2MC00MWJhLWFiYTAtZDkyZjBkYjg1N2Y2&event=eyIkcGFnZV9pbmZvIjp7InVybCI6Imh0dHA6Ly9hc3NldHMuZGVlcGZvcmVzdG1lZGlhLmNvbS9pbmRleC5odG1sIiwidWEiOiJNb3ppbGxhLzUuMCAoTWFjaW50b3NoOyBJbnRlbCBNYWMgT1MgWCAxMF84XzMpIEFwcGxlV2ViS2l0LzUzNy4zMSAoS0hUTUwsIGxpa2UgR2Vja28pIENocm9tZS8yNi4wLjE0MTAuNjUgU2FmYXJpLzUzNy4zMSJ9LCIkbGliX3ZlciI6IjAuOS4yIiwiaW5pdGlhbFJlZmVycmVyIjoiIiwidXNlcm5hbWUiOiJ0ZXN0ZXIiLCIkZXZlbnRfbmFtZSI6IiRwYWdldmlldyIsIiRwYWdlIjoiaHR0cDovL2Fzc2V0cy5kZWVwZm9yZXN0bWVkaWEuY29tL2luZGV4Lmh0bWwifQ==&_=MTM2OTE3NTIwODI1Mg== HTTP/1.1 200 2 http://assets.deepforestmedia.com/index.html Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31";
			decodedurl = URLDecoder.decode(payload, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decodedurl;
	}

	/**
	 * @author Prateek
	 * @brief Extracts different parts from the JSON object received with the
	 *        GET request
	 */
	private void decodeJSON() {
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

	/**
	 * @brief Writes the extracted information (stored in variables) into SQlite
	 *        database
	 * @author Prateek
	 * @return writtentodb
	 */
	private Boolean writetodb() {
		Connection con = null;
		Statement statement = null;
		try {
			Class.forName("org.sqlite.JDBC");
			con = DriverManager
					.getConnection("jdbc:sqlite:C:/Users/Prateek/Desktop/mydatabase.sqlite");
			statement = con.createStatement();
			String query = "INSERT INTO URLinformation (userid, auth, mid, cookie, json) VALUES ('"
					+ uid
					+ "','"
					+ auth
					+ "','"
					+ mid
					+ "','"
					+ cookie
					+ "','"
					+ json + "');";
			statement.executeUpdate(query);

		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block

		}
		try {
			String query1 = "INSERT INTO JsonInfo (url, ua, libver, iniref, uname, ename) VALUES ('"
					+ jsonurl
					+ "','"
					+ ua
					+ "','"
					+ libver
					+ "','"
					+ iniref
					+ "','" + uname + "','" + ename + "');";
			statement.executeUpdate(query1);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
		}
		try {
			String query2 = "INSERT INTO UserInfo (userid, auth) VALUES ('"
					+ uid + "','" + auth + "');";
			statement.executeUpdate(query2);
			writtentodb = true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
		}
		return writtentodb;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		BufferedReader br = null;
		OutputStreamWriter writer = new OutputStreamWriter(
				response.getOutputStream(), "UTF-8");

		StringBuilder stringBuilder = new StringBuilder();
		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				br = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = br.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}
		writer.write("<html><head></head><body><h1>Home<h1><p>" + stringBuilder
				+ "</p></body></html>");
		writer.flush();
	}

}
