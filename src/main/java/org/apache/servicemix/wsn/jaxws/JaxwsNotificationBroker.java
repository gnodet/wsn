package org.apache.servicemix.wsn.jaxws;

import javax.jms.ConnectionFactory;
import javax.jws.WebService;

import org.apache.servicemix.wsn.jms.JmsNotificationBroker;
import org.apache.servicemix.wsn.jms.JmsPublisher;
import org.apache.servicemix.wsn.jms.JmsSubscription;

@WebService(endpointInterface = "org.oasis_open.docs.wsn.brw_2.NotificationBroker")
public class JaxwsNotificationBroker extends JmsNotificationBroker {

    public JaxwsNotificationBroker(String name) {
        super(name);
    }

    public JaxwsNotificationBroker(String name, ConnectionFactory connectionFactory) {
        super(name, connectionFactory);
    }

    @Override
    protected JmsSubscription createJmsSubscription(String name) {
        return new JaxwsSubscription(name);
    }

    @Override
    protected JmsPublisher createJmsPublisher(String name) {
        JaxwsPublisher publisher = new JaxwsPublisher(name);
        publisher.setNotificationBrokerAddress(address);
        return publisher;
    }
}
