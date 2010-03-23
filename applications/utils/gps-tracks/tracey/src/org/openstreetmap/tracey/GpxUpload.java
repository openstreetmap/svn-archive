/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstreetmap.tracey;

/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.*;

/**
 * Small java class that allows to upload gpx files to www.openstreetmap.org via its api call.
 *
 * @author cdaller
 */
public class GpxUpload {

	public static final String API_VERSION = "0.6";
	private static final int BUFFER_SIZE = 65535;
	private static final String BASE64_ENC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final String BOUNDARY = "----------------------------d10f7aa230e8";
	private static final String LINE_END = "\r\n";
	private String username;
	private String password;
	private String serveruri;

	public GpxUpload() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		this.username = prefs.get(TraceyPreferencesBox.USERNAME, "");
		this.password = prefs.get(TraceyPreferencesBox.PASSWORD, "");
		this.serveruri = prefs.get(TraceyPreferencesBox.APIURI, "http://www.openstreetmap.org/api/0.6/");
	}

	public void upload(String description, String tags, File gpxFile) throws IOException {
		upload(description, tags, Privacy.PRIVATE, gpxFile);
	}

	public void upload(String description, String tags, Privacy privacy, File gpxFile) throws IOException {
		System.err.println("uploading " + gpxFile.getAbsolutePath() + " to openstreetmap.org");
		try {
			//String urlGpxName = URLEncoder.encode(gpxName.replaceAll("\\.;&?,/","_"), "UTF-8");
			String urlDesc = description.replaceAll("\\.;&?,/", "_");
			String urlTags = tags.replaceAll("\\\\.;&?,/", "_");
			URL url = new URL(serveruri + API_VERSION + "/gpx/create");
			System.err.println("url: " + url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(15000);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.addRequestProperty("Authorization", "Basic " + encodeBase64(username + ":" + password));
			con.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
			con.addRequestProperty("Connection", "close"); // counterpart of keep-alive
			con.addRequestProperty("Expect", "");

			con.connect();
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(con.getOutputStream()));
//            DataOutputStream out  = new DataOutputStream(System.out);

			writeContentDispositionFile(out, "file", gpxFile);
			writeContentDisposition(out, "description", urlDesc);
			writeContentDisposition(out, "tags", urlTags);
			writeContentDisposition(out, "visibility", privacy.getValue());

			out.writeBytes("--" + BOUNDARY + "--" + LINE_END);
			out.flush();

			int retCode = con.getResponseCode();
			String retMsg = con.getResponseMessage();
			System.err.println("\nreturn code: " + retCode + " " + retMsg);
			if (retCode != 200) {
				// Look for a detailed error message from the server
				if (con.getHeaderField("Error") != null) {
					retMsg += "\n" + con.getHeaderField("Error");
				}
				con.disconnect();
				throw new RuntimeException(retCode + " " + retMsg);
			}
			out.close();
			con.disconnect();
			System.out.println(gpxFile.getAbsolutePath() + " uploaded");
		} catch (UnsupportedEncodingException ignore) {
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param out
	 * @param string
	 * @param gpxFile
	 * @throws IOException
	 */
	private void writeContentDispositionFile(DataOutputStream out, String name, File gpxFile) throws IOException {
		out.writeBytes("--" + BOUNDARY + LINE_END);
		out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + gpxFile.getName() + "\"" + LINE_END);
		out.writeBytes("Content-Type: application/octet-stream" + LINE_END);
		out.writeBytes(LINE_END);

		byte[] buffer = new byte[BUFFER_SIZE];
		//int fileLen = (int)gpxFile.length();
		int read;
		int sumread = 0;
		InputStream in = new BufferedInputStream(new FileInputStream(gpxFile));
		System.err.println("Transferring data to server");
		while ((read = in.read(buffer)) >= 0) {
			out.write(buffer, 0, read);
			out.flush();
			sumread += read;
//            System.out.print("Transferred " + ((1.0 * sumread / fileLen) * 100) + "%                                \r");
		}
		in.close();
		out.writeBytes(LINE_END);
	}

	/**
	 * @param string
	 * @param urlDesc
	 * @throws IOException
	 */
	public void writeContentDisposition(DataOutputStream out, String name, String value) throws IOException {
		out.writeBytes("--" + BOUNDARY + LINE_END);
		out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_END);
		out.writeBytes(LINE_END);
		out.writeBytes(value + LINE_END);
	}

	public static String encodeBase64(String s) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < (s.length() + 2) / 3; ++i) {
			int l = Math.min(3, s.length() - i * 3);
			String buf = s.substring(i * 3, i * 3 + l);
			out.append(BASE64_ENC.charAt(buf.charAt(0) >> 2));
			out.append(BASE64_ENC.charAt((buf.charAt(0) & 0x03) << 4 | (l == 1 ? 0 : (buf.charAt(1) & 0xf0) >> 4)));
			out.append(l > 1 ? BASE64_ENC.charAt((buf.charAt(1) & 0x0f) << 2 | (l == 2 ? 0 : (buf.charAt(2) & 0xc0) >> 6)) : '=');
			out.append(l > 2 ? BASE64_ENC.charAt(buf.charAt(2) & 0x3f) : '=');
		}
		return out.toString();
	}
}
