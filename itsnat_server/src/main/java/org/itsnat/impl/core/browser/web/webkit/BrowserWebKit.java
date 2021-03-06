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

package org.itsnat.impl.core.browser.web.webkit;

import org.itsnat.impl.core.browser.web.BrowserW3C;
import org.itsnat.impl.core.domutil.DOMUtilHTML;
import org.itsnat.impl.core.scriptren.jsren.node.html.JSRenderHTMLElementImpl;
import org.itsnat.impl.core.scriptren.jsren.node.html.msie.JSRenderHTMLElementMSIEOldImpl;
import org.itsnat.impl.core.scriptren.jsren.node.html.w3c.JSRenderHTMLElementWebKitImpl;
import org.w3c.dom.html.HTMLElement;

/**
   * De acuerdo con el sistema de versiones de WebKit
    (ejemplo: https://bugs.webkit.org/show_bug.cgi?id=17754, ver combo "Version")
    las �ltimas versiones importantes de WebKit son:

    413  usada por S60WebKit
    420+ parece ser la versi�n usada por el primer firmware iPhone
    523.x Safari 3.0
    525.x Safari 3.1
    528.x Safari 4 beta
    531.9 Safari 4.0.3
 
   Una tabla similar se encuentra en (no estoy de acuerdo en algunos casos):
   http://en.wikipedia.org/wiki/WebKit
 
 * @author jmarranz
 */
public abstract class BrowserWebKit extends BrowserW3C
{
    // SubType dentro de los WebKit:
    // Los n�meros se alternan porque se han eliminado varios subtipos
    protected static final int SAFARIDESKTOP = 1;
    protected static final int IPHONE = 2;
    protected static final int ANDROID = 3;
    protected static final int GCHROME = 7;
    protected static final int OPERA = 13;
    
    protected int webKitVersion;

    /** Creates a new instance of BrowserWebKit */
    public BrowserWebKit(String userAgent,int browserSubType)
    {
        super(userAgent);

        this.browserType = WEBKIT;
        this.browserSubType = browserSubType;

        // Versi�n del WebKit:
        try
        {
            int start = userAgent.indexOf("WebKit/");
            start += "WebKit/".length();
            int end = start;
            while(true)
            {
                char c = userAgent.charAt(end);
                if ((c == '.') || (c == '+') || (c == ' '))
                    break;
                end++;
            }
            String strVer = userAgent.substring(start,end);
            this.webKitVersion = Integer.parseInt(strVer);
        }
        catch(Exception ex) // Caso de user agent de formato desconocido
        {
            this.webKitVersion = 0;
        }
    }

    public static BrowserWebKit createBrowserWebKit(String userAgent)
    {
        if (userAgent.contains("OPR/")) // Lo llamamos ANTES de Chrome porque Chrome se incluye en el user agent
            return new BrowserWebKitOpera(userAgent);         
        else if (userAgent.contains("Chrome")) // Debe chequearse antes que "Android" porque incluimos el Chrome de Android
            return new BrowserWebKitChrome(userAgent);                  
        else if (userAgent.contains("Android"))
            return new BrowserWebKitAndroid(userAgent);
        else if ((userAgent.contains("iPod")) ||
                 (userAgent.contains("iPhone")) ||
                 (userAgent.contains("iPad")))              
            return new BrowserWebKitIOS(userAgent);
        else if (userAgent.contains("Mac OS X"))
            return new BrowserWebKitSafariDesktop(userAgent);
        else
        {
            // Safari Destkop o WebKit desconocido (suponemos Safari desktop)
            return new BrowserWebKitSafariDesktop(userAgent);
        }
    }

    public static boolean isWebKit(String userAgent)
    {
        // Podr�a usarse "Safari" pero alg�n navegador antiguo no la ten�a
        return (userAgent.contains("WebKit"));
    }


    @Override
    public boolean isReferrerReferenceStrong()
    {
        // El nuevo documento siempre se carga antes de que el anterior se destruya
        return false;
    }

    @Override
    public boolean isCachedBackForward()
    {
        return false;
    }

    @Override
    public boolean isCachedBackForwardExecutedScripts()
    {
        return false;
    }


