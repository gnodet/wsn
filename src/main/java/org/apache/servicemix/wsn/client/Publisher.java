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
package org.apache.servicemix.wsn.client;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.apache.servicemix.wsn.util.WSNHelper;
import org.oasis_open.docs.wsn.br_2.DestroyRegistration;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationManager;
import org.oasis_open.docs.wsn.brw_2.ResourceNotDestroyedFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

public class Publisher implements Referencable {

    private final PublisherRegistrationManager publisher;
    private final W3CEndpointReference epr;

    public Publisher(String address) {
        this(WSNHelper.createWSA(address));
    }

    public Publisher(W3CEndpointReference epr) {
        this.publisher = WSNHelper.getPort(epr, PublisherRegistrationManager.class);
        this.epr = epr;
    }

    public PublisherRegistrationManager getPublisher() {
        return publisher;
    }

    public W3CEndpointReference getEpr() {
        return epr;
    }

    public void destroyRegistration() throws ResourceUnknownFault, ResourceNotDestroyedFault {
        publisher.destroyRegistration(new DestroyRegistration());
    }

}
