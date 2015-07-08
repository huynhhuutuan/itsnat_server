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
package org.itsnat.impl.core.req.attachsrv;

import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedServerImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedServerScriptImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.resp.attachsrv.ResponseAttachedServerLoadDocFormImpl;
import org.itsnat.impl.core.resp.attachsrv.ResponseAttachedServerLoadDocImpl;
import org.itsnat.impl.core.resp.attachsrv.ResponseAttachedServerLoadDocScriptImpl;
import org.itsnat.impl.core.servlet.ItsNatServletRequestImpl;
import org.itsnat.impl.core.servlet.ItsNatSessionImpl;

/**
 *
 * @author jmarranz
 */
public class RequestAttachedServerLoadDocImpl extends RequestAttachedServerImpl
{
    protected String clientId;
    protected ClientDocumentAttachedServerImpl clientDocAttachedServer;

    public RequestAttachedServerLoadDocImpl(String clientId,ItsNatServletRequestImpl itsNatRequest)
    {
        super(itsNatRequest);

        this.clientId = clientId;
    }

    public static RequestAttachedServerLoadDocImpl createRequestAttachedServerLoadDocBase(String clientId,ItsNatServletRequestImpl itsNatRequest)
    {
        return new RequestAttachedServerLoadDocImpl(clientId,itsNatRequest);
    }

    public ItsNatStfulDocumentImpl getItsNatStfulDocumentReferrer()
    {
        // El referrer s�lo tiene sentido en tiempo de carga de una p�gina para que tenga acceso a la anterior
        // en este caso el request se acabar� procesando por un request normal que es que recoger� el referrer
        return null;
    }

    public ClientDocumentAttachedServerImpl getClientDocumentAttachedServer()
    {
        return (ClientDocumentAttachedServerImpl)clientDocAttachedServer;
    }

    public String getClientMarkup()
    {
        return clientDocAttachedServer.getClientMarkup();
    }

    public ResponseAttachedServerLoadDocImpl getResponseAttachedServerLoadDoc()
    {
        return (ResponseAttachedServerLoadDocImpl)response;
    }

    @Override    
    public void processRequest(ClientDocumentStfulImpl clientDocStateless)
    {
        ItsNatSessionImpl session = itsNatRequest.getItsNatSessionImpl();

        this.clientDocAttachedServer = session.getClientDocumentAttachedServersById(clientId);
        if (clientDocAttachedServer == null) throw new ItsNatException("Not found in server"); // MUY raro, las requests son muy seguidas, posiblemente de un cliente malicioso

        try
        {
            // Aunque llamemos a  bindClientToRequest hay que tener en cuenta que
            // posteriormente el proceso normal registrar� de nuevo "el cliente definitivo"
            bindClientToRequest(clientDocAttachedServer,false);

            if (clientDocAttachedServer instanceof ClientDocumentAttachedServerScriptImpl)
                this.response = new ResponseAttachedServerLoadDocScriptImpl(this);
            else
                this.response = new ResponseAttachedServerLoadDocFormImpl(this);

            response.process();
        }
        finally
        {
            clientDocAttachedServer.setInvalid(); // Ha terminado su misi�n, no se necesita el registro m�s, pues es un cliente provisional para el proceso de carga
        }
    }
}
