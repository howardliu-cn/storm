/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.storm.sql.runtime.serde.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.storm.sql.runtime.IOutputSerializer;

public class JsonSerializer implements IOutputSerializer, Serializable {
    private static final long serialVersionUID = -2471613552079329240L;
    private final List<String> fieldNames;
    private final JsonFactory jsonFactory;

    public JsonSerializer(List<String> fieldNames) {
        this.fieldNames = fieldNames;
        jsonFactory = new JsonFactory();
    }

    @Override
    public ByteBuffer write(List<Object> data, ByteBuffer buffer) {
        Preconditions.checkArgument(data != null && data.size() == fieldNames.size(), "Invalid schema");
        StringWriter sw = new StringWriter();
        try (JsonGenerator jg = jsonFactory.createGenerator(sw)) {
            jg.writeStartObject();
            for (int i = 0; i < fieldNames.size(); ++i) {
                jg.writeFieldName(fieldNames.get(i));
                jg.writeObject(data.get(i));
            }
            jg.writeEndObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ByteBuffer.wrap(sw.toString().getBytes(StandardCharsets.UTF_8));
    }
}
