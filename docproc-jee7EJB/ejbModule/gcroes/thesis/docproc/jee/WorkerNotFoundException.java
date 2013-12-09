package gcroes.thesis.docproc.jee;

public class WorkerNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973629183756780342L;

	public WorkerNotFoundException(String msg) {
		super(msg);
	}
	
	public WorkerNotFoundException(){
		super();
	}
	
}
