package ch.bind.philib.exp.signal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import ch.bind.philib.validation.Validation;

public final class Signals {

	/**
	 * Registers a handler for the given signal.
	 * This method will use reflection to search for a sun specific package to register the handler.
	 * If the package was not found or an exception occurred the handler will not be registered and
	 * this method returns {@code false}.
	 * 
	 * @param signal -
	 * @param handler -
	 * @return {@code true} if a signal handler was registered, {@code false} otherwise.
	 */
	public static boolean registerSignalHandler(Signal signal, SignalHandler handler) {
		Validation.notNull(signal);
		Validation.notNull(handler);

		Class<?> signalClass = null;
		Class<?> signalHandlerClass = null;
		try {
			signalClass = Class.forName("sun.misc.Signal");
			signalHandlerClass = Class.forName("sun.misc.SignalHandler");
		} catch (ClassNotFoundException e) {
			return false;
		}
		Constructor<?> ctor = null;
		try {
			ctor = signalClass.getConstructor(String.class);
		} catch (Exception e) {
			System.out.println("ctor");
			e.printStackTrace();
			return false;
		}
		Object sigObj = null;
		try {
			sigObj = ctor.newInstance(signal.toString());
		} catch (Exception e) {
			System.out.println("newInstance");
			e.printStackTrace();
			return false;
		}
		Method registerMethod = null;
		try {
			registerMethod = signalClass.getMethod("handle", signalClass, signalHandlerClass);
		} catch (Exception e) {
			System.out.println("getMethod handle");
			e.printStackTrace();
			return false;
		}
		SigHandlerInvocationHandler invocationHandler = new SigHandlerInvocationHandler(handler, signal);
		Object sunMiscSignalHandlerProxy = Proxy.newProxyInstance(Signals.class.getClassLoader(), new Class<?>[] { signalHandlerClass }, invocationHandler);
		try {
			Object old = registerMethod.invoke(null, sigObj, sunMiscSignalHandlerProxy);
			if (old != null) {
				invocationHandler.invokeChain = old;
			}
		} catch (Exception e) {
			System.out.println("invoke");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static final class SigHandlerInvocationHandler implements InvocationHandler {

		private final SignalHandler ourHandler;
		private final Signal ourSignal;
		private Object invokeChain;

		public SigHandlerInvocationHandler(SignalHandler ourHandler, Signal ourSignal) {
			super();
			this.ourHandler = ourHandler;
			this.ourSignal = ourSignal;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			ourHandler.handleSignal(ourSignal);
			if (invokeChain != null) {
				method.invoke(invokeChain, args);
			}
			return null;
		}
	}

	//	private static Proxy createSignalHandlerProxy() {
	//
	//	}

	public static void main(String[] args) {

		boolean ret = registerSignalHandler(Signal.HUP, new SignalHandler() {

			@Override
			public void handleSignal(Signal signal) {
				System.out.println("handleSignal(" + signal + ")");
			}
		});

		if (!ret) {
			System.out.println("registerSignalHandler failed");
			System.exit(1);
		}
		
		ret = registerSignalHandler(Signal.HUP, new SignalHandler() {

			@Override
			public void handleSignal(Signal signal) {
				System.out.println("second handleSignal(" + signal + ")");
			}
		});
		
		if (!ret) {
			System.out.println("registerSignalHandler 2 failed");
			System.exit(1);
		}
		
		System.out.println("signal registered");
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("interrupted");
				System.exit(0);
			}
		}
	}
}
