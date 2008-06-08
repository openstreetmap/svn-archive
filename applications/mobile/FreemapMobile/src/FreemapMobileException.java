
public class FreemapMobileException extends Exception
{
	String userMessage, debugMessage;

	public FreemapMobileException(String userMessage,String debugMessage)
	{
		this.userMessage=userMessage;
		this.debugMessage=debugMessage;
	}

	public String toString()
	{
		return userMessage;
	}
	
	public String getDebugMessage()
	{
		return debugMessage;
	}
}