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

package org.itsnat.impl.comp.inplace;

import java.io.Serializable;
import org.itsnat.comp.ItsNatComponent;
import org.itsnat.comp.ItsNatHTMLElementComponent;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.comp.mgr.web.ItsNatHTMLDocComponentManagerImpl;
import org.itsnat.impl.comp.mgr.ItsNatStfulDocComponentManagerImpl;
import org.itsnat.impl.comp.mgr.web.ItsNatXULDocComponentManagerImpl;
import org.itsnat.impl.core.browser.web.BrowserWeb;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.scriptren.jsren.JSRenderMethodCallImpl;
import org.w3c.dom.Element;

/**
 *
 * @author jmarranz
 */
public abstract class DelegateComponentEditorImpl implements Serializable
{
    protected ItsNatComponent compEditor;

    public DelegateComponentEditorImpl(ItsNatComponent compEditor)
    {
        this.compEditor = compEditor;
    }

    public static DelegateComponentEditorImpl createDelegateComponentEditor(ItsNatComponent compEditor,ItsNatCellEditorImpl parent)
    {
        if (compEditor != null)
        {
            if (compEditor instanceof ItsNatHTMLElementComponent)
                return DelegateHTMLElementComponentEditorImpl.createDelegateHTMLElementComponentEditor((ItsNatHTMLElementComponent)compEditor);
            else // Ejemplo: futuros componentes XUL
                throw new ItsNatException("This component is not supported as editor: " + compEditor,compEditor);
        }
        else
        {
            ItsNatStfulDocComponentManagerImpl compMgr = parent.getItsNatStfulDocComponentManager();
            if (compMgr instanceof ItsNatHTMLDocComponentManagerImpl)
            {
                // Lo m�s normal es que el editor vaya a un elemento HTML
                return new DelegateHTMLInputTextEditorImpl(((ItsNatHTMLDocComponentManagerImpl)compMgr).createItsNatHTMLInputText(null,null));
            }
            else if (compMgr instanceof ItsNatXULDocComponentManagerImpl)
            {
                 // El futuro componente de texto XUL ir�a aqu�
                throw new ItsNatException("Cannot be created a default component editor for this type of document");
            }
            else // SVG, no tiene controles de edici�n propios
                throw new ItsNatException("Cannot be created a default component editor for this type of document");
        }
    }

    public abstract Object getCellEditorValue();
    public abstract void preSetValue(Object value);
    public abstract void setValue(Object value);

    public ItsNatComponent getCellEditorComponent()
    {
        return compEditor;
    }

    public void setFocus()
    {
        ItsNatStfulDocumentImpl itsNatDoc = (ItsNatStfulDocumentImpl)compEditor.getItsNatDocument();
        Element elem = (Element)compEditor.getNode(); // S�lo admitimos elementos por ahora

        JSRenderMethodCallImpl render = JSRenderMethodCallImpl.getJSRenderMethodCall(elem);

        ClientDocumentStfulImpl[] allClient = itsNatDoc.getAllClientDocumentStfulsCopy();
        for(int i = 0; i < allClient.length; i++)
        {
            ClientDocumentStfulImpl clientDoc = allClient[i];
            if (!(clientDoc.getClientDocumentStfulDelegate() instanceof ClientDocumentStfulDelegateWebImpl)) continue; // Por si acaso
            ClientDocumentStfulDelegateWebImpl clientDocDeleg = (ClientDocumentStfulDelegateWebImpl)clientDoc.getClientDocumentStfulDelegate();
            BrowserWeb browser = clientDocDeleg.getBrowserWeb();
            if (clientDoc.isSendCodeEnabled() && !render.isFocusOrBlurMethodWrong("focus",elem,browser))
            {
                // S�lo generamos la llamada a focus() si es procesada correctamente
                // si no lo es el usuario tendr� que pulsar el elemento, no pasa nada por ello
                // En esos casos no tiene sentido enviar un evento focus "manualmente" pues ya lo har� el usuario
                // cuando pulse elemento as� evitamos enviar dos eventos focus.
                String code = JSRenderMethodCallImpl.getCallFormControlFocusBlurCodeDefault(elem, "focus", true, clientDocDeleg);
                clientDoc.addCodeToSend( code );
            }
        }
    }
}
