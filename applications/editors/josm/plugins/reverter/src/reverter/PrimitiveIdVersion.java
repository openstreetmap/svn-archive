package reverter;

import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.tools.CheckParameterUtil;

public class PrimitiveIdVersion {
    private final PrimitiveId id;
    private final int version;
    public PrimitiveIdVersion(PrimitiveId id, int version) {
        CheckParameterUtil.ensureParameterNotNull(id, "id");
        this.id = id;
        this.version = version;
    }

    public PrimitiveId getPrimitiveId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PrimitiveIdVersion other = (PrimitiveIdVersion) obj;
        if (!id.equals(other.id)) return false;
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return id.toString() + "/" + version;
    }

}
