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

import org.itsnat.comp.button.normal.ItsNatHTMLInputReset;
import org.itsnat.core.NameValue;
import org.itsnat.impl.comp.mgr.web.ItsNatStfulWebDocComponentManagerImpl;
import org.w3c.dom.html.HTMLInputElement;

/**
 * La principal finalidad de este componente es permitir cancelar
 * el env�o del formulario al pulsar el bot�n
 * Aunque un tipo "reset" o "submit" generen un reset o submit ante un click dicho evento
 * no es recogido por el propio bot�n, es decir onreset y onsubmit no son llamados
 * (es el <form> el que lo recibe)
 *
 * @author jmarranz
 */
public class ItsNatHTMLInputResetImpl extends ItsNatHTMLInputButtonTextImpl implements ItsNatHTMLInputReset
{
    /**
     * Creates a new instance of ItsNatHTMLInputButtonTextImpl
     */
    public ItsNatHTMLInputResetImpl(HTMLInputElement element,NameValue[] artifacts,ItsNatStfulWebDocComponentManagerImpl componentMgr)
    {
        super(element,artifacts,componentMgr);

        init();
    }

    public String getExpectedType()
    {
        return "reset";
    }

}
