/**
 * 
 */
package livegps;

/**
 * This class is only used to prevent concurrent object modification. So all classes that
 * read or write live gps data must synchronize to this class. Especially the save action
 * takes quite long, so concurrency problems occur.
 * 
 * @author cdaller
 *
 */
public class LiveGpsLock {

}
