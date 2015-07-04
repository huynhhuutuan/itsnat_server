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

package org.itsnat.impl.core.doc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import org.itsnat.core.ItsNatException;
import org.itsnat.core.ItsNatServletRequest;
import org.itsnat.core.ItsNatServletResponse;
import org.itsnat.core.event.CodeToSendListener;
import org.itsnat.core.event.ItsNatAttachedClientEventListener;
import org.itsnat.core.event.ItsNatServletRequestListener;
import org.itsnat.core.event.ParamTransport;
import org.itsnat.core.script.ScriptUtil;
import org.itsnat.impl.comp.mgr.ItsNatStfulDocComponentManagerImpl;
import org.itsnat.impl.core.*;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.clientdoc.ClientDocStfulTask;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedClientImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulOwnerImpl;
import org.itsnat.impl.core.domimpl.ElementDocContainer;
import org.itsnat.impl.core.event.CodeToSendEventImpl;
import org.itsnat.impl.core.event.CodeToSendListenersImpl;
import org.itsnat.impl.core.event.server.ServerItsNatNormalEventImpl;
import org.itsnat.impl.core.listener.dom.domext.ItsNatDOMExtEventListenerWrapperImpl;
import org.itsnat.impl.core.listener.dom.domext.ItsNatUserEventListenerWrapperImpl;
import org.itsnat.impl.core.mut.doc.DocMutationEventListenerStfulImpl;
import org.itsnat.impl.core.registry.dom.domext.ItsNatUserEventListenerRegistryImpl;
import org.itsnat.impl.core.scriptren.shared.ScriptUtilImpl;
import org.itsnat.impl.core.servlet.ItsNatServletConfigImpl;
import org.itsnat.impl.core.servlet.ItsNatServletRequestImpl;
import org.itsnat.impl.core.servlet.ItsNatSessionImpl;
import org.itsnat.impl.core.template.ItsNatDocumentTemplateVersionImpl;
import org.itsnat.impl.core.template.ItsNatStfulDocumentTemplateVersionImpl;
import org.itsnat.impl.core.util.MapUniqueId;
import org.itsnat.impl.core.util.WeakSetImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public abstract class ItsNatStfulDocumentImpl extends ItsNatDocumentImpl
{
    protected ScriptUtilImpl scriptUtil;
    protected ItsNatUserEventListenerRegistryImpl userListenerRegistry;
    protected boolean enabledSendCode = true;
    protected WeakSetImpl<ClientDocumentAttachedClientImpl> clientDocAttachedSet; // No se utilizan ids (pues pueden ser generados por otras sesiones), se utiliza la identidad del objeto
    protected int commMode;
    protected long eventTimeout;
    protected CodeToSendListenersImpl codeToSendListeners;
    protected LinkedList<EventListener> globalNormalEventListeners;
    protected LinkedList<ItsNatAttachedClientEventListener> attachedClientListeners;
    protected LinkedList<ItsNatServletRequestListener> referrerRequestListeners;
    protected transient ThreadLocal<ClientDocumentStfulImpl> evtDispThreadLocal = new ThreadLocal<ClientDocumentStfulImpl>();
    protected long evtDispMaxWait;
    protected int maxOpenClients;
    protected MapUniqueId<BoundElementDocContainerImpl> boundElemDocContainers;
    protected Random random;

    
    /** Creates a new instance of ItsNatStfulDocumentImpl */
    public ItsNatStfulDocumentImpl(Document doc,ItsNatDocumentTemplateVersionImpl docTemplate,Browser browser,String requestURL,ItsNatSessionImpl ownerSession,boolean stateless)
    {
        super(doc,docTemplate,browser,requestURL,ownerSession,stateless);

        this.commMode = docTemplate.getCommMode();
        this.eventTimeout = docTemplate.getEventTimeout();
        this.evtDispMaxWait = docTemplate.getEventDispatcherMaxWait();
        this.maxOpenClients = docTemplate.getMaxOpenClientsByDocument();
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        this.evtDispThreadLocal = new ThreadLocal<ClientDocumentStfulImpl>();

        in.defaultReadObject();
    }

    
    public ItsNatStfulDocComponentManagerImpl getItsNatStfulComponentManager()
    {
        return (ItsNatStfulDocComponentManagerImpl)componentMgr;
    }

    public DocMutationEventListenerStfulImpl getDocMutationListenerEventStful()
    {
        return (DocMutationEventListenerStfulImpl)mutationListener;
    }

    public boolean canRenderAndSendMutationCode()
    {
        boolean loading = isLoading();
        return !loading || (loading && !isFastLoadMode());
    }

    public ItsNatStfulDocumentTemplateVersionImpl getItsNatStfulDocumentTemplateVersion()
    {
        return (ItsNatStfulDocumentTemplateVersionImpl)docTemplateVersion;
    }

    public ClientDocumentImpl createClientDocumentOwner(Browser browser,ItsNatSessionImpl ownerSession)
    {
        return new ClientDocumentStfulOwnerImpl(this,browser,ownerSession);
    }

    public ClientDocumentStfulOwnerImpl getClientDocumentStfulOwner()
    {
        return (ClientDocumentStfulOwnerImpl)clientDocOwner;
    }

    public ClientDocumentStfulImpl getRequestingClientDocumentStful()
    {
        ItsNatServletRequestImpl request = getCurrentItsNatServletRequest();
        if (request != null) return (ClientDocumentStfulImpl)request.getClientDocumentImpl(); // No puede ser null el ClientDocument devuelto (no tiene sentido en este contexto)
        return getClientDocumentStfulOwner(); // Caso de que no haya un request en proceso
    }

    public ScriptUtil getScriptUtil()
    {
        if (scriptUtil == null)
            this.scriptUtil = ScriptUtilImpl.createScriptUtilFromDoc(this);
        return scriptUtil;
    }

    public int getCommMode()
    {
        return commMode;
    }

    public void setCommMode(int commMode)
    {
        CommModeImpl.checkMode(commMode);
        this.commMode = commMode;
    }

    public long getEventTimeout()
    {
        return eventTimeout;
    }

    public void setEventTimeout(long timeout)
    {
        this.eventTimeout = timeout;
    }

    public void addEventListener(EventTarget nodeTarget,String type,EventListener listener,boolean useCapture)
    {
        int commMode = getCommMode();
        long eventTimeout = getEventTimeout();
        addEventListener(nodeTarget,type,listener,useCapture,commMode,null,null,eventTimeout);
    }

    public void addEventListener(EventTarget nodeTarget,String type,EventListener listener,boolean useCapture,int commMode)
    {
        long eventTimeout = getEventTimeout();
        addEventListener(nodeTarget,type,listener,useCapture,commMode,null,null,eventTimeout);
    }

    public void addEventListener(EventTarget nodeTarget,String type,EventListener listener,boolean useCapture,ParamTransport extraParam)
    {
        ParamTransport[] extraParams = new ParamTransport[]{ extraParam };
        addEventListener(nodeTarget,type,listener,useCapture,extraParams);
    }

    public void addEventListener(EventTarget nodeTarget,String type,EventListener listener,boolean useCapture,ParamTransport[] extraParams)
    {
        int commMode = getCommMode();
        long eventTimeout = getEventTimeout();
        addEventListener(nodeTarget,type,listener,useCapture,commMode,extraParams,null,eventTimeout);
    }

    public void addEventListener(EventTarget nodeTarget,String type,EventListener listener,boolean useCapture,String preSendCode)
    {
        int commMode = getCommMode();
        long eventTimeout = getEventTimeout();
        addEventListener(nodeTarget,type,listener,useCapture,commMode,null,preSendCode,eventTimeout);
    }

    public void addEventListener(EventTarget nodeTarget,String type,EventListener listener,boolean useCapture,int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout)
    {
        addEventListener(nodeTarget,type,listener,useCapture,commMode,extraParams,preSendCode,eventTimeout,null);
    }

    public void addEventListener(EventTarget nodeTarget,String type,EventListener listener,boolean useCapture,int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout,String bindToCustomFunc)
    {
        if (ItsNatDOMExtEventListenerWrapperImpl.isExtensionType(type))
            addDOMExtEventListener(nodeTarget,type,listener,useCapture,commMode,extraParams,preSendCode,eventTimeout,bindToCustomFunc);
        else
            addPlatformEventListener(nodeTarget,type,listener,useCapture,commMode,extraParams,preSendCode,eventTimeout,bindToCustomFunc);
    }

    public abstract void addPlatformEventListener(EventTarget nodeTarget,String type,EventListener listener,boolean useCapture,int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout,String bindToCustomFunc);

    public void addDOMExtEventListener(EventTarget nodeTarget,String type,EventListener listener,boolean useCapture,int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout,String bindToCustomFunc)
    {
        if (useCapture) throw new ItsNatException("Capturing is not allowed for this type:" + type,this);

        if (ItsNatUserEventListenerWrapperImpl.isUserType(type))
        {
            String name = ItsNatUserEventListenerWrapperImpl.getNameFromType(type,false);
            addUserEventListener(nodeTarget,name,listener,commMode,extraParams,preSendCode,eventTimeout,bindToCustomFunc);
        }
        else // itsnat:continue, itsnat:timer, itsnat:asynctask o itsnat:comet
            throw new ItsNatException("This method is not allowed to register this event listener type:" + type,this);
    }


    public void addMutationEventListener(EventTarget target,EventListener listener,boolean useCapture,int commMode,String preSendCode,long eventTimeout)
    {
        addMutationEventListener(target,listener,useCapture,commMode,preSendCode,eventTimeout,null);
    }

    public abstract void addMutationEventListener(EventTarget target,EventListener listener,boolean useCapture,int commMode,String preSendCode,long eventTimeout,String bindToCustomFunc);
    
    public void removeEventListener(EventTarget target,String type,EventListener listener,boolean useCapture)
    {
        removeEventListener(target,type,listener,useCapture,true);
    }

    public void removeEventListener(EventTarget target,String type,EventListener listener,boolean useCapture,boolean updateClient)
    {
        if (ItsNatDOMExtEventListenerWrapperImpl.isExtensionType(type))
            removeDOMExtEventListener(target,type,listener,useCapture,updateClient);
        else
            removePlatformEventListener(target,type,listener,useCapture,updateClient);
    }

    public abstract void removePlatformEventListener(EventTarget target,String type,EventListener listener,boolean useCapture,boolean updateClient);
    public abstract int removeAllPlatformEventListeners(EventTarget target,boolean updateClient);
    
    public void removeDOMExtEventListener(EventTarget target,String type,EventListener listener,boolean useCapture,boolean updateClient)
    {
        if (useCapture) return; // Como no puede haber listeners registrados capturing no hacemos nada

        if (ItsNatUserEventListenerWrapperImpl.isUserType(type))
        {
            String name = ItsNatUserEventListenerWrapperImpl.getNameFromType(type,false);
            removeUserEventListener(target,name,listener,updateClient);
        }
        else  // itsnat:continue, itsnat:timer, itsnat:asynctask o itsnat:comet , en estos tipos el desregistro se hace internamente.
            throw new ItsNatException("This method is not allowed to unregister this event listener type:" + type,this);
    }



    public boolean hasUserEventListeners()
    {
        if (userListenerRegistry == null)
            return false;
        return !userListenerRegistry.isEmpty();
    }

    public ItsNatUserEventListenerRegistryImpl getUserEventListenerRegistry()
    {
        if (userListenerRegistry == null)
            userListenerRegistry = new ItsNatUserEventListenerRegistryImpl(this,null); // para ahorrar memoria si no se usa
        return userListenerRegistry;
    }

    public void addUserEventListener(EventTarget target,String name,EventListener listener,int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout)
    {
        addUserEventListener(target,name,listener,commMode,extraParams,preSendCode,eventTimeout,null);
    }

    public void addUserEventListener(EventTarget target,String name,EventListener listener)
    {
        addUserEventListener(target,name,listener,getCommMode(),null,null,getEventTimeout(), null);
    }

    public void addUserEventListener(EventTarget target,String name,EventListener listener,int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout,String bindToCustomFunc)
    {
        getUserEventListenerRegistry().addItsNatUserEventListener(target,name,listener,commMode,extraParams,preSendCode,eventTimeout,bindToCustomFunc);
    }

    public ItsNatUserEventListenerWrapperImpl getUserEventListenerById(String listenerId)
    {
        if (!hasUserEventListeners()) return null;

        return getUserEventListenerRegistry().getItsNatUserEventListenerById(listenerId);
    }

    public void removeUserEventListener(EventTarget target,String name,EventListener listener)
    {
        removeUserEventListener(target,name,listener,true);
    }

    public void removeUserEventListener(EventTarget target,String name,EventListener listener,boolean updateClient)
    {
        getUserEventListenerRegistry().removeItsNatUserEventListener(target,name,listener,updateClient);
    }

    public int removeAllUserEventListeners(EventTarget target,boolean updateClient)
    {
        if (!hasUserEventListeners()) return 0;

        return getUserEventListenerRegistry().removeAllItsNatUserEventListeners(target,updateClient);
    }

    public boolean isSendCodeEnabled()
    {
        return enabledSendCode;
    }

    public void disableSendCode()
    {
        this.enabledSendCode = false;
    }

    public void enableSendCode()
    {
        this.enabledSendCode = true;
    }

    public boolean hasClientDocumentAttachedClient()
    {
        if (clientDocAttachedSet == null)
            return false;
        return !clientDocAttachedSet.isEmpty();
    }

    public int getClientDocumentAttachedCount()
    {
        if (clientDocAttachedSet == null)
            return 0;
        return clientDocAttachedSet.size();
    }

    public WeakSetImpl<ClientDocumentAttachedClientImpl> getClientDocumentAttachedClientSet()
    {
        if (clientDocAttachedSet == null)
            this.clientDocAttachedSet = new WeakSetImpl<ClientDocumentAttachedClientImpl>();
        return clientDocAttachedSet;
    }

    public ClientDocumentAttachedClientImpl[] getClientDocumentAttachedClientArray()
    {
        if (!hasClientDocumentAttachedClient()) return null;

        WeakSetImpl<ClientDocumentAttachedClientImpl> attachedClients = getClientDocumentAttachedClientSet();
        return attachedClients.toArray(new ClientDocumentAttachedClientImpl[attachedClients.size()]);
    }

    public void addClientDocumentAttachedClient(ClientDocumentAttachedClientImpl clientDoc)
    {
        getClientDocumentAttachedClientSet().add(clientDoc);

        if (clientDoc.canReceiveSOMENormalEvents()) // Con este chequeo nos ahorramos llamadas in�tiles
        {
            renderPlatformEventListeners(clientDoc);
            if (hasUserEventListeners())
                getUserEventListenerRegistry().renderItsNatNormalEventListeners(clientDoc);
        }

        getItsNatStfulComponentManager().addClientDocumentAttachedClient(clientDoc);
    }

    public abstract void renderPlatformEventListeners(ClientDocumentAttachedClientImpl clientDoc);
    
    public void removeClientDocumentAttachedClient(ClientDocumentAttachedClientImpl clientDoc)
    {
        clientDoc.setInvalid();
        getClientDocumentAttachedClientSet().remove(clientDoc);

        getItsNatStfulComponentManager().removeClientDocumentAttachedClient(clientDoc);
    }

    public boolean allClientDocumentWillReceiveCodeSent()
    {
        if (!isSendCodeEnabled())
            return false; // No, pues est� desactivado

        ClientDocumentStfulOwnerImpl owner = getClientDocumentStfulOwner();
        if (!owner.isSendCodeEnabled())
            return false;

        if (hasClientDocumentAttachedClient())
        {
            WeakSetImpl<ClientDocumentAttachedClientImpl> clientDocs = getClientDocumentAttachedClientSet();
            for(ClientDocumentAttachedClientImpl client : clientDocs)
            {
                if (!client.isSendCodeEnabled())
                    return false;
            }
        }

        return true; // Todos pueden recibir c�digo
    }

    public ClientDocumentStfulImpl[] getAllClientDocumentStfulsCopy()
    {
        return (ClientDocumentStfulImpl[])getAllClientDocumentsCopy();
    }

    public ClientDocumentImpl[] getAllClientDocumentsCopy()
    {
        ClientDocumentStfulOwnerImpl ownerClient = getClientDocumentStfulOwner();
        if (!hasClientDocumentAttachedClient())
            return new ClientDocumentStfulImpl[] { ownerClient };

        WeakSetImpl<ClientDocumentAttachedClientImpl> attachedClients = getClientDocumentAttachedClientSet();
        ClientDocumentStfulImpl[] res = new ClientDocumentStfulImpl[1 + attachedClients.size()];
        res[0] = ownerClient;
        int i = 1;
        for(Iterator<ClientDocumentAttachedClientImpl> it = attachedClients.iterator(); it.hasNext(); i++)
        {
            ClientDocumentAttachedClientImpl clientDoc = it.next();
            res[i] = clientDoc;
        }
        return res;
    }

    public boolean executeTaskOnClients(ClientDocStfulTask clientTask,Object arg)
    {
        ClientDocumentStfulOwnerImpl owner = getClientDocumentStfulOwner();
        boolean cont = clientTask.doTask(owner,arg);
        if (!cont) return false;

        if (hasClientDocumentAttachedClient())
        {
            WeakSetImpl<ClientDocumentAttachedClientImpl> attachedClient = getClientDocumentAttachedClientSet();
            for(ClientDocumentAttachedClientImpl clientDoc : attachedClient)
            {
                cont = clientTask.doTask(clientDoc,arg);
                if (!cont) return false;
            }
        }

        return true; // recorri� todos y siempre se devolvi� true
    }

    public void addCodeToSend(Object code)
    {
        if (!isScriptingEnabled())
            return;

        if (!isSendCodeEnabled()) // No enviar a nadie
            return;

        CodeToSendEventImpl event = null;
        if (hasCodeToSendListeners())
        {
            event = getCodeToSendListeners().preProcessCodeToSend(code);
            code = event.getCode();
            if (code == null) return; // Ha sido rechazado
        }

        ClientDocumentStfulImpl[] allClient = getAllClientDocumentStfulsCopy();
        for(int i = 0; i < allClient.length; i++)
        {
            ClientDocumentStfulImpl clientDoc = allClient[i];
            if (clientDoc.isSendCodeEnabled())
                clientDoc.addCodeToSend(code);
        }

        if (event != null)
            getCodeToSendListeners().postProcessCodeToSend(event);
    }

    public boolean hasCodeToSendListeners()
    {
        if (codeToSendListeners == null)
            return false;
        return codeToSendListeners.hasCodeToSendListeners();
    }

    public CodeToSendListenersImpl getCodeToSendListeners()
    {
        if (codeToSendListeners == null)
            this.codeToSendListeners = new CodeToSendListenersImpl(this);
        return codeToSendListeners;
    }

    public void addCodeToSendListener(CodeToSendListener listener)
    {
        getCodeToSendListeners().addCodeToSendListener(listener);
    }

    public void removeCodeToSendListener(CodeToSendListener listener)
    {
        getCodeToSendListeners().removeCodeToSendListener(listener);
    }

    @Override
    protected void setInvalidInternal()
    {
        super.setInvalidInternal();

        ClientDocumentStfulImpl[] allClient = getAllClientDocumentStfulsCopy();
        for(int i = 0; i < allClient.length; i++)
        {
            ClientDocumentStfulImpl clientDoc = allClient[i];
            clientDoc.setInvalid();
        }
    }

    public boolean isNodeCacheEnabled()
    {
        // No tiene sentido activar la cach� cuando es una p�gina sin JavaScript
        if (!isScriptingEnabled())
           return false;
        return getItsNatDocumentTemplateVersion().isNodeCacheEnabled();
    }

    public LinkedList<ItsNatAttachedClientEventListener> getItsNatAttachedClientEventListeners()
    {
        if (attachedClientListeners == null)
            this.attachedClientListeners = new LinkedList<ItsNatAttachedClientEventListener>();
        return attachedClientListeners;
    }

    public void getItsNatAttachedClientEventListenerList(LinkedList<ItsNatAttachedClientEventListener> list)
    {
        // No sincronizamos porque s�lo admitimos s�lo lectura
        if (attachedClientListeners == null)
            return;
        list.addAll(attachedClientListeners);
    }

    public void addItsNatAttachedClientEventListener(ItsNatAttachedClientEventListener listener)
    {
        LinkedList<ItsNatAttachedClientEventListener> attachedEventListeners = getItsNatAttachedClientEventListeners();
        attachedEventListeners.add(listener);
    }

    public void removeItsNatAttachedClientEventListener(ItsNatAttachedClientEventListener listener)
    {
        LinkedList<ItsNatAttachedClientEventListener> attachedEventListeners = getItsNatAttachedClientEventListeners();
        attachedEventListeners.remove(listener);
    }

    public boolean hasGlobalEventListenerListeners()
    {
        if (globalNormalEventListeners == null)
            return false;
        return !globalNormalEventListeners.isEmpty();
    }    
    
    public LinkedList<EventListener> getGlobalEventListenerList()
    {
        if (globalNormalEventListeners == null)
            this.globalNormalEventListeners = new LinkedList<EventListener>();
        return globalNormalEventListeners;
    }

    public void getGlobalEventListenerList(LinkedList<EventListener> list)
    {
        if (globalNormalEventListeners == null)
            return;
        list.addAll(globalNormalEventListeners);
    }

    public void addEventListener(EventListener listener)
    {
        LinkedList<EventListener> globalEventListeners = getGlobalEventListenerList();
        globalEventListeners.add(listener);
    }

    public void addEventListener(int index,EventListener listener)
    {
        LinkedList<EventListener> globalEventListeners = getGlobalEventListenerList();
        globalEventListeners.add(index,listener);
    }

    public void removeEventListener(EventListener listener)
    {
        LinkedList<EventListener> globalEventListeners = getGlobalEventListenerList();
        globalEventListeners.remove(listener);
    }

    public Event createEvent(String eventType) throws DOMException
    {
        return ServerItsNatNormalEventImpl.createServerNormalEvent(eventType,this);
    }

    public void lockThread(long maxWait)
    {
        synchronized(this) // si ya est� sincronizado (lo normal) es redundante
        {
            // Al esperar liberamos el lock sobre el documento y otro hilo lo podr� usar
            try { wait(maxWait); } catch(InterruptedException ex) { throw new ItsNatException(ex,this); }
        }
    }

    public void unlockThreads()
    {
        synchronized(this)
        {
            notifyAll(); // Liberamos el hilo request/response bloqueado
        }
    }

    public boolean dispatchEvent(EventTarget target,Event evt) throws EventException
    {
        ClientDocumentStfulImpl clientDoc = getEventDispatcherClientDocByThread();
        if (clientDoc != null)
            return clientDoc.dispatchEvent(target,evt);
        else
            return dispatchEventLocally(target,evt);
    }

    public boolean dispatchEventLocally(EventTarget target,Event evt) throws EventException
    {
        return ServerItsNatNormalEventImpl.dispatchEventLocally(target,evt);
    }

    public long getEventDispatcherMaxWait()
    {
        return evtDispMaxWait;
    }

    public void setEventDispatcherMaxWait(long wait)
    {
        this.evtDispMaxWait = wait;
    }

    public int getMaxOpenClientsByDocument()
    {
        return maxOpenClients;
    }

    public void setMaxOpenClientsByDocument(int value)
    {
        ItsNatServletConfigImpl.checkMaxOpenClientsByDocument(value);
        this.maxOpenClients = value;
        getClientDocumentStfulOwner().getItsNatSessionImpl().cleanExcessClientDocumentAttachedClients(this); // Lo hacemos efectivo ya mismo
    }

    /*
    public ThreadLocal<ClientDocumentStfulImpl> getEventDispatcherThreadLocal()
    {
        return evtDispThreadLocal;
    }
*/
    public ClientDocumentStfulImpl getEventDispatcherClientDocByThread()
    {
        return evtDispThreadLocal.get();
    }

    public void setEventDispatcherClientDocByThread(ClientDocumentStfulImpl clientDoc)
    {
        evtDispThreadLocal.set(clientDoc);
    }

    public LinkedList<ItsNatServletRequestListener> getReferrerItsNatServletRequestListenerList()
    {
        if (referrerRequestListeners == null)
            this.referrerRequestListeners = new LinkedList<ItsNatServletRequestListener>();
        return referrerRequestListeners;
    }

    public Iterator<ItsNatServletRequestListener> getReferrerItsNatServletRequestListenerIterator()
    {
        if (referrerRequestListeners == null) return null;
        if (referrerRequestListeners.isEmpty()) return null;
        return referrerRequestListeners.iterator();
    }

    public void addReferrerItsNatServletRequestListener(ItsNatServletRequestListener listener)
    {
        if (!isReferrerEnabled())
            throw new ItsNatException("Referrer feature is not enabled",this);

        LinkedList<ItsNatServletRequestListener> referrerRequestListeners = getReferrerItsNatServletRequestListenerList();
        referrerRequestListeners.add(listener);
    }

    public void removeReferrerItsNatServletRequestListener(ItsNatServletRequestListener listener)
    {
        LinkedList<ItsNatServletRequestListener> referrerRequestListeners = getReferrerItsNatServletRequestListenerList();
        referrerRequestListeners.remove(listener);
    }

    public void dispatchReferrerRequestListeners(ItsNatServletRequest request,ItsNatServletResponse response)
    {
        // Sincronizar el documento antes de llamar a este m�todo
        Iterator<ItsNatServletRequestListener> iterator = getReferrerItsNatServletRequestListenerIterator();
        if (iterator != null)
        {
            while(iterator.hasNext())
            {
                ItsNatServletRequestListener listener = iterator.next();
                listener.processRequest(request,response);
            }
        }
    }

    public void normalEventReceived(ClientDocumentStfulImpl clientDocSource)
    {
        // clientDoc es el cliente que lo recibi�, no lo notificamos pues ya
        // sabe que ha recibido un evento "normal", adem�s en el caso
        // de control remoto (Comet) evitamos refrescos in�tiles (y que pueden bloquear el navegador),
        // si ha de refrescarse que lo decida el m�todo del cliente que lo recibi�.

        ClientDocumentStfulOwnerImpl clientDocOwner = getClientDocumentStfulOwner();
        if (clientDocOwner != clientDocSource)
            clientDocOwner.normalEventReceivedInDocument();

        if (hasClientDocumentAttachedClient())
        {
            WeakSetImpl<ClientDocumentAttachedClientImpl> clientDocs = getClientDocumentAttachedClientSet();
            for(ClientDocumentAttachedClientImpl clientDocAttached : clientDocs)
            {
                if (clientDocAttached != clientDocSource)
                    clientDocAttached.normalEventReceivedInDocument();
            }
        }
    }

    public Random getRandom()
    {
        if (random == null) this.random = new Random();
        return random;
    }

    public boolean hasBoundElementDocContainers()
    {
        if (boundElemDocContainers == null) return false;
        return !boundElemDocContainers.isEmpty();
    }

    public MapUniqueId<BoundElementDocContainerImpl> getBoundElementDocContainerMap()
    {
        if (boundElemDocContainers == null) this.boundElemDocContainers = new MapUniqueId<BoundElementDocContainerImpl>(getUniqueIdGenerator());
        return boundElemDocContainers;
    }

    public BoundElementDocContainerImpl getBoundElementDocContainer(String id,int random)
    {
        if (!hasBoundElementDocContainers()) return null;

        BoundElementDocContainerImpl bindInfo = getBoundElementDocContainerMap().get(id);
        if (bindInfo == null) return null;
        if (bindInfo.getRandomNumber() != random) return null; // Podr�amos provocar un error pero por si acaso es un extra�o caso de expiraci�n
        return bindInfo;
    }

    public BoundElementDocContainerImpl addBoundElementDocContainer(ElementDocContainer elem,String docName)
    {
        BoundElementDocContainerImpl bindInfo = new BoundElementDocContainerImpl(elem,docName,this);

        Object res = getBoundElementDocContainerMap().put(bindInfo);
        if (res != null) throw new ItsNatException("INTERNAL ERROR");
        return bindInfo;
    }

    public boolean removeBoundElementDocContainer(BoundElementDocContainerImpl bindInfo)
    {
        if (!hasBoundElementDocContainers()) return false; // Ya no est� registrado (RARO)

        Object res = getBoundElementDocContainerMap().remove(bindInfo);
        if (res != bindInfo) throw new ItsNatException("INTERNAL ERROR");

        return true;
    }

    public boolean isDisconnectedChildNodesFromClient(Node node)
    {
        return getDocMutationListenerEventStful().isDisconnectedChildNodesFromClient(node);
    }

    public Node disconnectChildNodesFromClient(Node node)
    {
        return getDocMutationListenerEventStful().disconnectChildNodesFromClient(node);
    }
    
    public void reconnectChildNodesToClient(Node node)
    {
        getDocMutationListenerEventStful().reconnectChildNodesToClient(node);
    }

    public abstract Element getVisualRootElement();
    public abstract boolean isNewNodeDirectChildOfContentRoot(Node newNode);
}
