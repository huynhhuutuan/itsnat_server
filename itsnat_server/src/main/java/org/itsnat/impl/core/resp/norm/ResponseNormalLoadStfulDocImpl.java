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

package org.itsnat.impl.core.resp.norm;

import org.itsnat.impl.core.resp.norm.web.ResponseNormalLoadStfulWebDocImpl;
import org.itsnat.impl.core.resp.norm.droid.ResponseNormalLoadDroidDocImpl;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.itsnat.core.ItsNatDOMException;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulOwnerImpl;
import org.itsnat.impl.core.doc.BoundElementDocContainerImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.doc.droid.ItsNatStfulDroidDocumentImpl;
import org.itsnat.impl.core.doc.web.ItsNatStfulWebDocumentImpl;
import org.itsnat.impl.core.domimpl.ElementDocContainer;
import org.itsnat.impl.core.domutil.DOMUtilInternal;
import org.itsnat.impl.core.domutil.NodeConstraints;
import org.itsnat.impl.core.req.norm.RequestNormalLoadDocImpl;
import org.itsnat.impl.core.resp.*;
import org.itsnat.impl.core.resp.shared.ResponseDelegateStfulLoadDocImpl;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 *
 * @author jmarranz
 */
public abstract class ResponseNormalLoadStfulDocImpl extends ResponseNormalLoadDocImpl implements ResponseLoadStfulDocumentValid
{
    protected ResponseDelegateStfulLoadDocImpl responseDelegate;
    protected Map<Node,Object> disconnectedNodesFastLoadMode;

    /**
     * Creates a new instance of ResponseNormalLoadStfulDocImpl
     */
    public ResponseNormalLoadStfulDocImpl(RequestNormalLoadDocImpl request)
    {
        super(request);

        this.responseDelegate = ResponseDelegateStfulLoadDocImpl.createResponseDelegateStfulLoadDoc(this);
    }

    
    public static ResponseNormalLoadStfulDocImpl createResponseNormalLoadStfulDoc(RequestNormalLoadDocImpl request)
    {
        ItsNatStfulDocumentImpl itsNatDoc = (ItsNatStfulDocumentImpl)request.getItsNatDocument();
        if (itsNatDoc instanceof ItsNatStfulDroidDocumentImpl)
            return new ResponseNormalLoadDroidDocImpl(request);
        else if (itsNatDoc instanceof ItsNatStfulWebDocumentImpl)
            return ResponseNormalLoadStfulWebDocImpl.createResponseNormalLoadStfulWebDoc(request);
        return null;
    }

    public ClientDocumentStfulImpl getClientDocumentStful()
    {
        return (ClientDocumentStfulImpl)getClientDocument();
    }

    public ClientDocumentStfulOwnerImpl getClientDocumentStfulOwner()
    {
        return (ClientDocumentStfulOwnerImpl)getClientDocument();
    }

    public ItsNatStfulDocumentImpl getItsNatStfulDocument()
    {
        return (ItsNatStfulDocumentImpl)getRequestNormalLoadDoc().getItsNatDocument();
    }

    public ResponseDelegateStfulLoadDocImpl getResponseDelegateStfulLoadDoc()
    {
        return responseDelegate;
    }

    public void processResponse()
    {
        if (getRequestNormalLoadDoc().isStateless())
        {
            // Descuidadamente es posible que el programador genere nodos cacheados en fase de carga del documento stateless por ejemplo al usar un getNodeReference 
            // por eso hacemos un clearNodeCache() al ppio en el cliente para que esos cacheos no tengan ning�n problema con alg�n posible resto de nodos cacheados en el cliente
            ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
            ClientDocumentStfulDelegateImpl clientDocDeleg = clientDoc.getClientDocumentStfulDelegate();
            clientDocDeleg.getNodeCacheRegistry().clearCache(); 
            clientDoc.addCodeToSend("itsNatDoc.clearNodeCache();\n");             
            
            responseDelegate.dispatchRequestListeners(); // Evitamos la serializaci�n innecesaria del ItsNatDocument
            
            // En la fase del evento stateless se hace otro clearNodeCache
        }
        else
        {
            responseDelegate.processResponse();
        }
        
        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        if (!clientDoc.canReceiveSOMENormalEvents())
        {
            // No hay eventos y por tanto no hay posibilidad de unload
            ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
            itsNatDoc.setInvalid();
        }
    }

    public boolean isSerializeBeforeDispatching()
    {
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        return !itsNatDoc.isFastLoadMode();
    }

