package ch.bind.philib.data;

public final class CompareUtil {

	private CompareUtil() {
	}

	public static final boolean equality(final Object o1, final Object o2) {
		if (o1 == null) {
			return (o2 == null);
		} else {
			if (o2 == null) {
				return false;
			} else {
				return o1.equals(o2);
			}
		}
	}
}
