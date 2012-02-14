package ch.bind.philib.util;

import java.util.concurrent.atomic.AtomicReference;

public class Service {
	
	private final AtomicReference<ServiceState> state = new AtomicReference<ServiceState>(ServiceState.INITIAL);
	
	
}
