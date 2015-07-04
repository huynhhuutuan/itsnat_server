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

package org.itsnat.impl.core.scriptren.jsren.node.html.w3c;

import org.itsnat.impl.core.browser.web.BrowserBatik;
import org.itsnat.impl.core.browser.web.BrowserW3C;
import org.itsnat.impl.core.scriptren.jsren.node.html.JSRenderHTMLPropertyImpl;

/**
 *
 * @author jmarranz
 */
public abstract class JSRenderHTMLPropertyW3CImpl extends JSRenderHTMLPropertyImpl
{
    /** Creates a new instance of JSRenderHTMLPropertyW3CDefaultImpl */
    public JSRenderHTMLPropertyW3CImpl()
    {
    }

    public static JSRenderHTMLPropertyW3CImpl getJSRenderHTMLPropertyW3C(BrowserW3C browser)
    {
        if (browser instanceof BrowserBatik)
            return null; // A d�a de hoy Batik no renderiza <foreigObject> y el intento de definir propiedades necesitar�a el uso de setUserData porque el elemento no es ScriptableObject, as� por ahora devolvemos nulo pues no vale la pena, devolver nulo es v�lido
        else
            return JSRenderHTMLPropertyW3CDefaultImpl.SINGLETON;
    }
}
