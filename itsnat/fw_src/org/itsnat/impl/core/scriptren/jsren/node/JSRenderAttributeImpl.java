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

package org.itsnat.impl.core.scriptren.jsren.node;

import org.itsnat.impl.core.scriptren.shared.node.NodeScriptRefImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulOwnerImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedClientImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.doc.BoundElementDocContainerImpl;
import org.itsnat.impl.core.doc.ElementDocContainerWrapperImpl;
import org.itsnat.impl.core.domimpl.ElementDocContainer;
import org.itsnat.impl.core.domutil.DOMUtilHTML;
import org.itsnat.impl.core.scriptren.jsren.node.html.JSRenderHTMLAttributeImpl;
import org.itsnat.impl.core.scriptren.jsren.node.otherns.JSRenderOtherNSAttributeImpl;
import org.itsnat.impl.core.dompath.NodeLocationImpl;
import org.itsnat.impl.core.scriptren.shared.node.RenderAttribute;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 *
 * @author jmarranz
 */
public abstract class JSRenderAttributeImpl extends JSRenderNodeImpl implements RenderAttribute
{
    /** Creates a new instance of JSRenderAttributeImpl */
    public JSRenderAttributeImpl()
    {
    }

    public static JSRenderAttributeImpl getJSRenderAttribute(String attrNamespaceURI,Element elem,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        if (DOMUtilHTML.isHTMLAttribute(attrNamespaceURI,elem))
            return JSRenderHTMLAttributeImpl.getJSRenderHTMLAttribute(clientDoc.getBrowserWeb());
        else // El atributo puede estar en un documento (X)HTML pero no es HTML o bien es XHTML pero en un documento no XHTML
            return JSRenderOtherNSAttributeImpl.getJSRenderOtherNSAttribute(elem,clientDoc);
    }

    public static JSRenderAttributeImpl getJSRenderAttribute(Attr attr,Element elem,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        return getJSRenderAttribute(attr.getNamespaceURI(),elem,clientDoc);
    }
    
    public abstract boolean isIgnored(Attr attr,Element elem);

    public String setAttributeCode(Attr attr,Element elem,boolean newElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        if (isIgnored(attr,elem))
            return "";
        String attrName = attr.getName();
        String jsValue = toJSAttrValue(attr,elem,newElem,clientDoc);
        return setAttributeCode(attr,attrName,jsValue,elem,newElem,clientDoc);
    }

    public String setAttributeCode(Attr attr,Element elem,String elemVarName,boolean newElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        if (isIgnored(attr,elem))
            return "";
        String attrName = attr.getName();
        String jsValue = toJSAttrValue(attr,elem,newElem,clientDoc);
        return setAttributeCode(attr,attrName,jsValue,elem,elemVarName,newElem,clientDoc);
    }

    public String removeAttributeCode(Attr attr,Element elem,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        if (isIgnored(attr,elem))
            return "";
        String attrName = attr.getName();
        return removeAttributeCode(attr,attrName,elem,clientDoc);
    }

    protected String toJSAttrValue(String value,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return toTransportableStringLiteral(value,clientDoc.getBrowser());
    }

    protected String getAttrValue(Attr attr,Element elem,boolean newElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        if (!newElem)  // En inserci�n (newElem true) el proceso ya se hace al insertar el elemento (recuerda que el elemento puede insertarse via innerHTML/XML y no pasar por aqu�)
        {
            ElementDocContainer elemDocCont = ElementDocContainerWrapperImpl.getElementDocContainerIfURLAttr(attr,elem);
            if (elemDocCont != null)
            {
                BoundElementDocContainerImpl bindInfo = elemDocCont.getElementDocContainerWrapper().getBoundElementDocContainer();
                if (bindInfo != null) // Si bindInfo es null es que el nuevo URL no cumple el formato esperado y no pudo registrarse
                {
                    ClientDocumentStfulImpl clientDocParent = clientDoc.getClientDocumentStful();
                    
                    String value;
                    if (clientDocParent instanceof ClientDocumentStfulOwnerImpl)
                        value = bindInfo.generateURLForClientOwner((ClientDocumentStfulOwnerImpl)clientDocParent);
                    else // attached
                        value = bindInfo.generateURLForClientAttached((ClientDocumentAttachedClientImpl)clientDocParent);

                    return value;
                }
            }
        }

        return attr.getValue();
    }

