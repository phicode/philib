package ch.bind.philib.lang;

public final class ToString {

	private ToString() {
	}

	public static StringBuilder start(final Object obj) {
		final StringBuilder sb = new StringBuilder();
		sb.append(obj.getClass().getSimpleName());
		sb.append('[');
		return sb;
	}

	public static String end(final StringBuilder sb) {
		return sb.append(']').toString();
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final Object obj) {
		sb.append(name).append('=').append(obj);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final Object obj) {
		sb.append(obj);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final boolean val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final boolean val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final char val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final char val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final int val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final int val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final long val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final long val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final float val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final float val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final String name, final double val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(final StringBuilder sb, final double val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final Object obj) {
		sb.append(", ").append(name).append('=').append(obj);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final Object obj) {
		sb.append(", ").append(obj);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final boolean val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final boolean val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final char val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final char val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final int val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final int val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final long val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final long val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final float val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final float val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final String name, final double val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(final StringBuilder sb, final double val) {
		sb.append(", ").append(val);
		return sb;
	}
}
