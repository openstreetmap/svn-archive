<%@ page import="java.util.*" %>
<%@ page import="java.util.zip.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.openstreetmap.util.*"%>
<%@ page import="org.apache.commons.fileupload.*" %>
<%@ page import="org.apache.xmlrpc.*" %>
<%@ page contentType="text/html" %>

<%!


%>

<html>

<jsp:include page="include/top.jsp" />

<%

if(FileUpload.isMultipartContent(request))
{

  String email = "";
  String pass = "";
  String sSaveFileName = "";
  String sUploadFileName = "";
  File fUploadedFile = null;

  out.print("Attempting to insert file...<br>");

  if(FileUpload.isMultipartContent(request))
  {

    out.print("multipart ok<br>");
    // is it a multipart post?

    DiskFileUpload upload = new DiskFileUpload();

    out.print("diskupload ok<br>");

    List items = upload.parseRequest(request);

    out.print("list ok<br>");

    Iterator iter = items.iterator();

    BufferedInputStream uploadedStream = null; 

    while (iter.hasNext()) {
      FileItem item = (FileItem) iter.next();

      if (item.isFormField()) {

        if(item.getFieldName().equals("email"))
        {
          email = item.getString();

        }

        if(item.getFieldName().equals("pass"))
        {
          pass = item.getString();

        }


        out.print("got a form field ok!<br>");

      } else {
        out.print("got a file<br>");

        uploadedStream = new BufferedInputStream(item.getInputStream());

        String fieldName = item.getFieldName();
        sUploadFileName = item.getName();
        String contentType = item.getContentType();
        boolean isInMemory = item.isInMemory();
        long sizeInBytes = item.getSize();

        out.print("fieldName: " + fieldName + "<br>");
        out.print("fileName: " + sUploadFileName + "<br>");
        out.print("contentType: " +  contentType + "<br>");
        out.print("isInMemory: " + isInMemory + "<br>");
        out.print("sizeInBytes: " + sizeInBytes + "<br>");
 
        sSaveFileName = "/tmp/" + System.currentTimeMillis() + ".osm";
       
        fUploadedFile = new File(sSaveFileName);
        
        item.write(
            fUploadedFile);
        

      }
    }



    // test the username and password

    boolean bLoggedIn = false;


    XmlRpcClient xmlrpc = new XmlRpcClient("http://www.openstreetmap.org/api/xml.jsp");

    Vector v = new Vector();

    v.addElement(email);
    v.addElement(pass);

    String sLoginToken = (String)xmlrpc.execute("openstreetmap.login",v);

    if( sLoginToken.equals("ERROR"))
    {
      out.print("login failure :-(<br>");

    }
    else
    {
      out.print("login success!<br>");

      if( uploadedStream != null)
      {

        out.print("now trying to upload it...<br>");

        osmGPXImporter gpxImporter = new osmGPXImporter();

        out.print("created importer ok at " + new java.util.Date() + "<br>");

        
        if( sUploadFileName.endsWith(".gz") )
        {
          out.print("looks like a gzip file...<br>");
          
          gpxImporter.upload( new GZIPInputStream(uploadedStream), out, sLoginToken);

        }
        else
        {
        
          out.print("looks like a gpx file...<br>");
          gpxImporter.upload(uploadedStream, out, sLoginToken);
 
        }

        out.println("All done at " + new java.util.Date());
      }

    }
  }


}
else
{

  %>

    <h1>Upload a GPX File</h1>
    <br>Here, you can upload a plain gpx file or a gzipped one.<br>
    <form action="http://www.openstreetmap.org/api/uploadGPX.jsp" enctype="multipart/form-data" method="post">
    <table>
    <tr><td>email address:</td><td><input type="text" name="email"></td></tr>
    <tr><td>password:</td><td><input type="password" name="pass"></td></tr>
    <tr><td>file:</td><td><input type="file" name="file"></td></tr>
    <tr><td></td><td><input type="submit" value="Go!"></td></tr>
    </table>
    </form>


    <%

}

%>


<jsp:include page="include/bottom.jsp" />
</html>
