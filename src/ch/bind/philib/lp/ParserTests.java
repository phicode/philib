package ch.bind.philib.lp;

import java.io.File;

public class ParserTests {

	public static void main(String[] args) {
		String folder = "/home/lance/fh/efalg/Uebungen/LP_problems";
		File dir = new File(folder);
		File[] files = dir.listFiles();
		String[] fileNames = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			fileNames[i] = files[i].getAbsolutePath();
		}
		Main.main(fileNames);
	}
}
