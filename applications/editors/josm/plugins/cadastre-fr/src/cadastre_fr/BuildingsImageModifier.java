package cadastre_fr;

public class BuildingsImageModifier extends ImageModifier {

    public BuildingsImageModifier() {super();};
        
    public BuildingsImageModifier(GeorefImage buildings, GeorefImage parcels) {
        bufferedImage = buildings.image;
        VectorImageModifier vim = new VectorImageModifier();
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                if (vim.isBuildingOrRoofColor(bufferedImage, x, y, false)
                        && !vim.isBackgroundColor(parcels.image, x, y)) {
                    // create a clear 'cut' for the parcels
                    bufferedImage.setRGB(x, y, parcelColor);
                }
            }
        }
    }
    
}
