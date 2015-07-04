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

package org.itsnat.impl.core.scriptren.jsren.node.html;

import org.itsnat.impl.core.browser.web.BrowserMSIEOld;
import org.itsnat.impl.core.browser.web.BrowserWeb;
import org.itsnat.impl.core.scriptren.jsren.node.JSRenderTextImpl;
import org.itsnat.impl.core.scriptren.jsren.node.html.msie.JSRenderHTMLTextMSIEOldImpl;


/**
 *
 * @author jmarranz
 */
public abstract class JSRenderHTMLTextImpl extends JSRenderTextImpl
{
    /**
     * Creates a new instance of JSRenderXMLTextDefaultImpl
     */
    public JSRenderHTMLTextImpl()
    {
    }

    public static JSRenderHTMLTextImpl getJSRenderHTMLText(BrowserWeb browser)
    {
        // Evitamos as� buscar el render una y otra vez pues hay muchos navegadores.
        JSRenderHTMLTextImpl render = browser.getJSRenderHTMLText();
        if (render != null)
            return render;

        if (browser instanceof BrowserMSIEOld)
            render = JSRenderHTMLTextMSIEOldImpl.getJSRenderHTMLTextMSIEOld((BrowserMSIEOld)browser);
        else
            render = JSRenderHTMLTextDefaultImpl.SINGLETON;

        browser.setJSRenderHTMLText(render);
        return render;
    }

}
