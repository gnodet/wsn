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
package org.apache.servicemix.wsn.jaxws;

import javax.jws.WebService;

import org.apache.servicemix.wsn.jms.JmsPublisher;
import org.apache.servicemix.wsn.util.WSNHelper;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.bw_2.PausableSubscriptionManager;

@WebService(endpointInterface = "org.oasis_open.docs.wsn.brw_2.PublisherRegistrationManager")
public class JaxwsPublisher extends JmsPublisher {

    public JaxwsPublisher(String name) {
        super(name);
    }

    @Override
    protected Object startSubscription() {
        return WSNHelper.getPort(publisherReference, PausableSubscriptionManager.class);
    }

    @Override
    protected void destroySubscription(Object sub) {
        try {
            ((PausableSubscriptionManager) sub).unsubscribe(new Unsubscribe());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
