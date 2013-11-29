package ch.bind.philib.lang;

public final class ToString {

	private ToString() {
	}

	public static StringBuilder start(Object obj) {
		StringBuilder sb = new StringBuilder();
		sb.append(obj.getClass().getSimpleName());
		sb.append('[');
		return sb;
	}

	public static String end(StringBuilder sb) {
		return sb.append(']').toString();
	}

	public static StringBuilder first(StringBuilder sb, String name, Object obj) {
		sb.append(name).append('=').append(obj);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, Object obj) {
		sb.append(obj);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, String name, boolean val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, boolean val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, String name, char val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, char val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, String name, int val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, int val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, String name, long val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, long val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, String name, float val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, float val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, String name, double val) {
		sb.append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder first(StringBuilder sb, double val) {
		sb.append(val);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, String name, Object obj) {
		sb.append(", ").append(name).append('=').append(obj);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, Object obj) {
		sb.append(", ").append(obj);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, String name, boolean val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, boolean val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, String name, char val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, char val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, String name, int val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, int val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, String name, long val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, long val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, String name, float val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, float val) {
		sb.append(", ").append(val);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, String name, double val) {
		sb.append(", ").append(name).append('=').append(val);
		return sb;
	}

	public static StringBuilder append(StringBuilder sb, double val) {
		sb.append(", ").append(val);
		return sb;
	}
}
