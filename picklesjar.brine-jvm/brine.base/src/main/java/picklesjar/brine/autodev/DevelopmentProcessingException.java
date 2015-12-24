package picklesjar.brine.autodev;

public class DevelopmentProcessingException
	extends Exception {
	
	private static final long serialVersionUID = -8030946709236687940L;
	
	public DevelopmentProcessingException() {
	
		super();
	}
	
	public DevelopmentProcessingException( Exception exp ) {
	
		super( exp );
	}
	
	public DevelopmentProcessingException( String message ) {
	
		super( message );
	}
	
	public DevelopmentProcessingException( String message, Exception exp ) {
	
		super( message, exp );
	}
}
