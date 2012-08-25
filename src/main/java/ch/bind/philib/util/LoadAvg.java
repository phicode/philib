package ch.bind.philib.util;

public interface LoadAvg {

	void start();

	void end();

	long getLoadAvg();

	double getLoadAvgAsFactor();

}
