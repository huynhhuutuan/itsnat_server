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

package org.itsnat.impl.core.registry;

import org.itsnat.impl.core.listener.dom.domext.AsyncTaskImpl;
import org.itsnat.impl.core.listener.dom.domext.ItsNatAsyncTaskEventListenerWrapperImpl;
import java.io.Serializable;
import org.itsnat.core.ItsNatException;
import org.itsnat.core.event.ParamTransport;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.listener.ItsNatNormalEventListenerWrapperImpl;
import org.itsnat.impl.core.util.MapUniqueId;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public class ItsNatAsyncTaskRegistryImpl extends EventListenerRegistryImpl implements Serializable
{
    protected MapUniqueId<ItsNatAsyncTaskEventListenerWrapperImpl> tasks;
    protected ClientDocumentStfulImpl clientDoc;

    /**
     * Creates a new instance of ItsNatAsyncTaskRegistryImpl
     */
    public ItsNatAsyncTaskRegistryImpl(ClientDocumentStfulImpl clientDoc)
    {
        this.clientDoc = clientDoc;

        this.tasks = new MapUniqueId<ItsNatAsyncTaskEventListenerWrapperImpl>(clientDoc.getUniqueIdGenerator());
    }

    public ItsNatStfulDocumentImpl getItsNatStfulDocument()
    {
        return clientDoc.getItsNatStfulDocument();
    }

    public ItsNatAsyncTaskEventListenerWrapperImpl createAsyncTaskEventListenerWrapper(AsyncTaskImpl taskContainer,EventTarget element,EventListener listener,int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout,String bindToCustomFunc)
    {
        return new ItsNatAsyncTaskEventListenerWrapperImpl(clientDoc,(AsyncTaskImpl)taskContainer,element,listener,commMode,extraParams,preSendCode,eventTimeout,bindToCustomFunc);
    }

    public void addAsynchronousTask(Runnable task,boolean lockDoc,long maxWait,EventTarget element,EventListener listener,int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout,String bindToCustomFunc)
    {
        // Usar por ejemplo cuando una tarea
        // va a ser muy larga pero no es deseable que est� bloqueado durante la tarea (lockDoc = false)
        // y dicha tarea modifica el documento
        // por lo que hay que recoger los cambios, dichos cambios se recoger�n
        // a trav�s de un evento generado autom�ticamente al volver al cliente y as�ncrono.
        if (!ItsNatNormalEventListenerWrapperImpl.canAddItsNatNormalEventListenerWrapper(listener,getItsNatStfulDocument(), clientDoc))
            return;

        AsyncTaskImpl taskContainer = new AsyncTaskImpl(task,lockDoc,maxWait,clientDoc);
        ItsNatAsyncTaskEventListenerWrapperImpl evtListener = createAsyncTaskEventListenerWrapper(taskContainer,element,listener,commMode,extraParams,preSendCode,eventTimeout,bindToCustomFunc);

        tasks.put(evtListener);

        taskContainer.start();

        ClientDocumentStfulDelegateImpl clientDocDeleg = clientDoc.getClientDocumentStfulDelegate();        
        addItsNatEventListenerCode(evtListener,clientDocDeleg);        
    }

    public ItsNatAsyncTaskEventListenerWrapperImpl removeAsynchronousTask(String id)
    {
        // M�todo no p�blico, es llamado por el framework
        // En este contexto hay que recordar que el ItsNatDocument est� bloqueado
        // por el hilo actual.

        ItsNatAsyncTaskEventListenerWrapperImpl listener = tasks.removeById(id);
        if (listener == null)
            throw new ItsNatException("Asynchronous Task with id " + id + " does not exist");

        // El hilo-tarea puede estar vivo todav�a, si locksDocument() es true no deber�a estar vivo
        // todav�a pues no podr�amos llegar aqu� si el documento est� bloqueado por la tarea
        // (o bien acaba de salir del synchronized y todav�a no se ha marcado el hilo como finalizado)
        // El problema m�s bien es cuando locksDocument() devuelve false, en ese caso
        // la tarea puede ser muy larga y el programador tiene la obligaci�n
        // de bloquear el ItsNatDocument en el momento que vaya a acceder al mismo (resultados, cambios en el DOM etc),
        // esto supone que como ahora el ItsNatDocument est� bloqueado por el hilo
        // actual NO podemos esperar al hilo-tarea a que acabe (con un join())
        // pues �ste en *cualquier momento* puede intentar bloquear el ItsNatDocument
        // por lo que entrar�amos en un bloqueo mutuo.
        // Para resolver el problema hay que obtener el Thread que no ha terminado todav�a, esto implica
        // que se ha de intentar de nuevo pero antes desbloqueando el ItsNatDocument
        // para que el hilo-tarea pueda bloquear el ItsNatDocument si lo necesita
        // o bien otros request puedan hacer su trabajo. Con el Thread
        // podremos hacer un join() fuera del documento sincronizado, de esa manera
        // el hilo podr� esperar "dormido" hasta que termine el hilo

        return listener;
    }
}
