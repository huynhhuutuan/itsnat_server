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

package org.itsnat.impl.core.clientdoc;

import java.util.LinkedList;
import org.itsnat.core.CometNotifier;
import org.itsnat.core.ItsNatException;
import org.itsnat.core.ItsNatTimer;
import org.itsnat.core.event.CodeToSendListener;
import org.itsnat.core.event.ParamTransport;
import org.itsnat.core.script.ScriptUtil;
import org.itsnat.impl.core.doc.ItsNatDocumentImpl;
import org.itsnat.impl.core.servlet.ItsNatSessionImpl;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.doc.ItsNatXMLDocumentImpl;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public class ClientDocumentXMLImpl extends ClientDocumentImpl
{
    protected ItsNatXMLDocumentImpl itsNatDoc;

    /**
     * Creates a new instance of ClientDocumentXMLImpl
     */
    public ClientDocumentXMLImpl(ItsNatXMLDocumentImpl itsNatDoc,Browser browser,ItsNatSessionImpl itsNatSession)
    {
        super(browser,itsNatSession);

        this.itsNatDoc = itsNatDoc;
    }

    @Override
    public ItsNatDocumentImpl getItsNatDocumentImpl()
    {
        return itsNatDoc;
    }

    public ItsNatXMLDocumentImpl getItsNatXMLDocumentImpl()
    {
        return itsNatDoc;
    }

    @Override
    protected void setInvalidInternal()
    {
        super.setInvalidInternal();

        // Es el propietario del documento, si es inv�lido lo ser� tambi�n el documento asociado
        // aunque la verdad es que no vale para nada en documentos XML pues no tienen eventos, no se registran
        // en la sesi�n etc
        ItsNatXMLDocumentImpl itsNatDoc = getItsNatXMLDocumentImpl();
        itsNatDoc.setInvalid();
    }

    @Override
    public void registerInSession()
    {
        // Nada que hacer pues no se guarda en la sesi�n
    }

    @Override
    public boolean dispatchEvent(EventTarget target, Event evt) throws EventException
    {
        throw new ItsNatException("XML documents have not events",this);
    }

    @Override
    public boolean dispatchEvent(EventTarget target, Event evt, int commMode, long eventTimeout) throws EventException
    {
        throw new ItsNatException("XML documents have not events",this);
    }

    @Override
    public void startEventDispatcherThread(Runnable task)
    {
        throw new ItsNatException("XML documents have not events",this);
    }

    @Override
    public CometNotifier createCometNotifier()
    {
        throw new ItsNatException("XML documents have not events",this);
    }

    @Override
    public CometNotifier createCometNotifier(long eventTimeout)
    {
        throw new ItsNatException("Not supported in this context");
    }

    @Override
    public CometNotifier createCometNotifier(int commMode,long eventTimeout)
    {
        throw new ItsNatException("XML documents have not events",this);
    }

    @Override
    public CometNotifier createCometNotifier(int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout)    
    {
        throw new ItsNatException("Not supported in this context");
    }        
    
    @Override
    public boolean isScriptingEnabled()
    {
        return false;
    }

    @Override
    public void addCodeToSend(Object code)
    {
        throw new ItsNatException("XML documents have not JavaScript",this);
    }

    @Override
    public boolean isSendCodeEnabled()
    {
        throw new ItsNatException("XML documents have not JavaScript",this);
    }

    @Override
    public void disableSendCode()
    {
        throw new ItsNatException("XML documents have not JavaScript",this);
    }

    @Override
    public void enableSendCode()
    {
        throw new ItsNatException("XML documents have not JavaScript",this);
    }

    @Override
    public void addCodeToSendListener(CodeToSendListener listener)
    {
        throw new ItsNatException("XML documents have not JavaScript",this);
    }

    @Override
    public void removeCodeToSendListener(CodeToSendListener listener)
    {
        throw new ItsNatException("XML documents have not JavaScript",this);
    }

    @Override
    public ScriptUtil getScriptUtil()
    {
        throw new ItsNatException("JavaScript utilities are not available for XML documents",this);
    }

    @Override
    public void addContinueEventListener(EventTarget target, EventListener listener)
    {
        // Ignored
    }

    @Override
    public void addContinueEventListener(EventTarget target, EventListener listener, int commMode, ParamTransport[] extraParams, String preSendCode, long eventTimeout)
    {
        // Ignored
    }

    @Override
    public ItsNatTimer createItsNatTimer()
    {
        throw new ItsNatException("XML documents have not events",this);
    }

    @Override
    public void addAsynchronousTask(Runnable task, boolean lockDoc, int maxWait, EventTarget target, EventListener listener, int commMode, ParamTransport[] extraParams, String preSendCode, long eventTimeout)
    {
        // Ignored
    }

    @Override
    public void addAsynchronousTask(Runnable task,EventListener listener)
    {
        // Ignored
    }

    @Override
    public String getCodeToSendAndReset()
    {
        return null;
    }

    @Override
    public void addEventListener(EventTarget target, String type, EventListener listener, boolean useCapture)
    {
        // Ignorado
    }

    @Override
    public void addEventListener(EventTarget target, String type, EventListener listener, boolean useCapture, int commMode)
    {
        // Ignorado
    }

    @Override
    public void addEventListener(EventTarget target, String type, EventListener listener, boolean useCapture, ParamTransport extraParam)
    {
        // Ignorado
    }

    @Override
    public void addEventListener(EventTarget target, String type, EventListener listener, boolean useCapture, ParamTransport[] extraParams)
    {
        // Ignorado
    }

    @Override
    public void addEventListener(EventTarget target, String type, EventListener listener, boolean useCapture, String preSendCode)
    {
        // Ignorado
    }

    @Override
    public void addEventListener(EventTarget nodeTarget,String type,EventListener listener,boolean useCapture,int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout)
    {
        // Ignorado
    }

    @Override
    public void addEventListener(EventTarget target, String type, EventListener listener, boolean useCapture, int commMode, ParamTransport[] extraParams, String preSendCode, long eventTimeout,String bindToCustomFunc)
    {
        // Ignorado
    }

    @Override
    public void removeEventListener(EventTarget target, String type, EventListener listener, boolean useCapture)
    {
        // Ignorado
    }

    @Override
    public void removeEventListener(EventTarget target,String type,EventListener listener,boolean useCapture,boolean updateClient)
    {
        // Ignorado
    }

    @Override
    public void addMutationEventListener(EventTarget target, EventListener listener, boolean useCapture)
    {
        // Ignorado
    }

    @Override
    public void addMutationEventListener(EventTarget target,EventListener listener,boolean useCapture,int commMode,String preSendCode,long eventTimeout)
    {
        // Ignorado
    }
    
    @Override
    public void addMutationEventListener(EventTarget target, EventListener listener, boolean useCapture, int commMode, String preSendCode, long eventTimeout,String bindToCustomFunc)
    {
        // Ignorado
    }

    @Override
    public void removeMutationEventListener(EventTarget target, EventListener listener, boolean useCapture)
    {
        // Ignorado
    }

    @Override
    public void addUserEventListener(EventTarget target, String name, EventListener listener, int commMode, ParamTransport[] extraParams, String preSendCode, long eventTimeout,String bindToCustomFunc)
    {
        // Ignorado
    }

    @Override
    public void addUserEventListener(EventTarget target, String name, EventListener listener, int commMode, ParamTransport[] extraParams, String preSendCode, long eventTimeout)
    {
        // Ignorado
    }
    
    @Override
    public void addUserEventListener(EventTarget target, String name, EventListener listener)
    {
        // Ignorado
    }

    @Override
    public void removeUserEventListener(EventTarget target, String name, EventListener listener)
    {
        // Ignorado
    }

    @Override
    public boolean hasGlobalEventListenerListeners()
    {
        // Ignorado, no hay eventos no ser� nunca llamado
        return false;
    }     
    
    @Override
    public void getGlobalEventListenerList(LinkedList<EventListener> list)
    {
        // Ignorado, no hay eventos no ser� nunca llamado
    }

    @Override
    public void addEventListener(EventListener listener)
    {
        // Ignorado
    }

    public void addEventListener(int index,EventListener listener)
    {
        // Ignorado
    }

    @Override
    public void removeEventListener(EventListener listener)
    {
        // Ignorado
    }

}
