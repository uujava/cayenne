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
package org.apache.cayenne.configuration;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.cayenne.ConfigurationException;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.resource.URLResource;

public class XMLDataChannelDescriptorLoaderTest extends TestCase {

    public void testLoadEmpty() {

        // create dependencies

        Module testModule = new Module() {

            public void configure(Binder binder) {
                binder.bind(DataMapLoader.class).to(XMLDataMapLoader.class);
            }
        };

        Injector injector = DIBootstrap.createInjector(testModule);

        // create and initialize loader instance to test
        XMLDataChannelDescriptorLoader loader = new XMLDataChannelDescriptorLoader();
        injector.injectMembers(loader);

        String testConfigName = "testConfig1";
        String baseUrl = getClass().getPackage().getName().replace('.', '/');
        URL url = getClass().getClassLoader().getResource(
                baseUrl + "/cayenne-" + testConfigName + ".xml");
        DataChannelDescriptor descriptor = loader.load(new URLResource(url));

        assertNotNull(descriptor);
        assertNull(descriptor.getName());
    }

    public void testLoad_MissingConfig() throws Exception {

        // create dependencies

        Module testModule = new Module() {

            public void configure(Binder binder) {
                binder.bind(DataMapLoader.class).to(XMLDataMapLoader.class);
            }
        };

        Injector injector = DIBootstrap.createInjector(testModule);

        // create and initialize loader instance to test
        XMLDataChannelDescriptorLoader loader = new XMLDataChannelDescriptorLoader();
        injector.injectMembers(loader);

        try {
            loader.load(new URLResource(new URL("file:///no_such_resource")));
            fail("No exception was thrown on bad absent config name");
        }
        catch (ConfigurationException e) {
            // expected
        }
    }

    public void testLoadDataMap() {

        // create dependencies

        Module testModule = new Module() {

            public void configure(Binder binder) {
                binder.bind(DataMapLoader.class).to(XMLDataMapLoader.class);
            }
        };

        Injector injector = DIBootstrap.createInjector(testModule);

        // create and initialize loader instance to test
        XMLDataChannelDescriptorLoader loader = new XMLDataChannelDescriptorLoader();
        injector.injectMembers(loader);

        String testConfigName = "testConfig2";
        String baseUrl = getClass().getPackage().getName().replace('.', '/');
        URL url = getClass().getClassLoader().getResource(
                baseUrl + "/cayenne-" + testConfigName + ".xml");

        DataChannelDescriptor descriptor = loader.load(new URLResource(url));

        assertNotNull(descriptor);

        assertNull(descriptor.getName());

        Collection<DataMap> maps = descriptor.getDataMaps();
        assertEquals(1, maps.size());
        assertEquals("testConfigMap2", maps.iterator().next().getName());
    }

    public void testLoadDataEverything() {

        // create dependencies

        Module testModule = new Module() {

            public void configure(Binder binder) {
                binder.bind(DataMapLoader.class).to(XMLDataMapLoader.class);
            }
        };

        Injector injector = DIBootstrap.createInjector(testModule);

        // create and initialize loader instance to test
        XMLDataChannelDescriptorLoader loader = new XMLDataChannelDescriptorLoader();
        injector.injectMembers(loader);

        String testConfigName = "testConfig3";
        String baseUrl = getClass().getPackage().getName().replace('.', '/');
        URL url = getClass().getClassLoader().getResource(
                baseUrl + "/cayenne-" + testConfigName + ".xml");

        DataChannelDescriptor descriptor = loader.load(new URLResource(url));

        assertNotNull(descriptor);

        assertNull(descriptor.getName());

        Collection<DataMap> maps = descriptor.getDataMaps();
        assertEquals(2, maps.size());

        Iterator<DataMap> mapsIt = maps.iterator();

        DataMap map1 = mapsIt.next();
        DataMap map2 = mapsIt.next();

        assertEquals("testConfigMap3_1", map1.getName());
        assertEquals("testConfigMap3_2", map2.getName());

        Collection<DataNodeDescriptor> nodes = descriptor.getDataNodeDescriptors();
        assertEquals(1, nodes.size());

        DataNodeDescriptor node1 = nodes.iterator().next();
        assertEquals("testConfigNode3", node1.getName());
        assertEquals("testConfigNode3.driver.xml", node1.getLocation());
        assertNotNull(node1.getConfigurationResource());
        assertEquals(descriptor.getConfigurationResource().getRelativeResource(
                "testConfigNode3.driver.xml").getURL(), node1
                .getConfigurationResource()
                .getURL());

        assertEquals("org.example.test.Adapter", node1.getAdapterType());
        assertEquals("org.example.test.DataSourceFactory", node1
                .getDataSourceFactoryType());
        assertEquals("org.example.test.SchemaUpdateStartegy", node1
                .getSchemaUpdateStrategyType());
        assertNotNull(node1.getDataMapNames());

        assertEquals(1, node1.getDataMapNames().size());

        assertEquals("testConfigMap3_2", node1.getDataMapNames().iterator().next());
    }
}