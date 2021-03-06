package org.dfm.TrackingServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
@WebServlet(description = "Extracts information fields from the HTTP request and puts it into MySql database", urlPatterns = { "/TrackingServer" })
public class TrackingServer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	JsonParser parser = new JsonParser();
	private Vector<String> vec = new Vector<String>();
	private Boolean writetoHbase = false;
	private Boolean writetoSqlite = false;
	private Boolean writetoMongoDB = false;
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

	/*
	 * @author Prateek
	 * 
	 * @brief Check the configuration file for the database that needs to be
	 * used for data insertion
	 */
	public void init() {
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(
					"C:\\Users\\Prateek\\Desktop\\Databaseconfig.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("Hbase = 1")) {
					writetoHbase = true;

				} else if (line.contains("Sqlite = 1")) {
					writetoSqlite = true;

				} else if (line.contains("MongoDB = 1")) {
					writetoMongoDB = true;
				}
			}
			in.close();
		} catch (Exception e) {

			System.out.println(e.toString());
		}
	}

	/**
	 * @author Prateek
	 * @brief Gets the request from the browser and forwards it for parsing
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
		if (writetoSqlite.booleanValue() == true) {
			writtentodb = ts.writetosqlite();
		} else if (writetoHbase.booleanValue() == true) {
			writtentodb = ts.writetohbase();
		} else if (writetoMongoDB.booleanValue() == true) {
			writtentodb = ts.writetomongodb();
		}
		writtentodb = ts.writetosqlite();
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

	private Boolean writetomongodb() {
		// TODO Load mongoDB drivers and connection parameters and then write to
		// DB
		return null;
	}

	private Boolean writetohbase() {
		// TODO Load HBase drivers and connection parameters and then write to
		// DB
		return null;
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
			System.out.println(e.toString());
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

	/*
	 * TODO---> Create object based on the type of database connected. The
	 * object of the connected database would be returned which will be used to
	 * insert data into the database.
	 */
	private Boolean writetosqlite() {
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
			System.out.println(e.toString());
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

	/*
	 * TODO---> As soon as structure of the post request is known then parse the
	 * body of the request and insert the data into the database.For now it is
	 * just parsing the body of POST and throws whatever it finds on the browser
	 * window.
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
