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
import org.itsnat.impl.core.scriptren.jsren.node.html.JSRenderHTMLElementImpl;
import org.itsnat.impl.core.scriptren.jsren.node.html.w3c.JSRenderHTMLElementWebKitImpl;
import org.w3c.dom.html.HTMLElement;

/**
 * Normalmente ser� el caso de un robot, los robots no suelen interpretar
 * JavaScript y por tanto no tienen eventos.
 * No se guardar� el documento en el servidor (como si fuera AJAX disabled).
 *
 * Suponemos que es W3C respecto a todo lo dem�s
 *
 * @author jmarranz
 */
public class BrowserUnknown extends BrowserW3C
{

    /** Creates a new instance of BrowserUnknown */
    public BrowserUnknown(String userAgent)
    {
        super(userAgent);

        this.browserType = UNKNOWN;
    }

    @Override
    public boolean isMobile()
    {
        return false;
    }

    @Override
    public boolean hasBeforeUnloadSupportHTML()
    {
        return false;
    }     
    
    @Override
    public boolean isReferrerReferenceStrong()
    {
        // Si es un robot evitamos guardar referencias fuertes.
        return false;
    }

    @Override
    public boolean isCachedBackForward()
    {
        // Es un robot, nos da igual.
        return false;
    }

    @Override
    public boolean isCachedBackForwardExecutedScripts()
    {
        // Si es un robot no tendr� mucho sentido hablar de back/forward
        // pero devolviendo false evitamos la generaci�n de c�digo de recarga etc
        return false;
    }

    @Override    
    public boolean isDOMContentLoadedSupported()
    {
        // Nos da igual pues no hay eventos.
        return false;
    }

    @Override
    public boolean isFocusOrBlurMethodWrong(String methodName,HTMLElement formElem)
    {
        return false;
    }

    @Override
    public Map<String,String[]> getHTMLFormControlsIgnoreZIndex()
    {
        return null;
    }


    @Override
    public boolean canNativelyRenderOtherNSInXHTMLDoc()
    {
        return true; // Por poner algo.
    }

    @Override
    public boolean isInsertedSVGScriptNotExecuted()
    {
        return false; // Por poner algo
    }

    @Override
    public boolean isTextAddedToInsertedSVGScriptNotExecuted()
    {
        return false; // Por poner algo
    }

    @Override
    public JSRenderHTMLElementImpl getJSRenderHTMLElementSingleton() 
    {
        return null; // Por poner algo
    }    
}
