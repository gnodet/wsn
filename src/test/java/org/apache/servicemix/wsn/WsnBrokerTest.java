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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.servicemix.wsn.client.Consumer;
import org.apache.servicemix.wsn.client.CreatePullPoint;
import org.apache.servicemix.wsn.client.NotificationBroker;
import org.apache.servicemix.wsn.client.Publisher;
import org.apache.servicemix.wsn.client.PullPoint;
import org.apache.servicemix.wsn.client.Registration;
import org.apache.servicemix.wsn.client.Subscription;
import org.apache.servicemix.wsn.jaxws.JaxwsCreatePullPoint;
import org.apache.servicemix.wsn.jaxws.JaxwsNotificationBroker;
import org.apache.servicemix.wsn.util.WSNHelper;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

public class WsnBrokerTest extends TestCase {

    private ActiveMQConnectionFactory activemq;
    private JaxwsNotificationBroker notificationBrokerServer;
    private JaxwsCreatePullPoint createPullPointServer;
    private NotificationBroker notificationBroker;
    private CreatePullPoint createPullPoint;

    @Override
    public void setUp() throws Exception {
        System.setProperty("java.util.logging.config.file", getClass().getClassLoader().getResource("logging.properties").getPath());
        java.util.logging.LogManager.getLogManager().readConfiguration();

        activemq = new ActiveMQConnectionFactory("vm:(broker:(tcp://localhost:6000)?persistent=false)");

        notificationBrokerServer = new JaxwsNotificationBroker("WSNotificationBroker", activemq);
        notificationBrokerServer.setAddress("http://0.0.0.0:8181/wsn/NotificationBroker");
        notificationBrokerServer.init();

        createPullPointServer = new JaxwsCreatePullPoint("CreatePullPoint", activemq);
        createPullPointServer.setAddress("http://0.0.0.0:8181/wsn/CreatePullPoint");
        createPullPointServer.init();

        notificationBroker = new NotificationBroker("http://0.0.0.0:8181/wsn/NotificationBroker");
        createPullPoint = new CreatePullPoint("http://0.0.0.0:8181/wsn/CreatePullPoint");
    }

    @Override
    public void tearDown() throws Exception {
        notificationBrokerServer.destroy();
        createPullPointServer.destroy();
    }

    public void testBroker() throws Exception {
        TestConsumer callback = new TestConsumer();
        Consumer consumer = new Consumer(callback, "http://0.0.0.0:8182/test/consumer");

        Subscription subscription = notificationBroker.subscribe(consumer, "myTopic");

        synchronized (callback.notifications) {
            notificationBroker.notify("myTopic", new JAXBElement(new QName("urn:test:org", "foo"), String.class, "bar"));
            callback.notifications.wait(1000000);
        }
        assertEquals(1, callback.notifications.size());
        NotificationMessageHolderType message = callback.notifications.get(0);
        assertEquals(WSNHelper.getWSAAddress(subscription.getEpr()), WSNHelper.getWSAAddress(message.getSubscriptionReference()));

        subscription.unsubscribe();
        consumer.stop();
    }

    public void testPullPoint() throws Exception {
        PullPoint pullPoint = createPullPoint.create();
        Subscription subscription = notificationBroker.subscribe(pullPoint, "myTopic");
        notificationBroker.notify("myTopic", new JAXBElement(new QName("urn:test:org", "foo"), String.class, "bar"));

        boolean received = false;
        for (int i = 0; i < 50; i++) {
            List<NotificationMessageHolderType> messages = pullPoint.getMessages(10);
            if (!messages.isEmpty()) {
                received = true;
                 break;
            }
            Thread.sleep(100);
        }
        assertTrue(received);

        subscription.unsubscribe();
        pullPoint.destroy();
    }

    public void testPublisher() throws Exception {
        TestConsumer consumerCallback = new TestConsumer();
        Consumer consumer = new Consumer(consumerCallback, "http://0.0.0.0:8182/test/consumer");

        Subscription subscription = notificationBroker.subscribe(consumer, "myTopic");

        PublisherCallback publisherCallback = new PublisherCallback();
        Publisher publisher = new Publisher(publisherCallback, "http://0.0.0.0:8182/test/publisher");
        Registration registration = notificationBroker.registerPublisher(publisher, "myTopic");

        synchronized (consumerCallback.notifications) {
            notificationBroker.notify(publisher, "myTopic", new JAXBElement(new QName("urn:test:org", "foo"), String.class, "bar"));
            consumerCallback.notifications.wait(1000000);
        }
        assertEquals(1, consumerCallback.notifications.size());
        NotificationMessageHolderType message = consumerCallback.notifications.get(0);
        assertEquals(WSNHelper.getWSAAddress(subscription.getEpr()), WSNHelper.getWSAAddress(message.getSubscriptionReference()));
        assertEquals(WSNHelper.getWSAAddress(publisher.getEpr()), WSNHelper.getWSAAddress(message.getProducerReference()));

        registration.destroy();
        subscription.unsubscribe();
        publisher.stop();
        consumer.stop();
    }

    public void testPublisherOnDemand() throws Exception {
        TestConsumer consumerCallback = new TestConsumer();
        Consumer consumer = new Consumer(consumerCallback, "http://0.0.0.0:8182/test/consumer");

        PublisherCallback publisherCallback = new PublisherCallback();
        Publisher publisher = new Publisher(publisherCallback, "http://0.0.0.0:8182/test/publisher");
        Registration registration = notificationBroker.registerPublisher(publisher, Arrays.asList("myTopic1", "myTopic2"), true);

        Subscription subscription = notificationBroker.subscribe(consumer, "myTopic1");
        assertTrue(publisherCallback.subscribed.get());

        synchronized (consumerCallback.notifications) {
            notificationBroker.notify(publisher, "myTopic1", new JAXBElement(new QName("urn:test:org", "foo"), String.class, "bar"));
            consumerCallback.notifications.wait(1000000);
        }

        subscription.unsubscribe();
        registration.destroy();
        publisher.stop();
        consumer.stop();

        assertTrue(publisherCallback.unsubscribed.get());
    }

    public static class TestConsumer implements Consumer.Callback {

        public final List<NotificationMessageHolderType> notifications = new ArrayList<NotificationMessageHolderType>();

        public void notify(NotificationMessageHolderType message) {
            synchronized (notifications) {
                notifications.add(message);
                notifications.notify();
            }
        }
    }

    public static class PublisherCallback implements Publisher.Callback {
        public final AtomicBoolean subscribed = new AtomicBoolean(false);
        public final AtomicBoolean unsubscribed = new AtomicBoolean(false);

        public void subscribe(TopicExpressionType topic) {
            subscribed.set(true);
        }

        public void unsubscribe(TopicExpressionType topic) {
            unsubscribed.set(true);
        }
    }

}
