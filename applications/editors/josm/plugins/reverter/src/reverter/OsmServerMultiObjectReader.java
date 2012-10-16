package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.xml.sax.SAXException;


public class OsmServerMultiObjectReader extends OsmServerReader {
    private MultiOsmReader rdr = new MultiOsmReader();
    public void ReadObject(PrimitiveId id, int version, ProgressMonitor progressMonitor) throws OsmTransferException {
        ReadObject(id.getUniqueId(), version, id.getType(), progressMonitor);
    }
    public void ReadObject(long id,int version,OsmPrimitiveType type,ProgressMonitor progressMonitor) throws OsmTransferException {
        StringBuffer sb = new StringBuffer();
        sb.append(type.getAPIName());
        sb.append("/");
        sb.append(id);
        sb.append("/");
        sb.append(version);
        progressMonitor.beginTask("", 1);
        InputStream in = getInputStream(sb.toString(), progressMonitor.createSubTaskMonitor(1, true));
        try {
            rdr.AddData(in);
        } catch (Exception e) {
            throw new OsmTransferException(e);
        } finally {
            progressMonitor.finishTask();
            if (in!=null) {
                try {
                    in.close();
                } catch(Exception e) {}
            }
        }
    }
    /**
     * Method to parse downloaded objects
     * @return the data requested
     * @throws SAXException
     * @throws IOException
     */
    @Override
    public DataSet parseOsm(ProgressMonitor progressMonitor) throws OsmTransferException {
        progressMonitor.beginTask("", 1);
        progressMonitor.subTask(tr("Preparing history data..."));
        try {
            rdr.ProcessData();
            return rdr.getDataSet();
        } catch (Exception e) {
            throw new OsmTransferException(e);
        } finally {
            progressMonitor.finishTask();
            activeConnection = null;
        }
    }
}
