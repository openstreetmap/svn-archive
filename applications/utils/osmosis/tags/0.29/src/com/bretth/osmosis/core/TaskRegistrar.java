// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bretth.osmosis.core.buffer.v0_6.ChangeBufferFactory;
import com.bretth.osmosis.core.buffer.v0_6.EntityBufferFactory;
import com.bretth.osmosis.core.change.v0_6.ChangeApplierFactory;
import com.bretth.osmosis.core.change.v0_6.ChangeDeriverFactory;
import com.bretth.osmosis.core.customdb.v0_6.DumpDatasetFactory;
import com.bretth.osmosis.core.customdb.v0_6.ReadDatasetFactory;
import com.bretth.osmosis.core.customdb.v0_6.WriteDatasetFactory;
import com.bretth.osmosis.core.filter.v0_6.BoundingBoxFilterFactory;
import com.bretth.osmosis.core.filter.v0_6.DatasetBoundingBoxFilterFactory;
import com.bretth.osmosis.core.filter.v0_6.PolygonFilterFactory;
import com.bretth.osmosis.core.filter.v0_6.UsedNodeFilterFactory;
import com.bretth.osmosis.core.filter.v0_6.WayKeyValueFilterFactory;
import com.bretth.osmosis.core.merge.v0_6.ChangeDownloadInitializerFactory;
import com.bretth.osmosis.core.merge.v0_6.ChangeDownloaderFactory;
import com.bretth.osmosis.core.merge.v0_6.ChangeMergerFactory;
import com.bretth.osmosis.core.merge.v0_6.EntityMergerFactory;
import com.bretth.osmosis.core.misc.v0_6.NullChangeWriterFactory;
import com.bretth.osmosis.core.misc.v0_6.NullWriterFactory;
import com.bretth.osmosis.core.mysql.v0_6.MySqlCurrentReaderFactory;
import com.bretth.osmosis.core.mysql.v0_6.MysqlChangeReaderFactory;
import com.bretth.osmosis.core.mysql.v0_6.MysqlChangeWriterFactory;
import com.bretth.osmosis.core.mysql.v0_6.MysqlReaderFactory;
import com.bretth.osmosis.core.mysql.v0_6.MysqlTruncatorFactory;
import com.bretth.osmosis.core.mysql.v0_6.MysqlWriterFactory;
import com.bretth.osmosis.core.pgsql.v0_6.PostgreSqlChangeWriterFactory;
import com.bretth.osmosis.core.pgsql.v0_6.PostgreSqlDatasetDumpWriterFactory;
import com.bretth.osmosis.core.pgsql.v0_6.PostgreSqlDatasetReaderFactory;
import com.bretth.osmosis.core.pgsql.v0_6.PostgreSqlDatasetTruncatorFactory;
import com.bretth.osmosis.core.pgsql.v0_6.PostgreSqlDatasetWriterFactory;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.plugin.PluginLoader;
import com.bretth.osmosis.core.progress.v0_6.ChangeProgressLoggerFactory;
import com.bretth.osmosis.core.progress.v0_6.EntityProgressLoggerFactory;
import com.bretth.osmosis.core.report.v0_6.EntityReporterFactory;
import com.bretth.osmosis.core.report.v0_6.IntegrityReporterFactory;
import com.bretth.osmosis.core.sort.v0_6.ChangeForSeekableApplierComparator;
import com.bretth.osmosis.core.sort.v0_6.ChangeForStreamableApplierComparator;
import com.bretth.osmosis.core.sort.v0_6.ChangeSorterFactory;
import com.bretth.osmosis.core.sort.v0_6.EntityByTypeThenIdComparator;
import com.bretth.osmosis.core.sort.v0_6.EntitySorterFactory;
import com.bretth.osmosis.core.tee.v0_6.ChangeTeeFactory;
import com.bretth.osmosis.core.tee.v0_6.EntityTeeFactory;
import com.bretth.osmosis.core.xml.v0_6.XmlChangeReaderFactory;
import com.bretth.osmosis.core.xml.v0_6.XmlChangeWriterFactory;
import com.bretth.osmosis.core.xml.v0_6.XmlDownloaderFactory;
import com.bretth.osmosis.core.xml.v0_6.XmlReaderFactory;
import com.bretth.osmosis.core.xml.v0_6.XmlWriterFactory;


