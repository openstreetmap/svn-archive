<%@ page import="java.io.*" %>
<%@ page import="org.apache.xmlrpc.*" %>
<%@ page import="org.openstreetmap.server.osmServerHandler" %>
<%@ page contentType="text/xml" %><%!
XmlRpcServer xmlrpc = new XmlRpcServer();
%><%

xmlrpc.addHandler("openstreetmap", new osmServerHandler());

ServletInputStream in = pageContext.getRequest().getInputStream();

byte[] result = xmlrpc.execute (in);

response.setContentLength (result.length);

for(int i = 0; i < result.length; i++)
{
  out.write (result[i]);
}

out.flush ();
%>
