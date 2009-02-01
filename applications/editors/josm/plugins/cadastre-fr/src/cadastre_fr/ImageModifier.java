package cadastre_fr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.tools.ColorHelper;

public class ImageModifier {

    /**
     * Current background color used by cadastre.gouv.fr
     */
    private static final long serialVersionUID = 1L;

    public static final int cadastreBackground = -1; // white

    public BufferedImage bufferedImage;

    private boolean withBackground = false;

    private int backgroundPixel = 0;
    
    private int backgroundSampleX, backgroundSampleY;

    public ImageModifier(BufferedImage bi) {
        bufferedImage = bi;
        if (Main.pref.getBoolean("cadastrewms.alterColors")) {
            changeColors();
            if (Main.pref.getBoolean("cadastrewms.backgroundTransparent")) {
                makeTransparent();
            }
        }
    }

    /**
     * Replace the background color by the josm color.background color.
     * @param bi
     * @return
     */
    private void changeColors() {
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        int pixel;
        int josmBackgroundColor = ColorHelper.html2color(Main.pref.get("color.background", "#FFFFFF")).getRGB();
        boolean invertGrey = (Main.pref.getBoolean("cadastrewms.invertGrey"));
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                pixel = bufferedImage.getRGB(x, y);
                if (pixel == cadastreBackground) {
                    bufferedImage.setRGB(x, y, josmBackgroundColor);
                    if (!withBackground)
                        withBackground = true;
                    backgroundSampleX = x;
                    backgroundSampleY = y;
                } else if (invertGrey) {
                    bufferedImage.setRGB(x, y, reverseIfGrey(pixel));
                }
            }
        }
    }
    
    /**
     * Reverse the grey value if the pixel is grey (light grey becomes dark grey)
     * Used for texts. 
     * @param pixel
     * @return
     */
    private int reverseIfGrey(int pixel) {
        Color col = new Color(pixel);
        int r = col.getRed();
        int g = col.getGreen();
        int b = col.getBlue();
        if ((b == r) && (b == g)) {
            pixel = (0x00 << 32) + ((byte) (255 - r) << 16) + ((byte) (255 - r) << 8) + ((byte) (255 - r));
        }
        return pixel;
    }

    private void makeTransparent() {
        ColorModel colorModel = bufferedImage.getColorModel();
        if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            // vector image (IndexColorModel)
            IndexColorModel icm = (IndexColorModel) colorModel;
            WritableRaster raster = bufferedImage.getRaster();
            // pixel is offset in ICM's palette
            if (withBackground)
                backgroundPixel = raster.getSample(backgroundSampleX, backgroundSampleY, 0);
            else
                backgroundPixel = 1; // default Cadastre background sample
            int size = icm.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            icm.getReds(reds);
            icm.getGreens(greens);
            icm.getBlues(blues);
            IndexColorModel icm2 = new IndexColorModel(colorModel.getPixelSize(), size, reds, greens, blues,
                    backgroundPixel);
            bufferedImage = new BufferedImage(icm2, raster, bufferedImage.isAlphaPremultiplied(), null);
        } else {
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color c = new Color(bufferedImage.getRGB(x, y));
                    int r = c.getRed();
                    int g = c.getGreen();
                    int b = c.getBlue();
                    Color maskedColor;
                    if (r==0 && g==0 && b==0) {
                        maskedColor = new Color(r, g, b, 0x00);
                    } else {
                        maskedColor = new Color(r, g, b, 0xFF);
                    }
                    bi.setRGB(x, y, maskedColor.getRGB());
                }
            }
            bufferedImage = bi;
        }
        return;
    }
}