/**
 * Provides the initialisation logic for registering all task factories.
 * 
 * @author Brett Henderson
 */
public class TaskRegistrar {
	
	
	/**
	 * Initialises factories for all tasks. No plugins are loaded by this
	 * method.
	 */
	public static void initialize() {
		initialize(new ArrayList<String>());
	}
	
	
	/**
	 * Initialises factories for all tasks. Loads additionally specified plugins
	 * as well as default tasks.
	 * 
	 * @param plugins
	 *            The class names of all plugins to be loaded.
	 */
	public static void initialize(List<String> plugins) {
		com.bretth.osmosis.core.sort.v0_5.EntitySorterFactory entitySorterFactory05;
		com.bretth.osmosis.core.sort.v0_5.ChangeSorterFactory changeSorterFactory05;
		EntitySorterFactory entitySorterFactory06;
		ChangeSorterFactory changeSorterFactory06;
		
		// Configure factories that require additional information.
		entitySorterFactory05 = new com.bretth.osmosis.core.sort.v0_5.EntitySorterFactory();
		entitySorterFactory05.registerComparator("TypeThenId", new com.bretth.osmosis.core.sort.v0_5.EntityByTypeThenIdComparator(), true);
		changeSorterFactory05 = new com.bretth.osmosis.core.sort.v0_5.ChangeSorterFactory();
		changeSorterFactory05.registerComparator("streamable", new com.bretth.osmosis.core.sort.v0_5.ChangeForStreamableApplierComparator(), true);
		changeSorterFactory05.registerComparator("seekable", new com.bretth.osmosis.core.sort.v0_5.ChangeForSeekableApplierComparator(), false);
		entitySorterFactory06 = new EntitySorterFactory();
		entitySorterFactory06.registerComparator("TypeThenId", new EntityByTypeThenIdComparator(), true);
		changeSorterFactory06 = new ChangeSorterFactory();
		changeSorterFactory06.registerComparator("streamable", new ChangeForStreamableApplierComparator(), true);
		changeSorterFactory06.registerComparator("seekable", new ChangeForSeekableApplierComparator(), false);
		
		// Register factories.
		TaskManagerFactory.register("apply-change", new com.bretth.osmosis.core.change.v0_5.ChangeApplierFactory());
		TaskManagerFactory.register("ac", new com.bretth.osmosis.core.change.v0_5.ChangeApplierFactory());
		TaskManagerFactory.register("bounding-box", new com.bretth.osmosis.core.filter.v0_5.BoundingBoxFilterFactory());
		TaskManagerFactory.register("bb", new com.bretth.osmosis.core.filter.v0_5.BoundingBoxFilterFactory());
		TaskManagerFactory.register("derive-change", new com.bretth.osmosis.core.change.v0_5.ChangeDeriverFactory());
		TaskManagerFactory.register("dc", new com.bretth.osmosis.core.change.v0_5.ChangeDeriverFactory());
		TaskManagerFactory.register("read-mysql", new com.bretth.osmosis.core.mysql.v0_5.MysqlReaderFactory());
		TaskManagerFactory.register("rm", new com.bretth.osmosis.core.mysql.v0_5.MysqlReaderFactory());
		TaskManagerFactory.register("read-mysql-change", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeReaderFactory());
		TaskManagerFactory.register("rmc", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeReaderFactory());
		TaskManagerFactory.register("read-mysql-current", new com.bretth.osmosis.core.mysql.v0_5.MySqlCurrentReaderFactory());
		TaskManagerFactory.register("rmcur", new com.bretth.osmosis.core.mysql.v0_5.MySqlCurrentReaderFactory());
		TaskManagerFactory.register("read-xml", new com.bretth.osmosis.core.xml.v0_5.XmlReaderFactory());
		TaskManagerFactory.register("rx", new com.bretth.osmosis.core.xml.v0_5.XmlReaderFactory());
		TaskManagerFactory.register("read-xml-change", new com.bretth.osmosis.core.xml.v0_5.XmlChangeReaderFactory());
		TaskManagerFactory.register("rxc", new com.bretth.osmosis.core.xml.v0_5.XmlChangeReaderFactory());
		TaskManagerFactory.register("sort", entitySorterFactory05);
		TaskManagerFactory.register("s", entitySorterFactory05);
		TaskManagerFactory.register("sort-change", changeSorterFactory05);
		TaskManagerFactory.register("sc", changeSorterFactory05);
		TaskManagerFactory.register("write-mysql", new com.bretth.osmosis.core.mysql.v0_5.MysqlWriterFactory());
		TaskManagerFactory.register("wm", new com.bretth.osmosis.core.mysql.v0_5.MysqlWriterFactory());
		TaskManagerFactory.register("write-mysql-change", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeWriterFactory());
		TaskManagerFactory.register("wmc", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeWriterFactory());
		TaskManagerFactory.register("truncate-mysql", new com.bretth.osmosis.core.mysql.v0_5.MysqlTruncatorFactory());
		TaskManagerFactory.register("tm", new com.bretth.osmosis.core.mysql.v0_5.MysqlTruncatorFactory());
		TaskManagerFactory.register("write-xml", new com.bretth.osmosis.core.xml.v0_5.XmlWriterFactory());
		TaskManagerFactory.register("wx", new com.bretth.osmosis.core.xml.v0_5.XmlWriterFactory());
		TaskManagerFactory.register("write-xml-change", new com.bretth.osmosis.core.xml.v0_5.XmlChangeWriterFactory());
		TaskManagerFactory.register("wxc", new com.bretth.osmosis.core.xml.v0_5.XmlChangeWriterFactory());
		TaskManagerFactory.register("write-null", new com.bretth.osmosis.core.misc.v0_5.NullWriterFactory());
		TaskManagerFactory.register("wn", new com.bretth.osmosis.core.misc.v0_5.NullWriterFactory());
		TaskManagerFactory.register("write-null-change", new com.bretth.osmosis.core.misc.v0_5.NullChangeWriterFactory());
		TaskManagerFactory.register("wnc", new com.bretth.osmosis.core.misc.v0_5.NullChangeWriterFactory());
		TaskManagerFactory.register("buffer", new com.bretth.osmosis.core.buffer.v0_5.EntityBufferFactory());
		TaskManagerFactory.register("b", new com.bretth.osmosis.core.buffer.v0_5.EntityBufferFactory());
		TaskManagerFactory.register("buffer-change", new com.bretth.osmosis.core.buffer.v0_5.ChangeBufferFactory());
		TaskManagerFactory.register("bc", new com.bretth.osmosis.core.buffer.v0_5.ChangeBufferFactory());
		TaskManagerFactory.register("merge", new com.bretth.osmosis.core.merge.v0_5.EntityMergerFactory());
		TaskManagerFactory.register("m", new com.bretth.osmosis.core.merge.v0_5.EntityMergerFactory());
		TaskManagerFactory.register("merge-change", new com.bretth.osmosis.core.merge.v0_5.ChangeMergerFactory());
		TaskManagerFactory.register("mc", new com.bretth.osmosis.core.merge.v0_5.ChangeMergerFactory());
		TaskManagerFactory.register("read-api", new com.bretth.osmosis.core.xml.v0_5.XmlDownloaderFactory());
		TaskManagerFactory.register("ra", new com.bretth.osmosis.core.xml.v0_5.XmlDownloaderFactory());
		TaskManagerFactory.register("bounding-polygon", new com.bretth.osmosis.core.filter.v0_5.PolygonFilterFactory());
		TaskManagerFactory.register("bp", new com.bretth.osmosis.core.filter.v0_5.PolygonFilterFactory());
		TaskManagerFactory.register("report-entity", new com.bretth.osmosis.core.report.v0_5.EntityReporterFactory());
		TaskManagerFactory.register("re", new com.bretth.osmosis.core.report.v0_5.EntityReporterFactory());
		TaskManagerFactory.register("report-integrity", new com.bretth.osmosis.core.report.v0_5.IntegrityReporterFactory());
		TaskManagerFactory.register("ri", new com.bretth.osmosis.core.report.v0_5.IntegrityReporterFactory());
		TaskManagerFactory.register("log-progress", new com.bretth.osmosis.core.progress.v0_5.EntityProgressLoggerFactory());
		TaskManagerFactory.register("lp", new com.bretth.osmosis.core.progress.v0_5.EntityProgressLoggerFactory());
		TaskManagerFactory.register("log-progress-change", new com.bretth.osmosis.core.progress.v0_5.ChangeProgressLoggerFactory());
		TaskManagerFactory.register("lpc", new com.bretth.osmosis.core.progress.v0_5.ChangeProgressLoggerFactory());
		TaskManagerFactory.register("tee", new com.bretth.osmosis.core.tee.v0_5.EntityTeeFactory());
		TaskManagerFactory.register("t", new com.bretth.osmosis.core.tee.v0_5.EntityTeeFactory());
		TaskManagerFactory.register("tee-change", new com.bretth.osmosis.core.tee.v0_5.ChangeTeeFactory());
		TaskManagerFactory.register("tc", new com.bretth.osmosis.core.tee.v0_5.ChangeTeeFactory());
		TaskManagerFactory.register("write-customdb", new com.bretth.osmosis.core.customdb.v0_5.WriteDatasetFactory());
		TaskManagerFactory.register("wc", new com.bretth.osmosis.core.customdb.v0_5.WriteDatasetFactory());
		TaskManagerFactory.register("dataset-bounding-box", new com.bretth.osmosis.core.filter.v0_5.DatasetBoundingBoxFilterFactory());
		TaskManagerFactory.register("dbb", new com.bretth.osmosis.core.filter.v0_5.DatasetBoundingBoxFilterFactory());
		TaskManagerFactory.register("dataset-dump", new com.bretth.osmosis.core.customdb.v0_5.DumpDatasetFactory());
		TaskManagerFactory.register("dd", new com.bretth.osmosis.core.customdb.v0_5.DumpDatasetFactory());
		TaskManagerFactory.register("read-customdb", new com.bretth.osmosis.core.customdb.v0_5.ReadDatasetFactory());
		TaskManagerFactory.register("rc", new com.bretth.osmosis.core.customdb.v0_5.ReadDatasetFactory());
		TaskManagerFactory.register("write-pgsql", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetWriterFactory());
		TaskManagerFactory.register("wp", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetWriterFactory());
		TaskManagerFactory.register("truncate-pgsql", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetTruncatorFactory());
		TaskManagerFactory.register("tp", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetTruncatorFactory());
		TaskManagerFactory.register("write-pgsql-dump", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetDumpWriterFactory());
		TaskManagerFactory.register("wpd", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetDumpWriterFactory());
		TaskManagerFactory.register("read-pgsql", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetReaderFactory());
		TaskManagerFactory.register("rp", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetReaderFactory());
		TaskManagerFactory.register("write-pgsql-change", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlChangeWriterFactory());
		TaskManagerFactory.register("wpc", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlChangeWriterFactory());
		TaskManagerFactory.register("used-node", new com.bretth.osmosis.core.filter.v0_5.UsedNodeFilterFactory());
		TaskManagerFactory.register("un", new com.bretth.osmosis.core.filter.v0_5.UsedNodeFilterFactory());
		TaskManagerFactory.register("way-key-value", new com.bretth.osmosis.core.filter.v0_5.WayKeyValueFilterFactory());
		TaskManagerFactory.register("wkv", new com.bretth.osmosis.core.filter.v0_5.WayKeyValueFilterFactory());
		TaskManagerFactory.register("read-change-interval", new com.bretth.osmosis.core.merge.v0_5.ChangeDownloaderFactory());
		TaskManagerFactory.register("rci", new com.bretth.osmosis.core.merge.v0_5.ChangeDownloaderFactory());
		TaskManagerFactory.register("read-change-interval-init", new com.bretth.osmosis.core.merge.v0_5.ChangeDownloadInitializerFactory());
		TaskManagerFactory.register("rcii", new com.bretth.osmosis.core.merge.v0_5.ChangeDownloadInitializerFactory());
		
		TaskManagerFactory.register("apply-change-0.5", new com.bretth.osmosis.core.change.v0_5.ChangeApplierFactory());
		TaskManagerFactory.register("bounding-box-0.5", new com.bretth.osmosis.core.filter.v0_5.BoundingBoxFilterFactory());
		TaskManagerFactory.register("derive-change-0.5", new com.bretth.osmosis.core.change.v0_5.ChangeDeriverFactory());
		TaskManagerFactory.register("read-mysql-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlReaderFactory());
		TaskManagerFactory.register("read-mysql-change-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeReaderFactory());
		TaskManagerFactory.register("read-mysql-current-0.5", new com.bretth.osmosis.core.mysql.v0_5.MySqlCurrentReaderFactory());
		TaskManagerFactory.register("read-xml-0.5", new com.bretth.osmosis.core.xml.v0_5.XmlReaderFactory());
		TaskManagerFactory.register("read-xml-change-0.5", new com.bretth.osmosis.core.xml.v0_5.XmlChangeReaderFactory());
		TaskManagerFactory.register("sort-0.5", entitySorterFactory05);
		TaskManagerFactory.register("sort-change-0.5", changeSorterFactory05);
		TaskManagerFactory.register("write-mysql-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlWriterFactory());
		TaskManagerFactory.register("write-mysql-change-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlChangeWriterFactory());
		TaskManagerFactory.register("truncate-mysql-0.5", new com.bretth.osmosis.core.mysql.v0_5.MysqlTruncatorFactory());
		TaskManagerFactory.register("write-xml-0.5", new com.bretth.osmosis.core.xml.v0_5.XmlWriterFactory());
		TaskManagerFactory.register("write-xml-change-0.5", new com.bretth.osmosis.core.xml.v0_5.XmlChangeWriterFactory());
		TaskManagerFactory.register("write-null-0.5", new com.bretth.osmosis.core.misc.v0_5.NullWriterFactory());
		TaskManagerFactory.register("write-null-change-0.5", new com.bretth.osmosis.core.misc.v0_5.NullChangeWriterFactory());
		TaskManagerFactory.register("buffer-0.5", new com.bretth.osmosis.core.buffer.v0_5.EntityBufferFactory());
		TaskManagerFactory.register("buffer-change-0.5", new com.bretth.osmosis.core.buffer.v0_5.ChangeBufferFactory());
		TaskManagerFactory.register("merge-0.5", new com.bretth.osmosis.core.merge.v0_5.EntityMergerFactory());
		TaskManagerFactory.register("merge-change-0.5", new com.bretth.osmosis.core.merge.v0_5.ChangeMergerFactory());
		TaskManagerFactory.register("read-api-0.5", new com.bretth.osmosis.core.xml.v0_5.XmlDownloaderFactory());
		TaskManagerFactory.register("bounding-polygon-0.5", new com.bretth.osmosis.core.filter.v0_5.PolygonFilterFactory());
		TaskManagerFactory.register("report-entity-0.5", new com.bretth.osmosis.core.report.v0_5.EntityReporterFactory());
		TaskManagerFactory.register("report-integrity-0.5", new com.bretth.osmosis.core.report.v0_5.IntegrityReporterFactory());
		TaskManagerFactory.register("log-progress-0.5", new com.bretth.osmosis.core.progress.v0_5.EntityProgressLoggerFactory());
		TaskManagerFactory.register("log-change-progress-0.5", new com.bretth.osmosis.core.progress.v0_5.ChangeProgressLoggerFactory());
		TaskManagerFactory.register("tee-0.5", new com.bretth.osmosis.core.tee.v0_5.EntityTeeFactory());
		TaskManagerFactory.register("tee-change-0.5", new com.bretth.osmosis.core.tee.v0_5.ChangeTeeFactory());
		TaskManagerFactory.register("write-customdb-0.5", new com.bretth.osmosis.core.customdb.v0_5.WriteDatasetFactory());
		TaskManagerFactory.register("dataset-bounding-box-0.5", new com.bretth.osmosis.core.filter.v0_5.DatasetBoundingBoxFilterFactory());
		TaskManagerFactory.register("dataset-dump-0.5", new com.bretth.osmosis.core.customdb.v0_5.DumpDatasetFactory());
		TaskManagerFactory.register("read-customdb-0.5", new com.bretth.osmosis.core.customdb.v0_5.ReadDatasetFactory());
		TaskManagerFactory.register("write-pgsql-0.5", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetWriterFactory());
		TaskManagerFactory.register("truncate-pgsql-0.5", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetTruncatorFactory());
		TaskManagerFactory.register("write-pgsql-dump-0.5", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetDumpWriterFactory());
		TaskManagerFactory.register("read-pgsql-0.5", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlDatasetReaderFactory());
		TaskManagerFactory.register("write-pgsql-change-0.5", new com.bretth.osmosis.core.pgsql.v0_5.PostgreSqlChangeWriterFactory());
		TaskManagerFactory.register("used-node-0.5", new com.bretth.osmosis.core.filter.v0_5.UsedNodeFilterFactory());
		TaskManagerFactory.register("way-key-value-0.5", new com.bretth.osmosis.core.filter.v0_5.WayKeyValueFilterFactory());
		TaskManagerFactory.register("read-change-interval-0.5", new com.bretth.osmosis.core.merge.v0_5.ChangeDownloaderFactory());
		TaskManagerFactory.register("read-change-interval-init-0.5", new com.bretth.osmosis.core.merge.v0_5.ChangeDownloadInitializerFactory());
		
		TaskManagerFactory.register("apply-change-0.6", new ChangeApplierFactory());
		TaskManagerFactory.register("bounding-box-0.6", new BoundingBoxFilterFactory());
		TaskManagerFactory.register("derive-change-0.6", new ChangeDeriverFactory());
		TaskManagerFactory.register("read-mysql-0.6", new MysqlReaderFactory());
		TaskManagerFactory.register("read-mysql-change-0.6", new MysqlChangeReaderFactory());
		TaskManagerFactory.register("read-mysql-current-0.6", new MySqlCurrentReaderFactory());
		TaskManagerFactory.register("read-xml-0.6", new XmlReaderFactory());
		TaskManagerFactory.register("read-xml-change-0.6", new XmlChangeReaderFactory());
		TaskManagerFactory.register("sort-0.6", entitySorterFactory06);
		TaskManagerFactory.register("sort-change-0.6", changeSorterFactory06);
		TaskManagerFactory.register("write-mysql-0.6", new MysqlWriterFactory());
		TaskManagerFactory.register("write-mysql-change-0.6", new MysqlChangeWriterFactory());
		TaskManagerFactory.register("truncate-mysql-0.6", new MysqlTruncatorFactory());
		TaskManagerFactory.register("write-xml-0.6", new XmlWriterFactory());
		TaskManagerFactory.register("write-xml-change-0.6", new XmlChangeWriterFactory());
		TaskManagerFactory.register("write-null-0.6", new NullWriterFactory());
		TaskManagerFactory.register("write-null-change-0.6", new NullChangeWriterFactory());
		TaskManagerFactory.register("buffer-0.6", new EntityBufferFactory());
		TaskManagerFactory.register("buffer-change-0.6", new ChangeBufferFactory());
		TaskManagerFactory.register("merge-0.6", new EntityMergerFactory());
		TaskManagerFactory.register("merge-change-0.6", new ChangeMergerFactory());
		TaskManagerFactory.register("read-api-0.6", new XmlDownloaderFactory());
		TaskManagerFactory.register("bounding-polygon-0.6", new PolygonFilterFactory());
		TaskManagerFactory.register("report-entity-0.6", new EntityReporterFactory());
		TaskManagerFactory.register("report-integrity-0.6", new IntegrityReporterFactory());
		TaskManagerFactory.register("log-progress-0.6", new EntityProgressLoggerFactory());
		TaskManagerFactory.register("log-change-progress-0.6", new ChangeProgressLoggerFactory());
		TaskManagerFactory.register("tee-0.6", new EntityTeeFactory());
		TaskManagerFactory.register("tee-change-0.6", new ChangeTeeFactory());
		TaskManagerFactory.register("write-customdb-0.6", new WriteDatasetFactory());
		TaskManagerFactory.register("dataset-bounding-box-0.6", new DatasetBoundingBoxFilterFactory());
		TaskManagerFactory.register("dataset-dump-0.6", new DumpDatasetFactory());
		TaskManagerFactory.register("read-customdb-0.6", new ReadDatasetFactory());
		TaskManagerFactory.register("write-pgsql-0.6", new PostgreSqlDatasetWriterFactory());
		TaskManagerFactory.register("truncate-pgsql-0.6", new PostgreSqlDatasetTruncatorFactory());
		TaskManagerFactory.register("write-pgsql-dump-0.6", new PostgreSqlDatasetDumpWriterFactory());
		TaskManagerFactory.register("read-pgsql-0.6", new PostgreSqlDatasetReaderFactory());
		TaskManagerFactory.register("write-pgsql-change-0.6", new PostgreSqlChangeWriterFactory());
		TaskManagerFactory.register("used-node-0.6", new UsedNodeFilterFactory());
		TaskManagerFactory.register("way-key-value-0.6", new WayKeyValueFilterFactory());
		TaskManagerFactory.register("read-change-interval-0.6", new ChangeDownloaderFactory());
		TaskManagerFactory.register("read-change-interval-init-0.6", new ChangeDownloadInitializerFactory());
		
		// Register the plugins.
		for (String plugin : plugins) {
			loadPlugin(plugin);
		}
	}
	
	
	/**
	 * Loads the tasks associated with a plugin.
	 * 
	 * @param plugin
	 *            The plugin loader class name.
	 */
	@SuppressWarnings("unchecked")
	private static void loadPlugin(String plugin) {
		ClassLoader classLoader;
		Class<?> untypedPluginClass;
		Class<PluginLoader> pluginClass;
		PluginLoader pluginLoader;
		Map<String, TaskManagerFactory> pluginTasks;
		
		// Obtain the thread context class loader. This becomes important if run
		// within an application server environment where plugins might be
		// inaccessible to this class's classloader.
		classLoader = Thread.currentThread().getContextClassLoader();
		
		// Load the plugin class.
		try {
			untypedPluginClass = classLoader.loadClass(plugin);
		} catch (ClassNotFoundException e) {
			throw new OsmosisRuntimeException("Unable to load plugin class (" + plugin + ").", e);
		}
		
		// Verify that the plugin implements the plugin loader interface.
		if (!PluginLoader.class.isAssignableFrom(untypedPluginClass)) {
			throw new OsmosisRuntimeException("The class (" + plugin + ") does not implement interface (" + PluginLoader.class.getName() + ").");
		}
		pluginClass = (Class<PluginLoader>) untypedPluginClass;
		
		// Instantiate the plugin loader.
		try {
			pluginLoader = pluginClass.newInstance();
		} catch (InstantiationException e) {
			throw new OsmosisRuntimeException("Unable to instantiate class (" + plugin + ")", e);
		} catch (IllegalAccessException e) {
			throw new OsmosisRuntimeException("Unable to instantiate class (" + plugin + ")", e);
		}
		
		// Obtain the plugin task factories with their names.
		pluginTasks = pluginLoader.loadTaskFactories();
		
		// Register the plugin tasks.
		for (Entry<String, TaskManagerFactory> taskEntry : pluginTasks.entrySet()) {
			TaskManagerFactory.register(taskEntry.getKey(), taskEntry.getValue());
		}
	}
}
