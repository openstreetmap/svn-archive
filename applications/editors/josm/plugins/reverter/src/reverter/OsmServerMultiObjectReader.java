// License: GPL. For details, see LICENSE file.
package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.Logging;

public class OsmServerMultiObjectReader extends OsmServerReader {
    private final MultiOsmReader rdr = new MultiOsmReader();

    public void readObject(PrimitiveId id, int version, ProgressMonitor progressMonitor) throws OsmTransferException {
        readObject(id.getUniqueId(), version, id.getType(), progressMonitor);
    }

    public void readObject(long id, int version, OsmPrimitiveType type, ProgressMonitor progressMonitor) throws OsmTransferException {
        StringBuilder sb = new StringBuilder();
        sb.append(type.getAPIName());
        sb.append("/");
        sb.append(id);
        sb.append("/");
        sb.append(version);
        progressMonitor.beginTask("", 1);
        try (InputStream in = getInputStream(sb.toString(), progressMonitor.createSubTaskMonitor(1, true))) {
            rdr.addData(in);
        } catch (IOException | IllegalDataException e) {
            throw new OsmTransferException(e);
        } finally {
            progressMonitor.finishTask();
        }
    }
    private List<String> makeQueryStrings(OsmPrimitiveType type, Map<Long,Integer> list) {
        List<String> result = new ArrayList<>((list.size()+maxQueryIds-1)/maxQueryIds);
        StringBuilder sb = new StringBuilder();
        int cnt=0;
        for (Entry<Long,Integer> entry : list.entrySet()) {
            if (cnt == 0) {
                sb.append(type.getAPIName());
                sb.append("s?");
                sb.append(type.getAPIName());
                sb.append("s=");
            } else {
                sb.append(",");
            }
            sb.append(entry.getKey());
            sb.append("v");
            sb.append(entry.getValue());
            cnt++;
            if (cnt >=maxQueryIds) {
                result.add(sb.toString());
                sb.setLength(0);
                cnt = 0;
            }
        }
        if (cnt>0) {
            result.add(sb.toString());
        }
        return result;
    }

    protected static final int maxQueryIds = 128;
    public void readMultiObjects(OsmPrimitiveType type, Map<Long,Integer> list, ProgressMonitor progressMonitor) {
        for (String query : makeQueryStrings(type,list)) {
            if (progressMonitor.isCanceled()) {
                return;
            }
            rdr.callback = new ParseCallback() {
                @Override
                public void primitiveParsed(PrimitiveId id) {
                    if (id.getType() == type && list.remove(id.getUniqueId()) != null) {
                        progressMonitor.worked(1);
                    }
                }
            };
            try (InputStream in = getInputStream(query, NullProgressMonitor.INSTANCE)) {
                rdr.addData(in);
            } catch (IOException | IllegalDataException | OsmTransferException e) {
                Logging.warn(e);
            } finally {
                rdr.callback = null;
            }
        }
    }


    /**
     * Method to parse downloaded objects
     * @return the data requested
     * @throws OsmTransferException in case of error
     */
    @Override
    public DataSet parseOsm(ProgressMonitor progressMonitor) throws OsmTransferException {
        progressMonitor.beginTask("", 1);
        progressMonitor.subTask(tr("Preparing history data..."));
        try {
            rdr.processData();
            return rdr.getDataSet();
        } catch (Exception e) {
            throw new OsmTransferException(e);
        } finally {
            progressMonitor.finishTask();
            activeConnection = null;
        }
    }
}
