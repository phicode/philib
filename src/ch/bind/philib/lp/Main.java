/*
 * Copyright (c) 2009 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.lp;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 
 * @author Philipp Meinen
 */
public class Main {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java " + Main.class.getName()
					+ " <file1> [<file2> ... ]");
			System.exit(-1);
		}
		for (int i = 0; i < args.length; i++) {
			String file = args[i];
			System.out.println("Parsing file: " + file);
			Parser p = new Parser(file);
			try {
				LinearProgram lp = p.parse();
				Solver solver = new Solver(lp);
				String result = solver.solve();
				System.out.print("non-negativ: ");
				for (boolean nonNeg : lp.getNonNegativity())
					System.out.print(nonNeg + " ");
				System.out.println();
				System.out.println(result);
			} catch (UnsupportedOperationException e) {
				System.out.println("\t" + e.getMessage());
				e.printStackTrace(System.out);
			} catch (IllegalArgumentException e) {
				System.out.println("\t" + e.getMessage());
				e.printStackTrace(System.out);
			} catch (IllegalStateException e) {
				System.out.println("\t" + e.getMessage());
			} catch (FileNotFoundException e) {
				System.out.println("\tfile not found");
			} catch (IOException e) {
				System.out.println("\tio-exception: " + e.getMessage());
			}
		}
	}
}
