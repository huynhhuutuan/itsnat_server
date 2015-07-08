/*
 * This file is not part of the ItsNat framework.
 *
 * Original source code use and closed source derivatives are authorized
 * to third parties with no restriction or fee.
 * The original source code is owned by the author.
 *
 * This program is distributed AS IS in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * (C) Innowhere Software a service of Jose Maria Arranz Santamaria, Spanish citizen.
 */

package org.itsnat.feashow.features.comp.shared;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import org.itsnat.comp.ItsNatComponent;
import org.itsnat.comp.ItsNatComponentManager;
import org.itsnat.comp.ItsNatComponentUI;
import org.itsnat.core.ClientDocument;
import org.itsnat.core.ItsNatDocument;
import org.itsnat.core.event.ParamTransport;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.EventListener;

public abstract class MyCustomComponentBase implements ItsNatComponent
{
    protected Element parentElement;
    protected ItsNatComponentManager compMgr;

    public MyCustomComponentBase(Element parentElement,ItsNatComponentManager compMgr)
    {
        this.parentElement = parentElement;
        this.compMgr = compMgr;
    }

    public Node getNode()
    {
        return parentElement;
    }

    public void setNode(Node node)
    {
        throw new RuntimeException("REATTACHMENT IS NOT SUPPORTED");
    }

    public ItsNatDocument getItsNatDocument()
    {
        return compMgr.getItsNatDocument();
    }

    public ItsNatComponentManager getItsNatComponentManager()
    {
        return compMgr;
    }

    public boolean isDisposed()
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void addEventListener(String type, EventListener listener,boolean before)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void removeEventListener(String type, EventListener listener,boolean before)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void addEventListener(String type, EventListener listener)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void removeEventListener(String type, EventListener listener)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void enableEventListener(String type)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void disableEventListener(String type)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public ItsNatComponentUI getItsNatComponentUI()
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public PropertyChangeListener[] getPropertyChangeListeners()
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public VetoableChangeListener[] getVetoableChangeListeners()
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void registerArtifact(String name, Object value)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public Object getArtifact(String name)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public Object getArtifact(String name,boolean cascade)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public Object removeArtifact(String name)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public String[] getArtifactNames(String name)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public Object removeUserValue(String name)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public Object getUserValue(String name)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public Object setUserValue(String name, Object value)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public String[] getUserValueNames()
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public boolean containsUserValueName(String name)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void setEventListenerParams(String type, boolean useCapture, int commMode, ParamTransport[] extraParams, String preSendCode, long eventTimeout)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public boolean isEnabled()
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void setEnabled(boolean b)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }
    
    public void addEventListener(ClientDocument clientDoc, String type, EventListener listener, boolean before)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void removeEventListener(ClientDocument clientDoc, String type, EventListener listener, boolean before)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void addEventListener(ClientDocument clientDoc, String type, EventListener listener)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void removeEventListener(ClientDocument clientDoc, String type, EventListener listener)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void enableEventListener(ClientDocument clientDoc, String type)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void disableEventListener(ClientDocument clientDoc, String type)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void setEventListenerParams(ClientDocument clientDoc, String type, boolean useCapture, int commMode, ParamTransport[] extraParams, String preSendCode, long eventTimeout)
    {
        throw new RuntimeException("NOT IMPLEMENTED");
    }
}
