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

import org.itsnat.impl.core.scriptren.jsren.node.otherns.JSRenderOtherNSTextImpl;
import org.itsnat.core.ItsNatDOMException;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.domutil.DOMUtilHTML;
import org.itsnat.impl.core.scriptren.jsren.node.html.JSRenderHTMLTextImpl;
import org.itsnat.impl.core.dompath.NodeLocationImpl;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.CharacterData;

/**
 *
 * @author jmarranz
 */
public abstract class JSRenderTextImpl extends JSRenderCharacterDataAliveImpl
{
    /**
     * Creates a new instance of JSTextRender
     */
    public JSRenderTextImpl()
    {
    }

    public static JSRenderTextImpl getJSRenderText(Text node,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        if (DOMUtilHTML.isHTMLCharacterData(node))
            return JSRenderHTMLTextImpl.getJSRenderHTMLText(clientDoc.getBrowserWeb());
        else
            return JSRenderOtherNSTextImpl.getJSRenderOtherNSText(node,clientDoc);
    }
   
    public String createNodeCode(Node node,ClientDocumentStfulDelegateImpl clientDoc)
    {
        Text nodeText = (Text)node;
        return dataTextToJS(nodeText,clientDoc);
        // No usamos createTextNode(...) porque los m�todos insertBefore y appendChild est� preparados para recibir directamente la cadena, as� ahorramos c�digo enviado
        // Se redefine en un caso (usando createTextNode)
    }
   
    @Override    
    public String getCharacterDataModifiedCode(CharacterData node,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        // Redefinimos completamente porque los nodos de texto
        // aparte de no ser cacheables necesitan un trato especial pues
        // a veces son filtrados en el cliente.

        // No tenemos la total garant�a de que el nodo de texto en el cliente
        // exista pues a veces el parser del navegador, cuando carga la p�gina
        // desde HTML serializado o via innerHTML, filtra nodos con espacios fines
        // de l�nea etc.
        // Por tanto lo que hacemos es enviar la localizaci�n del nextSibling que tenemos
        // la seguridad que est�, en el cliente si el nodo anterior no es un nodo de texto
        // es que fue filtrado, lo crearemos.
        // De esta manera tambi�n podemos tolerar el caso de nodos de texto con cadena vac�a
        // de forma equivalente a "no existe el nodo"
        Text textNode = (Text)node;
        String value = dataTextToJS(textNode,clientDoc);

        Node parent = textNode.getParentNode();
        Node nextSibling = textNode.getNextSibling(); // Puede ser null
        if ((nextSibling != null)&& (nextSibling.getNodeType() == Node.TEXT_NODE))
            throw new ItsNatDOMException("Two contiguous text nodes, avoid this practice",textNode);
        // El programador tiene la oportunidad de unir ambos nodos de texto (por ejemplo con normalize o eliminado el siguiente y cambiando el valor del que queda)

        NodeLocationImpl parentLoc = clientDoc.getNodeLocation(parent,true);
        NodeLocationImpl nextSiblingLoc = clientDoc.getNodeLocationRelativeToParent(nextSibling);
        // Hay que tener en cuenta que nextSibling puede ser nulo
        return "itsNatDoc.setTextData2(" + parentLoc.toScriptNodeLocation(true) + "," + nextSiblingLoc.toScriptNodeLocation(false) + "," + value + ");\n"; // nextSiblingLoc puede ser una referencia nula
    }

}
