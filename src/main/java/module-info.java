module ch.bind.philib {
	//
	// EXPORTS
	//
	exports ch.bind.philib.cache;
	exports ch.bind.philib.concurrent;
	exports ch.bind.philib.conf;
	exports ch.bind.philib.io;
	exports ch.bind.philib.lang;
	exports ch.bind.philib.math;
	exports ch.bind.philib.net;

	exports ch.bind.philib.pool;
	exports ch.bind.philib.pool.buffer;
	exports ch.bind.philib.pool.manager;
	exports ch.bind.philib.pool.object;

	exports ch.bind.philib.test;
	exports ch.bind.philib.util;
	exports ch.bind.philib.validation;

	//
	// REQUIRES
	//
	requires org.slf4j;
}
