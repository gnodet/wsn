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
package org.apache.servicemix.wsn.client;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.apache.servicemix.common.util.DOMUtil;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class AbstractWSAClient {

    public static W3CEndpointReference createWSA(String address) {
        Source src = new StringSource("<EndpointReference xmlns='http://www.w3.org/2005/08/addressing'><Address>"
                + address + "</Address></EndpointReference>");
        return new W3CEndpointReference(src);
    }

    public static String getWSAAddress(W3CEndpointReference ref) {
        try {
            Element element = DOMUtil.newDocument().createElement("elem");
            ref.writeTo(new DOMResult(element));
            NodeList nl = element.getElementsByTagNameNS("http://www.w3.org/2005/08/addressing", "Address");
            if (nl != null && nl.getLength() > 0) {
                Element e = (Element) nl.item(0);
                return DOMUtil.getElementText(e).trim();
            }
        } catch (ParserConfigurationException e) {
            // Ignore
        }
        return null;
    }


}
