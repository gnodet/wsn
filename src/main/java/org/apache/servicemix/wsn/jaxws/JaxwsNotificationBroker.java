package org.apache.servicemix.wsn.jaxws;

import javax.jms.ConnectionFactory;

import org.apache.servicemix.wsn.jms.JmsNotificationBroker;
import org.apache.servicemix.wsn.jms.JmsPublisher;
import org.apache.servicemix.wsn.jms.JmsSubscription;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: 10/4/11
 * Time: 8:56 AM
 * To change this template use File | Settings | File Templates.
 */
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
