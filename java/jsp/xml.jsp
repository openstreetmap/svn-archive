<%@ page import="java.io.*" %><%@ page import="java.util.zip.*" %><%@ page import="org.apache.xmlrpc.*" %><%@ page import="org.openstreetmap.server.osmServerHandler" %><%@ page contentType="text/xml" %><%!
XmlRpcServer xmlrpc = new XmlRpcServer();%><%

xmlrpc.addHandler("openstreetmap", new osmServerHandler());

ServletInputStream in = pageContext.getRequest().getInputStream();

byte[] result = xmlrpc.execute (in);


String encoding = request.getHeader("Accept-Encoding");    

boolean bUseGzip = false;

if(encoding != null && encoding.indexOf("gzip") != -1)
{
//  System.out.println("got gzip stream");
  response.setHeader("Content-Encoding" , "gzip");
  GZIPOutputStream out2 = new GZIPOutputStream(response.getOutputStream());

  out2.write(result,0,result.length);
  
  out2.finish();

  out2.flush();

}
else
{

//  System.out.println("got normal stream");
  for(int i = 0; i < result.length; i++)
  {
    out.write (result[i]);
  }

  out.flush();

}

%>
