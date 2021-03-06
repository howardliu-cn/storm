/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.storm.mongodb.bolt;

import org.apache.commons.lang.Validate;
import org.apache.storm.mongodb.common.QueryFilterCreator;
import org.apache.storm.mongodb.common.mapper.MongoUpdateMapper;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.TupleUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Basic bolt for updating from MongoDB.
 * Note: Each MongoUpdateBolt defined in a topology is tied to a specific collection.
 */
public class MongoUpdateBolt extends AbstractMongoBolt {
    private static final long serialVersionUID = -5520612633851250677L;
    private QueryFilterCreator queryCreator;
    private MongoUpdateMapper mapper;

    private boolean upsert;  //the default is false.
    private boolean many;  //the default is false.

    /**
     * MongoUpdateBolt Constructor.
     * @param url The MongoDB server url
     * @param collectionName The collection where reading/writing data
     * @param queryCreator QueryFilterCreator
     * @param mapper MongoMapper converting tuple to an MongoDB document
     */
    public MongoUpdateBolt(String url, String collectionName, QueryFilterCreator queryCreator, MongoUpdateMapper mapper) {
        super(url, collectionName);

        Validate.notNull(queryCreator, "QueryFilterCreator can not be null");
        Validate.notNull(mapper, "MongoUpdateMapper can not be null");

        this.queryCreator = queryCreator;
        this.mapper = mapper;
    }

    @Override
    public void execute(Tuple tuple) {
        if (TupleUtils.isTick(tuple)) {
            return;
        }

        try {
            //get document
            Document doc = mapper.toDocument(tuple);
            //get query filter
            Bson filter = queryCreator.createFilter(tuple);
            mongoClient.update(filter, doc, upsert, many);
            this.collector.ack(tuple);
        } catch (Exception e) {
            this.collector.reportError(e);
            this.collector.fail(tuple);
        }
    }

    public MongoUpdateBolt withUpsert(boolean upsert) {
        this.upsert = upsert;
        return this;
    }

    public MongoUpdateBolt withMany(boolean many) {
        this.many = many;
        return this;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        
    }

}
