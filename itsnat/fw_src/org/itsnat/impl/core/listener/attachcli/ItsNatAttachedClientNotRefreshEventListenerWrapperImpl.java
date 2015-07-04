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

package org.itsnat.impl.core.listener.attachcli;

import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedClientNotRefreshImpl;
import org.itsnat.impl.core.event.client.ItsNatAttachedClientEventImpl;
import org.itsnat.impl.core.event.client.ItsNatAttachedClientEventNotRefreshImpl;
import org.itsnat.impl.core.req.attachcli.RequestAttachedClientEventImpl;

/**
 *
 * @author jmarranz
 */
public class ItsNatAttachedClientNotRefreshEventListenerWrapperImpl extends ItsNatAttachedClientEventListenerWrapperImpl
{
    /**
     * Creates a new instance of ItsNatAttachedClientTimerEventListenerWrapperImpl
     */
    public ItsNatAttachedClientNotRefreshEventListenerWrapperImpl(ClientDocumentAttachedClientNotRefreshImpl clientDoc)
    {
        super(clientDoc);
    }

    public ClientDocumentAttachedClientNotRefreshImpl getClientDocumentAttachedClientNotRefresh()
    {
        return (ClientDocumentAttachedClientNotRefreshImpl)clientDoc;
    }

    public ItsNatAttachedClientEventImpl createItsNatAttachedClientEvent(RequestAttachedClientEventImpl request)
    {
        return new ItsNatAttachedClientEventNotRefreshImpl(request);
    }

}
