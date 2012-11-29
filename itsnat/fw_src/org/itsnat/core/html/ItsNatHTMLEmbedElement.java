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

package org.itsnat.core.html;

import org.w3c.dom.Document;
import org.w3c.dom.html.HTMLElement;

/**
 * This interface is implemented by HTML embed objects to provide
 * access to the child document loaded if any.
 *
 * @author Jose Maria Arranz Santamaria
 */
public interface ItsNatHTMLEmbedElement extends HTMLElement
{
    /**
     * Returns the child document loaded by this node.
     *
     * @return the child document loaded by this node. May be null.
     * @see org.itsnat.core.ItsNatDocument#getContainerNode()
     */
    public Document getContentDocument();
}
