/*
  ItsNat Java Web Application Framework
  Copyright (C) 2007-2014 Jose Maria Arranz Santamaria, Spanish citizen

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

package org.itsnat.impl.core.scriptren.shared.node;

import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.dompath.NodeLocationImpl;
import org.itsnat.impl.core.domutil.DOMUtilInternal;
import org.itsnat.impl.core.template.ItsNatStfulDocumentTemplateVersionImpl;
import org.itsnat.impl.core.template.MarkupTemplateVersionImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author jmarranz
 */
public class JSAndBSRenderElementImpl extends JSAndBSRenderHasChildrenNodeImpl 
{
    public static CannotInsertAsMarkupCauseImpl canInsertSingleChildNodeAsMarkup(Node newChildNode,ClientDocumentStfulDelegateImpl clientDoc,RenderElement render)
    {
        // Este es un escenario en donde queremos insertar un nuevo nodo pero puede
        // haber antes ya otros previamente insertados, por lo que s�lo podemos
        // insertar como markup si este nodo es el �nico nodo o el �ltimo
        // y disponemos de un InnerMarkupCodeImpl

        Element parent = (Element)newChildNode.getParentNode();
        if (!DOMUtilInternal.isTheOnlyChildNode(newChildNode))
        {
            // Debe ser el �ltimo.
            // De esta manera permitimos "reusar" el innerHTML (o innerXML) en casos
            // por ejemplo de appendChild sucesivos por parte del programador (mismo padre claro)
            if (parent.getLastChild() != newChildNode)
                return new CannotInsertAsMarkupCauseImpl(parent); // No es el �ltimo
            // Los anteriores debieron ser insertados inmediatamente antes como innerHTML
            // quiz�s en el futuro podamos detectar que los �ltimos cambios realizados en el DOM no afectan
            // al �ltimo InnerMarkupCodeImpl asociado al nodo actual pero por ahora es complicado
            // y no merece la pena.
            Object lastCode = clientDoc.getClientDocumentStful().getLastCodeToSend();
            if (!(lastCode instanceof InnerMarkupCodeImpl))
                return new CannotInsertAsMarkupCauseImpl(parent); // Si existe un InnerMarkupCodeImpl debe ser lo �ltimo que se hizo
            InnerMarkupCodeImpl lastInnerCode = (InnerMarkupCodeImpl)lastCode;
            if (lastInnerCode.getParentNode() != parent)
                return new CannotInsertAsMarkupCauseImpl(parent); // Es un inner de otro nodo padre
            // Si llegamos aqu� es que los anteriores al nuevo son compatibles con innerHTML o nuestro innerXML
        }

        ItsNatStfulDocumentTemplateVersionImpl template = clientDoc.getItsNatStfulDocument().getItsNatStfulDocumentTemplateVersion();
        return render.canInsertChildNodeAsMarkupIgnoringOther(parent,newChildNode,template); // En el caso de �nico hijo obviamente los dem�s se ignoran pues no hay m�s
    }    
    
    public static InnerMarkupCodeImpl appendSingleChildNodeAsMarkup(Node newNode, ClientDocumentStfulDelegateWebImpl clientDoc,RenderElement render)
    {
        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        String newNodeMarkup = itsNatDoc.serializeNode(newNode);
        if (DOMUtilInternal.isTheOnlyChildNode(newNode))
        {
            // Caso de �nico nodo
            Element parent = (Element)newNode.getParentNode();
            return render.appendChildrenCodeAsMarkup(null,parent,newNodeMarkup,clientDoc);
        }
        else // Caso de �ltimo nodo, sabemos que podemos usar el �ltimo InnerMarkupCodeImpl el cual est� asociado al nodo padre
        {
            InnerMarkupCodeImpl lastCode = (InnerMarkupCodeImpl)clientDoc.getClientDocumentStful().getLastCodeToSend();
            lastCode.addInnerMarkup(newNodeMarkup);
            return null; // No se a�ade nada y se deja como �ltimo este lastCode
        }
    }    
    
    public static InnerMarkupCodeImpl appendSingleChildNodeAsMarkup(Node newNode, ClientDocumentStfulDelegateImpl clientDoc,RenderElement render)
    {
        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        String newNodeMarkup = itsNatDoc.serializeNode(newNode);
        if (DOMUtilInternal.isTheOnlyChildNode(newNode))
        {
            // Caso de �nico nodo
            Element parent = (Element)newNode.getParentNode();
            return render.appendChildrenCodeAsMarkup(null,parent,newNodeMarkup,clientDoc);
        }
        else // Caso de �ltimo nodo, sabemos que podemos usar el �ltimo InnerMarkupCodeImpl el cual est� asociado al nodo padre
        {
            InnerMarkupCodeImpl lastCode = (InnerMarkupCodeImpl)clientDoc.getClientDocumentStful().getLastCodeToSend();
            lastCode.addInnerMarkup(newNodeMarkup);
            return null; // No se a�ade nada y se deja como �ltimo este lastCode
        }
    }    
    