    public boolean isReferrerEnabled()
    {
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();        
        return itsNatDoc.isReferrerEnabled() && !getRequestNormalLoadDocBase().isStateless(); // El referrer se guarda en la sesi�n y si estamos en stateless no queremos ni oir de hablar de ello
    }
    


    public boolean hasDisconnectedNodesFastLoadMode()
    {
        if (disconnectedNodesFastLoadMode == null) return false;
        return !disconnectedNodesFastLoadMode.isEmpty();
    }

    public Map<Node,Object> getDisconnectedNodesFastLoadMode()
    {
        if (disconnectedNodesFastLoadMode == null)
            this.disconnectedNodesFastLoadMode = new HashMap<Node,Object>();
        return disconnectedNodesFastLoadMode;
    }

    @Override
    public String serializeDocument()
    {
        // Como este m�todo inserta muchos nodos ha de ejecutarse lo antes posible para que otros procesos
        // pre-serializaci�n puedan hacer algo con ellos
        preSerializeDocDisconnectedNodesFastLoadMode();

        LinkedList<BoundElementDocContainerImpl> boundHTMLElemDocContainerList = preSerializeDocProcessBoundElementDocContainer();

        String docMarkup = super.serializeDocument();

        postSerializeDocProcessBoundElementDocContainer(boundHTMLElemDocContainerList);

        postSerializeDocDisconnectedNodesFastLoadMode();

        return docMarkup;
    }

    public void preSerializeDocDisconnectedNodesFastLoadMode()
    {
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();

        if (itsNatDoc.isFastLoadMode() && hasDisconnectedNodesFastLoadMode())
        {
            // Insertamos temporalmente los nodos hijo eliminados pues el cliente
            // debe recibirlos al serializar
            Map<Node,Object> disconnectedNodesFastLoadMode = getDisconnectedNodesFastLoadMode();
            for(Map.Entry<Node,Object> entry : disconnectedNodesFastLoadMode.entrySet())
            {
                Node parentNode = entry.getKey();
                Object content = entry.getValue();
                if (parentNode.hasChildNodes())
                    throw new RuntimeException("INTERNAL ERROR"); // Por si acaso
                if (content instanceof Node) // Nodo concreto
                {
                    Node childNode = (Node)content;
                    if (itsNatDoc.isDebugMode() && DOMUtilInternal.isNodeBoundToDocumentTree(childNode))
                        throw new ItsNatDOMException("Child nodes removed from a disconnected node cannot be reinserted in a different place on load phase and fast mode",childNode);
                    parentNode.appendChild(childNode);
                }
                else
                {
                    @SuppressWarnings("unchecked")
                    LinkedList<Node> nodeList = (LinkedList<Node>)content;
                    Iterator<Node> itChildNodes = nodeList.iterator();
                    DocumentFragment childNodesFragment = (DocumentFragment)itChildNodes.next(); // Sabemos que el primero es el DocumentFragment que se le dio al usuario
                    while(itChildNodes.hasNext())
                    {
                        Node childNode = itChildNodes.next();
                        if (itsNatDoc.isDebugMode() && DOMUtilInternal.isNodeBoundToDocumentTree(childNode))
                            throw new ItsNatDOMException("Child nodes removed from a disconnected node cannot be reinserted in a different place on load phase and fast mode",childNode);
                        parentNode.appendChild(childNode);
                    }
                    // Al mismo tiempo que los insertamos se eliminaron en teor�a del DocumentFragment que se dio al usuario y que los conten�a,
                    // lo comprobamos
                    if (childNodesFragment.hasChildNodes())
                        throw new ItsNatDOMException("DocumentFragment containing the child nodes removed from a disconnected node cannot be reinserted in a different place on load phase and fast mode",childNodesFragment);
                }
            }
        }
    }

