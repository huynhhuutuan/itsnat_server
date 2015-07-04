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

package org.itsnat.impl.core.dompath;

import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Esta clase es �til para buscar un elemento en un documento
 * padre desde un documento hijo cargado via iframe etc
 * sin recurrir al API ItsNat que podr�a no estar cargada.
 *
 * @author jmarranz
 */
public class SimpleElementPathResolver
{
    public static String getMethodNameGetElementFromPath()
    {
        return "itsNatGetElementFromPath";
    }

    public static String bindGetElementFromPath(String docVarName,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        String method = getMethodNameGetElementFromPath();

        StringBuilder code = new StringBuilder();
        code.append( "var func = function(path)" );
        code.append( "{" );
        code.append( "  var node = this;" );  // this es "document"
        code.append( "  for(var i = 0; i < path.length; i++)" );
        code.append( "  {" );
        code.append( "    var pos = path[i];" );
        code.append( "    var childNode = null;" );
        code.append( "    var childList = node.childNodes;" );
        code.append( "    var currPos = 0;" );
        code.append( "    for(var j = 0; j < childList.length; j++)" );
        code.append( "    {" );
        code.append( "      var currNode = childList.item(j);" );
        code.append( "      if (currNode.nodeType != 1) continue;" ); // 1 == Node.ELEMENT
        code.append( "      if (currPos == pos) { childNode = currNode; break; }" );
        code.append( "      currPos++;" );
        code.append( "    }" );
        code.append( "    node = childNode;" );
        code.append( "  }" );
        code.append( "  return node;" );
        code.append( "};" );

        code.append( docVarName + "." + method + " = func;\n" );
        code.append( docVarName + "." + method + "_src = func.toString();\n" );
        clientDoc.bindClientMethod(method);

        return code.toString();
    }

    public static String callGetElementFromPath(String resElemVarName,String docVarName,String pathParam,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        // No se cual es la raz�n pero la comunicaci�n entre ASV e Internet Explorer
        // es TERRIBLE. Necesitamos "inyectar" una funci�n al documento padre (MSIE)
        // para no tener que hacerlo para cada SVG cargado via IFRAME, la primera
        // vez funciona pero al acceder a la funci�n en el padre desde otro documento SVG,
        // por lo menos si el primer documento SVG se ha descargado.
        // La raz�n seguramente es que el objeto funci�n se cre� en el SVG y
        // al perderse el documento SVG se pierde la funci�n aunque err�neamente
        // exista un puntero a funci�n en el documento padre.
        // La t�cnica usada aqu� es asociar al padre adem�s del puntero a funci�n
        // el c�digo fuente del m�todo tal que en nuevos documentos SVG
        // reconstruimos el objeto funci�n a partir del c�digo fuente el cual no
        // tiene problema de "perderse".

        StringBuilder code = new StringBuilder();

        String method = getMethodNameGetElementFromPath();
        if (!clientDoc.isClientMethodBounded(method))
            code.append(bindGetElementFromPath(docVarName,clientDoc));
        else
        {
            code.append( "eval(\"var func = \" + " + docVarName + "." + method + "_src);\n" );
            code.append( docVarName + "." + method + " = func;\n" );
        }

        code.append( resElemVarName + " = " + docVarName + "." + method + "(" + pathParam + ");\n" );
        return code.toString();
    }

    public static Element getElementFromPath(Document doc,int[] path)
    {
        // NO se usa, est� como patr�n Java del mismo c�digo en JavaScript
        Node node = doc;
        for(int i = 0; i < path.length; i++)
        {
            int pos = path[i];
            Node childNode = null;
            NodeList childList = node.getChildNodes();
            int currPos = 0;
            int len = childList.getLength();
            for(int j = 0; j < len; i++)
            {
                Node currNode = childList.item(j);
                if (currNode.getNodeType() != Node.ELEMENT_NODE) continue;
                if (currPos == pos) { childNode = (Element)currNode; break; }
                currPos++;
            }

            node = childNode;
        }
        return (Element)node;
    }

    private static int getElementDeep(Element elem)
    {
        Document topParent = elem.getOwnerDocument();
        Node node = elem;
        int i = 0;
        while(node != topParent)
        {
            i++;
            node = node.getParentNode();
        }
        return i; // Debe ser i > 0
    }

    private static int getElementChildPosition(Element elem)
    {
        Node parent = elem.getParentNode();

        if (parent.hasChildNodes())
        {
            int pos = 0;
            Node nodeItem = parent.getFirstChild();
            while(nodeItem != null)
            {
                if (nodeItem.getNodeType() == Node.ELEMENT_NODE)
                {
                    if (nodeItem == elem)
                        return pos;
                    pos++;
                }
                
                nodeItem = nodeItem.getNextSibling();
            }
        }
  
        return -1;
    }

    private static int[] getPathFromElement(Element elem)
    {
        // El path del documentElement ser� [0]
        int len = getElementDeep(elem);
        int[] path = new int[len];
        Node node = elem;
        for(int i = len - 1; i >= 0; i--)
        {
            int pos = getElementChildPosition((Element)node);
            path[i] = pos;
            node = node.getParentNode();
        }
        return path;
    }

    public static String getPathFromElementAsScript(Element elem)
    {
        int[] path = getPathFromElement(elem);
        StringBuilder code = new StringBuilder();
        code.append("[");
        for(int i = 0; i < path.length; i++)
        {
            if (i != 0) code.append(",");
            code.append(path[i]);
        }
        code.append("]");
        return code.toString();
    }
}
