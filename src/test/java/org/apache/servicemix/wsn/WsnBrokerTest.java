package org.apache.servicemix.wsn;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.servicemix.wsn.jaxws.JaxwsEndpointManager;
import org.apache.servicemix.wsn.jaxws.JaxwsNotificationBroker;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.ObjectFactory;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.brw_2.NotificationBroker;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: 10/4/11
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class WsnBrokerTest extends TestCase {

    @Override
    public void setUp() throws Exception {
        System.setProperty("java.util.logging.config.file", getClass().getClassLoader().getResource("logging.properties").getPath());
        java.util.logging.LogManager.getLogManager().readConfiguration();
    }

    public void testBroker() throws Exception {
        ActiveMQConnectionFactory activemq = new ActiveMQConnectionFactory("vm:(broker:(tcp://localhost:6000)?persistent=false)");
        JaxwsNotificationBroker wsnBroker = new JaxwsNotificationBroker("WSNotificationBroker", activemq);
        wsnBroker.setManager(new JaxwsEndpointManager());
        wsnBroker.setAddress("http://0.0.0.0:8181/wsn/NotificationBroker");
        wsnBroker.init();

        Service brokerClient = Service.create(
                new URL("http://0.0.0.0:8181/wsn/NotificationBroker?wsdl"),
                new QName("http://jaxws.wsn.servicemix.apache.org/", "JaxwsNotificationBrokerService"));
        NotificationBroker broker = brokerClient.getPort(NotificationBroker.class);

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
