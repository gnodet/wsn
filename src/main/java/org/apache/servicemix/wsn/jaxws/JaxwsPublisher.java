package org.apache.servicemix.wsn.jaxws;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.servicemix.wsn.jms.JmsPublisher;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.bw_2.PausableSubscriptionManager;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: 10/4/11
 * Time: 6:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class JaxwsPublisher extends JmsPublisher {

    private String notificationBrokerAddress;

    public JaxwsPublisher(String name) {
        super(name);
    }

    public String getNotificationBrokerAddress() {
        return notificationBrokerAddress;
    }

    public void setNotificationBrokerAddress(String notificationBrokerAddress) {
        this.notificationBrokerAddress = notificationBrokerAddress;
    }

    @Override
    protected Object startSubscription() {
        Service service = Service.create(
                getClass().getClassLoader().getResource("/org/apache/servicemix/wsn/bw-2.wsdl"),
                new QName("http://docs.oasis-open.org/wsn/bw-2", "PausableSubscriptionManager")
        );
        PausableSubscriptionManager manager = service.getPort(publisherReference, PausableSubscriptionManager.class);
        return manager;
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
