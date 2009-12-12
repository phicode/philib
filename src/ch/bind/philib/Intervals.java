package ch.bind.philib;

public final class Intervals {

	private Intervals() {
	}

	public static int chooseInterval(int maxValue, int maxSegments) {
		// try to bring maxSegments or less lines on to the chart
		int interval = 1;
		int segments = maxValue;

		// use these intervals:
		// 1, 2, 5,
		// 10, 25, 50
		// 100, 250, 500
		// ... and so on
		int intervalNum = 0;
		while (segments > maxSegments) {
			intervalNum++;

			// 0 for 1, 2, 5
			// 1 for 10, 25, 50
			// 2 for 100, 250, 500
			int power = (intervalNum / 3);

			double multiply = Math.pow(10, power);
			int num = intervalNum % 3;
			if (num == 0) { // 1, 10, 100, 100, ...
				interval = (int) (1.0 * multiply);
			} else if (num == 1) { // 2, 25, 250, 2500, ...
				interval = (int) (2.5 * multiply);
			} else { // 5, 50, 500, 5000, ...
				interval = (int) (5.0 * multiply);
			}

			segments = maxValue / interval;
			if (segments * interval < maxValue)
				segments++;
		}

		return interval;
	}
}
