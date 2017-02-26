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
 * CQL toString function.
 * @author vroyer
 *
 */
public class ToStringFct extends NativeScalarFunction
{
    public static final FunctionName NAME = FunctionName.nativeFunction("tostring");

    private static final Map<AbstractType<?>, ToStringFct> instances = new ConcurrentHashMap<>();

    public static ToStringFct getInstance(List<AbstractType<?>> argTypes) throws InvalidRequestException
    {
        assert argTypes.size() == 1 : "Expected 1 argument for toString(), but got " + argTypes.size();
        AbstractType<?> argType = argTypes.get(0);
        ToStringFct func = instances.get(argType);
        if (func == null)
        {
            func = new ToStringFct(argType);
            instances.put(argType, func);
        }
        return func;
    }

    private ToStringFct(AbstractType<?> argType)
    {
        super("tostring", UTF8Type.instance, argType);
    }

    public ByteBuffer execute(int protocolVersion, List<ByteBuffer> parameters) throws InvalidRequestException
    {
        ByteBuffer bb = parameters.get(0);
        if (bb == null)
            return ByteBufferUtil.bytes("null");
        
        return ByteBufferUtil.bytes(argTypes.get(0).getSerializer().deserialize(bb).toString());
    }
}
