package iodb;

/**
 * A listener for {@link SimpleOffsetQueryTask}.
 *
 * @author Zverik
 * @license WTFPL
 */
public interface QuerySuccessListener {

    /**
     * Query has been processed and did not fail.
     */
    void queryPassed();
}
