package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.LanguageInfo;
import org.openstreetmap.josm.tools.OpenBrowser;
import relcontext.ChosenRelation;
import relcontext.ChosenRelationListener;

public class RelationHelpAction extends AbstractAction implements ChosenRelationListener {
    private ChosenRelation rel;

    public RelationHelpAction( ChosenRelation rel ) {
        super();
        putValue(NAME, tr("Relation wiki page"));
        putValue(SHORT_DESCRIPTION, tr("Launch browser with wiki help for selected object"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "search"));
        this.rel = rel;
        rel.addChosenRelationListener(this);
        setEnabled(false);
    }

    /**
     * Copypasted from {@link org.openstreetmap.josm.gui.dialogs.properties.PropertiesDialog.HelpAction}.
     */
    public void actionPerformed( ActionEvent e ) {
        if( rel.get() == null )
            return;
        try {
            String base = Main.pref.get("url.openstreetmap-wiki", "http://wiki.openstreetmap.org/wiki/");
            String lang = LanguageInfo.getWikiLanguagePrefix();
            final List<URI> uris = new ArrayList<URI>();
            String type = URLEncoder.encode(rel.get().get("type"), "UTF-8");

            if (type != null && !type.equals("")) {
                uris.add(new URI(String.format("%s%sRelation:%s", base, lang, type)));
                uris.add(new URI(String.format("%sRelation:%s", base, type)));
            }

            uris.add(new URI(String.format("%s%sRelations", base, lang)));
            uris.add(new URI(String.format("%sRelations", base)));

            Main.worker.execute(new Runnable(){
                public void run() {
                    try {
                        // find a page that actually exists in the wiki
                        HttpURLConnection conn;
                        for (URI u : uris) {
                            conn = (HttpURLConnection) u.toURL().openConnection();
                            conn.setConnectTimeout(5000);

                            if (conn.getResponseCode() != 200) {
                                System.out.println("INFO: " + u + " does not exist");
                                conn.disconnect();
                            } else {
                                int osize = conn.getContentLength();
                                conn.disconnect();

                                conn = (HttpURLConnection) new URI(u.toString()
                                        .replace("=", "%3D") /* do not URLencode whole string! */
                                        .replaceFirst("/wiki/", "/w/index.php?redirect=no&title=")
                                ).toURL().openConnection();
                                conn.setConnectTimeout(5000);

                                /* redirect pages have different content length, but retrieving a "nonredirect"
                                 *  page using index.php and the direct-link method gives slightly different
                                 *  content lengths, so we have to be fuzzy.. (this is UGLY, recode if u know better)
                                 */
                                if (Math.abs(conn.getContentLength() - osize) > 200) {
                                    System.out.println("INFO: " + u + " is a mediawiki redirect");
                                    conn.disconnect();
                                } else {
                                    System.out.println("INFO: browsing to " + u);
                                    conn.disconnect();

                                    OpenBrowser.displayUrl(u.toString());
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void chosenRelationChanged( Relation oldRelation, Relation newRelation ) {
        setEnabled(newRelation != null);
    }
}
