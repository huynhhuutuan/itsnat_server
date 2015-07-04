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

import org.itsnat.impl.core.browser.web.BrowserGecko;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;

/**
 *
 * @author jmarranz
 */
public class JSRenderHTMLElementGeckoImpl extends JSRenderHTMLElementW3CImpl
{
    public static final JSRenderHTMLElementGeckoImpl SINGLETON = new JSRenderHTMLElementGeckoImpl();
    
    /** Creates a new instance of JSMSIEHTMLElementRenderImpl */
    public JSRenderHTMLElementGeckoImpl()
    {
        // En FireFox seg�n el enlace:
        // http://developer.mozilla.org/en/docs/DOM:element.innerHTML
        // s�lo los nodos de tablas excepto TD no pueden usar innerHTML
        // El caso es que he probado: TABLE, TBODY, TFOOT, THEAD y TR, COLGROUP
        // y todos funcionan bien con innerHTML, incluso en FireFox 1.0 (probado),
        // yo creo que es una recomendaci�n por compatibilidad con MSIE.
        // El �nico que no funciona bien es COL, pero este nodo en teor�a
        // no admite hijos el problema es que si hay un simple espacio en el innerHTML
        // inserta un <col> err�neo como hijo

        tagNamesWithoutInnerHTML.add("col");
        // No probado: tagNamesWithoutInnerHTML.add("frameset");

        /* Caso <style> dentro de innerHTML: funciona bien, el estilo se aplica.
         */
    }

    public static JSRenderHTMLElementGeckoImpl getJSRenderHTMLElementGeckoImpl(BrowserGecko browser)
    {
         return JSRenderHTMLElementGeckoImpl.SINGLETON;
    }

    @Override    
    public String getCurrentStyleObject(String itsNatDocVar,String elemName,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        return itsNatDocVar + ".win.getComputedStyle(" + elemName + ", null)";
    }
}

