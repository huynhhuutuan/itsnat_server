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

package org.itsnat.impl.core.listener.dom.domstd;

import org.itsnat.core.CommMode;
import org.itsnat.core.ItsNatException;
import org.itsnat.core.event.ParamTransport;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.servlet.ItsNatServletResponseImpl;
import org.itsnat.impl.core.browser.web.BrowserWeb;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.droid.ClientDocumentStfulDelegateDroidImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.event.DOMStdEventGroupInfo;
import org.itsnat.impl.core.event.client.ClientItsNatNormalEventImpl;
import org.itsnat.impl.core.event.client.dom.domstd.ClientItsNatDOMStdEventFactory;
import org.itsnat.impl.core.event.client.dom.domstd.ClientItsNatDOMStdEventImpl;
import org.itsnat.impl.core.listener.ItsNatNormalEventListenerWrapperImpl;
import org.itsnat.impl.core.scriptren.jsren.event.domstd.JSRenderItsNatDOMStdEventImpl;
import org.itsnat.impl.core.req.norm.RequestNormalEventImpl;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public class ItsNatDOMStdEventListenerWrapperImpl extends ItsNatNormalEventListenerWrapperImpl
{
    protected int commMode;
    protected String type;
    protected boolean useCapture;
    protected int eventGroupCode;

    /**
     * Creates a new instance of ItsNatDOMStdEventListenerWrapperImpl
     */
    public ItsNatDOMStdEventListenerWrapperImpl(ItsNatStfulDocumentImpl itsNatDoc,ClientDocumentStfulImpl clientDoc,EventTarget elem,String type,EventListener listener,boolean useCapture,int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout,String bindToCustomFunc)
    {
        super(itsNatDoc,clientDoc,elem,listener,extraParams,preSendCode,eventTimeout,bindToCustomFunc);

        this.commMode = commMode;
        this.type = type;
        this.useCapture = useCapture;

        this.eventGroupCode = DOMStdEventGroupInfo.getEventGroupCode(type);  // type no cambia por lo que no hay problema de "sincronizaci�n". Obtenerlo de una vez es mejor pues hay que buscar en una colecci�n
    }

    public int getCommModeDeclared()
    {
        return commMode;
    }

    public String getType()
    {
        return type;
    }

    public int getEventGroupCode()
    {
        return eventGroupCode;
    }

    public boolean getUseCapture()
    {
        return useCapture;
    }

    @Override
    public void handleEvent(ClientItsNatNormalEventImpl event)
    {
        super.handleEvent(event);

        boolean sync = (event.getCommMode() == CommMode.XHR_SYNC); // S�lo XHR s�ncrono es completamente s�ncrono

        if (sync)
        {
            StringBuilder retEvent = new StringBuilder();            
            
            if (event.getPreventDefault())
            {
                ClientDocumentStfulDelegateImpl clientDoc = event.getClientDocumentStful().getClientDocumentStfulDelegate();
                if (clientDoc instanceof ClientDocumentStfulDelegateWebImpl)
                {
                    ClientDocumentStfulDelegateWebImpl clientDocWeb = (ClientDocumentStfulDelegateWebImpl)clientDoc;
                    BrowserWeb browser = clientDocWeb.getBrowserWeb();
                    JSRenderItsNatDOMStdEventImpl render = JSRenderItsNatDOMStdEventImpl.getJSItsNatDOMStdEventRender((ClientItsNatDOMStdEventImpl)event,browser);
                    retEvent.append( render.getPreventDefault("event.getNativeEvent()",clientDocWeb) );
                }
                else if (clientDoc instanceof ClientDocumentStfulDelegateDroidImpl)
                {
                    throw new ItsNatException("preventDefault not supported in Droid");
                }
            }

            if (event.getStopPropagation())
            {
                ClientDocumentStfulDelegateImpl clientDoc = event.getClientDocumentStful().getClientDocumentStfulDelegate();
                if (clientDoc instanceof ClientDocumentStfulDelegateWebImpl)
                {
                    ClientDocumentStfulDelegateWebImpl clientDocWeb = (ClientDocumentStfulDelegateWebImpl)clientDoc;
                    BrowserWeb browser = clientDocWeb.getBrowserWeb();
                    JSRenderItsNatDOMStdEventImpl render = JSRenderItsNatDOMStdEventImpl.getJSItsNatDOMStdEventRender((ClientItsNatDOMStdEventImpl)event,browser);
                    retEvent.append( render.getStopPropagation("event.getNativeEvent()",clientDocWeb) );
                }
                else if (clientDoc instanceof ClientDocumentStfulDelegateDroidImpl)
                {
                    throw new ItsNatException("stopPropagation not supported in Droid"); // Con un poco esfuerzo s� podr�amos soportarlo (evitar continuar el "capture") por ej.
                }                    
            }
            
            if (retEvent.length() > 0)
            {
                ItsNatServletResponseImpl itsNatResponse = event.getItsNatServletResponseImpl();
                itsNatResponse.addCodeToSend(retEvent.toString()); // Este c�digo no es notificado a los observers
            }            
            
        }


    }

    public ClientItsNatNormalEventImpl createClientItsNatNormalEvent(RequestNormalEventImpl request)
    {
        return ClientItsNatDOMStdEventFactory.createClientItsNatDOMStdEvent(this,request);
    }
}
