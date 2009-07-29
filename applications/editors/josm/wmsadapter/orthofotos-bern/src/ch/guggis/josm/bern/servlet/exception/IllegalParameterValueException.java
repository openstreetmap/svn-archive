package ch.guggis.josm.bern.servlet.exception;

public class IllegalParameterValueException extends
		OrthofotoBernWMSAdapterException {
	private String parameterName;
	private String reason;
	private Throwable cause;

	public IllegalParameterValueException(String parameterName, String reason) {
		this.parameterName = parameterName;
		this.reason = reason;
	}

	public IllegalParameterValueException(String parameterName, String reason,
			Throwable cause) {
		this(parameterName, reason);
		this.cause = cause;
	}

	@Override
	public Throwable getCause() {
		return cause;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("invalid parameter value for parameter '%s'."));
		if (reason != null) {
			sb.append("reason:");
			sb.append(reason);
		}
		return sb.toString();
	}

}
