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

package org.itsnat.impl.core.scriptren.jsren.node.otherns;

import org.itsnat.impl.core.browser.web.BrowserMSIEOld;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.web.SVGWebInfoImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * No se necesita mucho m�s en esta clase pues los m�todos removeChild,appendChild
 * e insertBefore de ItsNat est� adaptados autom�ticamente para detectar
 * el caso de SVG root y eliminar, a�adir e insertar a trav�s de los m�todos
 * del objeto window.svgweb.
 * 
 * @author jmarranz
 */
public class JSRenderSVGElementSVGWebImpl extends JSRenderOtherNSElementImpl
{
    public static final JSRenderSVGElementSVGWebImpl SINGLETON = new JSRenderSVGElementSVGWebImpl();

    /**
     * Creates a new instance of JSRenderOtherNSElementW3CImpl
     */
    public JSRenderSVGElementSVGWebImpl()
    {
    }

    @Override
    protected String createElement(Element nodeElem,String tagName,ClientDocumentStfulDelegateImpl clientDoc)
    {
        // Redefinimos totalmente pues es siempre SVG
        String namespace = nodeElem.getNamespaceURI(); // Es namespace SVG necesariamente
        return "itsNatDoc.doc.createElementNS(\"" + namespace + "\",\"" + tagName + "\")";
    }

    @Override    
    public String getCurrentStyleObject(String itsNatDocVar, String elemName,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        // No se usa nunca para SVG (s�lo para elementos HTML)
        // Al parecer SVGWeb aunque intenta simular nodos W3C usa nodos del navegador
        // por lo que en MSIE funciona currentStyle, el problema es que
        // window.getComputedStyle funciona regular (error en FireFox 3.5, null en Chrome 3 ...)
        // en ese caso devolvemos style y ya est�.
        if (clientDoc.getBrowserWeb() instanceof BrowserMSIEOld)
            return elemName + ".currentStyle";
        else
            return elemName + ".style";
    }

    @Override    
    public boolean isInsertedScriptNotExecuted(Element script,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return true;
    }

    @Override    
    public boolean isTextAddedToInsertedScriptNotExecuted(Element script,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        return true;
    }

    @Override
    public boolean isAddChildNodesBeforeNode(Node parent,ClientDocumentStfulDelegateImpl clientDoc)
    {
        // Los elementos insertados inmediatamente despu�s de la inserci�n del nodo SVG root no son correctamente
        // procesados.
        if (SVGWebInfoImpl.isSVGRootElementProcessedBySVGWebFlash((Element)parent,(ClientDocumentStfulDelegateWebImpl)clientDoc))
            return true;

        return super.isAddChildNodesBeforeNode(parent,clientDoc);
    }
}
