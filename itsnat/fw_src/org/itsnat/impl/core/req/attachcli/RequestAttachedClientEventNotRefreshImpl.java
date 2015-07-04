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

package org.itsnat.impl.core.req.attachcli;

import org.itsnat.impl.core.servlet.ItsNatServletRequestImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedClientImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedClientNotRefreshImpl;
import org.itsnat.impl.core.listener.attachcli.ItsNatAttachedClientNotRefreshEventListenerWrapperImpl;
import org.itsnat.impl.core.resp.attachcli.ResponseAttachedClientEventImpl;

/**
 *
 * @author jmarranz
 */
public class RequestAttachedClientEventNotRefreshImpl extends RequestAttachedClientEventImpl
{
    public RequestAttachedClientEventNotRefreshImpl(ItsNatServletRequestImpl itsNatRequest)
    {
        super(itsNatRequest);
    }

    public ResponseAttachedClientEventImpl createResponseAttachedClientEventUnload(ClientDocumentAttachedClientImpl clientDoc)
    {
        return createResponseAttachedClientEvent(clientDoc);
    }

    public ResponseAttachedClientEventImpl createResponseAttachedClientEventRefresh(ClientDocumentAttachedClientImpl clientDoc)
    {
        return createResponseAttachedClientEvent(clientDoc);
    }

    private ResponseAttachedClientEventImpl createResponseAttachedClientEvent(ClientDocumentAttachedClientImpl clientDoc)
    {
        ClientDocumentAttachedClientNotRefreshImpl attachedClientDoc = (ClientDocumentAttachedClientNotRefreshImpl)clientDoc;
        ItsNatAttachedClientNotRefreshEventListenerWrapperImpl listener = attachedClientDoc.getItsNatAttachedClientNotRefreshEventListenerWrapper();
        return new ResponseAttachedClientEventImpl(listener,this);
    }
}
