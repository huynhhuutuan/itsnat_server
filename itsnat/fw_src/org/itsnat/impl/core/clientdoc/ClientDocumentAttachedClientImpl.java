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

import org.itsnat.impl.core.clientdoc.web.SVGWebInfoImpl;
import org.itsnat.impl.core.servlet.ItsNatSessionImpl;
import org.itsnat.core.ItsNatException;
import org.itsnat.core.event.ItsNatAttachedClientEvent;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.listener.WaitForEventListenerImpl;
import org.w3c.dom.events.EventListener;

/**
 *
 * @author jmarranz
 */
public abstract class ClientDocumentAttachedClientImpl extends ClientDocumentStfulImpl
{
    protected boolean readOnly;
    protected int commMode;
    protected long eventTimeout;
    protected long waitDocTimeout;
    protected int phase = ItsNatAttachedClientEvent.REQUEST;
    protected boolean accepted = false; // Inicialmente NO se acepta (hay que aceptar expl�citamente).

    /** Creates a new instance of ClientDocumentAttachedClientImpl */
    public ClientDocumentAttachedClientImpl(boolean readOnly,int commMode,long eventTimeout,long waitDocTimeout,Browser browser,ItsNatSessionImpl itsNatSession,ItsNatStfulDocumentImpl itsNatDoc)
    {
        super(itsNatDoc,browser,itsNatSession);

        this.readOnly = readOnly;
        this.commMode = commMode;
        this.eventTimeout = eventTimeout;
        this.waitDocTimeout = waitDocTimeout;
    }

    public abstract String getAttachType();
    
    public void registerInSession()
    {
        getItsNatSessionImpl().registerClientDocumentAttachedClient(this);
    }

    public boolean isScriptingEnabled()
    {
        return true; // De otra manera no podr�amos hacer control remoto
    }

    public boolean isEventsEnabled()
    {
        return true; // De otra manera no podr�amos hacer control remoto
    }

    public boolean canReceiveALLNormalEvents()
    {
        return !isReadOnly();
    }

    public boolean canReceiveSOMENormalEvents()
    {
        if (canReceiveALLNormalEvents()) return true;

        if (getClientDocumentStfulDelegate() instanceof ClientDocumentStfulDelegateWebImpl)
        {
            // Aunque sea read only los eventos para WaitForEventListenerImpl
            // pueden ser recibidos.
            // A d�a de hoy s�lo son necesarios con SVGWeb
             return SVGWebInfoImpl.isSVGWebEnabled((ClientDocumentStfulDelegateWebImpl)getClientDocumentStfulDelegate());
        }
        else return false; // No estoy seguro
    }

    public boolean canReceiveNormalEvents(EventListener listener)
    {
        if (canReceiveALLNormalEvents()) return true; // Como puede recibir todos los eventos el listener del par�metro est� incluido sea cual sea

        if (getClientDocumentStfulDelegate() instanceof ClientDocumentStfulDelegateWebImpl)
        {
            // Es read only
            // A lo mejor puede recibir eventos via el listener WaitForEventListenerImpl
            // relacionado con SVGWeb
            if (!SVGWebInfoImpl.isSVGWebEnabled((ClientDocumentStfulDelegateWebImpl)getClientDocumentStfulDelegate())) return false; // SVGWebInfo desactivado
        }
        
        // Aunque sea read only hacemos una excepci�n con los WaitForEventListenerImpl
        // porque vienen a ser eventos "de servicio"
        // A d�a de hoy s�lo son necesarios con SVGWeb
        return (listener instanceof WaitForEventListenerImpl);
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public int getCommModeDeclared()
    {
        return commMode;
    }

    @Override
    public long getEventTimeout()
    {
        return eventTimeout;
    }

    public long getWaitDocTimeout()
    {
        return waitDocTimeout; 
    }

    public abstract void startAttachedClient();

    public int getPhase()
    {
        return phase;
    }

    public void setPhase(int phase)
    {
        // El UNLOAD prevalece sobre los dem�s estados pues este estado es que libera el ClientDocument
        // El UNLOAD puede haberse definido o bien porque el usuario ha cerrado la p�gina observadora
        // o bien porque no ha sido aceptado un evento.
        // Evitamos as� tambi�n "volver atr�s" en el caso de REFRESH.
        if ((this.phase == ItsNatAttachedClientEvent.UNLOAD) &&
            (this.phase != phase))
                throw new ItsNatException("INTERNAL ERROR"); // return;

        this.phase = phase;
    }

    public boolean isAccepted()
    {
        return accepted;
    }

    public void setAccepted(boolean accepted)
    {
        if (!accepted)
        {
            setInvalid(); // Env�a el JavaScript de parar el timer por ejemplo
        }

        this.accepted = accepted;
    }

    public void attachedClientEventReceived()
    {
        this.lastEventTime = System.currentTimeMillis();
    }

    protected void setInvalidInternal()
    {
        super.setInvalidInternal();

//        ItsNatDocumentImpl itsNatDoc = getItsNatDocumentImpl();
//        if (itsNatDoc.isInvalid()) // El setInvalid del cliente ha podido ser llamado por el documento que est� siendo invalidado
//            setPhase(ItsNatAttachedClientEvent.OBSERVED_INVALID); // Recuerda que en caso de UNLOAD este estado prevalece

        // NO DESREGISTRAMOS, pues el setInvalidInternal ha podido ser llamado
        // por un ClientDocumentOwner destruy�ndose, el cliente observador
        // viene dado por la existencia de la p�gina en el navegador del usuario
        // y conviene que se entere al procesar el siguiente evento y terminar de forma
        // elegante p.ej. avisando con un alert.

        //ItsNatSessionImpl itsNatSession = getItsNatSessionOwner();
        //itsNatSession.unregisterClientDocumentAttachedClient(this);
    }

    public void invalidateAndUnregister()
    {
        // El motivo de este m�todo es asegurar que el m�todo unregisterClientDocumentAttachedClient
        // se llame en la sesi�n correcta pues los ClientDocumentAttachedClientImpl
        // est�n registrados en su sesi�n pero tambi�n en el documento al que est�n asociados
        // que puede pertenecer a otra sesi�n
        getItsNatSessionImpl().unregisterClientDocumentAttachedClient(this);
    }

    public abstract String getRefreshMethod();


}
