/*
 * Copyright (c) 2006-2022 Philipp Meinen <philipp@bind.ch>
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
