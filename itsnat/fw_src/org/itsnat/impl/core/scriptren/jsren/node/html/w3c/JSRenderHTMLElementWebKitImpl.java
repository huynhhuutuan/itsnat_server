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

import org.itsnat.impl.core.browser.web.webkit.BrowserWebKit;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;

/**
 *
 * @author jmarranz
 */
public class JSRenderHTMLElementWebKitImpl extends JSRenderHTMLElementW3CImpl
{
    public static final JSRenderHTMLElementWebKitImpl SINGLETON = new JSRenderHTMLElementWebKitImpl();
    
    /** Creates a new instance of JSMSIEHTMLElementRenderImpl */
    public JSRenderHTMLElementWebKitImpl()
    {
        // A partir de la lista de MSIE probados uno a uno en Safari

        // En algunos tags es que no se admiten elementos hijos seg�n el standard, pero ni siquiera
        // se tolera asignar espacios usando innerHTML (caso de COL, IMG,INPUT) ni siquiera un: elem.innerHTML = "";
        tagNamesWithoutInnerHTML.add("col");
        tagNamesWithoutInnerHTML.add("colgroup");
        // No probado: tagNamesWithoutInnerHTML.add("frameset");
        tagNamesWithoutInnerHTML.add("style");
        // Safari via DOM acepta el contenido de <style> pero a�n as� no se aplican los estilos, esto es porque <style> debe estar dentro de <head> para Safari: http://youngisrael-stl.org/wordpress/index.php/2007/10/02/ajax-style-elements-and-safari/
        tagNamesWithoutInnerHTML.add("title");

        tagNamesWithoutInnerHTML.add("img"); // No admite siquiera un innerHTML = ""
        tagNamesWithoutInnerHTML.add("input"); // Idem

        /* Caso <style> dentro de innerHTML:
           Safari acepta el nodo pero el estilo no se aplica, pero tampoco se aplica via DOM
           por lo que aceptamos usar con innerHTML.
           Idem S60WebKit.
         */
    }

    public static JSRenderHTMLElementWebKitImpl getJSRenderHTMLElementWebKit(BrowserWebKit browser)
    {
        return JSRenderHTMLElementWebKitImpl.SINGLETON; // Los dem�s
    }

    public String getCurrentStyleObject(String itsNatDocVar,String elemName,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        return itsNatDocVar + ".win.getComputedStyle(" + elemName + ", null)";
    }
}
