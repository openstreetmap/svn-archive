package org.openstreetmap.josm.plugins.graphview.core.transition;

/**
 * observer that will be informed about changes in a TransitionStructure
 * if it has been registered using {@link TransitionStructure#addObserver(TransitionStructureObserver)}
 */
public interface TransitionStructureObserver {

	/**
	 * informs this observer about changes in an observed transition structure
	 * @param transitionStructure  observed transition structure that has changed; != null
	 */
	public void update(TransitionStructure transitionStructure);

}
