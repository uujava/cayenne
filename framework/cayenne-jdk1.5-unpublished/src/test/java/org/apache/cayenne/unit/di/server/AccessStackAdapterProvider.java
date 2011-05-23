/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.apache.cayenne.unit.di.server;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.ConfigurationException;
import org.apache.cayenne.conn.DataSourceInfo;
import org.apache.cayenne.dba.DbAdapter;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;
import org.apache.cayenne.unit.AccessStackAdapter;
import org.apache.cayenne.util.Util;

public class AccessStackAdapterProvider implements Provider<AccessStackAdapter> {

    static final String TEST_ADAPTERS_MAP = "org.apache.cayenne.unit.di.server.CayenneResourcesAccessStackAdapterProvider.adapters";

    private DbAdapter adapter;
    private DataSourceInfo dataSourceInfo;
    private Map<String, String> adapterTypesMap;

    public AccessStackAdapterProvider(
            @Inject(TEST_ADAPTERS_MAP) Map<String, String> adapterTypesMap,
            @Inject DataSourceInfo dataSourceInfo, @Inject DbAdapter adapter) {
        this.dataSourceInfo = dataSourceInfo;
        this.adapterTypesMap = adapterTypesMap;
        this.adapter = adapter;
    }

    public AccessStackAdapter get() throws ConfigurationException {

        String testAdapterType = adapterTypesMap
                .get(dataSourceInfo.getAdapterClassName());
        if (testAdapterType == null) {
            throw new IllegalStateException("Unmapped adapter type: "
                    + dataSourceInfo.getAdapterClassName());
        }

        Class<AccessStackAdapter> type;
        try {
            type = (Class<AccessStackAdapter>) Util.getJavaClass(testAdapterType);
        }
        catch (ClassNotFoundException e) {
            throw new CayenneRuntimeException(
                    "Invalid class %s of type AccessStackAdapter",
                    e,
                    testAdapterType);
        }

        if (!AccessStackAdapter.class.isAssignableFrom(type)) {
            throw new CayenneRuntimeException(
                    "Class %s is not assignable to AccessStackAdapter",
                    testAdapterType);
        }

        try {
            Constructor<AccessStackAdapter> c = type.getConstructor(DbAdapter.class);
            return c.newInstance(adapter);
        }
        catch (Exception e) {
            throw new ConfigurationException("Error instantiating " + testAdapterType, e);
        }
    }
}
