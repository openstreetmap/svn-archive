<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.apache.commons.fileupload.*" %>
<%@ page import="org.apache.xmlrpc.*" %>
<%@ page contentType="text/html" %>

<%!


%>

<html>

<%

String action = request.getParameter("action");

if(FileUpload.isMultipartContent(request))
{

  String email = "";
  String pass = "";
  
  out.print("Attempting to insert file...");

  if(FileUpload.isMultipartContent(request))
  {
    
    out.print("multipart ok<br>");
    // is it a multipart post?

    DiskFileUpload upload = new DiskFileUpload();

    out.print("diskupload ok<br>");

    List /* FileItem */ items = upload.parseRequest(request);

    out.print("list ok<br>");
    
    Iterator iter = items.iterator();
    
    InputStream uploadedStream; 
    
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
    
        uploadedStream = item.getInputStream();
      
      
      }
    }
  }


  // test the username and password

  boolean bLoggedIn = false;


  XmlRpcClient xmlrpc = new XmlRpcClient("http://www.openstreetmap.org/api/xml.jsp");

  Vector v = new Vector();

  v.addElement(email);
  v.addElement(pass);

  String sLoginToken = (String)xmlrpc.execute("openstreetmap.login",v);


  out.println("login: " + sLoginToken);


}
else
{

  %>

    <h1>Upload a GPX File</h1>
    <br><br>
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
