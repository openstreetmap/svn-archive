<%@ page import="java.util.*" %>
<%@ page import="java.util.zip.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.openstreetmap.util.*"%>
<%@ page import="org.openstreetmap.server.*"%>
<%@ page import="org.apache.xmlrpc.*" %>
<%@ page contentType="text/html" %>

<%@ include file="include/top.jsp" %>


<div id="main_area">
  <%
if( !bLoggedIn )
{
  %>
    <%@ include file="include/loginMessage.jsp" %>
    <%
}
else
{
  String sAction = request.getParameter("action");


  if( sAction != null && sAction.equals("editSingleKey"))
  {
    String sKeyNum = request.getParameter("key");
    if( sKeyNum == null)
    {
      %>strange key value, defaulting to 1<br><%

    }
    else
    {
      int nKeyNum = 1;

      try{

        nKeyNum = Integer.parseInt(sKeyNum);

      }
      catch(Exception e)
      {

        %>strange key value, defaulting to 1<br><%

          nKeyNum = 1;
        
      }

        osmServerHandler osmSH = new osmServerHandler();

    %>
      <h1>Edit key</h1>
      <%

      String sSubAction = request.getParameter("subaction");

    if(sSubAction != null)
    {
      if( sSubAction.equals("changeKeyName"))
      {
        String sNewKeyName = request.getParameter("newKeyName");
        if( sNewKeyName != null )
        {
          osmSH.setNewKeyName(sToken, sNewKeyName, nKeyNum);
        }

      }

      if( sSubAction.equals("deleteKey"))
      {

        boolean bSuccess = osmSH.deleteKey(sToken, nKeyNum);
        String sSuccess = " ...failed!";
        if(bSuccess)
        {
          sSuccess = " ... success!";
        }

        %>
          Deleting key... <%=sSuccess%>
          <%

      }
      if( sSubAction.equals("undeleteKey"))
      {

        boolean bSuccess = osmSH.undeleteKey(sToken, nKeyNum);
        String sSuccess = " ...failed!";
        if(bSuccess)
        {
          sSuccess = " ... success!";
        }

        %>
          Undeleting key... <%=sSuccess%>
          <%


      }


    }

    Vector keys = osmSH.getKeyHistory(sToken, nKeyNum);


    Enumeration e = keys.elements();
    %>
      <br>
      <form action="editKeys.jsp">
      <table>
      <tr><td>Change key name to:</td><td><input type="text" name="newKeyName"></td></tr>
      <tr><td></td><td><input type="submit" value="Go!"></td></tr>
      </table>
      <input type="hidden" name="action" value="editSingleKey">
      <input type="hidden" name="subaction" value="changeKeyName">
      <%
      out.print("<input type=\"hidden\" name=\"key\" value=\"" + nKeyNum + "\">");
    %>
      </form>

      <br>
      <form action="editKeys.jsp">
      <input type="hidden" name="action" value="editSingleKey">
      <%
      out.print("<input type=\"hidden\" name=\"key\" value=\"" + nKeyNum + "\">");
    if( osmSH.getKeyVisible(sToken, nKeyNum) )
    {
      %>
        <input type="hidden" name="subaction" value="deleteKey">
        <input type="submit" value="Delete key">
        <%
    }
    else
    {
      %>
        <input type="hidden" name="subaction" value="undeleteKey">
        <input type="submit" value="Undelete key">
        <%
    }

    %>
      </form>



      <br><br>

      <h2>Key history:</h2>



      <table id="keyvalue" border="0" width="100%">
      <tr>
      <th>
      Key name
      </th>
      <th>
      Edited by
      </th>
      <th>
      Edited at
      </th>
      <th>
      Deleted
      </th>
      </tr>
      <%

      String sEmphasisColour = "";
    int nCount = 0;

    while( e.hasMoreElements() )
    {
      if( (nCount & 1) != 1)
      {
        sEmphasisColour = "<td bgcolor=\"#82bcff\">";
      }
      else
      {
        sEmphasisColour = "<td bgcolor=\"#ffffff\">";
      }

      String sName = (String)e.nextElement();
      Date dLastEdited = new Date( Long.parseLong((String)e.nextElement()));
      boolean bDeleted = ((String)e.nextElement()).equals("0");

      String sYesNo = "no";
      if(bDeleted)
      {
        sYesNo = "yes";
      }
      String sUserName = (String)e.nextElement();


      String sKeyURL = "<a href=\"editKeys.jsp?action=editSingleKey&key=" + sKeyNum + "\">";
      %>
        <tr>
        <%=sEmphasisColour%>
        <%=sName%>
        </td>
        <%=sEmphasisColour%>
        <%=sUserName%>
        </td><%=sEmphasisColour%>
        <%=dLastEdited%>
        </td>
        <%=sEmphasisColour%><%=sYesNo%></td>
        </tr>
        <%
        nCount++;
    }

    %>
      </table>
      <%





    }
  }
  else
  {
    osmServerHandler osmSH = new osmServerHandler();

    Vector keys = osmSH.getAllKeys(sToken, true); //get all visible keys

    Enumeration e = keys.elements();

    %>
      <h2>Visible keys:</h2>
      <table id="keyvalue" border="0" width="100%">
      <tr>
      <th>
      Key name
      </th>
      <th>
      Created by
      </th>
      <th>
      Created at
      </th>
      <th>
      Actions
      </th>
      </tr>
      <%

      String sEmphasisColour = "";
    int nCount = 0;

    while( e.hasMoreElements() )
    {
      if( (nCount & 1) != 1)
      {
        sEmphasisColour = "<td bgcolor=\"#82bcff\">";
      }
      else
      {
        sEmphasisColour = "<td bgcolor=\"#ffffff\">";
      }

      String sKeyNum = (String)e.nextElement();
      String sName = (String)e.nextElement();
      String sUserName = (String)e.nextElement();
      Date dLastEdited = new Date( Long.parseLong((String)e.nextElement()));

      String sKeyURL = "<a href=\"editKeys.jsp?action=editSingleKey&key=" + sKeyNum + "\">";
      %>
        <tr>
        <%=sEmphasisColour%>
        <%=sName%>
        </td>
        <%=sEmphasisColour%>
        <%=sUserName%>
        </td><%=sEmphasisColour%>
        <%=dLastEdited%>
        </td>
        <%=sEmphasisColour%><%=sKeyURL%><img src="/images/stock_edit-16.png" alt="edit this key" border="0"></a></td>
        </tr>
        <%
        nCount++;
    }

    %>
      </table>
      <br>
      <h2>Deleted keys:</h2>
      <%

      keys = osmSH.getAllKeys(sToken, false); // get all the deleted keys

    e = keys.elements();

    %>
      <table id="keyvalue" border="0" width="100%">
      <tr>
      <th>
      Key name
      </th>
      <th>
      Created by
      </th>
      <th>
      Created at
      </th>
      <th>
      Actions
      </th>
      </tr>
      <%

      nCount = 0;

    while( e.hasMoreElements() )
    {
      if( (nCount & 1) != 1)
      {
        sEmphasisColour = "<td bgcolor=\"#82bcff\">";
      }
      else
      {
        sEmphasisColour = "<td bgcolor=\"#ffffff\">";
      }

      String sKeyNum = (String)e.nextElement();
      String sName = (String)e.nextElement();
      String sUserName = (String)e.nextElement();
      Date dLastEdited = new Date( Long.parseLong((String)e.nextElement()));

      String sKeyURL = "<a href=\"editKeys.jsp?action=editSingleKey&key=" + sKeyNum + "\">";
      %>
        <tr>
        <%=sEmphasisColour%>
        <%=sName%>
        </td>
        <%=sEmphasisColour%>
        <%=sUserName%>
        </td><%=sEmphasisColour%>
        <%=dLastEdited%>
        </td>
        <%=sEmphasisColour%><%=sKeyURL%><img src="/images/stock_edit-16.png" alt="edit this key" border="0"></a></td>
        </tr>
        <%
        nCount++;
    }

    %>
      </table>
      <%


  }
}
%>
</div>
<%@ include file="include/bottom.jsp" %>
