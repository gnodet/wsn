package org.apache.servicemix.wsn.jaxws;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.servicemix.wsn.jms.JmsPublisher;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.bw_2.PausableSubscriptionManager;

@WebService(endpointInterface = "org.oasis_open.docs.wsn.brw_2.PublisherRegistrationManager")
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
                getClass().getClassLoader().getResource("org/apache/servicemix/wsn/wsn.wsdl"),
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
