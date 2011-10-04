package org.apache.servicemix.wsn.jaxws;

import javax.xml.ws.Endpoint;

import org.apache.servicemix.wsn.EndpointManager;
import org.apache.servicemix.wsn.EndpointRegistrationException;

public class JaxwsEndpointManager implements EndpointManager {

    public Object register(String address, Object service) throws EndpointRegistrationException {
        Endpoint endpoint = Endpoint.create(service);
        endpoint.publish(address);
        return endpoint;
    }

    public void unregister(Object endpoint) throws EndpointRegistrationException {
        ((Endpoint) endpoint).stop();
    }

}
