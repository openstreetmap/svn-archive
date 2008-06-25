import javax.microedition.location.LandmarkStore;
import org.xml.sax.helpers.DefaultHandler;

public class LandmarkSourceParserHandler extends DefaultHandler
{
  protected LandmarkStore store;
  
  public void setLandmarkStore(LandmarkStore store)
  {
    this.store=store;
  }
}