// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.tools.ColorHelper;

public class VectorImageModifier extends ImageModifier {

    private int cadastreBackground = -1; // white
    
    public static int cadastreBackgroundTransp = 1; // original white but transparent

    private boolean withBackground = false;

    private int backgroundPixel = 0;

    private int backgroundSampleX, backgroundSampleY;

    public VectorImageModifier(BufferedImage bi) {
        bufferedImage = bi;
        if (Main.pref.getBoolean("cadastrewms.backgroundTransparent"))
            makeTransparent();
        else if (Main.pref.getBoolean("cadastrewms.alterColors"))
            replaceBackground();
        if (Main.pref.getBoolean("cadastrewms.invertGrey"))
            invertGrey();
    }

    /**
     * Replace the background color by the josm color.background color.
     */
    private void replaceBackground() {
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        int josmBackgroundColor = ColorHelper.html2color(Main.pref.get("color.background", "#000000")).getRGB();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = bufferedImage.getRGB(x, y);
                if (pixel == cadastreBackground) {
                    bufferedImage.setRGB(x, y, josmBackgroundColor);
                    if (!withBackground)
                        withBackground = true;
                    backgroundSampleX = x;
                    backgroundSampleY = y;
                }
            }
        }
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
                if (pixel != cadastreBackground) {
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
        }
        return;
    }
}
