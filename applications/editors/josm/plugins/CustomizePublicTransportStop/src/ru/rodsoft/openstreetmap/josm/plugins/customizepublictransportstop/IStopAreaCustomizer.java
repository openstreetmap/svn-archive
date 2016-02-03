package ru.rodsoft.openstreetmap.josm.plugins.customizepublictransportstop;

/**
 * 
 * @author Rodion Scherbakov
 * Interface of operation of stop area customizing
 */
public interface IStopAreaCustomizer 
{
	/**
	 * Perform operation of customizing of stop area
	 * @param stopArea Stop area
	 * @return Stop area after customizing
	 */
	public StopArea performCustomizing(StopArea stopArea);
}
