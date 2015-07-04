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

package org.itsnat.impl.core.resp.shared.bybrow.web;

import org.itsnat.impl.core.resp.shared.bybrow.web.ResponseDelegStfulLoadDocByBW3CImpl;
import org.itsnat.impl.core.resp.shared.*;

/**
 *
 * @author jmarranz
 */
public class ResponseDelegStfulLoadDocByBWebKitImpl extends ResponseDelegStfulLoadDocByBW3CImpl
{
    public ResponseDelegStfulLoadDocByBWebKitImpl(ResponseDelegateStfulWebLoadDocImpl parent)
    {
        super(parent);
    }

    public String getOnInitScriptContentCodeFixDOMCode()
    {
        return null;
    }

    @Override
    public boolean getRevertJSChanges()
    {
        // Los navegadores WebKit rellenan los formularios con valores
        // antiguos al volver a la p�gina con un back, incluso cuando se recarga la p�gina desde el servidor
        // (pues la p�gina NO se cachea sin embargo los valores de los formularios S�)
        // Esto incluye controles tal y como <input type="button"> que por el usuario no pueden cambiarse
        // pero via c�digo pudieron ser cambiados en el estado �ltimo de la p�gina.

        // El S60WebKit no tiene autofill sin embargo el WebKit usado es muy antiguo y es
        // esperable que en versiones futuras lo tenga.
        return true;
    }

}
