/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elassandra.cluster;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.ColumnDefinition.Kind;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.cql3.ColumnIdentifier;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.schema.KeyspaceMetadata;

public class ColumnDescriptor implements Comparable<ColumnDescriptor> {
    public String name;
    public CQL3Type.Raw type;
    public ColumnDefinition.Kind kind;
    public int position;
    public boolean desc;
    public boolean exists = false;

    public ColumnDescriptor(String name) {
        this(name, null);
    }

    public ColumnDescriptor(String name, CQL3Type.Raw type) {
        this(name, type, Kind.REGULAR, ColumnDefinition.NO_POSITION);
    }

    public ColumnDescriptor(String name, CQL3Type.Raw type, Kind kind, int position) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.position = position;
    }

    public void validate(KeyspaceMetadata ksm , CFMetaData cfm) throws ConfigurationException {
        ColumnDefinition cd = cfm.getColumnDefinition(new ColumnIdentifier(name, true));
        if (cd == null)
            return;
        exists = true;

        // do not enforce PK constraints if not specified explicitly in oder to keep backward compatibility.
        if (cd.kind != this.kind && this.kind != Kind.REGULAR)
            throw new ConfigurationException("Bad mapping, column ["+this.name+"] kind [" + this.kind + "] does not match the existing one type [" + cd.kind + "]");

        AbstractType<?> cql3Type = this.type.prepare(ksm).getType();

        if (!cd.type.isCollection()) {
            if (cql3Type.toString().equals("frozen<geo_point>")) {
                if (!(cql3Type.toString().equals("text") ||cql3Type.toString().equals("frozen<geo_point>"))) {
                    throw new ConfigurationException("geo_point cannot be mapped to column ["+this.name+"] with CQL type ["+this.type+"]. ");
                }
            } else {
                String inferedCql = cql3Type.asCQL3Type().toString();
                String existingCql3 = cd.type.asCQL3Type().toString();
                // cdef.type.asCQL3Type() does not include frozen, nor quote, so can do this check for collection.
                if (!existingCql3.equals(inferedCql) &&
                    !(existingCql3.endsWith("uuid") && inferedCql.equals("text")) && // #74 uuid is mapped as keyword
                    !(existingCql3.equals("timeuuid") && (inferedCql.equals("timestamp") || inferedCql.equals("text"))) &&
                    !(existingCql3.equals("date") && inferedCql.equals("timestamp")) &&
                    !(existingCql3.equals("time") && inferedCql.equals("bigint"))
                    ) // timeuuid can be mapped to date
                throw new ConfigurationException("Existing column [" + this.name + "] type [" +existingCql3 + "] mismatch with inferred type [" + inferedCql + "]");
            }
        }
        // TODO: Add collection type check
    }

    public ColumnDefinition createColumnDefinition(KeyspaceMetadata ksm, String cfName) {
        return new ColumnDefinition(ksm.name, cfName, ColumnIdentifier.getInterned(name, true), type.prepare(ksm).getType(), position, kind);
    }

    public boolean exists() {
        return this.exists;
    }

    @Override
    public String toString() {
        return this.kind + "[" + this.name + "/" + this.type + "/"+ this.position + ((kind==Kind.CLUSTERING) ? (desc ? "/DESC":"/ASC") : "") + "]";
    }

    @Override
    public int compareTo(ColumnDescriptor o) {
        if (this.position == -1)
            return (o.position == -1) ? 0 : 1;

        return (o.position == -1) ? -1 : this.position - o.position;
    }
}
