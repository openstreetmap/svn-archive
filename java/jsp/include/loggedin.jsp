<% 

String cookieName = "openstreetmao";
Cookie cookies [] = request.getCookies ();
Cookie myCookie = null;
boolean bLoggedIn = false;
String sToken = "";

if (cookies != null)
{
  for (int i = 0; i < cookies.length; i++) 
  {
    if (cookies [i].getName().equals (cookieName))
    {
      myCookie = cookies[i];
      break;
    }
  }
}

if(myCookie != null)
{
  String sToken = myCookie.getValue();

  osmServerHandler osmSH = new osmServerHandler();

  bLoggedIn = osmSH.validateToken(sToken);

}



%>
