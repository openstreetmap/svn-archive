package ch.guggis.josm.bern.servlet.exception;

public class MissingParameterException extends OrthofotoBernWMSAdapterException {

	private String parameterName = null;

	public MissingParameterException(String parameterName) {
		super();
		this.parameterName = parameterName;
	}

	@Override
	public Throwable getCause() {
		// TODO Auto-generated method stub
		return super.getCause();
	}

	@Override
	public String getMessage() {
		return String.format(
				"mandatory paramter '%s' was missing in the servlet request",
				parameterName);
	}
}