    @Override
    public boolean isDOMContentLoadedSupported()
    {
        // Se propuso para Mac desde 420+ : https://bugs.webkit.org/show_bug.cgi?id=5122
        // pero al parecer fue introducido en Safari 3.1 Webkit 525 (Safari 3.0 por ejemplo no lo soporta)
        // http://dev.mootools.net/ticket/732
        // http://developer.yahoo.com/yui/docs/Env.js.html  Webkit nightly 1/2008:525+  Supports DOMContentLoaded event.
        // No he encontrado ning�n navegador que contradiga esta regla del 525
        // salvo el S60WebKit 5th v1.0, se redefine en ese caso

        // Navegadores que han tenido versiones < 525:
        //    Safari,iPhone
        // Navegadores que NO han llegado a 525 : S40WebKit ya veremos si se cumple esta regla del 525.
        // Los dem�s navegadores (Android, Chrome, SWTWebKit) parten de WebKit superior a 525.
        return true; // El mundo antiguo ha quedado atr�s
    }

    
    public boolean isAJAXEmptyResponseFails()
    {
        // El retorno vac�o puede dejar
        // el motor AJAX en un estado err�neo m�s all� del request (hay que recargar la p�gina)
        // Esto ha sido detectado en el ejemplo "Event Monitor" del Feature Showcase
        // No se han detectado m�s casos, sin embargo el iPhone "real" con firmware antiguo (420+)
        // no ha sido testeado as� que por si acaso retornamos espacios en ese caso
        // y  nos "curamos en salud"
        // Nota: estas pruebas se han hecho en modo compresi�n con Gzip

        // if (webKitVersion <= 420) return true; // Por si acaso

        return false;
    }    
        
    @Override
    public boolean isTextAddedToInsertedSVGScriptNotExecuted()
    {
        // Cuando la inserci�n del script funciona funciona bien
        // en los dos casos (a�adiendo antes el c�digo o despu�s de insertado el elemento <script>).
        return isInsertedSVGScriptNotExecuted();
    }    
    
    @Override
    public boolean canNativelyRenderOtherNSInXHTMLDoc()
    {
        // http://caniuse.com/svg
        
        // Android:  SVG es soportado desde la v3
        // iOS: la v2.0 (525.18.1) no soporta SVG, la 2.1 ya lo soporta aunque la versi�n del WebKit no cambia
        return true; 
    }    
    
    @Override
    public boolean isInsertedSVGScriptNotExecuted()
    {
        // Ver notas en canNativelyRenderOtherNSInXHTMLDoc()

        // iOS: Hasta la v3 la ejecuci�n del <script> no se hace
        
        // Safary y Chrome: ni la versi�n 3 (3.1 WebKit 525.13) al menos de Safari desktop ni el Chrome 1.0 (WebKit 525.19)
        // ejecutan el texto dentro de <script> SVG, ni dentro del <script>
        // antes de insertar, ni a�adido despu�s.
        // Sin embargo en Chrome 2.0 (WebKit 530) y Safari 4 (531.9) funciona bien en ambos casos,
        // luego devolvemos false (no hacer nada).        
        
        return false;
    }    
    
    public boolean isChangeEventNotFiredUseBlur(HTMLElement formElem)
    {
        // Se ha detectado en la versi�n m�s actual de Chrome tanto en desktop como en Android en Julio de 2013, concretamente en desktop la versi�n
        // es 28.0.1500.72  y en Android  28.0.1500.94, que en un input text insertado via AJAX con un texto inicial, el eliminar TODO el texto (y perder el foco) NO dispara
        // el evento change, s�lo ocurre la primera vez. Ocurre en el ejemplo de input text del Feature Showcase
        // Incluye el caso "file" que no est� afectado por �sto, pero da igual
        // Aunque lo hemos detectado en Chrome, el caso es que parece un fallo del WebKit pues existe en el Safari del iPhone y de Mac OS X 
        // y UC Browser Windows(fecha: 28-8-2015 Chrome es AppleWebKit/537.36, Safari Mac 536.30.1, Safari iPhone 537.51.2)
        return DOMUtilHTML.isHTMLInputTextBox(formElem); // Incluye el caso "file" que no est� afectado por �sto, pero da igual
    } 


    @Override
    public JSRenderHTMLElementImpl getJSRenderHTMLElementSingleton() 
    {
        return getJSRenderHTMLElementSingletonStatic(); 
    }    
    
    public static JSRenderHTMLElementImpl getJSRenderHTMLElementSingletonStatic() 
    {
        return JSRenderHTMLElementWebKitImpl.SINGLETON; 
    }      
}
