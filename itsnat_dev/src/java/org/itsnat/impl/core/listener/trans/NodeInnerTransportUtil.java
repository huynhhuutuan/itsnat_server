/*
  ItsNat Java Web Application Framework
  Copyright (C) 2007-2011 Jose Maria Arranz Santamaria, Spanish citizen

  This software is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 3 of
  the License, or (at your option) any later version.
  This software is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details. You should have received
  a copy of the GNU Lesser General Public License along with this program.
  If not, see <http://www.gnu.org/licenses/>.
*/

package org.itsnat.impl.core.listener.trans;

import org.itsnat.core.event.NodeInnerTransport;
import org.itsnat.core.event.ParamTransport;
import org.itsnat.impl.core.doc.ItsNatDocumentImpl;
import org.itsnat.impl.core.domutil.DOMUtilInternal;
import org.itsnat.impl.core.event.client.ClientItsNatNormalEventImpl;
import org.itsnat.impl.core.req.norm.RequestNormalEventImpl;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 *
 * @author jmarranz
 */
public class NodeInnerTransportUtil extends SingleParamTransportUtil
{
    public static final NodeInnerTransportUtil SINGLETON = new NodeInnerTransportUtil();


    /**
     * Creates a new instance of NodeInnerTransportUtil
     */
    public NodeInnerTransportUtil()
    {
    }

    public static String getName()
    {
        return "itsnat_node_inner";
    }

    public String getCodeToSend(ParamTransport param)
    {
        NodeInnerTransport item = (NodeInnerTransport)param;
        String name = item.getName();
        return "  event.getUtil().transpNodeInner(event,\"" + name + "\");\n";
    }

    public void syncServerBeforeDispatch(ParamTransport param,RequestNormalEventImpl request,ClientItsNatNormalEventImpl event)
    {
        syncWithServer(request,event);
    }

    public void syncServerAfterDispatch(ParamTransport param, RequestNormalEventImpl request,ClientItsNatNormalEventImpl event)
    {
    }

    public static void syncWithServer(RequestNormalEventImpl request,ClientItsNatNormalEventImpl event)
    {
        Element elem = (Element)event.getCurrentTarget();
        String value = request.getAttrOrParam(getName());
        DOMUtilInternal.removeAllChildren(elem);
        if (value != null)
        {
            // http://webfx.eae.net/dhtml/ieemu/htmlmodel.html
            ItsNatDocumentImpl itsNatDoc = event.getItsNatStfulDocument();
            DocumentFragment newNodeFragment = itsNatDoc.getItsNatDocumentTemplateVersion().parseFragmentToDocFragment(value,itsNatDoc);
            elem.appendChild(newNodeFragment);
        }
        // Si es null es que est� vac�o en el cliente por lo que el elemento queda vac�o
    }
}
