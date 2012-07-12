/*
 * Copyright (c) 2006-2011 Philipp Meinen <philipp@bind.ch>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.bind.philib.validation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ValidationUtilTest {

	@Test
	public void validateOne() {
		ValidatableDummy dummy = new ValidatableDummy();

		ValidationResults results = ValidationUtil.validate(dummy);
		assertEquals(0, results.getNumErrors());

		dummy.setGenerateError(true);
		results = ValidationUtil.validate(dummy);
		assertEquals(1, results.getNumErrors());
	}

	@Test
	public void validateCollection() {
		List<ValidatableDummy> list = new ArrayList<ValidatableDummy>();
		list.add(new ValidatableDummy());
		list.add(new ValidatableDummy());

		ValidationResults results = ValidationUtil.validate(list);
		assertEquals(0, results.getNumErrors());

		list.get(0).setGenerateError(true);
		results = ValidationUtil.validate(list);
		assertEquals(1, results.getNumErrors());

		list.get(0).setGenerateError(false);
		list.get(1).setGenerateError(true);
		results = ValidationUtil.validate(list);
		assertEquals(1, results.getNumErrors());

		list.get(0).setGenerateError(true);
		list.get(1).setGenerateError(true);
		results = ValidationUtil.validate(list);
		assertEquals(2, results.getNumErrors());
	}

	@Test
	public void validateArray() {
		ValidatableDummy[] arr = new ValidatableDummy[2];
		arr[0] = new ValidatableDummy();
		arr[1] = new ValidatableDummy();

		ValidationResults results = ValidationUtil.validate(arr);
		assertEquals(0, results.getNumErrors());

		arr[0].setGenerateError(true);
		results = ValidationUtil.validate(arr);
		assertEquals(1, results.getNumErrors());

		arr[0].setGenerateError(false);
		arr[1].setGenerateError(true);
		results = ValidationUtil.validate(arr);
		assertEquals(1, results.getNumErrors());

		arr[0].setGenerateError(true);
		arr[1].setGenerateError(true);
		results = ValidationUtil.validate(arr);
		assertEquals(2, results.getNumErrors());
	}

	@Test(expected = NullPointerException.class)
	public void validateOneThrowNPE() {
		ValidatableDummy dummy = null;
		ValidationUtil.validate(dummy);
	}

	@Test(expected = NullPointerException.class)
	public void validateCollectionThrowNPE() {
		List<Validatable> list = new ArrayList<Validatable>();
		ValidatableDummy dummy = null;
		list.add(dummy);
		assertEquals(1, list.size());
		ValidationUtil.validate(list);
	}

	@Test(expected = NullPointerException.class)
	public void validateArrayThrowNPE() {
		Validatable[] arr = new Validatable[1];
		ValidationUtil.validate(arr);
	}
}
