<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.openstreetmap.util.*"%>
<%@ page import="org.apache.commons.fileupload.*" %>
<%@ page import="org.apache.xmlrpc.*" %>
<%@ page contentType="text/html" %>

<%!


%>

<html>

<%

if(FileUpload.isMultipartContent(request))
{

  String email = "";
  String pass = "";
  String sFileName = "";
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
        String fileName = item.getName();
        String contentType = item.getContentType();
        boolean isInMemory = item.isInMemory();
        long sizeInBytes = item.getSize();

        out.print("fieldName: " + fieldName + "<br>");
        out.print("fileName: " + fileName + "<br>");
        out.print("contentType: " +  contentType + "<br>");
        out.print("isInMemory: " + isInMemory + "<br>");
        out.print("sizeInBytes: " + sizeInBytes + "<br>");
 
        sFileName = "/tmp/" + System.currentTimeMillis() + ".osm";
       
        fUploadedFile = new File(sFileName);
        
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

        out.print("now trying to upload it!<br>");

        osmGPXImporter gpxImporter = new osmGPXImporter();
        
        gpxImporter.upload(uploadedStream, out, sLoginToken);
        

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


</html>
