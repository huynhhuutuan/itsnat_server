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



import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.clientdoc.web.SVGWebInfoImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.scriptren.jsren.node.JSRenderElementImpl;
import org.itsnat.impl.core.domutil.NamespaceUtil;
import org.itsnat.impl.core.scriptren.shared.node.CannotInsertAsMarkupCauseImpl;
import org.itsnat.impl.core.scriptren.shared.node.InnerMarkupCodeImpl;
import org.itsnat.impl.core.scriptren.shared.node.InsertAsMarkupInfoImpl;
import org.itsnat.impl.core.template.MarkupTemplateVersionImpl;
import org.itsnat.impl.core.template.web.otherns.ItsNatSVGDocumentTemplateVersionImpl;
import org.itsnat.impl.core.template.web.otherns.ItsNatUnknownDocumentTemplateVersionImpl;
import org.itsnat.impl.core.template.web.otherns.ItsNatXULDocumentTemplateVersionImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author jmarranz
 */
public abstract class JSRenderOtherNSElementImpl extends JSRenderElementImpl
{

    /**
     * Creates a new instance of JSRenderOtherNSElementW3CImpl
     */
    public JSRenderOtherNSElementImpl()
    {
    }

    public static JSRenderOtherNSElementImpl getJSRenderOtherNSElement(Element elem,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        // En el caso de MSIE es el caso de insertar elementos de namespace desconocido
        // en documentos HTML
        if (SVGWebInfoImpl.isSVGNodeProcessedBySVGWebFlash(elem,clientDoc))
            return JSRenderSVGElementSVGWebImpl.SINGLETON;
        else
            return JSRenderOtherNSElementNativeImpl.getJSRenderOtherNSElementNative(clientDoc.getBrowserWeb());
    }

    public boolean isInsertChildNodesAsMarkupCapable(Element parent,MarkupTemplateVersionImpl template)
    {
        // En principio todos los elementos tienen capacidad de insertar nodos hijos como markup
        // a trav�s de nuestro setInnerXML 
        return true;
    }

    public boolean isChildNotValidInsertedAsMarkup(Node childNode,MarkupTemplateVersionImpl template)
    {
        // Para detectar si el nodo puede ser insertado como markup

        if (childNode.getNodeType() == Node.ELEMENT_NODE) 
        {
            // El caso de los elementos <script> es problem�tico pues
            // su simple inserci�n (que se har�a en el cliente via setInnerXML)
            // no asegura que se ejecute el c�digo contenido

            String localName = ((Element)childNode).getLocalName();
            return "script".equals(localName); // Los dem�s Element se puede insertar sin problema 
        }
        
        return false; // Los no Element se puede insertar sin problema 
    }

    @Override
    public String getAppendChildrenCodeAsMarkupSentence(InnerMarkupCodeImpl innerMarkupRender,ClientDocumentStfulDelegateImpl clientDoc)
    {
        // Navegadores W3C:

        // Usamos DOMRender que es mucho m�s eficaz que
        // con DOM en JavaScript pues JavaScript es muy lento
        // y via DOMRender se procesa con el parser C/C++ del navegador
        // http://www.w3schools.com/dom/dom_parser.asp
        // La alternativa a DOMRender es Range.createContextualFragment pero por ejemplo
        // no es soportado por Safari en puro SVG
        // http://developer.mozilla.org/en/docs/DOM:range.createContextualFragment
        // http://www.quirksmode.org/bugreports/archives/2004/11/innerhtml_in_xh.html (ver comentarios)
        // Otra alternativa cross-platform es usar XMLHttpRequest localmente:
        // http://web-graphics.com/mtarchive/001606.php

        // MSIE:
        // Usamos loadXML y una simulaci�n de importNode
        //   http://msdn.microsoft.com/en-us/library/ms754585%28VS.85%29.aspx
        //   http://social.msdn.microsoft.com/Forums/en-US/jscript/thread/fc6618f1-5130-47de-9840-c66af68d6c85
        //   http://www.alistapart.com/articles/crossbrowserscripting
       
        String parentNodeLocator = innerMarkupRender.getParentNodeLocator();
        String valueJS = toTransportableStringLiteral(innerMarkupRender.getInnerMarkup(),clientDoc.getBrowser());
        if (innerMarkupRender.isUseNodeLocation())
            return "itsNatDoc.setInnerXML2(" + parentNodeLocator + "," + valueJS + ");\n";
        else // Es directamente una variable
            return "itsNatDoc.setInnerXML(" + parentNodeLocator + "," + valueJS + ");\n";
    }

    @Override
    public InnerMarkupCodeImpl appendChildrenCodeAsMarkup(String parentVarName,Element parentNode,String childrenCode,ClientDocumentStfulDelegateImpl clientDoc)
    {
        childrenCode = buildOtherNSDocument(parentNode,childrenCode,clientDoc);

        return super.appendChildrenCodeAsMarkup(parentVarName, parentNode, childrenCode, clientDoc);
    }

    private String buildOtherNSDocument(Element parent,String body,ClientDocumentStfulDelegateImpl clientDoc)
    {
        // Tratamos de simular en un nuevo documento el contexto del elemento padre
        // y los hijos respecto al namespace, el nodo root de dicho documento
        // ser� similar al elemento parent respecto al prefijo, namespace etc.
        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        String encoding = getEncoding(itsNatDoc);
        String prefix = parent.getPrefix();
        String defaultNS = null;
        if (prefix != null) defaultNS = itsNatDoc.getNamespace(); // Si prefix es no nulo el namespace por defecto lo impone el elemento. Hay que tener en cuenta que el namespace por defecto, salvo redefinici�n en el elemento via xmlns="...", lo normal es que sea el namespace del documento contenedor.

        String namespace = parent.getNamespaceURI();
        if (namespace != null)
        {
            if (NamespaceUtil.isSVGNamespace(namespace))
                return ItsNatSVGDocumentTemplateVersionImpl.wrapBodyAsDocument(body,encoding,prefix,defaultNS);
            else if (NamespaceUtil.isXULNamespace(namespace))
                return ItsNatXULDocumentTemplateVersionImpl.wrapBodyAsDocument(body,encoding,prefix,defaultNS);
            else
                return ItsNatUnknownDocumentTemplateVersionImpl.wrapBodyAsDocument(namespace,body,encoding,prefix,defaultNS);
        }
        else  // Seguramente el elemento se cre� con Document.createElement()
            return ItsNatUnknownDocumentTemplateVersionImpl.wrapBodyAsDocument(null,body,encoding,prefix,defaultNS);
    }

    public String getEncoding(ItsNatStfulDocumentImpl itsNatDoc)
    {
        return itsNatDoc.getEncoding();
    }

}
