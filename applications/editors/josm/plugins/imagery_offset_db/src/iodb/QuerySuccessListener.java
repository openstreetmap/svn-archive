package iodb;

/**
 * A listener for {@link SimpleOffsetQueryTask}.
 *
 * @author zverik
 */
interface QuerySuccessListener {

    /**
     * Query has been processed and did not fail.
     */
    void queryPassed();
}
