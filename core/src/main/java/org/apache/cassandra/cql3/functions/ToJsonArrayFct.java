/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cassandra.cql3.functions;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.utils.ByteBufferUtil;

/**
 * CQL toJsonArray function. 
 * @author vroyer
 *
 */
public class ToJsonArrayFct extends NativeScalarFunction
{
    public static final FunctionName NAME = FunctionName.nativeFunction("tojsonarray");

    private static final Map<List<AbstractType<?>>, ToJsonArrayFct> instances = new ConcurrentHashMap<>();

    public static ToJsonArrayFct getInstance(List<AbstractType<?>> argTypes) throws InvalidRequestException
    {
        ToJsonArrayFct func = instances.get(argTypes);
        if (func == null)
        {
            func = new ToJsonArrayFct(argTypes);
            instances.put(argTypes, func);
        }
        return func;
    }

    private ToJsonArrayFct(List<AbstractType<?>> argTypes)
    {
        super("tojsonarray", UTF8Type.instance, argTypes.toArray(new AbstractType<?>[argTypes.size()]));
    }

    public ByteBuffer execute(int protocolVersion, List<ByteBuffer> parameters) throws InvalidRequestException
    {
        assert parameters.size() >= 1 : "Expected at least 1 argument for toJsonArray(), but got " + parameters.size();
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int i=0;
        for(ByteBuffer bb : parameters) {
            if (sb.length() > 1) 
                sb.append(',');
            sb.append(argTypes.get(i).toJSONString(bb, protocolVersion));
            i++;
        }
        sb.append(']');
        return ByteBufferUtil.bytes(sb.toString());
    }
}
