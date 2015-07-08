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
import org.itsnat.impl.core.servlet.ItsNatSessionImpl;
import org.itsnat.impl.core.req.*;
import org.itsnat.core.ItsNatException;
import org.itsnat.core.event.ItsNatAttachedClientEvent;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedClientImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedClientErrorImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.doc.ItsNatDocSynchronizerImpl;
import org.itsnat.impl.core.resp.ResponseEventDoNothingImpl;
import org.itsnat.impl.core.resp.attachcli.ResponseAttachedClient;
import org.itsnat.impl.core.resp.attachcli.ResponseAttachedClientEventImpl;
import org.itsnat.impl.core.resp.attachcli.ResponseAttachedClientErrorEventLostClientDocImpl;
import org.itsnat.impl.core.resp.attachcli.ResponseAttachedClientErrorEventLostSessionImpl;

/**
 *
 * @author jmarranz
 */
public abstract class RequestAttachedClientEventImpl extends RequestEventStfulImpl implements RequestAttachedClient
{
    /**
     * Creates a new instance of RequestAttachedClientEventImpl
     */
    public RequestAttachedClientEventImpl(ItsNatServletRequestImpl itsNatRequest)
    {
        super(itsNatRequest);
    }

    public static RequestAttachedClientEventImpl createRequestAttachedClientEvent(int evtType,ItsNatServletRequestImpl itsNatRequest)
    {
        switch(evtType)
        {
            case EVENT_TYPE_ATTACH_TIMER:
                return new RequestAttachedClientEventTimerImpl(itsNatRequest);
            case EVENT_TYPE_ATTACH_COMET:
                return new RequestAttachedClientEventCometImpl(itsNatRequest);
            case EVENT_TYPE_ATTACH_NOT_REFRESH:
                return new RequestAttachedClientEventNotRefreshImpl(itsNatRequest);
            default:
                throw new ItsNatException("Malformed URL/request");
        }
    }

    public ResponseAttachedClient getResponseAttachedClient()
    {
        return (ResponseAttachedClient)response;
    }

    public ResponseAttachedClientEventImpl getResponseAttachedClientEvent()
    {
        return (ResponseAttachedClientEventImpl)response;
    }

    public boolean isUnloadEvent()
    {
        String unloadParam = getAttrOrParam("itsnat_unload");
        return ((unloadParam != null) && unloadParam.equals("true"));
    }

    public ClientDocumentStfulImpl getClientDocumentStfulById(String clientId)
    {
        ItsNatSessionImpl session = getItsNatSession();
        return session.getClientDocumentAttachedClientById(clientId);
    }

    public void processClientDocument(ClientDocumentStfulImpl clientDoc)
    {
        processClientDocumentAttachedClient((ClientDocumentAttachedClientImpl)clientDoc);
    }

    public void processClientDocumentAttachedClient(final ClientDocumentAttachedClientImpl clientDoc)
    {
        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument(); // Es seguro que no es nulo pero puede ser inv�lido

        ItsNatDocSynchronizerImpl syncTask = new ItsNatDocSynchronizerImpl()
        {
            protected void syncMethod()
            {
                processClientDocumentAttachedClientThreadSync(clientDoc);
            }
        };
        syncTask.exec(itsNatDoc);
    }

