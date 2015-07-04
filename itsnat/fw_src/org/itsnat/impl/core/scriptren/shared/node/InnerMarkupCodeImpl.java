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

package org.itsnat.impl.core.scriptren.shared.node;

import java.lang.ref.WeakReference;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.clientdoc.ClientDocumentImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.w3c.dom.Element;

/**
 *
 * @author jmarranz
 */
public class InnerMarkupCodeImpl
{
    protected RenderElement render;
    protected WeakReference<Element> parentNodeRef; // No usamos una referencia normal pues supondria sujetar nodos in�tilmente, pues cuando es usada todav�a forma parte del documento y est� sujeta por referencias normales, si se pierde no pasa nada porque devuelva null, no se usa para renderizar s�lo para a�adir nuevos trozos (implica que sigue referenciado)
    protected String parentNodeLocator;
    protected boolean useNodeLocation;
    protected StringBuilder innerMarkup = new StringBuilder();

    public InnerMarkupCodeImpl(RenderElement render,Element parentNode,String parentNodeLocator,boolean useNodeLocation,String firstInnerMarkup)
    {
        this.render = render;
        this.parentNodeRef = new WeakReference<Element>(parentNode);
        this.parentNodeLocator = parentNodeLocator;
        this.useNodeLocation = useNodeLocation;        
        innerMarkup.append(firstInnerMarkup);
    }

    public Element getParentNode()
    {
        return parentNodeRef.get();
    }

    public String getParentNodeLocator()
    {
        return parentNodeLocator;
    }

    public boolean isUseNodeLocation()
    {
        return useNodeLocation;
    }

    public void addInnerMarkup(String newInnerMarkup)
    {
        innerMarkup.append(newInnerMarkup);
    }

    public String getInnerMarkup()
    {
        return innerMarkup.toString();
    }

    @Override
    public String toString()
    {
        throw new ItsNatException("INTERNAL ERROR");
    }
    
    public String render(ClientDocumentImpl clientDoc)
    {
        ClientDocumentStfulDelegateImpl clientDocDeleg = (ClientDocumentStfulDelegateImpl)((ClientDocumentStfulImpl)clientDoc).getClientDocumentStfulDelegate();
        return render.getAppendChildrenCodeAsMarkupSentence(this,clientDocDeleg);
    }

}
