<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="javax.mail.*" %>
<%@ page import="javax.mail.internet.*" %>
<%@ page import="org.apache.xmlrpc.*" %>
<%@ page import="org.openstreetmap.server.osmServerHandler" %>

<%@ page contentType="text/html" %>
<%!
String smtpServer = "localhost";

%>
<html>
<%


// get the message parameters from the HTML page
String from = "bounce@openstreetmap.org";
String subject = "Your openstreetmap.org account request";
String to = "";
String text = ""; //req.getParameter("text");

String action = request.getParameter("action");
String email = request.getParameter("email");
String pass1 = request.getParameter("pass1");
String pass2 = request.getParameter("pass2");
String sPassedToken = request.getParameter("token");

if( action == null)
{
  %>
    <h1>Create a user account</h1><br>
    Fill in the form, we'll send you an email to confirm you're you and activate your account.<br><br>
    <form action="http://www.openstreetmap.org/api/newUser.jsp">
    <table>
    <tr><td>email address:</td><td><input type="text" name="email"></td></tr>
    <tr><td>password:</td><td><input type="password" name="pass1"></td></tr>
    <tr><td>retype password:</td><td><input type="password" name="pass2"></td></tr>
    <tr><td></td><td><input type="submit" value="Go!"></td></tr>
    </table>
    <input type="hidden" name="action" value="send">
    </form>


    <%
}
else
{
  if( action.equals("confirm")
      && sPassedToken != null
      && email != null)
  {
    try
    {

      XmlRpcClient xmlrpc = new XmlRpcClient("http://www.openstreetmap.org/api/xml.jsp");

      Vector v = new Vector();

      v.addElement(email);
      v.addElement(sPassedToken);

      Boolean bReturn = (Boolean)xmlrpc.execute("openstreetmap.confirmUser",v);

      if( bReturn.booleanValue() )
      {
        %>
         Thanks, all done! Your account is now active.<br><br><a href="http://www.openstreetmap.org/">back to openstreetmap</a>
        <%
      }
      else
      {
        throw new Exception();

      }


    } 
    catch (Exception e)
    {
      %>
        Something went wrong confirming that address, if the problem persists please <a href="mailto:steve@fractalus.com">report it</a>.<br><br><a href="http://www.openstreetmap.org/">back to openstreetmap</a>
        <%
    }



  }

    if( action.equals("send")
        && pass1 != null
        && pass2 != null
        && email != null
        && pass1.equals(pass2))
    {

      try {

        boolean bXmlSuccess = false;


        XmlRpcClient xmlrpc = new XmlRpcClient("http://www.openstreetmap.org/api/xml.jsp");

        Vector v = new Vector();

        v.addElement(email);

        boolean bUserExists = ((Boolean)xmlrpc.execute("openstreetmap.userExists",v)).booleanValue();

        v = new Vector();

        v.addElement(email);
        v.addElement(pass1);

        String sToken = (String)xmlrpc.execute("openstreetmap.addUser",v);

        if( !sToken.equals("ERROR"))
        {

          bXmlSuccess = true;

        }

        if( !bUserExists && bXmlSuccess)
        {


          Properties properties = System.getProperties();
          properties.put("mail.smtp.host", smtpServer);

          Session session2 = Session.getInstance(properties, null);

          MimeMessage message = new MimeMessage(session2);

          // set the from address
          Address fromAddress = new InternetAddress(from);
          message.setFrom(fromAddress);

          // set the to address
          if (to != null) {
            Address[] toAddress = InternetAddress.parse(email);
            Address[] openstreetmapAddress = InternetAddress.parse("dev@openstreetmap.org");
            message.setRecipients(Message.RecipientType.TO, toAddress);
            message.setRecipients(Message.RecipientType.BCC, openstreetmapAddress);
          }

          // set the subject
          message.setSubject(subject);


          text = ""
            + "Dear " + email + ",\n"
            + "\n"
            + "Someone (maybe you) has requested an openstreetmap.org user account.\n"
            + "\n"
            + "To activate it, click this link\n"
            + "http://www.openstreetmap.org/api/newUser.jsp?action=confirm&email=" + email + "&token=" + sToken + "\n"
            + "\n"
            + "To report abuse, please mail webmaster@openstreetmap.org";

          message.setText(text);

          // send the message
          Transport.send(message);

          %>
            <b>All done!</b> You should get an email shortly. Read it, click on the link and your account will be activated. Have fun!<br><br><a href="http://www.openstreetmap.org/">back to openstreetmap</a>
            <%
        }

    }
    catch (Exception e) {
      %>
        Something went wrong with that email address, if the problem persists please <a href="mailto:dev@openstreetmap.org">report it</a>.<br><br><a href="http://www.openstreetmap.org/">back to openstreetmap</a>
        <%
    }
  }
}

out.flush();

%>

</html>
