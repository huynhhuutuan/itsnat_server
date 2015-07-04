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

package org.itsnat.impl.core.registry.dom.domext;

import org.itsnat.impl.core.doc.ItsNatTimerImpl;
import org.itsnat.core.ItsNatException;
import org.itsnat.core.event.ParamTransport;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.listener.ItsNatNormalEventListenerWrapperImpl;
import org.itsnat.impl.core.listener.dom.domext.ItsNatTimerEventListenerWrapperImpl;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * ����REVISAR ESTE TEXTO!!!!
 * NO derivamos de DOMEventListenerRegistryByTargetTooImpl porque
 * nos interesa registrar m�ltiples veces un mismo listener y
 * DOMEventListenerRegistryByTargetTooImpl est� m�s orientado
 * a eventos DOM normales con el registro-identidad: target,type,listener,useCapture
 *
 * Pero como nos interesa asociar los timers a tambi�n a nodos y admitir el auto-desregistro
 * cuando se pierde el target, utilizamos nuestra colecci�n especial WeakMapPuggable,
 * sobre el uso de esta clase ver ItsNatDOMEventListenerRegistryByTargetTooImpl.
 *
 * @author jmarranz
 */
public class ItsNatTimerEventListenerRegistryImpl extends ItsNatDOMExtEventListenerRegistryImpl
{
    /**
     * Creates a new instance of ContinueEventListenerRegistryImpl
     */
    public ItsNatTimerEventListenerRegistryImpl(ClientDocumentStfulImpl clientDoc)
    {
        super(clientDoc.getItsNatStfulDocument(),clientDoc);
    }

    @Override
    protected void addItsNatNormalEventListener(ItsNatNormalEventListenerWrapperImpl listenerWrapper)
    {
        super.addItsNatNormalEventListener(listenerWrapper);

        ItsNatTimerEventListenerWrapperImpl timerListener = (ItsNatTimerEventListenerWrapperImpl)listenerWrapper;
        timerListener.getItsNatTimerImpl().addListenerLocal(timerListener);
    }

    public ItsNatTimerEventListenerWrapperImpl addItsNatTimerEventListener(EventTarget target,EventListener listener,long time,long period,boolean fixedRate,int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout,String bindToCustomFunc,ItsNatTimerImpl timer)
    {
        // El target puede ser nulo
        // Permitimos registrar m�ltiples veces el mismo listener para el mismo target
        if (!canAddItsNatNormalEventListener(target,ItsNatTimerEventListenerWrapperImpl.getTypeStatic(),listener))
            return null;

        if (period < 0)
            throw new ItsNatException("Negative period");

        ItsNatTimerEventListenerWrapperImpl listenerWrapper = new ItsNatTimerEventListenerWrapperImpl(target,listener,time,period,fixedRate,commMode,extraParams,preSendCode,eventTimeout,bindToCustomFunc,timer);
        addItsNatNormalEventListener(listenerWrapper);
        return listenerWrapper;
    }

    @Override
    public ItsNatNormalEventListenerWrapperImpl removeItsNatNormalEventListenerById(String id,boolean updateClient)
    {
        ItsNatTimerEventListenerWrapperImpl listenerWrapper = (ItsNatTimerEventListenerWrapperImpl)super.removeItsNatNormalEventListenerById(id,updateClient);
        if (listenerWrapper == null) return null;

        listenerWrapper.getItsNatTimerImpl().removeListenerLocal(listenerWrapper);

        return listenerWrapper;
    }

    @Override
    public void removeItsNatNormalEventListener(ItsNatNormalEventListenerWrapperImpl listenerWrapper,boolean updateClient,boolean expunged)
    {
        // Este m�todo es llamado tambi�n por processExpunged

        ItsNatTimerEventListenerWrapperImpl timerListener = (ItsNatTimerEventListenerWrapperImpl)listenerWrapper;

        timerListener.getItsNatTimerImpl().removeListenerLocal(timerListener);

        super.removeItsNatNormalEventListener(timerListener, updateClient,expunged);
    }

    public void removeItsNatTimerEventListener(ItsNatTimerEventListenerWrapperImpl listener,boolean updateClient)
    {
        removeItsNatNormalEventListener(listener,updateClient,false);
    }

    public ItsNatTimerEventListenerWrapperImpl getItsNatTimerEventListenerById(String listenerId)
    {
        return (ItsNatTimerEventListenerWrapperImpl)getItsNatNormalEventListenerById(listenerId);
    }

    @Override
    public EventListener[] getEventListenersArrayCopy(EventTarget target,String type,boolean useCapture)
    {
        // No se usa nunca, por ejemplo porque los timer events no pueden ser disparados desde el servidor
        // (no est� soportado por ahora)
        throw new ItsNatException("INTERNAL ERROR");
    }

    public int removeAllItsNatTimerEventListeners(EventTarget target,boolean updateClient)
    {
        return removeAllItsNatNormalEventListeners(target,updateClient);
    }

}
