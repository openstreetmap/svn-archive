/**
 * print version of batik to standard out and exit.
 *
 *
 * <table border=1>
 * <tr><td> Source           </td><td>  Version string                 </td></tr>
 * <tr><td> Release version  </td><td>  version                        </td></tr>
 * <tr><td> Trunk            </td><td>  version&#43;rrevision              </td></tr>
 * <tr><td> Branch           </td><td>  version&#43;rrevision; branch-name </td></tr>
 * <tr><td> Unknown          </td><td>  development version            </td></tr>
 * </table>
 *
 * Prior to release 1.7, the version string would be the straight tag
 * (e.g. &quot;batik-1_6&quot;) or the string &quot;development.version&quot;. revision is the 
 * Subversion working copy's revision number. 
 *
 * @see org.apache.batik.Version
 */

public class PrintBatikVersion {
   /**
    * print version of batik to standard out and exit.
    */
   public static void main(String[] args) {
      System.out.println(org.apache.batik.Version.getVersion());
   }
}
