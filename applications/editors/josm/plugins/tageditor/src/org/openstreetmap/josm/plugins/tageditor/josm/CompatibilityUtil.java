package org.openstreetmap.josm.plugins.tageditor.josm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;

/**
 * Static helper for the transition from JOSM with {@see Main#ds} to {@see Main#getCurrentDataSet()}.
 * 
 *
 */
public class CompatibilityUtil {

	private static boolean useMainDs = false;
	private static boolean useGetCurrentDataSet = false;

	private CompatibilityUtil(){}

	private static boolean hasMainDs() {
		try {
			Field f = Main.class.getField("ds");
		} catch(NoSuchFieldException e) {
			return false;
		}
		return true;
	}

	private static boolean hasGetCurrentDataSet() {
		try {
			Method m = Main.class.getMethod("getCurrentDataSet");
		} catch(NoSuchMethodException e) {
			return false;
		}
		return true;
	}

	private static void analyse() {
		if (useMainDs || useGetCurrentDataSet) return;
		if (hasMainDs()) {
			useMainDs = true;
			System.out.println(tr("INFO: entering compatibilty mode for JOSM with Main.ds"));
		} else if (hasGetCurrentDataSet()) {
			useGetCurrentDataSet = true;
			System.out.println(tr("INFO: entering compatibilty mode for JOSM with Main.getCurrentDataSet()"));
		} else
			throw new RuntimeException(tr("Unexpected version of JOSM. Neither Main.ds nor Main.getCurrentDataSet() found."));
	}

	private static DataSet getMainDs() throws RuntimeException{
		DataSet ds = null;
		try {
			Field f = Main.class.getField("ds");
			ds = (DataSet) f.get(null);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return ds;
	}

	private static DataSet getGetCurrentDataSet() throws RuntimeException{
		DataSet ds = null;
		try {
			Method m = Main.class.getMethod("getCurrentDataSet");
			ds = (DataSet) m.invoke(null);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return ds;
	}

	/**
	 * Replies the current data set in JOSM. Depending on the JOSM version, either uses
	 * {@see Main#ds} or {@see Main#getCurrentDataSet()}.
	 * 
	 * @return the current dataset. <strong>This may be null</strong>
	 * @throws RuntimeException thrown, if the current data set can't be read
	 */
	static public DataSet getCurrentDataSet() throws RuntimeException {
		analyse();
		if (useMainDs)
			return getMainDs();
		else if (useGetCurrentDataSet)
			return getGetCurrentDataSet();

		// should not happen
		return null;
	}
}