    protected String toJSAttrValue(Attr attr,Element elem,boolean newElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        // Es llamado �nicamente por los dos setAttributeCode(...)
        // y s�lo una vez.
        // Se redefine en clases inferiores
        String value = getAttrValue(attr,elem,newElem,clientDoc);
        return toJSAttrValue(value,clientDoc);
    }

    protected String setAttributeCode(Attr attr,String attrName,String jsValue,Element elem,boolean newElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        if (newElem)
            return setAttributeOnlyCode(attr,attrName,jsValue,elem,newElem,clientDoc);
        else
        {
            PropertyImpl prop = JSRenderPropertyImpl.getProperty(elem,attrName,clientDoc.getBrowser());
            if (prop != null)
                return setAttributeWithProperty(attr,attrName,jsValue,elem,newElem,prop,clientDoc);
            else
                return setAttributeOnlyCode(attr,attrName,jsValue,elem,newElem,clientDoc);
        }
    }

    protected String setAttributeCode(Attr attr,String attrName,String jsValue,Element elem,String elemVarName,boolean newElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        if (newElem)
            return setAttributeOnlyCode(attr,attrName,jsValue,elem,elemVarName,newElem,clientDoc);
        else
        {
            PropertyImpl prop = JSRenderPropertyImpl.getProperty(elem,attrName,clientDoc.getBrowser());
            if (prop != null)
                return setAttributeWithProperty(attr,attrName,jsValue,elem,elemVarName,newElem,prop,clientDoc);
            else
                return setAttributeOnlyCode(attr,attrName,jsValue,elem,elemVarName,newElem,clientDoc);
        }
    }

    protected String setAttributeOnlyCode(Attr attr,String attrName,String jsValue,Element elem,boolean newElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        NodeLocationImpl nodeLoc = clientDoc.getNodeLocation(elem,true);
        return setAttributeOnlyCode(attr,attrName,jsValue,new NodeScriptRefImpl(nodeLoc),newElem,clientDoc);
    }

    protected String setAttributeOnlyCode(Attr attr,String attrName,String jsValue,Element elem,String elemVarName,boolean newElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return setAttributeOnlyCode(attr,attrName,jsValue,new NodeScriptRefImpl(elemVarName,clientDoc),newElem,clientDoc);
    }

    public String setAttributeOnlyCode(Attr attr,String attrName,String jsValue,NodeScriptRefImpl nodeRef,boolean newElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        if (nodeRef.getNodeRef() instanceof NodeLocationImpl)
        {
            NodeLocationImpl nodeLoc = (NodeLocationImpl)nodeRef.getNodeRef();
            return "itsNatDoc.setAttribute2(" + nodeLoc.toScriptNodeLocation(true) + ",\"" + attrName + "\"," + jsValue + ");\n";
        }
        else
        {
            String elemVarName = (String)nodeRef.getNodeRef();
            return "itsNatDoc.setAttribute(" + elemVarName + ",\"" + attrName + "\"," + jsValue + ");\n";
        }
    }    
     
    protected String removeAttributeCode(Attr attr,String attrName,Element elem,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        PropertyImpl prop = JSRenderPropertyImpl.getProperty(elem,attrName,clientDoc.getBrowserWeb());
        if (prop != null)
            return removeAttributeWithProperty(attr,attrName,elem,prop,clientDoc);
        else
            return removeAttributeOnlyCode(attr,attrName,elem,clientDoc);
    }

    protected String removeAttributeOnlyCode(Attr attr,String attrName,Element elem,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        NodeLocationImpl nodeLoc = clientDoc.getNodeLocation(elem,true);
        return removeAttributeOnlyCode(attr,attrName,elem,new NodeScriptRefImpl(nodeLoc),clientDoc);
    }