    public static InnerMarkupCodeImpl appendChildrenAsMarkup(String parentVarName, Node parentNode, ClientDocumentStfulDelegateImpl clientDoc,RenderElement render)
    {
        // Se supone que hay nodos hijo (si no no llamar).
        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        StringBuilder childrenCode = new StringBuilder();

        if (parentNode.hasChildNodes())
        {
            Node child = parentNode.getFirstChild();
            while(child != null)
            {
                String nodeCode = itsNatDoc.serializeNode(child);
                childrenCode.append( nodeCode );

                child = child.getNextSibling();
            }
        }
        
        return render.appendChildrenCodeAsMarkup(parentVarName,(Element)parentNode,childrenCode.toString(),clientDoc);
    }    
    
    public static CannotInsertAsMarkupCauseImpl canInsertChildNodeAsMarkupIgnoringOther(Element parent,Node childNode,MarkupTemplateVersionImpl template,RenderElement render)
    {
        if (!render.isInsertChildNodesAsMarkupCapable(parent,template))
            return new CannotInsertAsMarkupCauseImpl(parent);

        // Preguntamos si puede insertarse como markup si fuera el �nico nodo hijo
        Node badChildNode = DOMUtilInternal.getNodeOrFirstContainedNodeMatching(childNode,render,template);
        if (badChildNode != null)
            return new CannotInsertAsMarkupCauseImpl(parent,badChildNode);

        return null;
    }    
    
    public static CannotInsertAsMarkupCauseImpl canInsertAllChildrenAsMarkup(Element parent,MarkupTemplateVersionImpl template,InsertAsMarkupInfoImpl insertMarkupInfo,RenderElement render)
    {
        int res = InsertAsMarkupInfoImpl.DO_NOT_KNOW;
        if (insertMarkupInfo != null)
        {
            res = insertMarkupInfo.canInsertAllChildrenAsMarkup(parent);
            if (res == InsertAsMarkupInfoImpl.CANNOT_INSERT_CHILDREN_VERIFIED)
                return new CannotInsertAsMarkupCauseImpl(insertMarkupInfo); // Salvamos as� en la causa todo lo que ya sabemos del "sub�rbol"
            // Si se llega aqu�, es el caso desconocido InsertAsMarkupInfoImpl.DO_NOT_KNOW
            // o bien IS_VALID_INSERTED_AS_MARKUP
        }
        // Vemos si tiene capacidad de insertar como markup sus hijos, por ejemplo si tiene innerHTML
        if (!render.isInsertChildNodesAsMarkupCapable(parent,template))
            return new CannotInsertAsMarkupCauseImpl(parent);

        // Ahora vemos si sus hijos son insertables como markup:

        // Si sabemos que puede ser insertado como markup entonces
        // sabemos que los hijos pueden ser tambi�n insertados como markup
        // por tanto no lo averiguamos de nuevo y as� ganamos en rendimiento
        if (res != InsertAsMarkupInfoImpl.IS_VALID_INSERTED_AS_MARKUP)
        {
            Node badChildNode = render.getFirstChildIsNotValidInsertedAsMarkup(parent,template);
            if (badChildNode != null)
                return new CannotInsertAsMarkupCauseImpl(parent,badChildNode);
        }
        
        // Debe haber al menos un Element como hijo para que valga la pena
        // usar serializaci�n y parsing con DOMRender
        // Hay que tener en cuenta que DOMRender no es como una simple llamada a innerHTML

        boolean hasSomeElement = false;
        if (parent.hasChildNodes())
        {
            Node child = parent.getFirstChild();
            while(child != null)
            {
                if (child.getNodeType() == Node.ELEMENT_NODE)
                {
                    hasSomeElement = true;  // S� merece la pena insertar como markup
                    break;
                }
                
                child = child.getNextSibling();
            }
        }
        
        if (!hasSomeElement) return new CannotInsertAsMarkupCauseImpl(parent); // No merece la pena          
        
        return null;
    }
       
    public static InnerMarkupCodeImpl createInnerMarkupCode(String parentVarName,Element parentNode,String childrenCode,ClientDocumentStfulDelegateImpl clientDoc,RenderElement render)
    {
        boolean useNodeLocation;
        String parentNodeLocator;
        if (parentVarName == null)
        {
            useNodeLocation = true;
            NodeLocationImpl parentLoc = clientDoc.getNodeLocation(parentNode,true);
            parentNodeLocator = parentLoc.toScriptNodeLocation(true);
        }
        else
        {
            useNodeLocation = false;
            parentNodeLocator = parentVarName;
        }

        return new InnerMarkupCodeImpl(render,parentNode,parentNodeLocator,useNodeLocation,childrenCode);    
    }
}
