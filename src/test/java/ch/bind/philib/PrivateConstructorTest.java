/*
 * Copyright (c) 2011 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;

import org.junit.Test;
import static org.junit.Assert.*;
import static ch.bind.philib.io.BitOps.*;
// this is here to satisfy the code-coverage tool emma
// by invoking the private constructors.
// This helps to identify classes which do not yet have 100%
// test coverage, even utility classes.

public class PrivateConstructorTest {

	@Test
	public void invokeAllPrivateCtors() throws Exception {
		Package rootPackage = getClass().getPackage();
		String packageName = rootPackage.getName();
		// resource paths use forward slash / as separator, on all platforms
		String resourceRootPath = packageName.replace('.', '/');
		// System.out.printf("resourceRootPath=%s%n", resourceRootPath);
		// System.out.println(rootPackage.getName());
		// System.out.println(resourceRootPath);
		Enumeration<URL> urls = getClass().getClassLoader().getResources(resourceRootPath);
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			File path = new File(url.getPath());
			recursiveTestPrivateCtors(packageName, path, 1);
		}
	}

	private void recursiveTestPrivateCtors(String packageName, File path, int lvl) throws Exception {
		// System.out.println("checking dir " + file);
		File[] subPaths = path.listFiles();
		assertNotNull(path + ".listFiles() == null", subPaths);
		int nextLvl = lvl + 1;
		for (File subPath : subPaths) {
			if (subPath.isDirectory()) {
				String subPackageName = packageName + '.' + subPath.getName();
				recursiveTestPrivateCtors(subPackageName, subPath, nextLvl);
			} else if (subPath.isFile()) {
				maybeTestFile(packageName, subPath);
			} else {
				System.out.println("unknown file type: " + subPath);
			}
		}
	}

	private static String TEST_FILE_SUFFIX = "Test.class";

	private void maybeTestFile(String packageName, File subPath) throws Exception {
		// System.out.println("file: " +subFile);
		String name = subPath.getName();
		boolean doCheck = name.endsWith(".class") && !name.endsWith(TEST_FILE_SUFFIX);
		if (doCheck) {
			// strip away '.class'
			String className = name.substring(0, name.length() - 6);
			String fullClassName = packageName + '.' + className;
			// System.out.println("checking " + className + " => " +
			// fullClassName + " => " + subPath);
			Class<?> clazz = Class.forName(fullClassName);
			maybeCheckClass(clazz);
		}
	}

	private void maybeCheckClass(Class<?> clazz) throws Exception {
		if (clazz.isInterface()) {
			// System.out.println("ignoring because interface");
			return;
		}
		if (clazz.isEnum()) {
			// System.out.println("ignoring because enum");
			return;
		}
		if (clazz.isAnnotation()) {
			// System.out.println("ignoring because annotation");
			return;
		}
		if (clazz.isLocalClass()) {
			// System.out.println("ignoring because local class");
			return;
		}
		// TODO: inner classes, anonymous classes
		Constructor<?>[] ctors = clazz.getDeclaredConstructors();
		assertTrue(ctors.length > 0);
		for (Constructor<?> ctor : ctors) {
			if (ctor.isSynthetic()) {
				// introduced by the compiler
				// System.out.println("ignoring because synthetic: " +
				// ctor.toString());
			} else {
				int modifiers = ctor.getModifiers();
				if (checkMask(modifiers, Modifier.PRIVATE)) {
					int numParams = ctor.getParameterTypes().length;
					if (numParams == 0) {
						testCtor(ctor);
					}
				}
			}
		}
	}

	private void testCtor(Constructor<?> ctor) throws Exception {
		Object o;
		if (!ctor.isAccessible()) {
			ctor.setAccessible(true);
			o = ctor.newInstance(new Object[0]);
			ctor.setAccessible(false);
		} else {
			o = ctor.newInstance();
		}
		// System.out.println("instantiated a " + o.getClass().getName());
	}
}
