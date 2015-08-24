package org.openstreetmap.josm.plugins.rasterfilters.preferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openstreetmap.josm.Main;
/**
 * This class is responsible for downloading jars which contains
 * filters implementations, for loading meta from the
 * <a href="https://josm.openstreetmap.de/wiki/ImageFilters">filter's page</a>.
 * Also it stores the downloaded information for creating filter's GUI and etc.
 *
 * @author Nipel-Crumple
 */
public class FiltersDownloader implements ActionListener {
	private static volatile String pluginDir;

	public static Set<JsonObject> filtersMeta = new HashSet<>();
	public static Set<String> filterTitles = new TreeSet<>();
	public static Set<URL> binariesLocalUrls = new HashSet<>();
	public static ClassLoader loader;
	public static Map<String, String> urlsMap = new HashMap<>();

	private static Set<JsonObject> filtersMetaToLoad = new HashSet<>();
	static List<FilterInfo> filtersInfoList = new ArrayList<>();

	public static List<FilterInfo> downloadFiltersInfoList() {

		JsonObject jsonRequest = Json
				.createObjectBuilder()
				.add("id", new Random().nextInt())
				.add("method", "wiki.getPageHTML")
				.add("params",
						Json.createArrayBuilder().add("ImageFilters").build())
				.build();

		String jsonRequestString = jsonRequest.toString();

		URL wikiApi;
		HttpURLConnection wikiConnection;
		try {
			wikiApi = new URL("https://josm.openstreetmap.de/jsonrpc");
			wikiConnection = (HttpURLConnection) wikiApi.openConnection();
			wikiConnection.setDoOutput(true);
			wikiConnection.setDoInput(true);

			wikiConnection.setRequestProperty("Content-Type",
					"application/json");
			wikiConnection.setRequestProperty("Method", "POST");
			wikiConnection.connect();

			OutputStream os = wikiConnection.getOutputStream();
			os.write(jsonRequestString.getBytes("UTF-8"));
			os.close();

			int HttpResult = wikiConnection.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {

				JsonReader jsonStream = Json
						.createReader(new InputStreamReader(wikiConnection
								.getInputStream(), "utf-8"));

				JsonObject jsonResponse = jsonStream.readObject();
				jsonStream.close();

				Elements trTagElems = Jsoup.parse(
						jsonResponse.getString("result"))
						.getElementsByTag("tr");
				for (Element element : trTagElems) {

					Elements elems = element.getElementsByTag("td");
					if (!elems.isEmpty()) {
						String name = elems.get(0).text();
						String owner = elems.get(1).text();
						String description = elems.get(2).text();

						String link = elems.get(0).getElementsByTag("a")
								.attr("href");

						JsonObject meta = loadMeta(link);

						String paramName = "rasterfilters."
								+ meta.getString("name");

						boolean needToLoad = Main.pref.getBoolean(paramName);

						if (needToLoad) {
							JsonArray binaries = meta.getJsonArray("binaries");
							filterTitles.add(meta.getString("title"));
							for (int i = 0; i < binaries.size(); i++) {
								filtersMetaToLoad.add(meta);
								loadBinaryToFile(binaries.getString(i));
							}
						}
						FilterInfo newFilterInfo = new FilterInfo(name,
								description, meta, needToLoad);
						newFilterInfo.setOwner(owner);

						if (!filtersInfoList.contains(newFilterInfo)) {
							filtersInfoList.add(newFilterInfo);
						}
					}
				}

			} else {
				Main.debug("Error happenned while requesting for the list of filters");
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		loadBinariesFromMeta(filtersMetaToLoad);

		return filtersInfoList;
	}

	public static JsonObject loadMeta(String link) {

		Pattern p = Pattern.compile("ImageFilters/\\w.*");
		Matcher m = p.matcher(link);

		if (m.find()) {
			link = link.substring(m.start());
		}

		JsonObject jsonRequest = Json.createObjectBuilder()
				.add("id", new Random().nextInt())
				.add("method", "wiki.getPageHTML")
				.add("params", Json.createArrayBuilder().add(link).build())
				.build();

		String jsonStringRequest = jsonRequest.toString();

		URL wikiApi;
		HttpURLConnection wikiConnection;
		JsonObject meta = null;

		try {
			wikiApi = new URL("https://josm.openstreetmap.de/jsonrpc");
			wikiConnection = (HttpURLConnection) wikiApi.openConnection();
			wikiConnection.setDoOutput(true);
			wikiConnection.setDoInput(true);

			wikiConnection.setRequestProperty("Content-Type",
					"application/json");
			wikiConnection.setRequestProperty("Method", "POST");
			wikiConnection.connect();

			OutputStream os = wikiConnection.getOutputStream();
			os.write(jsonStringRequest.getBytes("UTF-8"));
			os.close();

			int HttpResult = wikiConnection.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {

				JsonReader jsonStream = Json
						.createReader(new InputStreamReader(wikiConnection
								.getInputStream(), "UTF-8"));

				JsonObject jsonResponse = jsonStream.readObject();
				jsonStream.close();

				String jsonPage = jsonResponse.getString("result");

				Document doc = Jsoup.parse(jsonPage, "UTF-8");
				String json = doc.getElementsByTag("pre").first().text();

				JsonReader reader = Json.createReader(new StringReader(json));
				meta = reader.readObject();
				reader.close();

			} else {
				Main.debug(wikiConnection.getResponseMessage());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		filtersMeta.add(meta);

		return meta;
	}

	public static void initFilters() {
		File file = new File(pluginDir, "urls.map");
		Main.debug("EXIST FILE? " + file.exists());

		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader br = new BufferedReader(fileReader);

			String temp = null;

			while ((temp = br.readLine()) != null) {
				String[] mapEntry = temp.split("\\t");
				File fileUrl = new File(mapEntry[1]);
				if (fileUrl.exists()) {
					URL url;
					try {
						url = new URL("jar", "", fileUrl.toURI().toURL() + "!/");
						Main.debug("binaryUrl: " + url.toString());
						binariesLocalUrls.add(url);
					} catch (MalformedURLException e) {
						Main.debug("Initializing filters with unknown protocol. \n"
								+ e.getMessage());
					}
				}
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Main.debug("BinariesLocal : " + binariesLocalUrls.toString());

		loader = new URLClassLoader(
				binariesLocalUrls.toArray(new URL[binariesLocalUrls.size()]),
				FiltersDownloader.class.getClassLoader());
	}

	public static void destroyFilters() {
		filterTitles.clear();
		binariesLocalUrls.clear();
		FiltersDownloader.filtersMeta.clear();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		for (FilterInfo temp : filtersInfoList) {
			if (temp.isNeedToDownload()) {

				if (!filtersMetaToLoad.contains(temp.getMeta())) {
					filtersMetaToLoad.add(temp.getMeta());
				}

				filterTitles.add(temp.getMeta().getString("title"));
			} else {
				filterTitles.remove(temp.getMeta().getString("title"));
			}
		}

		loadBinariesFromMeta(filtersMetaToLoad);

		filtersMetaToLoad.clear();
	}

	public static void loadBinariesFromMeta(Set<JsonObject> metaList) {

		File file = new File(pluginDir, "urls.map");
		Main.debug("pluginDir and urls map" + file.getAbsoluteFile());

		FileWriter fileWriter = null;
		BufferedWriter writer = null;
		try {
			fileWriter = new FileWriter(file);
			writer = new BufferedWriter(fileWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (JsonObject temp : metaList) {
			JsonArray binaries = temp.getJsonArray("binaries");

			for (int i = 0; i < binaries.size(); i++) {

				String localFile = loadBinaryToFile(binaries.getString(i));

				try {
					writer.append(binaries.getString(i));
					writer.append("\t");
					writer.append(localFile);
					writer.append("\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			writer.close();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setPluginDir(String dir) {
		pluginDir = dir;
	}

	public static String loadBinaryToFile(String fromUrl) {

		// Main.debug("Need to load binary from " + fromUrl);

		URL url = null;
		URLConnection con = null;

		Pattern p = Pattern.compile("\\w.*/");
		Matcher m = p.matcher(fromUrl);

		String localFile = null;
		File plugin = new File(pluginDir);
		Main.debug("plugin dir" + plugin.getAbsolutePath());

		if (m.find()) {

			if (plugin.exists()) {

				localFile = fromUrl.substring(m.end());
				Main.debug("localFile: " + localFile);
			}
		}

		try {
			url = new URL(fromUrl);
			con = url.openConnection();
			String plugDir = plugin.getAbsolutePath();
			File file = new File(plugDir, localFile);
			Main.debug("Binary file: " + file.getAbsolutePath());

			if (file.exists()) {
				Main.debug("File " + localFile + " already exists");

				return file.getAbsolutePath();
			} else {

				BufferedInputStream in = new BufferedInputStream(
						con.getInputStream());
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream(file));
				int i;

				while ((i = in.read()) != -1) {
					out.write(i);
				}

				out.flush();
				out.close();
				in.close();

				return localFile;
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}