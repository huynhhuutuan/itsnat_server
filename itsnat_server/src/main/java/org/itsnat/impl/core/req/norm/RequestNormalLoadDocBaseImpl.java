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

package org.itsnat.impl.core.req.norm;

import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.req.ContainsItsNatStfulDocumentReferrer;
import org.itsnat.impl.core.servlet.ItsNatServletRequestImpl;
import org.itsnat.impl.core.servlet.ItsNatServletImpl;
import org.itsnat.impl.core.req.RequestLoadDocImpl;
import org.itsnat.impl.core.resp.norm.ResponseNormal;
import org.itsnat.impl.core.resp.norm.ResponseNormalLoadDocBaseImpl;
import org.itsnat.impl.core.template.ItsNatStfulDocumentTemplateAttachedServerImpl;
import org.itsnat.impl.core.template.ItsNatDocumentTemplateImpl;

/**
 *
 * @author jmarranz
 */
public abstract class RequestNormalLoadDocBaseImpl extends RequestLoadDocImpl 
        implements RequestNormal,ContainsItsNatStfulDocumentReferrer
{
    protected ItsNatStfulDocumentImpl itsNatDocReferrer;
    protected boolean stateless;
    
    /**
     * Creates a new instance of RequestNormalLoadDocImpl
     */
    public RequestNormalLoadDocBaseImpl(ItsNatServletRequestImpl itsNatRequest,boolean stateless)
    {
        super(itsNatRequest);
        
        this.stateless = stateless;
    }

    public static RequestNormalLoadDocBaseImpl createRequestNormalLoadDocBase(String docName,ItsNatServletRequestImpl itsNatRequest,boolean stateless)
    {
        // Tenemos la seguridad de que docName no es nulo, sino no llegar�amos aqu�

        ItsNatServletImpl itsNatServlet = itsNatRequest.getItsNatServletImpl();
        ItsNatDocumentTemplateImpl docTemplate = itsNatServlet.getItsNatDocumentTemplateImpl(docName);

        if (docTemplate == null)
            return new RequestNormalLoadDocNotFoundImpl(docName,itsNatRequest,stateless);
        else
        {
            // Provocamos una excepci�n sin m�s contemplaciones pues se detectar�
            // en tiempo de desarrollo o por un intento de violaci�n de seguridad
            if (docTemplate instanceof ItsNatStfulDocumentTemplateAttachedServerImpl)
                throw new ItsNatException("Document/page " + docName + " is of type attached server, not valid in this context",itsNatRequest);
            return new RequestNormalLoadDocDefaultImpl(docTemplate,itsNatRequest,stateless);
        }
    }

    public boolean isStateless()
    {
        return stateless;
    }
    
    public ItsNatStfulDocumentImpl getItsNatStfulDocumentReferrer()
    {
        return itsNatDocReferrer;
    }

    public void setItsNatStfulDocumentReferrer(ItsNatStfulDocumentImpl itsNatDocReferrer)
    {
        this.itsNatDocReferrer = itsNatDocReferrer;
    }

    public ResponseNormal getResponseNormal()
    {
        return (ResponseNormal)response;
    }

    public ResponseNormalLoadDocBaseImpl getResponseNormalLoadDocBase()
    {
        return (ResponseNormalLoadDocBaseImpl)response;
    }

    protected boolean isMustNotifyEndOfRequestToSession()
    {
        if (isStateless())
            return false;
        
        // Devolvemos true porque incluso en el caso de documento no encontrado
        // se cambia el referrer de la sesi�n.
        return true;
    }        
}
