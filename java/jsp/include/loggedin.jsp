<%

String cookieName = "openstreetmap";
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
  sToken = myCookie.getValue();

  osmServerHandler osmSH = new osmServerHandler();

  bLoggedIn = osmSH.validateToken(sToken);

  if( bLoggedIn )
  {
    String sCookieName = "openstreetmap";
    java.util.Date now = new java.util.Date();
    String timestamp = now.toString();
    Cookie cookie = new Cookie (sCookieName, sToken);
    cookie.setMaxAge(10 * 60);
    response.addCookie(cookie);
    bLoggedIn = true;
  }

}

%>
