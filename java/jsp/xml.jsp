<%@ page import="java.io.*" %>
<%@ page import="java.util.zip.*" %>
<%@ page import="org.apache.xmlrpc.*" %>
<%@ page import="org.openstreetmap.server.osmServerHandler" %>
<%@ page contentType="text/xml" %><%!
XmlRpcServer xmlrpc = new XmlRpcServer();
%><%

xmlrpc.addHandler("openstreetmap", new osmServerHandler());

ServletInputStream in = pageContext.getRequest().getInputStream();

byte[] result = xmlrpc.execute (in);


String encoding = request.getHeader("Accept-Encoding");    

OutputStream out2;

boolean bUseGzip = false;

if (encoding != null && encoding.indexOf("gzip") != -1)
{
  System.out.println("got gzip stream");
  response.setHeader("Content-Encoding" , "gzip");
  out2 = new GZIPOutputStream(response.getOutputStream());

  PrintWriter pw = new PrintWriter(out2, false);

  for(int i = 0; i < result.length; i++)
  {
    out2.write (result[i]);
  }


  out2.flush();


}
else
{

  System.out.println("got normal  stream");
  for(int i = 0; i < result.length; i++)
  {
    out.write (result[i]);
  }



}


//response.setContentLength (result.length);

out.flush ();
%>
