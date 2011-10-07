/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cxf.wsn.jaxws;

import javax.xml.ws.Endpoint;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.apache.cxf.wsn.EndpointManager;
import org.apache.cxf.wsn.EndpointRegistrationException;

public class JaxwsEndpointManager implements EndpointManager {

    public Object register(String address, Object service) throws EndpointRegistrationException {
        Endpoint endpoint = Endpoint.create(service);
        endpoint.publish(address);
        return endpoint;
    }

    public void unregister(Object endpoint) throws EndpointRegistrationException {
        ((Endpoint) endpoint).stop();
    }

    public W3CEndpointReference getEpr(Object endpoint) {
        return ((Endpoint) endpoint).getEndpointReference(W3CEndpointReference.class);
    }
}
