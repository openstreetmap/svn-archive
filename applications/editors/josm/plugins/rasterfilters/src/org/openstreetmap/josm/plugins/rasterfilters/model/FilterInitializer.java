package org.openstreetmap.josm.plugins.rasterfilters.model;


public class FilterInitializer {
/*
	public static Set<String> filterTitles = new TreeSet<>();
	public static List<JsonObject> filtersMeta = FiltersInfoListDownloader.filtersMeta;
	public static Set<URL> urls = new HashSet<>();
	public static ClassLoader loader;

	public static void initFilters() {

		for (JsonObject json : filtersMeta) {

			filterTitles.add(json.getString("title"));

			JsonArray binaries = json.getJsonArray("binaries");

			for (int i = 0; i < binaries.size(); i++) {

				File file = new File(binaries.getString(i));

				if (file.exists()) {
					URL url;
					try {
						url = new URL("jar", "", file.toURI().toURL() + "!/");
						urls.add(url);
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}

		loader = new URLClassLoader(urls.toArray(new URL[urls.size()]),
				FilterInitializer.class.getClassLoader());
	}

	public static void destroyFilters() {
		filterTitles.clear();
		urls.clear();
		FiltersInfoListDownloader.filtersMeta.clear();
	}*/
}
