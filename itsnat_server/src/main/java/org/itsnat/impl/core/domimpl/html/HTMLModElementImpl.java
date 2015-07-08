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

package org.itsnat.impl.core.domimpl.html;

import org.itsnat.impl.core.domimpl.DocumentImpl;
import org.w3c.dom.html.HTMLModElement;

/**
 *
 * @author jmarranz
 */
public class HTMLModElementImpl extends HTMLElementImpl implements HTMLModElement
{
    protected HTMLModElementImpl()
    {
    }

    public HTMLModElementImpl(String name,DocumentImpl owner)
    {
        super(name,owner);
    }

    protected HTMLElementImpl newHTMLElement()
    {
        return new HTMLModElementImpl();
    }

    public String getCite()
    {
        return getAttribute( "cite" );
    }

    public void setCite( String cite )
    {
        setAttribute( "cite", cite );
    }

    public String getDateTime()
    {
        return getAttribute( "datetime" );
    }

    public void setDateTime( String dateTime )
    {
        setAttribute( "datetime", dateTime );
    }
}
