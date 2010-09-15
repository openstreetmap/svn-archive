// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
// Some of the procedures below are imported from image4j.sourceforge.net, license LGPL.
package cadastre_fr;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

public abstract class ImageModifier {
    /**
     * Current background color used by cadastre.gouv.fr
     */
    //public static int cadastreBackgroundTransp = 1; // original white but transparent

    private static final long serialVersionUID = 1L;

    protected int parcelColor = Color.RED.getRGB();

    public BufferedImage bufferedImage;

    public static int[] cRoofColors = new int[] {-197380, -592138};
    public static int[] cBuilingFootColors = new int[] {-256};

    protected BufferedImage convert1(BufferedImage src) {
        IndexColorModel icm = new IndexColorModel(
            1, 2, new byte[] { (byte) 0, (byte) 0xFF },
            new byte[] { (byte) 0, (byte) 0xFF },
            new byte[] { (byte) 0, (byte) 0xFF }
        );

        BufferedImage dest = new BufferedImage(
            src.getWidth(), src.getHeight(),
            BufferedImage.TYPE_BYTE_BINARY,
            icm
            );

        ColorConvertOp cco = new ColorConvertOp(
            src.getColorModel().getColorSpace(),
            dest.getColorModel().getColorSpace(),
            null
            );

        cco.filter(src, dest);

        return dest;
      }

    /**
     * Converts the source image to 4-bit colour
     * using the default 16-colour palette:
     * <ul>
     *  <li>black</li><li>dark red</li><li>dark green</li>
     *  <li>dark yellow</li><li>dark blue</li><li>dark magenta</li>
     *  <li>dark cyan</li><li>dark grey</li><li>light grey</li>
     *  <li>red</li><li>green</li><li>yellow</li><li>blue</li>
     *  <li>magenta</li><li>cyan</li><li>white</li>
     * </ul>
     * No transparency.
     * @param src the source image to convert
     * @return a copy of the source image with a 4-bit colour depth, with the default colour pallette
     */
    protected BufferedImage convert4(BufferedImage src) {
        int[] cmap = new int[] {
          0x000000, 0x800000, 0x008000, 0x808000,
          0x000080, 0x800080, 0x008080, 0x808080,
          0xC0C0C0, 0xFF0000, 0x00FF00, 0xFFFF00,
          0x0000FF, 0xFF00FF, 0x00FFFF, 0xFFFFFF
        };
        return convert4(src, cmap);
      }

      /**
       * Converts the source image to 4-bit colour
       * using the given colour map.  No transparency.
       * @param src the source image to convert
       * @param cmap the colour map, which should contain no more than 16 entries
       * The entries are in the form RRGGBB (hex).
       * @return a copy of the source image with a 4-bit colour depth, with the custom colour pallette
       */
    protected BufferedImage convert4(BufferedImage src, int[] cmap) {
        IndexColorModel icm = new IndexColorModel(
            4, cmap.length, cmap, 0, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE
            );
        BufferedImage dest = new BufferedImage(
            src.getWidth(), src.getHeight(),
            BufferedImage.TYPE_BYTE_BINARY,
            icm
            );
        ColorConvertOp cco = new ColorConvertOp(
            src.getColorModel().getColorSpace(),
            dest.getColorModel().getColorSpace(),
            null
            );
        cco.filter(src, dest);

        return dest;
      }

    protected BufferedImage convert8(BufferedImage src) {
        BufferedImage dest = new BufferedImage(
            src.getWidth(), src.getHeight(),
            BufferedImage.TYPE_BYTE_INDEXED
            );
        ColorConvertOp cco = new ColorConvertOp(
            src.getColorModel().getColorSpace(),
            dest.getColorModel().getColorSpace(),
            null
            );
        cco.filter(src, dest);
        return dest;
      }

    public boolean isBuildingColor(int rgb, boolean ignoreParcelColor) {
        for (int i = 0; i < cBuilingFootColors.length; i++)
            if (rgb == cBuilingFootColors[i])
                    return true;
        if (ignoreParcelColor && (rgb == parcelColor))
            return true;
        return false;
    }

    public boolean isRoofColor(int rgb, boolean ignoreParcelColor) {
        for (int i = 0; i < cRoofColors.length; i++)
            if (rgb == cRoofColors[i])
                    return true;
        if (ignoreParcelColor && (rgb == parcelColor))
            return true;
        return false;
    }

    public boolean isParcelColor(BufferedImage img, int x, int y) {
        int rgb = img.getRGB(x, y);
        return (rgb == parcelColor);
    }

    public boolean isBuildingOrRoofColor(BufferedImage img, int x, int y, boolean ignoreParcelColor) {
        int rgb = img.getRGB(x, y);
        boolean ret = isBuildingColor(rgb, ignoreParcelColor) || isRoofColor(rgb, ignoreParcelColor);
        return ret;
    }

    public boolean isBuildingOrRoofColor(BufferedImage img, int x, int y, boolean colorType, boolean ignoreParcelColor) {
        int rgb = img.getRGB(x, y);
        boolean ret;
        if (colorType)
            ret = isBuildingColor(rgb, ignoreParcelColor);
        else
            ret = isRoofColor(rgb, ignoreParcelColor);
        return ret;
    }

    /**
     * Checks if the rgb value is the black background color
     * @param
     * @return
     */
    public boolean isBackgroundColor(BufferedImage img, int x, int y) {
        return (img.getRGB(x, y) == -1);
    }

}
