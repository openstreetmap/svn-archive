import java.lang.*;
import java.util.*;

import com.bbn.openmap.omGraphics.OMLine;


public class osmStreetSegment extends OMLine
{

  int nUid;


  public osmStreetSegment(
      float lat1,
      float lon1,
      float lat2,
      float lon2,
      int line_type,
      int uid)
  {

    super(lat1, lon1, lat2, lon2, line_type);

    nUid = uid;
    

  } // osmStreetSegment



  public int getUid()
  {
    return nUid;

  } // getUid
  
  


} // osmStreetSegment











