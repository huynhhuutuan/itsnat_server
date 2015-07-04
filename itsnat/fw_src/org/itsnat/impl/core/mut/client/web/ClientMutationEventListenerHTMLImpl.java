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

package org.itsnat.impl.core.mut.client.web;

import org.itsnat.impl.core.browser.web.BrowserGecko;
import org.itsnat.impl.core.browser.web.BrowserMSIEOld;
import org.itsnat.impl.core.browser.web.BrowserWeb;
import org.itsnat.impl.core.browser.web.webkit.BrowserWebKit;
import org.itsnat.impl.core.clientdoc.web.SVGWebInfoImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.listener.WaitForEventListenerImpl;
import org.itsnat.impl.core.mut.client.ClientMutationEventListenerStfulWebImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

/**
 *
 * @author jmarranz
 */
public abstract class ClientMutationEventListenerHTMLImpl extends ClientMutationEventListenerStfulWebImpl
{
    public ClientMutationEventListenerHTMLImpl(ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        super(clientDoc);
    }

    public static ClientMutationEventListenerHTMLImpl createClientMutationEventListenerHTML(ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        BrowserWeb browser = clientDoc.getBrowserWeb();
        if (browser instanceof BrowserMSIEOld)
            return new ClientMutationEventListenerHTMLMSIEOldImpl(clientDoc);        
        else if (browser instanceof BrowserWebKit)
            return ClientMutationEventListenerHTMLWebKitImpl.createClientMutationEventListenerHTMLWebKit(clientDoc);
        else if (browser instanceof BrowserGecko)
            return new ClientMutationEventListenerHTMLDefaultImpl(clientDoc);        
        else
            return new ClientMutationEventListenerHTMLDefaultImpl(clientDoc);
    }

    public void preRenderAndSendMutationCode(MutationEvent mutEvent)
    {
        ClientDocumentStfulDelegateWebImpl clientDoc = getClientDocumentStfulDelegateWeb(); 
            
        String type = mutEvent.getType();

        if (type.equals("DOMNodeRemoved"))
        {           
            if (SVGWebInfoImpl.isSVGWebEnabled(clientDoc))
            {
                Node removedNode = (Node)mutEvent.getTarget();
                SVGWebInfoImpl svgWeb = clientDoc.getSVGWebInfo();
                if (!svgWeb.isSVGNodeProcessedBySVGWebFlash(removedNode))
                {
                    // Si el nodo removeNode es un nodo normal
                    // son los hijos los que pueden tener nodos SVG procesados por SVGWeb
                    // en esos nodos hay que hacer un removeChild para cada uno de ellos
                    // (en el propio nodo a eliminar obviamente se har� como parte del proceso normal)
                    fixTreeRemovedSVGRootSVGWeb(removedNode);
                }
            }
        }
    }

    @Override
    public void postRenderAndSendMutationCode(MutationEvent mutEvent)
    {
        super.postRenderAndSendMutationCode(mutEvent);

        ClientDocumentStfulDelegateWebImpl clientDoc = getClientDocumentStfulDelegateWeb();        
        
        String type = mutEvent.getType();

        if (type.equals("DOMNodeRemoved"))
        {
            Node removedNode = (Node)mutEvent.getTarget();
            if (removedNode instanceof Text)
            {
                if (SVGWebInfoImpl.isSVGNodeProcessedBySVGWebFlash(removedNode, clientDoc))
                {
                    // SVGWeb no soporta bien la eliminaci�n de nodos de texto
                    // se elimina del DOM pero no se actualiza visualmente
                    // sin embargo he descubierto que simplemente reinsertando
                    // el nodo padre se actualiza.
                    StringBuilder code = new StringBuilder();

                    Node parentNode = removedNode.getParentNode(); // Ser� un elemento
                    String jsRef = clientDoc.getNodeReference(parentNode,true,true);

                    code.append("var elem = " + jsRef + ";\n");
                    code.append("var elemClone = elem.cloneNode(false);\n");
                    code.append("elem.parentNode.replaceChild(elemClone,elem);");
                    code.append("elemClone.parentNode.replaceChild(elem,elemClone);");

                    clientDoc.addCodeToSend(code);
                }
            }
        }
        else if (type.equals("DOMNodeInserted"))
        {
            if (SVGWebInfoImpl.isSVGWebEnabled(clientDoc))
            {
                Node newNode = (Node)mutEvent.getTarget();
                processTreeInsertedSVGRootSVGWebWaitForEvent(newNode);
            }
        }
    }

