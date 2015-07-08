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

package org.itsnat.impl.comp.button.normal;

import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.event.ChangeEvent;
import org.itsnat.comp.ItsNatComponentUI;
import org.itsnat.comp.button.ItsNatButtonUI;
import org.itsnat.comp.button.normal.ItsNatHTMLAnchor;
import org.itsnat.core.NameValue;
import org.itsnat.core.event.ParamTransport;
import org.itsnat.impl.comp.ItsNatHTMLElementComponentImpl;
import org.itsnat.impl.comp.button.ItsNatButtonInternal;
import org.itsnat.impl.comp.button.ItsNatButtonSharedImpl;
import org.itsnat.impl.comp.listener.ItsNatCompNormalEventListenersByClientDefaultImpl;
import org.itsnat.impl.comp.listener.ItsNatCompNormalEventListenersByClientImpl;
import org.itsnat.impl.comp.listener.ItsNatCompNormalEventListenersByDocDefaultImpl;
import org.itsnat.impl.comp.listener.ItsNatCompNormalEventListenersByDocImpl;
import org.itsnat.impl.comp.mgr.web.ItsNatStfulWebDocComponentManagerImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.domutil.NamespaceUtil;
import org.itsnat.impl.core.scriptren.jsren.JSRenderMethodCallImpl;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.html.HTMLAnchorElement;

/**
 *
 * @author jmarranz
 */
public abstract class ItsNatHTMLAnchorImpl extends ItsNatHTMLElementComponentImpl implements ItsNatHTMLAnchor,ItsNatButtonInternal,ItsNatButtonNormalInternal
{
    protected ItsNatButtonSharedImpl buttonDelegate = createItsNatButtonShared();

    /** Creates a new instance of ItsNatHTMLAnchorImpl */
    public ItsNatHTMLAnchorImpl(HTMLAnchorElement element,NameValue[] artifacts,ItsNatStfulWebDocComponentManagerImpl componentMgr)
    {
        super(element,artifacts,componentMgr);
    }

    public ItsNatCompNormalEventListenersByDocImpl createItsNatCompNormalEventListenersByDoc()
    {
        return new ItsNatCompNormalEventListenersByDocDefaultImpl(this);
    }

    public ItsNatCompNormalEventListenersByClientImpl createItsNatCompNormalEventListenersByClient(ClientDocumentImpl clientDoc)
    {
        return new ItsNatCompNormalEventListenersByClientDefaultImpl(this,clientDoc);
    }

    public ItsNatButtonSharedImpl getItsNatButtonShared()
    {
        return buttonDelegate;
    }

    public ItsNatButtonSharedImpl createItsNatButtonShared()
    {
        return new ItsNatButtonNormalSharedImpl(this);
    }

    public void enableEventListenersByDoc()
    {
        super.enableEventListenersByDoc();

        enableEventListener("click"); // Por defecto se procesa, pues es lo importante
    }

    public HTMLAnchorElement getHTMLAnchorElement()
    {
        return (HTMLAnchorElement)node;
    }

    public ItsNatComponentUI createDefaultItsNatComponentUI()
    {
        return new ItsNatButtonNormalBasedUIImpl(this);
    }

    public void bindDataModel()
    {
        buttonDelegate.bindDataModel();
    }

    public void unbindDataModel()
    {
        buttonDelegate.unbindDataModel();
    }

    public void initialSyncUIWithDataModel()
    {
        buttonDelegate.initialSyncUIWithDataModel();
    }

    public void syncWithDataModel()
    {
        buttonDelegate.syncWithDataModel();
    }

    public void stateChanged(ChangeEvent e)
    {
        buttonDelegate.stateChanged(e);
    }

    public ButtonModel getButtonModel()
    {
        return (ButtonModel)dataModel;
    }

    public void setButtonModel(ButtonModel dataModel)
    {
        setDataModel(dataModel);
    }

    public boolean isEnabled()
    {
        // Est� propiedad est� en el modelo no s�lo en el DOM
        // el modelo modifica el DOM via listeners pues es el modelo el que manda
        return getButtonModel().isEnabled();
    }

    public void setEnabled(boolean b)
    {
        // Est� propiedad est� en el modelo no s�lo en el DOM
        // el modelo modificar� el DOM via listeners pues es el modelo el que manda
        getButtonModel().setEnabled(b);
    }

    public void setDOMEnabled(boolean b)
    {
        // Nada que hacer
    }

    public void processNormalEvent(Event evt)
    {
        if (!buttonDelegate.handleEvent(evt))
            return;

        super.processNormalEvent(evt);
    }

    public void blur()
    {
        ItsNatStfulDocumentImpl itsNatDoc = (ItsNatStfulDocumentImpl)getItsNatDocumentImpl();
        JSRenderMethodCallImpl.addCallMethodHTMLFormControlCode(getHTMLElement(),"blur",itsNatDoc);
    }

    public void focus()
    {
        ItsNatStfulDocumentImpl itsNatDoc = (ItsNatStfulDocumentImpl)getItsNatDocumentImpl();
        JSRenderMethodCallImpl.addCallMethodHTMLFormControlCode(getHTMLElement(),"focus",itsNatDoc);
    }

    public Object createDefaultModelInternal()
    {
        return createDefaultButtonModel();
    }

    public ButtonModel createDefaultButtonModel()
    {
        return new DefaultButtonModel();
    }

    public ItsNatButtonUI getItsNatButtonUI()
    {
        return (ItsNatButtonUI)compUI;
    }

    public ParamTransport[] getInternalParamTransports(String type,ClientDocumentImpl clientDoc)
    {
        return null;
    }

    public Node createDefaultNode()
    {
        return (HTMLAnchorElement)getItsNatDocument().getDocument().createElementNS(NamespaceUtil.XHTML_NAMESPACE,"a");
    }

}
