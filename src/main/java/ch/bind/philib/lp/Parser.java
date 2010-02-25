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
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Pattern;

/**
 * 
 * @author Philipp Meinen
 */
public final class Parser {

    private final String file;

    public Parser(String file) {
        this.file = file;
    }

    public LinearProgram parse() throws FileNotFoundException, IOException {
        LineNumberReader reader = new LineNumberReader(new FileReader(file));
        LinearProgram lp = null;
        int lineNum = 1;
        try {
            String line = reader.readLine();
            while (line != null) {
                switch (lineNum) {
                case 1:
                    lp = parseNumVars(line);
                    break;
                case 2:
                    parseProblemTypeAndTargetFunction(line, lp);
                    break;
                case 3:
                    parseNonNegativityLine(line, lp);
                    break;
                default:
                    parseSideCondition(lineNum, line, lp);
                    break;
                }
                lineNum++;
                line = reader.readLine();
            }
            if (lineNum != lp.getNumSideConditions() + 4)
                throwException(lp.getNumSideConditions() + 4, "invalid number of side-conditions");
            return lp;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("error while closing a read-only file, wtf?");
                }
            }
        }
    }

    private void parseSideCondition(int lineNum, String line, LinearProgram lp) {
        String[] parts = line.split(Pattern.quote(";"));
        if (parts.length < lp.getNumVars())
            throwException(lineNum, "less then " + lp.getNumVars() + " arguments");

        int sideConditionNum = lineNum - 4;
        int numArgs = lp.getNumVars() + 2;
        if (parts.length < numArgs)
            throwException(lineNum, "less then " + numArgs + " arguments");

        String type = parts[0].toLowerCase().trim();
        if ("<=".equals(type)) {
            lp.setSideConditionType(sideConditionNum, SideConditionType.SMALLER_EQUAL);
        } else if ("=".equals(type)) {
            lp.setSideConditionType(sideConditionNum, SideConditionType.EQUAL);
        } else if (">=".equals(type)) {
            lp.setSideConditionType(sideConditionNum, SideConditionType.GREATER_EQUAL);
        } else {
            throwException(lineNum, "invalid side-condition type: " + type);
        }

        double[] coeffs = new double[lp.getNumVars() + 1];
        for (int i = 0; i <= lp.getNumVars(); i++) {
            String arg = parts[i + 1].trim();
            try {
                coeffs[i] = Double.parseDouble(arg);
            } catch (NumberFormatException exc) {
                throwException(lineNum, "illegal side-condition coefficient: " + arg);
            }
        }
        lp.setSideCondition(sideConditionNum, coeffs);
    }

    private void parseNonNegativityLine(String line, LinearProgram lp) {
        String[] parts = line.split(Pattern.quote(";"));
        if (parts.length < lp.getNumVars())
            throwException(3, "less then " + lp.getNumVars() + " arguments");

        for (int i = 0; i < lp.getNumVars(); i++) {
            String arg = parts[i].toLowerCase().trim();
            if ("true".equals(arg)) {
                lp.setNonNegativity(i, true);
            } else if ("false".equals(arg)) {
                lp.setNonNegativity(i, false);
            } else {
                throwException(3, "illegal argument: " + arg + ", expected <true|false>");
            }
        }
    }

    private void parseProblemTypeAndTargetFunction(String line, LinearProgram lp) {
        String[] parts = line.split(Pattern.quote(";"));
        int numArgs = lp.getNumVars() + 2;
        if (parts.length < numArgs)
            throwException(2, "less then " + numArgs + " arguments");

        String type = parts[0].toLowerCase().trim();
        if ("min".equals(type)) {
            lp.setProblemType(ProblemType.MINIMIZE);
        } else if ("max".equals(type)) {
            lp.setProblemType(ProblemType.MAXIMIZE);
        } else {
            throwException(2, "unknown problem type: " + type);
        }

        double[] targetFunction = new double[lp.getNumVars() + 1];
        for (int var = 0; var <= lp.getNumVars(); var++) {
            String arg = parts[var + 1].trim();
            try {
                targetFunction[var] = Double.parseDouble(arg);
            } catch (NumberFormatException exc) {
                throwException(2, "illegal target-function number: " + arg);
            }
        }
        lp.setTargetFunction(targetFunction);
    }

    private LinearProgram parseNumVars(String line) {
        String[] parts = line.split(Pattern.quote(";"));
        if (parts.length < 2) {
            throwException(1, "less then two variables");
        }
        int numVars = 0;
        int numSideConditions = 0;
        try {
            numVars = Integer.parseInt(parts[0]);
            numSideConditions = Integer.parseInt(parts[1]);
        } catch (NumberFormatException exc) {
            throwException(1, "illegal number format");
        }

        if (numVars < 1 || numSideConditions < 1)
            throwException(1, "not enough variables or side-conditions");

        return new LinearProgram(numVars, numSideConditions);
    }

    private void throwException(int line, String msg) {
        throw new IllegalStateException("error on line " + line + ": " + msg);
    }
}
