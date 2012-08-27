package ch.bind.philib.net.events;

public interface HandleResult {

	
	
	public static final HandleResult DONT_CHANGE = new DontChange();
	
	public static final HandleResult CHANGE_OPS_READ = ;
	public static final HandleResult CHANGE_OPS_WRITE = ;
	public static final HandleResult CHANGE_OPS_READ_WRITE = ;
	public static final HandleResult CHANGE_HANDLER = ;
	
	public static final class DontChange implements HandleResult {
		
	}
}
