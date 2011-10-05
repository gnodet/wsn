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
package org.apache.servicemix.wsn;

import java.util.ArrayList;
import java.util.List;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.servicemix.wsn.jaxws.JaxwsNotificationBroker;
import org.apache.servicemix.wsn.util.WSNHelper;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.brw_2.NotificationBroker;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.oasis_open.docs.wsn.bw_2.PausableSubscriptionManager;

public class WsnBrokerTest extends TestCase {

    @Override
    public void setUp() throws Exception {
        System.setProperty("java.util.logging.config.file", getClass().getClassLoader().getResource("logging.properties").getPath());
        java.util.logging.LogManager.getLogManager().readConfiguration();
    }

    public void testBroker() throws Exception {
        ActiveMQConnectionFactory activemq = new ActiveMQConnectionFactory("vm:(broker:(tcp://localhost:6000)?persistent=false)");
        JaxwsNotificationBroker wsnBroker = new JaxwsNotificationBroker("WSNotificationBroker", activemq);
        wsnBroker.setAddress("http://0.0.0.0:8181/wsn/NotificationBroker");
        wsnBroker.init();

        NotificationBroker broker = WSNHelper.getPort("http://0.0.0.0:8181/wsn/NotificationBroker", NotificationBroker.class);

        TestConsumer consumer = new TestConsumer();
        Endpoint epConsumer = Endpoint.create(consumer);
        epConsumer.publish("http://0.0.0.0:8182/test/consumer");

        Subscribe subscribeRequest = new Subscribe();
        FilterType filter = new FilterType();
        TopicExpressionType topic = new TopicExpressionType();
        topic.setDialect("http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple");
        topic.getContent().add("myTopic");
        filter.getAny().add(new ObjectFactory().createTopicExpression(topic));
        subscribeRequest.setConsumerReference(
                new W3CEndpointReferenceBuilder().address("http://0.0.0.0:8182/test/consumer").build()
        );
        subscribeRequest.setFilter(filter);
        SubscribeResponse subscribeResponse = broker.subscribe(subscribeRequest);

        Notify notify = new Notify();
        NotificationMessageHolderType message = new NotificationMessageHolderType();
        NotificationMessageHolderType.Message msg = new NotificationMessageHolderType.Message();
        msg.setAny(subscribeResponse);
        message.setTopic(topic);
        message.setMessage(msg);
        notify.getNotificationMessage().add(message);



        synchronized (consumer.notifications) {
            broker.notify(notify);
            consumer.notifications.wait(1000000);
        }
        assertEquals(1, consumer.notifications.size());


        WSNHelper.getPort(subscribeResponse.getSubscriptionReference(), PausableSubscriptionManager.class).unsubscribe(new Unsubscribe());

    }

    @WebService(endpointInterface = "org.oasis_open.docs.wsn.bw_2.NotificationConsumer")
    public static class TestConsumer implements NotificationConsumer {

        public final List<Notify> notifications = new ArrayList<Notify>();

        public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Notify notify) {
            synchronized (notifications) {
                notifications.add(notify);
                notifications.notify();
            }
        }
    }

}
