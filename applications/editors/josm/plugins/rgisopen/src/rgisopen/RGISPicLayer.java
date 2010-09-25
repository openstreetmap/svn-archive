package rgisopen;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.openstreetmap.josm.plugins.piclayer.PicLayerAbstract;

/**
 * Modified PicLayerFromFile, ok.
 */
public class RGISPicLayer extends PicLayerAbstract {
    // File to load from.
    private File m_file;
    // Tooltip text
    private String m_tooltiptext;

    public RGISPicLayer( File file ) {
        // Remember the file
        m_file = file;
        // Generate tooltip text
        m_tooltiptext = m_file.getAbsolutePath();
    }

    @Override
    protected Image createImage() throws IOException {
        // Try to load file
        Image image = null;
        image = ImageIO.read(m_file);
        return image;
    }

    @Override
    protected String getPicLayerName() {
        return m_tooltiptext;
    }
}
