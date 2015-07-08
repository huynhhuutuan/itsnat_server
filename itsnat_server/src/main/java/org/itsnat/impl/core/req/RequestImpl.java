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

package org.itsnat.impl.core.req;

import org.itsnat.impl.core.req.attachsrv.RequestAttachedServerImpl;
import org.itsnat.impl.core.req.script.RequestLoadScriptImpl;
import org.itsnat.impl.core.req.norm.RequestIFrameFileUploadImpl;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.servlet.ItsNatServletRequestImpl;
import org.itsnat.impl.core.servlet.ItsNatSessionImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.doc.ItsNatDocumentImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.req.norm.RequestNormalLoadDocBaseImpl;
import org.itsnat.impl.core.req.attachcli.RequestAttachedClientLoadDocImpl;
import org.itsnat.impl.core.resp.ResponseImpl;

/**
 *
 * @author jmarranz
 */
public abstract class RequestImpl
{
    public static final String ITSNAT_ACTION_EVENT = "event";
    public static final String ITSNAT_ACTION_LOAD_SCRIPT = "load_script";   
    public static final String ITSNAT_ACTION_IFRAME_FILE_UPLOAD = "iframe_file_upload";    
    public static final String ITSNAT_ACTION_ATTACH_CLIENT = "attach_client";   // P�blico y documentado  
    public static final String ITSNAT_ACTION_ATTACH_SERVER = "attach_server";   // P�blico y documentado 
    public static final String ITSNAT_ACTION_ITSNAT_INFO = "itsnat_info";  // P�blico y documentado 
    public static final String ITSNAT_ACTION_EVENT_STATELESS = "event_stateless";   // P�blico  y documentado 
    public static final String ITSNAT_ACTION_EVENT_STATELESS_PHASE_LOAD_DOC = "event_stateless_phase_load_doc";     
    
    protected ItsNatServletRequestImpl itsNatRequest;
    protected ResponseImpl response;
    protected ClientDocumentImpl clientDoc; // Puede ser nulo

    /** Creates a new instance of RequestImpl */
    public RequestImpl(ItsNatServletRequestImpl itsNatRequest)
    {
        this.itsNatRequest = itsNatRequest;
    }

    public static RequestImpl createRequest(String action,ItsNatServletRequestImpl itsNatRequest)
    {
        if (action != null)
        {
            if (action.equals(ITSNAT_ACTION_EVENT))
                return RequestEventStfulImpl.createRequestEventStful(itsNatRequest);
            else if (action.equals(ITSNAT_ACTION_LOAD_SCRIPT))
                return RequestLoadScriptImpl.createRequestLoadScript(itsNatRequest);
            else if (action.equals(ITSNAT_ACTION_IFRAME_FILE_UPLOAD))
                return RequestIFrameFileUploadImpl.createRequestIFrameFileUpload(itsNatRequest);
            else if (action.equals(ITSNAT_ACTION_ATTACH_CLIENT))
                return RequestAttachedClientLoadDocImpl.createRequestAttachedClientLoadDoc(itsNatRequest);
            else if (action.equals(ITSNAT_ACTION_ATTACH_SERVER))
                return RequestAttachedServerImpl.createRequestAttachedServer(itsNatRequest);
            else if (action.equals(ITSNAT_ACTION_ITSNAT_INFO))
                return RequestItsNatInfoImpl.createRequestItsNatInfo(itsNatRequest);
            else if (action.equals(ITSNAT_ACTION_EVENT_STATELESS))
                return RequestEventStatelessImpl.createRequestEventStateless(itsNatRequest);            
            else if (action.equals(ITSNAT_ACTION_EVENT_STATELESS_PHASE_LOAD_DOC))
            {
                String docName = itsNatRequest.getAttrOrParam("itsnat_doc_name");
                if (docName != null)
                    return RequestNormalLoadDocBaseImpl.createRequestNormalLoadDocBase(docName,itsNatRequest,true);                
                else
                    throw new ItsNatException("INTERNAL ERROR");  // Hemos asegurado antes que no es nulo
            }
            else throw new ItsNatException("Unrecognized itsnat_action: \"" + action + "\"");
        }
        else
        {
            String docName = itsNatRequest.getAttrOrParam("itsnat_doc_name");
            if (docName != null)
                return RequestNormalLoadDocBaseImpl.createRequestNormalLoadDocBase(docName,itsNatRequest,false);
            else
                return RequestCustomImpl.createRequestCustom(itsNatRequest);
        }
    }

    public ItsNatSessionImpl getItsNatSession()
    {
        return itsNatRequest.getItsNatSessionImpl();
    }

    public ItsNatServletRequestImpl getItsNatServletRequest()
    {
        return itsNatRequest;
    }

    public ResponseImpl getResponse()
    {
        return response;
    }

    public void setResponse(ResponseImpl response)
    {
        this.response = response;
    }

    public abstract ItsNatStfulDocumentImpl getItsNatStfulDocumentReferrer();

    
    public String getAttrOrParam(String name)
    {
        return itsNatRequest.getAttrOrParam(name);
    }

    public String getAttrOrParamExist(String name)
    {
        return itsNatRequest.getAttrOrParamExist(name);
    }

    public ItsNatDocumentImpl getItsNatDocument()
    {
        if (clientDoc != null) // Pr�cticamente en todos los casos es no nulo pero por si acaso (en el caso de request custom es nulo por ejemplo aunque creo que YA NO)
            return clientDoc.getItsNatDocumentImpl();
        else
            return null;
    }

    public ClientDocumentImpl getClientDocument()
    {
        return clientDoc; // NUNCA es null si se ha hecho el "binding" 
    }

    public void bindClientToRequest(ClientDocumentImpl clientDoc)
    {
        bindClientToRequest(clientDoc,true);
    }

    public void bindClientToRequest(ClientDocumentImpl clientDoc,boolean bindReqToDoc)
    {
        this.clientDoc = clientDoc;
        if (bindReqToDoc) itsNatRequest.bindRequestToDocument(clientDoc.getItsNatDocumentImpl());
    }

    public void unbindRequestFromDocument()
    {
        itsNatRequest.unbindRequestFromDocument();
    }

    public void process(ClientDocumentStfulImpl clientDocStateless)
    {
        try
        {
            processRequest(clientDocStateless);
        }
        finally
        {
            // Post processRequest
            notifyEndOfRequestToSession();

            // Aprovechamos el request para hacer limpieza
            ItsNatSessionImpl itsNatSession = itsNatRequest.getItsNatSessionImpl();
            itsNatSession.invalidateLostResources();
        }
    }


    // Devolver false cuando podamos evitar serializaciones de sesi�n seguidas algunas de ellas in�tiles
    // cuando el request no sea ni un request de carga ni un evento normal
    // y por tanto no se ha ejecutado c�digo del usuario que cambie el objeto sesi�n ItsNat.
    protected abstract boolean isMustNotifyEndOfRequestToSession();

    private void notifyEndOfRequestToSession()
    {
        // La finalidad de esta llamada es que se serialicen en la sesi�n los cambios que hayamos
        // hecho en el ItsNatDocument de trabajo en el caso de session replication capable
        // activado (ej. para GAE)
        if (isMustNotifyEndOfRequestToSession())
        {
            ItsNatSessionImpl itsNatSession = itsNatRequest.getItsNatSessionImpl();
            itsNatSession.endOfRequest();
        }
    }

    public abstract void processRequest(ClientDocumentStfulImpl clientDocStateless);
}
