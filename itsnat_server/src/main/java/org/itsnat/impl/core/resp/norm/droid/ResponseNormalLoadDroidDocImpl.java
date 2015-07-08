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

package org.itsnat.impl.core.resp.norm.droid;

import javax.servlet.http.HttpServletResponse;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.listener.dom.domstd.OnUnloadListenerImpl;
import org.itsnat.impl.core.req.norm.RequestNormalLoadDocImpl;
import org.itsnat.impl.core.resp.norm.ResponseNormalLoadStfulDocImpl;
import org.itsnat.impl.core.servlet.ItsNatSessionImpl;
import org.itsnat.impl.core.servlet.http.ItsNatHttpServletResponseImpl;
import org.itsnat.impl.core.template.ItsNatStfulDocumentTemplateNormalDroidImpl;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public class ResponseNormalLoadDroidDocImpl extends ResponseNormalLoadStfulDocImpl
{
    /**
     * Creates a new instance of ResponseNormalLoadDroidDocImpl
     * @param request
     */
    public ResponseNormalLoadDroidDocImpl(RequestNormalLoadDocImpl request)
    {
        super(request);
    }

    @Override
    public void dispatchRequestListeners()
    {
        // Caso de carga del documento por primera vez, el documento est� reci�n creado

        super.dispatchRequestListeners(); // En el m�todo base en el caso de referrer se procesar� el anterior antes de ser substituido por el actual documento
        
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();         
        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();          
        if (isReferrerEnabled())
        {
            // No nos complicamos la vida con listeners load etc                
            ItsNatSessionImpl itsNatSession = clientDoc.getItsNatSessionImpl();
            itsNatSession.getReferrer().pushItsNatStfulDocument(itsNatDoc);            
        }   

        Element rootElem = itsNatDoc.getDocument().getDocumentElement(); // En SVG es tambi�n el elemento root, es m�s "natural" que forzar el uso de Window 
        clientDoc.addEventListener((EventTarget)rootElem,"unload",OnUnloadListenerImpl.SINGLETON,false, clientDoc.getCommMode());        
    }

    @Override
    protected void prepareResponse()
    {
        super.prepareResponse();
        
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();         
        ItsNatStfulDocumentTemplateNormalDroidImpl template = (ItsNatStfulDocumentTemplateNormalDroidImpl)itsNatDoc.getItsNatDocumentTemplateImpl();
        
        HttpServletResponse response = ((ItsNatHttpServletResponseImpl)getItsNatServletResponse()).getHttpServletResponse();
        response.addHeader("ItsNat-bitmapDensityReference","" + template.getBitmapDensityReference()); 
    }    
}
