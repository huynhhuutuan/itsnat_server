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

package org.itsnat.impl.core.event.client;

import org.itsnat.core.ItsNatException;
import org.itsnat.core.event.ItsNatNormalEvent;
import org.itsnat.impl.core.event.EventInternal;
import org.itsnat.impl.core.req.norm.RequestNormalEventImpl;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public class ClientItsNatNormalEventErrorImpl extends ClientItsNatEventStfulImpl implements ItsNatNormalEvent,EventInternal
{
    public ClientItsNatNormalEventErrorImpl(RequestNormalEventImpl request)
    {
        super(request); // No hay documento
    }

    public void checkInitializedEvent()
    {
        // Est� bien formado porque viene del cliente.
    }

    public RequestNormalEventImpl getRequestNormalEvent()
    {
        return (RequestNormalEventImpl)getSource();
    }

    public void setTarget(EventTarget target)
    {
        throw new ItsNatException("Not supported in this context");
    }

    public boolean getStopPropagation()
    {
        throw new ItsNatException("Not supported in this context");
    }

    public boolean getPreventDefault()
    {
        throw new ItsNatException("Not supported in this context");
    }

    public long getTimeStamp()
    {
        throw new ItsNatException("Not supported in this context");
    }

    public short getEventPhase()
    {
        throw new ItsNatException("Not supported in this context");
    }

    public void preventDefault()
    {
        throw new ItsNatException("Not supported in this context");
    }

    public void stopPropagation()
    {
        throw new ItsNatException("Not supported in this context");
    }

    public boolean getBubbles()
    {
        throw new ItsNatException("Not supported in this context");
    }

    public boolean getCancelable()
    {
        throw new ItsNatException("Not supported in this context");
    }

    public String getType()
    {
        throw new ItsNatException("Not supported in this context");
    }

    public void initEvent(String eventTypeArg, boolean canBubbleArg, boolean cancelableArg)
    {
        throw new ItsNatException("Not supported in this context");
    }

    public EventTarget getCurrentTarget()
    {
        throw new ItsNatException("Not supported in this context");
    }

    public EventTarget getTarget()
    {
        throw new ItsNatException("Not supported in this context");
    }

    public int getCommModeDeclared()
    {
        throw new ItsNatException("Not supported in this context");
    }

}
