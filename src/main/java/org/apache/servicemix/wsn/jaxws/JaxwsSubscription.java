package org.apache.servicemix.wsn.jaxws;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.servicemix.wsn.jms.JmsSubscription;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeCreationFailedFaultType;
import org.oasis_open.docs.wsn.bw_2.InvalidFilterFault;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidProducerPropertiesExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableInitialTerminationTimeFault;
import org.oasis_open.docs.wsn.bw_2.UnrecognizedPolicyRequestFault;
import org.oasis_open.docs.wsn.bw_2.UnsupportedPolicyRequestFault;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: 10/4/11
 * Time: 8:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class JaxwsSubscription extends JmsSubscription {

    private NotificationConsumer consumer;

    public JaxwsSubscription(String name) {
        super(name);
    }

    @Override
    protected void validateSubscription(Subscribe subscribeRequest) throws InvalidFilterFault,
            InvalidMessageContentExpressionFault, InvalidProducerPropertiesExpressionFault,
            InvalidTopicExpressionFault, SubscribeCreationFailedFault, TopicExpressionDialectUnknownFault,
            TopicNotSupportedFault, UnacceptableInitialTerminationTimeFault,
            UnsupportedPolicyRequestFault, UnrecognizedPolicyRequestFault {
        super.validateSubscription(subscribeRequest);
        try {
            Service service = Service.create(
                    getClass().getClassLoader().getResource("/org/apache/servicemix/wsn/bw-2.wsdl"),
                    new QName("http://docs.oasis-open.org/wsn/bw-2", "NotificationConsumer")
            );
            consumer = service.getPort(subscribeRequest.getConsumerReference(), NotificationConsumer.class);
        } catch (Exception e) {
            SubscribeCreationFailedFaultType fault = new SubscribeCreationFailedFaultType();
            throw new SubscribeCreationFailedFault("Unable to resolve consumer reference endpoint", fault, e);
        }
    }

    @Override
    protected void doNotify(Notify notify) {
        // TODO: reimplement UseRaw
        consumer.notify(notify);
    }
}
