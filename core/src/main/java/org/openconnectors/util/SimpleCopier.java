/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openconnectors.util;

import org.openconnectors.config.Config;
import org.openconnectors.connect.PushSourceConnector;
import org.openconnectors.connect.SinkConnector;
import org.openconnectors.connect.PullSourceConnector;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

/**
 * Copy Topology template targeting unit testing
 */
public class SimpleCopier<I, O> {

    private PullSourceConnector<I> pullSource;
    private SinkConnector<O> sink;
    private Function<I, O> transformer;

    public SimpleCopier(PullSourceConnector<I> source, SinkConnector<O> sink,
                        Function<I, O> transformer) {
        this.pullSource = source;
        this.sink = sink;
        this.transformer = transformer;
    }

    public SimpleCopier(PushSourceConnector<I> source, SinkConnector<O> sink,
                        Function<I, O> transformer) {
        this.pullSource = new PullWrapperOnPushSource<>(source, 1024 * 1024, PullWrapperOnPushSource.BufferStrategy.BLOCK);
        this.sink = sink;
        this.transformer = transformer;
    }

    public void setup(Config config) throws Exception {
        this.pullSource.open(config);
        this.sink.open(config);
    }

    public void run() throws Exception {
        while (true) {
            Collection<I> messages = pullSource.fetch();
            consume(messages);
        }
    }

    private void consume(Collection<I> messages) {
        if (messages != null) {
            Collection<O> output = new LinkedList<>();
            for (I message : messages) {
                output.add(transformer.apply(message));
            }
            sink.publish(output);
        }
    }

}