    public void postSerializeDocDisconnectedNodesFastLoadMode()
    {
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        if (itsNatDoc.isFastLoadMode() && hasDisconnectedNodesFastLoadMode())
        {
            // Eliminamos el contenido de los nodos nuevo para dejarlos en el servidor como el programador lo hizo
            // cuando desconect�
            Map<Node,Object> disconnectedNodesFastLoadMode = getDisconnectedNodesFastLoadMode();
            for(Map.Entry<Node,Object> entry : disconnectedNodesFastLoadMode.entrySet())
            {
                Node parentNode = entry.getKey();
                Object content = entry.getValue();

                if (content instanceof Node) // Nodo concreto
                {
                    // S�lo esperamos un nodo
                    Node childNode = (Node)content;
                    if (childNode != DOMUtilInternal.extractChildren(parentNode))
                        throw new RuntimeException("INTERNAL ERROR"); // Para que quede claro
                }
                else
                {
                    @SuppressWarnings("unchecked")
                    LinkedList<Node> nodeList = (LinkedList<Node>)content;
                    Iterator<Node> itChildNodes = nodeList.iterator();
                    DocumentFragment childNodesFragment = (DocumentFragment)itChildNodes.next(); // Sabemos que el primero es el DocumentFragment que se le dio al usuario
                    DocumentFragment childNodesFragmentAux = (DocumentFragment)DOMUtilInternal.extractChildren(parentNode);
                    // Copiamos uno en otro para restaurar el DocumentFragment del usuario
                    while(childNodesFragmentAux.getFirstChild() != null)
                    {
                        childNodesFragment.appendChild(childNodesFragmentAux.getFirstChild());
                    }
                }
            }

            this.disconnectedNodesFastLoadMode = null; // Ya no los necesitamos m�s, liberamos cuanto antes memoria
        }
    }

    public LinkedList<BoundElementDocContainerImpl> preSerializeDocProcessBoundElementDocContainer()
    {
        LinkedList<BoundElementDocContainerImpl> boundHTMLElemDocContainerList = null;

        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        Document doc = itsNatDoc.getDocument();

        // Elementos que implementan ElementDocContainer: <object>, <iframe> y <embed>
        NodeConstraints rules = new NodeConstraints()
        {
            public boolean match(Node node, Object context)
            {
                return (node instanceof ElementDocContainer); // <iframe> y <object>
            }
        };
        LinkedList<Node> elemList = DOMUtilInternal.getChildNodeListMatching(doc,rules,true,null);
        if (elemList != null)
        {
            ClientDocumentStfulOwnerImpl cliendDoc = getClientDocumentStfulOwner();
            for(Iterator<Node> it = elemList.iterator(); it.hasNext(); )
            {
                ElementDocContainer elem = (ElementDocContainer)it.next();
                BoundElementDocContainerImpl bindInfo = BoundElementDocContainerImpl.register(elem, itsNatDoc);
                if (bindInfo == null)
                    continue; // No tiene el formato de URL relativa esperado o los par�metros est�n malformados

                bindInfo.setURLForClientOwner(cliendDoc);

                if (boundHTMLElemDocContainerList == null) boundHTMLElemDocContainerList = new LinkedList<BoundElementDocContainerImpl>();
                boundHTMLElemDocContainerList.add(bindInfo);
            }
        }

        return boundHTMLElemDocContainerList;
    }

    public void postSerializeDocProcessBoundElementDocContainer(LinkedList<BoundElementDocContainerImpl> boundHTMLElemDocContainerList)
    {
        // Restauramos los URLs originales ("src" en iframe o "data" en object)
        if (boundHTMLElemDocContainerList != null)
        {
            ClientDocumentStfulOwnerImpl cliendDoc = getClientDocumentStfulOwner();
            for(BoundElementDocContainerImpl bindInfo : boundHTMLElemDocContainerList)
            {
                bindInfo.restoreOriginalURL(cliendDoc);
            }
        }
    }


    public void preSerializeDocumentStful()
    {
        // Nada que hacer
    }

    public boolean isOnlyReturnMarkupOfFinalScripts()
    {
        if (getParentResponseAttachedServerLoadDoc() != null)
            return true;  // Porque el markup ya est� en el cliente no es necesario enviarlo de nuevo
        return false;
    }

    public boolean isNeededAbsoluteURL()
    {
        if (getParentResponseAttachedServerLoadDoc() != null)
            return true;  // Porque los requests se enviar�n posiblemente a un servidor diferente al que carg� la p�gina inicial
        return false;
    }

    public boolean isInlineLoadFrameworkScripts()
    {
        if (getParentResponseAttachedServerLoadDoc() != null)
            return true;  // Porque as� por una parte evitamos un request (lo menos importante) y evitamos que a trav�s de un URL el archivo se cargue despu�s del c�digo inicial en navegadores tal y como MSIE 6 en donde la carga de <script src=""> introducidos via document.write() sigue siendo as�ncrona, al estar el c�digo ahora dentro del <script>c�digo</script> es inevitable su ejecuci�n inmediata
        return false;
    }
}
