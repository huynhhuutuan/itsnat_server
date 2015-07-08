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

package org.itsnat.impl.comp.label;

import org.itsnat.impl.comp.listener.ItsNatCompNormalEventListenersByDocDefaultImpl;
import org.itsnat.impl.comp.listener.ItsNatCompNormalEventListenersByClientImpl;
import org.itsnat.impl.comp.listener.ItsNatCompNormalEventListenersByDocImpl;
import org.itsnat.impl.comp.listener.ItsNatCompNormalEventListenersByClientDefaultImpl;
import org.itsnat.impl.comp.mgr.ItsNatDocComponentManagerImpl;
import org.itsnat.impl.comp.*;
import java.beans.PropertyVetoException;
import org.itsnat.comp.label.ItsNatLabel;
import org.itsnat.comp.label.ItsNatLabelEditor;
import org.itsnat.comp.label.ItsNatLabelRenderer;
import org.itsnat.comp.ItsNatComponentUI;
import org.itsnat.comp.label.ItsNatLabelUI;
import org.itsnat.core.event.ParamTransport;
import org.itsnat.core.NameValue;
import org.itsnat.impl.comp.label.ItsNatLabelUIImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentImpl;
import org.w3c.dom.Element;

/**
 *
 * @author jmarranz
 */
public abstract class ItsNatLabelImpl extends ItsNatElementComponentImpl implements ItsNatLabel
{
    protected boolean enabled = true;
    protected ItsNatLabelRenderer renderer;
    protected LabelEditorProcessor editorProcessor = new LabelEditorProcessor(this);
    protected Object oldValue;

    /**
     * Creates a new instance of ItsNatLabelImpl
     */
    public ItsNatLabelImpl(Element element,NameValue[] artifacts,ItsNatDocComponentManagerImpl componentMgr)
    {
        super(element,artifacts,componentMgr);
    }

    public void init()
    {
        super.init();

        ItsNatDocComponentManagerImpl compMgr = getItsNatComponentManagerImpl();
        setItsNatLabelRenderer(compMgr.createDefaultItsNatLabelRenderer());
    }

    public ItsNatCompNormalEventListenersByDocImpl createItsNatCompNormalEventListenersByDoc()
    {
        return new ItsNatCompNormalEventListenersByDocDefaultImpl(this);
    }

    public ItsNatCompNormalEventListenersByClientImpl createItsNatCompNormalEventListenersByClient(ClientDocumentImpl clientDoc)
    {
        return new ItsNatCompNormalEventListenersByClientDefaultImpl(this,clientDoc);
    }

    public void enableEventListenersByDoc()
    {
        super.enableEventListenersByDoc();

        // No hay eventos propios del label

        editorProcessor.enableEventListenersByDoc();
    }

    public void disableEventListenersByDoc(boolean updateClient)
    {
        super.disableEventListenersByDoc(updateClient);

        editorProcessor.disableEventListeners(updateClient);
    }

    public ItsNatLabelUI getItsNatLabelUI()
    {
        return (ItsNatLabelUI)compUI;
    }

    public ItsNatLabelUIImpl getItsNatLabelUIImpl()
    {
        return (ItsNatLabelUIImpl)compUI;
    }

    public ItsNatComponentUI createDefaultItsNatComponentUI()
    {
        return new ItsNatLabelUIImpl(this);
    }
    
    public Object createDefaultStructure()
    {
        return null;
    }

    public LabelEditorProcessor getLabelEditorProcessor()
    {
        return editorProcessor;
    }

    public void bindDataModel()
    {
        // Nada que hacer
    }

    public void unbindDataModel()
    {
        // Nada que hacer
    }

    public void initialSyncUIWithDataModel()
    {
        // No se llama hasta que se llama a setValue(Object) expl�citamente
        ItsNatLabelUIImpl labelUI = getItsNatLabelUIImpl();
        Object value = getValue();
        if (value != null)
        {
            if (oldValue == null)
                labelUI.addLabelMarkup(value);
            else
                labelUI.setLabelValue(value);
        }
        else labelUI.removeLabelMarkup();
    }

    public Object getValue()
    {
        return getDataModel();
    }

    public void setValue(Object value) throws PropertyVetoException
    {
        Object oldValue = getValue();

        fireVetoableChange("value", oldValue, value); // Si se produce una excepci�n no se cambiar� el valor. Es interesante por ejemplo cuando la etiqueta es cambiada por el usuario via edici�n, as� podemos filtrar valores v�lidos/no v�lidos

        this.oldValue = oldValue;
        setDataModel(value,true); // Se acepta el caso null
        this.oldValue = getValue();

        // Como el data model es un simple objeto as� tenemos notificaci�n de cuando cambia
        firePropertyChange("value", oldValue, value);  // Si son iguales no se lanza
    }

    public Object createDefaultModelInternal()
    {
        return null;  // Hace que no se intente renderizar quedando vac�o el label
    }

    public ParamTransport[] getInternalParamTransports(String type,ClientDocumentImpl clientDoc)
    {
        return null;
    }

    public ItsNatLabelRenderer getItsNatLabelRenderer()
    {
        return renderer;
    }

    public void setItsNatLabelRenderer(ItsNatLabelRenderer renderer)
    {
        this.renderer = renderer;
    }

    public ItsNatLabelEditor getItsNatLabelEditor()
    {
        LabelEditorProcessor editorProcessor = getLabelEditorProcessor();
        return editorProcessor.getItsNatLabelEditor();
    }

    public void setItsNatLabelEditor(ItsNatLabelEditor cellEditor)
    {
        editorProcessor.setItsNatLabelEditor(cellEditor);
    }

    public void startEditing()
    {
        getLabelEditorProcessor().startEdition();
    }

    public boolean isEditing()
    {
        return getLabelEditorProcessor().isEditing();
    }

    public String getEditorActivatorEvent()
    {
        return getLabelEditorProcessor().getEditorActivatorEvent();
    }

    public void setEditorActivatorEvent(String editorActivatorEvent)
    {
        getLabelEditorProcessor().setEditorActivatorEvent(editorActivatorEvent);
    }

    public boolean isEditingEnabled()
    {
        return getLabelEditorProcessor().isEditingEnabled();
    }

    public void setEditingEnabled(boolean value)
    {
        getLabelEditorProcessor().setEditingEnabled(value);
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
