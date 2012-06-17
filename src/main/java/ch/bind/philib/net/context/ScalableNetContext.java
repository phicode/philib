package ch.bind.philib.net.context;

public class ScalableNetContext extends NetContextImpl implements NetContext {

	public ScalableNetContext() {
		super(null, null);

		// TODO
		// multi threaded net selector and buffer cache
		throw new UnsupportedOperationException("TODO: scalable net selector");
	}
}