    protected void fixTreeRemovedSVGRootSVGWeb(Node node)
    {
        // Los nodos SVG root procesados por SVGWeb necesitan ser eliminados
        // a trav�s de un m�todo especial de SVGWeb, si eliminamos un nodo
        // normal pero que contiene nodos SVG de SVGWeb dichos nodos (procesados
        // por <objects>/<embeds>) no ser�n liberados correctamente,
        // por eso antes de hacer el borrado normal del nodo padre buscamos
        // nodos hijos SVG de SVGWeb para eliminarlos antes liberando recursos.

        StringBuilder code = fixTreeRemovedSVGRootSVGWeb(node,null);

        if ((code != null) && (code.length() > 0))
            clientDoc.addCodeToSend(code.toString());
    }

    protected StringBuilder fixTreeRemovedSVGRootSVGWeb(Node node,StringBuilder code)
    {
        if (node.getNodeType() != Node.ELEMENT_NODE) return code;

        ClientDocumentStfulDelegateWebImpl clientDoc = getClientDocumentStfulDelegateWeb();         
        
        Element elem = (Element)node;
        if (SVGWebInfoImpl.isSVGRootElementProcessedBySVGWebFlash(elem,clientDoc))
        {
            if (code == null) code = new StringBuilder();

            String jsRef = clientDoc.getNodeReference(elem,false,true); // No cacheamos pues lo vamos a eliminar
            code.append("var elem = " + jsRef + ";\n");
            code.append("itsNatDoc.win.svgweb.removeChild(elem,elem.parentNode);\n");

            return code;
        }

        Node child = elem.getFirstChild();
        while (child != null)
        {
            code = fixTreeRemovedSVGRootSVGWeb(child,code);
            child = child.getNextSibling();
        }

        return code;
    }


    protected void processTreeInsertedSVGRootSVGWebWaitForEvent(Node node)
    {
        // Cuando se inserta din�micamente un nuevo nodo SVG via SVGWeb
        // posteriores cambios en el DOM SVG tras la inserci�n fallar�n,
        // apenas cambios en los atributos del nodo SVG root funcionan.
        // Hay que tener en cuenta que los nodos utilizados en la inserci�n
        // son provisionales y ser�n substituidos por otros tras la renderizaci�n
        // Por ello SVGWeb recomienda posponer estos accesos/cambios al DOM
        // a despu�s del evento SVGLoad que es emitido artificialmente
        // a los listeners SVGLoad registrados en el nodo SVG tras la inserci�n.
        // En ItsNat esto obliga al programador a usar la misma t�cnica, para
        // evitarlo usamos la t�cnica WaitForEventListener que es una marca que a�adimos
        // a la cola de c�digo JS a enviar al cliente tal que se env�a el c�digo
        // hasta la marca (podr� a�adirse c�digo a la cola despu�s pero no se env�a
        // al cliente hasta que no se quite la marca).
        // Cuando el evento SVGLoad se recibe quitamos la marca y ya se puede
        // enviar el c�digo JS que le sigue (hasta otra posible marca de otro
        // nodo root SVGWeb insertado).
        // Esto permite funcionar inserciones din�micas en SVGWeb en control remoto
        // La pega es si el usuario registra un listener SVGLoad propio (sabe que puede
        // pues SVGWeb lo permite), dicho listener no es enviado al cliente pues est�
        // despu�s de la marca y cuando es enviado es cuando se ha recibido ya el evento SVGLoad,
        // lo que hacemos para evitar esto es quitar la marca WaitForEventListener cuando
        // detectemos que el usuario a�ade un SVGLoad listener, suponemos que
        // el usuario es consciente de lo que hace y no deber�a cambiar el DOM
        // inmediatamente tras la inserci�n (para eso est� el SVGLoad). Esto se hace en otro lugar.

        if (node.getNodeType() != Node.ELEMENT_NODE) return;

        ClientDocumentStfulDelegateWebImpl clientDoc = getClientDocumentStfulDelegateWeb();

        Element elem = (Element)node;
        if (SVGWebInfoImpl.isSVGRootElementProcessedBySVGWebFlash(elem,clientDoc))
        {
            WaitForEventListenerImpl listener = new WaitForEventListenerImpl(elem,"SVGLoad");
            clientDoc.getClientDocumentStful().addEventListener((EventTarget)elem,"SVGLoad",listener,false);
            clientDoc.addCodeToSend(listener); // A�adimos la marca.
            return;
        }

        Node child = elem.getFirstChild();
        while (child != null)
        {
            processTreeInsertedSVGRootSVGWebWaitForEvent(child);
            child = child.getNextSibling();
        }
    }
}