    protected String removeAttributeOnlyCode(Attr attr,String attrName,Element elem,NodeScriptRefImpl nodeRef,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        if (nodeRef.getNodeRef() instanceof NodeLocationImpl)
        {
            NodeLocationImpl nodeLoc = (NodeLocationImpl)nodeRef.getNodeRef();
            return "itsNatDoc.removeAttribute2(" + nodeLoc.toScriptNodeLocation(true) + ",\"" + attrName + "\");\n";
        }
        else
        {
            String elemVarName = (String)nodeRef.getNodeRef();
            return "itsNatDoc.removeAttribute(" + elemVarName + ",\"" + attrName + "\");\n";
        }
    }

    protected String removeAttributeWithProperty(Attr attr,String attrName,Element elem,PropertyImpl prop,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        StringBuilder code = new StringBuilder();
        code.append( "var elem = " + clientDoc.getNodeReference(elem,true,true) + ";\n" );
        code.append( removeAttributeWithProperty(attr,attrName,elem,"elem",prop,clientDoc) );
        return code.toString();
    }

    protected String removeAttributeWithProperty(Attr attr,String attrName,Element elem,String elemVarName,PropertyImpl prop,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        StringBuilder code = new StringBuilder();
        JSRenderPropertyImpl render = prop.getJSRenderProperty();
        code.append( render.renderRemoveProperty(prop,elem,elemVarName,clientDoc) );

        if (isRenderAttributeAlongsideProperty(attrName,elem))
        {
            // En el iPhone DEBE ir despu�s de la definici�n de la propiedad
            // de otra manera se l�a (al menos en tiempo de carga de la p�gina)
            code.append( removeAttributeOnlyCode(attr,attrName,elem,new NodeScriptRefImpl(elemVarName,clientDoc),clientDoc) );
        }

        return code.toString();
    }

    protected String setAttributeWithProperty(Attr attr,String attrName,String jsValue,Element elem,boolean newElem,PropertyImpl prop,ClientDocumentStfulDelegateImpl clientDoc)
    {
        StringBuilder code = new StringBuilder();
        code.append( "var elem = " + clientDoc.getNodeReference(elem,true,true) + ";\n" );
        code.append( setAttributeWithProperty(attr,attrName,jsValue,elem,"elem",newElem,prop,clientDoc) );
        return code.toString();
    }

    protected String setAttributeWithProperty(Attr attr,String attrName,String jsValue,Element elem,String elemVarName,boolean newElem,PropertyImpl prop,ClientDocumentStfulDelegateImpl clientDoc)
    {
        if (isRenderAttributeAlongsideProperty(attrName,elem))
        {
            StringBuilder code = new StringBuilder();
            code.append( "var value = " + jsValue + ";\n" );
            code.append( setAttributeAndProperty(attr,attrName,"value",elem,elemVarName,newElem,prop,clientDoc) );
            return code.toString();
        }
        else
        {
            return renderSetProperty(attr,jsValue,elem,elemVarName,prop,clientDoc);
        }
    }

    protected String setAttributeAndProperty(Attr attr,String attrName,String valueJS,Element elem,String elemVarName,boolean newElem,PropertyImpl prop,ClientDocumentStfulDelegateImpl clientDoc)
    {
        StringBuilder code = new StringBuilder();

        code.append( setAttributeOnlyCode(attr,attrName,valueJS,new NodeScriptRefImpl(elemVarName,clientDoc),newElem,clientDoc) );
        code.append( renderSetProperty(attr,valueJS,elem,elemVarName,prop,clientDoc) );

        return code.toString();
    }

    public String renderSetProperty(Attr attr,String valueJS,Element elem,String elemVarName,PropertyImpl prop,ClientDocumentStfulDelegateImpl clientDoc)
    {
        String value = attr.getValue();
        JSRenderPropertyImpl render = prop.getJSRenderProperty();
        return render.renderSetProperty(prop,elem,elemVarName,valueJS,value,clientDoc);
    }

    protected abstract boolean isRenderAttributeAlongsideProperty(String attrName,Element ele);

}
