package cadastre_fr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;

import org.openstreetmap.josm.Main;

public class RasterImageModifier extends ImageModifier {

    private int cadastreBackground = -1; // white
    
    public static int cadastreBackgroundTransp = 16777215; // original white but transparent
    
    private boolean transparencyEnabled = false;

    public RasterImageModifier(BufferedImage bi) {
        bufferedImage = bi;
        transparencyEnabled = Main.pref.getBoolean("cadastrewms.backgroundTransparent"); 
        if (transparencyEnabled)
            makeTransparent();
        if (Main.pref.getBoolean("cadastrewms.invertGrey"))
            invertGrey();
    }

    /**
     * Invert black/white/grey pixels (to change original black characters to white).
     */
    private void invertGrey() {
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = bufferedImage.getRGB(x, y);
                if ((!transparencyEnabled && pixel != cadastreBackground) 
                        || (transparencyEnabled && pixel != cadastreBackgroundTransp)) {
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
        if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
            // raster image (ComponentColorModel)
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    Color c = new Color(rgb);
                    int r = c.getRed();
                    int g = c.getGreen();
                    int b = c.getBlue();
                    Color maskedColor;
                    if (rgb == cadastreBackground) {
                        maskedColor = new Color(r, g, b, 0x00); // transparent
                    } else {
                        maskedColor = new Color(r, g, b, 0xFF); // opaque
                    }
                    //maskedColor = new Color(r, g, b, alpha);
                    bi.setRGB(x, y, maskedColor.getRGB());
                }
            }
            bufferedImage = bi;
        }
        return;
    }

}
