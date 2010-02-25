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
