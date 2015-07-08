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

package org.itsnat.impl.core.domimpl.deleg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.domimpl.ElementNSImpl;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

/**
 *
 * @author jmarranz
 */
public abstract class DelegateElementNSImpl extends DelegateNotDocWithtParentNodeImpl
        implements EventListener
{
    protected boolean disconnectedChildNodesFromClient = false;
    
    public DelegateElementNSImpl(ElementNSImpl node)
    {
        super(node);
    }

    public ElementNSImpl getElementNSImpl()
    {
        return (ElementNSImpl)node;
    }
    
    public void checkHasSenseDisconnectedChildNodesFromClient()
    {
        checkHasSenseDisconnectedFromClient();
    }
        
    public boolean isDisconnectedChildNodesFromClient()
    {
        checkHasSenseDisconnectedChildNodesFromClient();

        if (isDisconnectedFromClient()) return true; // Si el propio nodo est� desconectado tambi�n lo estar�n los hijos

        // El propio elemento puede estar conectado pero no los hijos
        return disconnectedChildNodesFromClient;
    }

    public void setDisconnectedChildNodesFromClient(boolean disconnectedChildNodesFromClient)
    {
        checkHasSenseDisconnectedChildNodesFromClient();

        //if (disconnectedChildNodesFromClient && isDisconnectedFromClient())
        //    throw new ItsNatDOMException("This node is already disconnected",node); // As� impedimos intentar desconectar nodos inferiores de nodos ya desconectados
        
        // Antes de llamar a este m�todo ya hemos comprobado que vamos a hacer un cambio, de otra manera no llamamos
        if (this.disconnectedChildNodesFromClient == disconnectedChildNodesFromClient)
            throw new ItsNatException("INTERNAL ERROR");

        this.disconnectedChildNodesFromClient = disconnectedChildNodesFromClient;

        ElementNSImpl elem = getElementNSImpl();
        if (disconnectedChildNodesFromClient)
        {
            // Para que si eliminamos del documento y reinsertamos el elemento de nuevo no de una falsa desconexi�n activada
            elem.addEventListenerInternal("DOMNodeRemovedFromDocument",this,false);
        }
        else
        {
            // Por si desconectamos previamehte y hemos cambiado de opini�n (y hemos reconectado)
            elem.removeEventListenerInternal("DOMNodeRemovedFromDocument",this,false);
        }
    }

    public void handleEvent(Event evt)
    {
        // El evento es disparado tambi�n al eliminar del documento nodos hijo de este elemento
        // por lo que tenemos que comprobar que es el propio elemento el que eliminamos y no s�lo un hijo
        // (yo creo que no ocurre)
        // Tambi�n es disparado cuando es un nodo padre de este elemento el que es eliminado,
        // pero el target en ese caso es este elemento, lo cual es lo que esperamos pues tenemos que quitar
        // la desconexi�n cuando el elemento se quita del documento de cualquier manera.
        ElementNSImpl elem = getElementNSImpl();
        if (evt.getTarget() == elem)
        {
            this.disconnectedChildNodesFromClient = false;
            elem.removeEventListenerInternal("DOMNodeRemovedFromDocument",this,false);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        // Esto es porque los eventos internos no se serializan
        if (disconnectedChildNodesFromClient)
        {
            ElementNSImpl elem = getElementNSImpl();
            elem.addEventListenerInternal("DOMNodeRemovedFromDocument",this,false);
        }
    }
}
