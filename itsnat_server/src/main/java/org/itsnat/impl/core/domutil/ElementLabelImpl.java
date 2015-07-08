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

package org.itsnat.impl.core.domutil;

import org.itsnat.core.ItsNatException;
import org.itsnat.core.domutil.ElementLabel;
import org.itsnat.core.domutil.ElementLabelRenderer;
import org.itsnat.impl.core.doc.ItsNatDocumentImpl;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 *
 * @author jmarranz
 */
public class ElementLabelImpl extends ElementGroupImpl implements ElementLabel
{
    protected Element parentElement;
    protected ElementLabelRenderer renderer;
    protected DocumentFragment contentPatternFragment; // Ser� recordado como patr�n, nunca es null pero puede estar vac�o
    protected boolean usePatternMarkupToRender;
    protected boolean hasLabel = false;

    /** Creates a new instance of ElementLabelImpl */
    public ElementLabelImpl(ItsNatDocumentImpl itsNatDoc,Element parentElement,boolean removePattern,ElementLabelRenderer renderer)
    {
        super(itsNatDoc);

        this.parentElement = parentElement;
        this.renderer = renderer;
        this.usePatternMarkupToRender = itsNatDoc.isUsePatternMarkupToRender();

        if (removePattern)
        {
            this.contentPatternFragment = DOMUtilInternal.extractChildrenToDocFragment(parentElement);
            // Hasta que no se defina un value expl�citamente el label est� vac�o
        }
        else
        {
            Element clonedParentElem = (Element)parentElement.cloneNode(true);
            this.contentPatternFragment = DOMUtilInternal.extractChildrenToDocFragment(clonedParentElem);
            this.hasLabel = true; // De esta manera evitamos que se use el patr�n para llenar el contenido la primera vez, pues el contenido original ya est� ah�
            // El contenido original queda como est�, el patr�n est� clonado del original
            // y se usar� si usePatternMarkupToRender es true
        }
    }

    public Element getParentElement()
    {
        return parentElement;
    }

    public ElementLabelRenderer getElementLabelRenderer()
    {
        return renderer;
    }

    public void setElementLabelRenderer(ElementLabelRenderer renderer)
    {
        this.renderer = renderer;
    }

    public DocumentFragment getContentPatternFragment()
    {
        return contentPatternFragment;
    }

    public boolean hasLabelMarkup()
    {
        return hasLabel;
    }

    public void setLabelValue(Object value)
    {
        if (!hasLabel)
        {
            // Definido por vez primera expl�citamente, en este caso
            // inicialmente el contenido del label est� vac�o, lo llenamos con el pattern
            addLabelMarkup(value);
        }
        else
        {
            setElementValue(value,false);
        }
    }

    public void setElementValue(Object value,boolean isNew)
    {
        prepareRendering(isNew);

        Element parent = getParentElement();
        ElementLabelRenderer renderer = getElementLabelRenderer();
        if (renderer != null)
            renderer.renderLabel(this,value,parent,isNew);
    }

    public void addLabelMarkup()
    {
        if (hasLabel) throw new ItsNatException("Label already has markup",this);

        Element parent = getParentElement();
        parent.appendChild(contentPatternFragment.cloneNode(true));
        this.hasLabel = true;
    }

    public void addLabelMarkup(Object value)
    {
        addLabelMarkup();
        setElementValue(value,true);
    }

    public void removeLabelMarkup()
    {
        Element parent = getParentElement();
        ElementLabelRenderer renderer = getElementLabelRenderer();
        if (renderer != null) // If null rendering disabled
            renderer.unrenderLabel(this,parent);

        DOMUtilInternal.removeAllChildren(parent); // Si est� ya vac�o no hace nada obviamente
        this.hasLabel = false;
    }

    public void prepareRendering(boolean isNew)
    {
        if (!isNew && isUsePatternMarkupToRender())
        {
            // Es una actualizaci�n en donde tenemos que usar el markup pattern en vez del contenido actual
            Element parent = getParentElement();
            restorePatternMarkupWhenRendering(parent,getContentPatternFragment());
        }
    }

    public boolean isUsePatternMarkupToRender()
    {
        return usePatternMarkupToRender;
    }

    public void setUsePatternMarkupToRender(boolean usePatternMarkupToRender)
    {
        this.usePatternMarkupToRender = usePatternMarkupToRender;
    }
}
