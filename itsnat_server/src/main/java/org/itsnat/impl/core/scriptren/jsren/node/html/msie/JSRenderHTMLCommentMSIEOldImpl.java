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

package org.itsnat.impl.core.scriptren.jsren.node.html.msie;

import org.itsnat.impl.core.browser.web.BrowserMSIEOld;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.scriptren.jsren.node.JSRenderCommentImpl;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLAppletElement;
import org.w3c.dom.html.HTMLObjectElement;

/**
 *
 * @author jmarranz
 */
public class JSRenderHTMLCommentMSIEOldImpl extends JSRenderCommentImpl
{
    public static final JSRenderHTMLCommentMSIEOldImpl SINGLETON = new JSRenderHTMLCommentMSIEOldImpl();
    
    /** Creates a new instance of JSRenderHTMLCommentMSIEOldImpl */
    public JSRenderHTMLCommentMSIEOldImpl()
    {
    }

    public static JSRenderHTMLCommentMSIEOldImpl getJSRenderHTMLCommentMSIEOld(BrowserMSIEOld browser)
    {
        return JSRenderHTMLCommentMSIEOldImpl.SINGLETON;
    }
    
    @Override
    public String getAppendCompleteChildNode(Node parent,Node newNode,String parentVarName,ClientDocumentStfulDelegateImpl clientDoc)
    {
        if (parent instanceof HTMLObjectElement)
            return "";  // <object> no tolera la inserci�n de nodos de texto hijo aunque sean espacios, ni antes ni despu�s de haberse insertado el <object> en el �rbol
        else if (parent instanceof HTMLAppletElement)
            return "";  // <applet> tampoco tolera la inserci�n de nodos de texto hijo aunque sean espacios, ni antes ni despu�s de haberse insertado el <applet> en el �rbol
        else
            return super.getAppendCompleteChildNode(parent,newNode,parentVarName,clientDoc);
    }

    @Override
    public String getInsertCompleteNodeCode(Node newNode,ClientDocumentStfulDelegateImpl clientDoc)
    {
        // El nodo de texto de <script> y <style> es el �nico hijo posible y
        // necesitan t�cnicas espec�ficas
        Node parent = newNode.getParentNode();
        if (parent instanceof HTMLObjectElement)
            return "";  // <object> no tolera la inserci�n de nodos de texto hijo aunque sean espacios, ni antes ni despu�s de haberse insertado el <object> en el �rbol
        else if (parent instanceof HTMLAppletElement)
            return "";  // <applet> tampoco tolera la inserci�n de nodos de texto hijo aunque sean espacios, ni antes ni despu�s de haberse insertado el <applet> en el �rbol
        else
            return super.getInsertCompleteNodeCode(newNode,clientDoc);
    }

    @Override
    public String getRemoveNodeCode(Node removedNode,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        Node parent = removedNode.getParentNode();
        if (parent instanceof HTMLObjectElement)
            return "";  // <object> no tolera la inserci�n de nodos de texto hijo aunque sean espacios, ni antes ni despu�s de haberse insertado el <object> en el �rbol, los que hubiera en carga no se reflejan en el DOM
        else if (parent instanceof HTMLAppletElement)
            return "";  // <applet> tampoco tolera la inserci�n de nodos de texto hijo aunque sean espacios, ni antes ni despu�s de haberse insertado el <applet> en el �rbol, los que hubiera en carga no se reflejan en el DOM
        else
            return super.getRemoveNodeCode(removedNode,clientDoc);
        // En el caso de nodo texto hijo de <object> si no lo encuentra pues no har� nada
    }

    @Override
    public String getCharacterDataModifiedCode(CharacterData node,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        Node parent = node.getParentNode();
        if (parent instanceof HTMLObjectElement)
            return "";  // <object> no tolera la inserci�n de nodos de texto hijo aunque sean espacios, ni antes ni despu�s de haberse insertado el <object> en el �rbol, los que hubiera en carga no se reflejan en el DOM
        else if (parent instanceof HTMLAppletElement)
            return "";  // <applet> tampoco tolera la inserci�n de nodos de texto hijo aunque sean espacios, ni antes ni despu�s de haberse insertado el <applet> en el �rbol, los que hubiera en carga no se reflejan en el DOM
        else
            return super.getCharacterDataModifiedCode(node, clientDoc);
    }    
}
