/*
 * Copyright (c) 2013 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.msg;

import java.util.Map;

public interface PubSub {

	/**
	 * subscribe a {@link MessageHandler} to a channel.
	 * 
	 * @param channelName
	 * @param handler
	 *            a non-null handler
	 * @return an {@link Subscription} object if this is a new message handler, {@code null} otherwise.
	 * @throws IllegalArgumentException
	 *             if the {@code channelName} parameter is {@code null} or empty
	 *             or if the {@code handler} parameter is {@code null}.
	 */
	Subscription subscribe(String channelName, MessageHandler handler);

	/**
	 * Asynchronously published a message to all subscribers of a channel.
	 * 
	 * @param channelName
	 * @param message
	 *            the non-null message to be sent
	 * @throws IllegalArgumentException
	 *             if the {@code channelName} parameter is {@code null} or empty
	 *             or if the {@code message} parameter is {@code null}.
	 */
	void publish(String channelName, Object message);

	/**
	 * Lists all channels with one or more subscriptions.<br />
	 * The returned {@link Map} is a representation of the state at the time of
	 * the invocation of this method. New subscriptions or cancelled
	 * subscriptions which happen after the invocation of this method will not
	 * be reflected in maps of previous queries.
	 * 
	 * @return a {@link Map} with active channel names as key and the number of
	 *         subscribers as value.
	 */
	Map<String, Integer> activeChannels();
}
