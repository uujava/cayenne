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
package org.apache.cayenne.project2.validate;

import org.apache.cayenne.configuration.DataChannelDescriptor;
import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.project.ProjectPath;
import org.apache.cayenne.util.Util;

public class DataMapValidator implements Validator {

    public void validate(Object object, ConfigurationValidationVisitor configurationValidationVisitor) {
        DataMap map = (DataMap) object;
        ProjectPath path = new ProjectPath(new Object[]{(DataChannelDescriptor)configurationValidationVisitor.getProject().getRootNode(), map});
        validateName(map, path, configurationValidationVisitor);

        // check if data map is not attached to any nodes
        validateNodeLinks(map, path, configurationValidationVisitor);
    }
  
    protected void validateNodeLinks(DataMap map, ProjectPath path, ConfigurationValidationVisitor validator) {
        DataChannelDescriptor domain = (DataChannelDescriptor) validator.getProject().getRootNode();
        if (domain == null) {
            return;
        }
        
        boolean unlinked = true;
        int nodeCount = 0;
        for (final DataNodeDescriptor node : domain.getNodeDescriptors()) {
            nodeCount++;
            if (node.getDataMapNames().contains(map.getName())) {
                unlinked = false;
                break;
            }
        }
        
        if(unlinked && nodeCount > 0) {
             validator.registerWarning("DataMap is not linked to any DataNodes.", path);
        }
    }

    protected void validateName(DataMap map, ProjectPath path, ConfigurationValidationVisitor validator) {
        String name = map.getName();

        if (Util.isEmptyString(name)) {
            validator.registerError("Unnamed DataMap.", path);
            return;
        }

        DataChannelDescriptor domain = (DataChannelDescriptor) validator.getProject().getRootNode();
        if (domain == null) {
            return;
        }

        // check for duplicate names in the parent context
        for (final DataMap otherMap : domain.getDataMaps()) {
            if (otherMap == map) {
                continue;
            }

            if (name.equals(otherMap.getName())) {
                validator.registerError("Duplicate DataMap name: " + name + ".", path);
                return;
            }
        }
    }
    
}