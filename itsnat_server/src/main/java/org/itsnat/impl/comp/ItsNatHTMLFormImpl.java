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

package org.itsnat.impl.comp;

import javax.swing.ButtonModel;
import org.itsnat.comp.ItsNatComponentUI;
import org.itsnat.comp.ItsNatHTMLForm;
import org.itsnat.core.NameValue;
import org.itsnat.core.event.ParamTransport;
import org.itsnat.impl.comp.listener.ItsNatCompNormalEventListenersByClientDefaultImpl;
import org.itsnat.impl.comp.listener.ItsNatCompNormalEventListenersByClientImpl;
import org.itsnat.impl.comp.listener.ItsNatCompNormalEventListenersByDocDefaultImpl;
import org.itsnat.impl.comp.listener.ItsNatCompNormalEventListenersByDocImpl;
import org.itsnat.impl.comp.mgr.web.ItsNatStfulWebDocComponentManagerImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.domutil.NamespaceUtil;
import org.itsnat.impl.core.scriptren.jsren.JSRenderMethodCallImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLFormElement;

/**
 *
 * @author jmarranz
 */
public class ItsNatHTMLFormImpl extends ItsNatHTMLElementComponentImpl implements ItsNatHTMLForm
{
    protected boolean enabled = true;

    /**
     * Creates a new instance of ItsNatHTMLFormImpl
     */
    public ItsNatHTMLFormImpl(HTMLFormElement element,NameValue[] artifacts,ItsNatStfulWebDocComponentManagerImpl componentMgr)
    {
        super(element,artifacts,componentMgr);

        init();
    }

    public ItsNatCompNormalEventListenersByDocImpl createItsNatCompNormalEventListenersByDoc()
    {
        return new ItsNatCompNormalEventListenersByDocDefaultImpl(this);
    }

    public ItsNatCompNormalEventListenersByClientImpl createItsNatCompNormalEventListenersByClient(ClientDocumentImpl clientDoc)
    {
        return new ItsNatCompNormalEventListenersByClientDefaultImpl(this,clientDoc);
    }

    @Override
    public void enableEventListenersByDoc()
    {
        super.enableEventListenersByDoc();

        // No activamos nada porque por defecto no se hace nada
        // cuando el usuario a�ada un listener se activar�n autom�ticamente

        //enableEventListener("submit");
        //enableEventListener("reset");
    }

    public HTMLFormElement getHTMLFormElement()
    {
        return (HTMLFormElement)node;
    }

    public ItsNatComponentUI createDefaultItsNatComponentUI()
    {
        return null;
        // return new ItsNatHTMLFormUIImpl(this);
    }

    public void bindDataModel()
    {
        // No hay modelo
    }

    public void unbindDataModel()
    {
        // No hay modelo
    }

    public void submit()
    {
        ItsNatStfulDocumentImpl itsNatDoc = (ItsNatStfulDocumentImpl)getItsNatDocumentImpl();
        JSRenderMethodCallImpl.addCallMethodCode(getHTMLElement(),"submit",null,true,itsNatDoc);
    }

    public void reset()
    {
        ItsNatStfulDocumentImpl itsNatDoc = (ItsNatStfulDocumentImpl)getItsNatDocumentImpl();
        JSRenderMethodCallImpl.addCallMethodCode(getHTMLElement(),"reset",null,true,itsNatDoc);
    }

    public Object createDefaultModelInternal()
    {
        return null; // No hay modelo
    }

    public ButtonModel createDefaultButtonModel()
    {
        return null; // No hay modelo
    }

    public ParamTransport[] getInternalParamTransports(String type,ClientDocumentImpl clientDoc)
    {
        return null;
    }

    public void initialSyncUIWithDataModel()
    {
        // no hay modelo
    }

    public Node createDefaultNode()
    {
        Document doc = getItsNatDocument().getDocument();
        HTMLFormElement form = (HTMLFormElement)doc.createElementNS(NamespaceUtil.XHTML_NAMESPACE,"form");
        return form;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean b)
    {
        this.enabled = b;
    }
}
