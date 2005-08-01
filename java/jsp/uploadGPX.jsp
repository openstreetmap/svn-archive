<%@ page import="java.util.*" %>
<%@ page import="java.util.zip.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.openstreetmap.util.*"%>
<%@ page import="org.openstreetmap.server.*"%>
<%@ page import="org.apache.commons.fileupload.*" %>
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


  if(FileUpload.isMultipartContent(request))
  {

    String sSaveFileName = "";
    String sUploadFileName = "";
    File fUploadedFile = null;

    out.print("Attempting to upload your file, please wait. Progress is indicated below:<br>");
    

    if(FileUpload.isMultipartContent(request))
    {

      DiskFileUpload upload = new DiskFileUpload();

      List items = upload.parseRequest(request);

      Iterator iter = items.iterator();

      BufferedInputStream uploadedStream = null; 

      while (iter.hasNext())
      {

        FileItem item = (FileItem) iter.next();

        if(item.isFormField())
        {
          // erm...


        }
        else
        {

          uploadedStream = new BufferedInputStream(item.getInputStream());

          String fieldName = item.getFieldName();
          sUploadFileName = item.getName();
          String contentType = item.getContentType();
          boolean isInMemory = item.isInMemory();
          long sizeInBytes = item.getSize();
          sSaveFileName = "/tmp/" + System.currentTimeMillis() + ".osm";

          fUploadedFile = new File(sSaveFileName);

          item.write(
              fUploadedFile);


        }
      }


      XmlRpcClient xmlrpc = new XmlRpcClient("http://www.openstreetmap.org/api/xml.jsp");



      if( uploadedStream != null)
      {


        osmGPXImporter gpxImporter = new osmGPXImporter();



        if( sUploadFileName.endsWith(".gz") )
        {

          gpxImporter.upload( new GZIPInputStream(uploadedStream), out, sToken, sUploadFileName);

        }
        else
        {
          gpxImporter.upload(uploadedStream, out, sToken,sUploadFileName);
        }

      }

    }
  }
  else
  {
    String sRequestType  = request.getParameter("action");

    %>

      <h1>Upload a GPX File</h1>
      <br>A GPX file contains a trail recorded by GPS receivers and uploaded to your computer. From your computer you can upload the GPX file to OpenStreetMap for editing. A plain gpx file or a gzipped one can be uploaded using the form below. To get help on this form and more information on GPX files, click <a href="/wiki/index.php/Upload">here</a>.<br><br>
      <form action="/edit/uploadGPX.jsp" enctype="multipart/form-data" method="post">
      <table>
      <tr><td>file:</td><td><input type="file" name="file"></td></tr>
      <tr><td></td><td><input type="submit" value="Go!"></td></tr>
      </table>
      </form>


      <%

      if(sRequestType != null)
      {
        if( sRequestType.equals("delete") )
        {
          String sGpxUID = request.getParameter("gpxUID");
          if( sGpxUID != null)
          {
            try
            {
              int nGPXUID = Integer.parseInt(sGpxUID);
              boolean bSuccess = osmSH.dropGPX(sToken, nGPXUID);
              if( bSuccess)
              {
                %>
                  Deleted GPX file successfully... 
                  <%

              }
              else
              {
                %>
                  Error deleting that GPX file...
                  <%

              }


            }
            catch(NumberFormatException e)
            {

            }



          }


        }
      }

        Vector v = osmSH.getGPXFileInfo(sToken);

        Enumeration e = v.elements();

        %>
          <h3>Previously uploaded GPX files:</h3>
          <table id="keyvalue">
          <tr>
          <th>Filename</th>
          <th>Uploaded at</th>
          <th>actions</th>
          </tr>
          <%
        int nCount = 0;
        
        while(e.hasMoreElements())
        {
          String s = (String)e.nextElement();
          Date d = (Date)e.nextElement();
          Integer i = (Integer)e.nextElement();
          String sEmphasisColour = "";
          if( (nCount & 1) != 1)
          {
            sEmphasisColour = "<td bgcolor=\"#82bcff\">";
          }
          else
          {
            sEmphasisColour = "<td bgcolor=\"#ffffff\">";
          }
          nCount++;

          String sDeleteLink = "<a href=\"uploadGPX.jsp?action=delete&gpxUID=" + i + "\">delete</a>";

          %>
            <tr>
            <%=sEmphasisColour%>
            <%=s%>
            </td>
            <%=sEmphasisColour%>
            <%=d%>
            </td>
            <%=sEmphasisColour%>
            <%=sDeleteLink%>
            </td>
            </tr>


            <%


        }
        %>
          </table>
          <%



  }


}
%>
</div>
<%@ include file="include/bottom.jsp" %>
