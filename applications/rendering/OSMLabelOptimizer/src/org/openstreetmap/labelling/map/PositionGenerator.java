/**
 * 
 */
package org.openstreetmap.labelling.map;


/**
 * @author sebi
 *
 */
public interface PositionGenerator
{
    /**
     * @param pointFeature
     * @param text
     */
    public void generatePointFeaturePositions(PointFeature pointFeature);

    /**
     * @param lf
     * @param text
     */
    public void generateLineFeaturePositions(LineFeature lf);

    /**
     * @param af
     * @param text
     */
    public void generateAreaFeaturePositions(AreaFeature af);

}
