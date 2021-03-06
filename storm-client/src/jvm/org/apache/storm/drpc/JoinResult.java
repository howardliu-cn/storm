/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.storm.drpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JoinResult extends BaseRichBolt {
    private static final long serialVersionUID = -1556674750669584127L;
    public static final Logger LOG = LoggerFactory.getLogger(JoinResult.class);

    private String returnComponent;
    private Map<Object, Tuple> returns = new HashMap<>();
    private Map<Object, Tuple> results = new HashMap<>();
    private OutputCollector collector;

    public JoinResult(String returnComponent) {
        this.returnComponent = returnComponent;
    }

    @Override
    public void prepare(Map<String, Object> map, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        Object requestId = tuple.getValue(0);
        if (tuple.getSourceComponent().equals(returnComponent)) {
            returns.put(requestId, tuple);
        } else {
            results.put(requestId, tuple);
        }

        if (returns.containsKey(requestId) && results.containsKey(requestId)) {
            Tuple result = results.remove(requestId);
            Tuple returner = returns.remove(requestId);
            LOG.debug(result.getValue(1).toString());
            List<Tuple> anchors = new ArrayList<>();
            anchors.add(result);
            anchors.add(returner);
            collector.emit(anchors, new Values("" + result.getValue(1), returner.getValue(1)));
            collector.ack(result);
            collector.ack(returner);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("result", "return-info"));
    }
}
