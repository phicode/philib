package ch.bind.philib.lang;

public final class ExceptionUtil {

	private ExceptionUtil() {
	}

	public static String buildMessageChain(Throwable t) {
		if (t == null) {
			return "";
		}
		else {
			StringBuilder sb = new StringBuilder(128);
			add(sb, t);
			t = t.getCause();
			while (t != null) {
				// separator between causes;
				sb.append(" => ");
				add(sb, t);
				t = t.getCause();
			}
			return sb.toString();
		}
	}

	private static void add(StringBuilder sb, Throwable t) {
		sb.append(t.getClass().getSimpleName());
		String msg = t.getMessage();
		if (msg == null) {
			sb.append("()");
		}
		else {
			sb.append('(');
			sb.append(msg);
			sb.append(')');
		}
	}
}
