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

package org.itsnat.impl.core.browser.web;

import java.util.Map;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.domutil.DOMUtilHTML;
import org.w3c.dom.html.HTMLElement;

/**
 * http://msdn.microsoft.com/en-us/ie/ff468705.aspx
 * 
 * @author jmarranz
 */
public class BrowserMSIE9 extends BrowserW3C
{
    protected int version; // Por ahora no lo usamos, ser� 9 o mayor (futuras versiones

    /** Creates a new instance of BrowserUnknown */
    public BrowserMSIE9(String userAgent,int version)
    {
        super(userAgent);

        this.browserType = MSIE_9;
        this.version = version;
    }

    public boolean isMobile()
    {
        return false;
    }

    public boolean isReferrerReferenceStrong()
    {
        return true;  // Estudiar aunque true es la opci�n m�s segura
    }

    public boolean isCachedBackForward()
    {
        return false;
    }

    public boolean isCachedBackForwardExecutedScripts()
    {
        return false;
    }

    public boolean isDOMContentLoadedSupported()
    {
        return true;
    }

    public boolean isFocusOrBlurMethodWrong(String methodName,HTMLElement formElem)
    {
         // ESTUDIAR, esta es la opci�n m�s conservadora, copiado de MSIE < 9
        return DOMUtilHTML.isHTMLTextAreaOrInputTextBox(formElem);
    }

    public Map<String,String[]> getHTMLFormControlsIgnoreZIndex()
    {
        return null;
    }
    
    public boolean isBlurBeforeChangeEvent(HTMLElement formElem)
    {
        return false;
    }

    public boolean hasHTMLCSSOpacity()
    {
        return true;
    }

    public boolean canNativelyRenderOtherNSInXHTMLDoc()
    {
        return true; 
    }

    public boolean isInsertedSVGScriptNotExecuted()
    {
        return true; // Revisar en la versi�n final de IE 9
    }

    public boolean isTextAddedToInsertedSVGScriptNotExecuted()
    {
        return true; // Revisar en la versi�n final de IE 9
    }

    public boolean isClientWindowEventTarget()
    {
        return true;
    }
}
