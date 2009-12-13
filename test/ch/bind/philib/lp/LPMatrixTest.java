package ch.bind.philib.lp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ch.bind.philib.lp.LPMatrix.MatrixPoint;

public class LPMatrixTest {

	@Test
	public void matrix() {
		LPMatrix matrix = new LPMatrix(2, 3);
		matrix.setSideCondition(0, new double[] { 1, 1, 40 },
				SideConditionType.SMALLER_EQUAL);
		matrix.setSideCondition(1, new double[] { 40, 120, 2400 },
				SideConditionType.SMALLER_EQUAL);
		matrix.setSideCondition(2, new double[] { 7, 12, 312 },
				SideConditionType.SMALLER_EQUAL);
		matrix.setTargetFunction(new double[] { 100, 250, 0 });

		System.out.println(matrix);
		
		// STEP 1
		MatrixPoint pivot = matrix.findPivot();
		assertNotNull(pivot);
		assertEquals(0, pivot.getX());
		assertEquals(0, pivot.getY());

		double result = matrix.transform(pivot);
		assertEquals(4000.0, result, 0.0);
		System.out.println(matrix);

		// STEP 2
		pivot = matrix.findPivot();
		assertNotNull(pivot);
		assertEquals(1, pivot.getX());
		assertEquals(2, pivot.getY());

		result = matrix.transform(pivot);
		assertEquals(4960.0, result, 0.0);
		System.out.println(matrix);
		
		// STEP 3
		pivot = matrix.findPivot();
		assertNotNull(pivot);
		assertEquals(0, pivot.getX());
		assertEquals(1, pivot.getY());

		result = matrix.transform(pivot);
		assertEquals(5400.0, result, 0.0);
		System.out.println(matrix);
		
		// NO STEP 4
		pivot = matrix.findPivot();
		assertNull(pivot);

		// CHECK RESULTS
		assertEquals(24.0, matrix.getX(0), 0.0);
		assertEquals(12.0, matrix.getX(1), 0.0);
	}

	public static void main(String[] args) {
		new LPMatrixTest().matrix();
	}
}
