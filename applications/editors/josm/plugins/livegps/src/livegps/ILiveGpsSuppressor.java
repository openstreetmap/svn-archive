package livegps;

/**
 * Interface for class LiveGpsSuppressor, only has a query if currently an update is allowed.
 * 
 * @author casualwalker 
 *
 */
public interface ILiveGpsSuppressor {

	/**
	 * Query, if an update is currently allowed.
	 * When it is allowed, it will disable the allowUpdate flag as a side effect.
	 * (this means, one thread got to issue an update event)
	 *
	 * @return true, if an update is currently allowed; false, if the update shall be suppressed.
	 */
	boolean isAllowUpdate();

}