    public void processClientDocumentAttachedClientThreadSync(ClientDocumentAttachedClientImpl clientDoc)
    {
        bindClientToRequest(clientDoc);

        try
        {
            if (isUnloadEvent())
            {
                clientDoc.setPhase(ItsNatAttachedClientEvent.UNLOAD);

                this.response = createResponseAttachedClientEventUnload(clientDoc);
            }
            else if (clientDoc.getPhase() == ItsNatAttachedClientEvent.UNLOAD)
            {
                // Por si acaso, NO deber�a ocurrir.
                this.response = new ResponseEventDoNothingImpl(this);
            }
            else
            {
                // Es un evento de refresco pero mientras se enviaba es posible
                // que el documento observado (el propietario) se haya invalidado aunque no haya sido garbage collected,
                // la invalidaci�n supone parar el timer o CometNotifier autom�ticamente aunque
                // el c�digo JavaScript no haya llegado al cliente todav�a (es en este evento cuando llegar�).
                // no habr� por tanto m�s refrescos, pero esto lo puede saber el c�digo del usuario pues
                // puede preguntar si el documento es invalid
                clientDoc.setPhase(ItsNatAttachedClientEvent.REFRESH); // No hace falta (ya tiene ese valor) pero por si acaso

                this.response = createResponseAttachedClientEventRefresh(clientDoc); // Hay un caso que devuelve null
            }

            if (response != null)
                response.process();
        }
        finally
        {
            if (clientDoc.isInvalid() || (clientDoc.getPhase() == ItsNatAttachedClientEvent.UNLOAD))
            {
                // Esto lo ponemos aqu� por si ha ocurrido una excepci�n y antes invalid� el cliente o se estaba procesando el caso unload
                clientDoc.invalidateAndUnregister(); // Invalida si no lo est�
            }

            unbindRequestFromDocument();
        }
    }

    public abstract ResponseAttachedClientEventImpl createResponseAttachedClientEventUnload(ClientDocumentAttachedClientImpl clientDoc);
    public abstract ResponseAttachedClientEventImpl createResponseAttachedClientEventRefresh(ClientDocumentAttachedClientImpl clientDoc);

    public void processLostSessionOrClientUnloading()
    {
        // No hacer nada
        ItsNatSessionImpl session = getItsNatSession();
        ClientDocumentAttachedClientErrorImpl clientDoc = new ClientDocumentAttachedClientErrorImpl(session);
        // Da igual la fase
        bindClientToRequest(clientDoc,false);  // El documento es nulo, no se vincula por tanto

        this.response = new ResponseEventDoNothingImpl(this);
        response.process();
    }

    public void processLostSessionError(String sessionId,String sessionToken)
    {
        // Puede darse por ejemplo en el caso de control remoto con timer con un lapso grande
        // y mientras tanto se ha reiniciado el servidor

        ItsNatSessionImpl session = getItsNatSession();
        ClientDocumentAttachedClientErrorImpl clientDoc = new ClientDocumentAttachedClientErrorImpl(session);
        clientDoc.setPhase(ItsNatAttachedClientEvent.REFRESH);

        bindClientToRequest(clientDoc,false);  // El documento es nulo, no se vincula por tanto

        this.response = new ResponseAttachedClientErrorEventLostSessionImpl(sessionId,sessionToken,this);
        response.process();
    }

    public void processClientDocumentNotFoundError(String clientId)
    {
        // Un caso MUY MUY raro probablemente NO se de nunca. El cliente remoto ha desaparecido del servidor
        // yo creo que no se da nunca pues el cliente remoto s�lo se quita de la sesi�n
        // cuando se invalida el documento o cuando el usuario cierra sale de la p�gina de alguna forma.
        // El caso de documento invalidado se da una oportunidad al c�digo servidor de enterarse
        // por ejemplo en un refresh, el hecho de invalidar el documento supone
        // el env�o de c�digo JavaScript para parar el timer por ejemplo.
        // Podr�a ser el caso de un evento "timer" encolado y enviado quiz�s antes de recibir la orden de parar.
        ItsNatSessionImpl session = getItsNatSession();
        ClientDocumentAttachedClientErrorImpl clientDoc = new ClientDocumentAttachedClientErrorImpl(session);
        clientDoc.setPhase(ItsNatAttachedClientEvent.REFRESH);

        bindClientToRequest(clientDoc,false);  // El documento es nulo, no se vincula por tanto

        this.response = new ResponseAttachedClientErrorEventLostClientDocImpl(this,clientId);
        response.process();
    }
}
