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

import org.itsnat.impl.core.scriptren.shared.node.InsertAsMarkupInfoImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.CodeListImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.scriptren.shared.node.JSAndBSRenderHasChildrenNodeImpl;
import org.itsnat.impl.core.scriptren.shared.node.RenderHasChildrenNode;
import org.w3c.dom.Node;

/**
 *
 * @author jmarranz
 */
public abstract class JSRenderHasChildrenNodeImpl extends JSRenderNotAttrOrAbstractViewNodeImpl implements RenderHasChildrenNode
{

    /** Creates a new instance of JSNotChildrenNodeRenderImpl */
    public JSRenderHasChildrenNodeImpl()
    {
    }

    public boolean isCreateComplete(Node node)
    {
        return !node.hasAttributes() && !node.hasChildNodes();
    }

    @Override
    public String getAppendCompleteChildNode(String parentVarName,Node newNode,String newNodeCode,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return JSAndBSRenderHasChildrenNodeImpl.getAppendCompleteChildNode(parentVarName, newNode, newNodeCode, clientDoc);
    }
    
    public Object getAppendNewNodeCode(Node parent,Node newNode,String parentVarName,InsertAsMarkupInfoImpl insertMarkupInfo,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return JSAndBSRenderHasChildrenNodeImpl.getAppendNewNodeCode(parent, newNode, parentVarName, insertMarkupInfo,clientDoc,this);  
    }

    public Object getInsertNewNodeCode(Node newNode,InsertAsMarkupInfoImpl insertMarkupInfo,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return JSAndBSRenderHasChildrenNodeImpl.getInsertNewNodeCode(newNode,insertMarkupInfo,clientDoc,this);
    }

    public Object appendChildNodes(Node parent, String parentVarName,boolean beforeParent,InsertAsMarkupInfoImpl insertMarkupInfo,ClientDocumentStfulDelegateImpl clientDoc)
    {
        // S�lo es llamado si hay alg�n hijo

        // Ojo, si el nodo a serializar de esta forma tiene
        // nodos procedientes de fragmentos cacheados no se pueden
        // tratar aqu�, la soluci�n es inhabilitar el cacheado
        // en el fragmento a insertar o bien declarar itsnat:nocache="true"
        // en el nodo problem�tico que se cachea autom�ticamente
        // En el caso de inserci�n de fragmentos XML en documentos XML
        // no es problema pues a d�a de hoy no admite eventos y este c�digo
        // es llamado ante mutation events los cuales no est�n activados
        // en la manipulaci�n de un documento todav�a no cargado

        CodeListImpl code = new CodeListImpl();

        if (parent.hasChildNodes())
        {
            Node child = parent.getFirstChild();
            while(child != null)
            {
                JSRenderNotAttrOrAbstractViewNodeImpl childRender = JSRenderNotAttrOrAbstractViewNodeImpl.getJSRenderNotAttrOrAbstractViewNode(child,(ClientDocumentStfulDelegateWebImpl)clientDoc);
                code.add( childRender.getAppendNewNodeCode(parent,child,parentVarName,insertMarkupInfo,clientDoc) );

                child = child.getNextSibling();
            }
        }
        
        return code;
    }
}
