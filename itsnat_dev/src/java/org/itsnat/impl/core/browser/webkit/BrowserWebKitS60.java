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

package org.itsnat.impl.core.browser.webkit;

import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLSelectElement;

/*

    - User agents (varios simuladores probados, primera versi�n: S60 3rd FP 1)
        S60 3rd Feature Pack 1: Mozilla/5.0 (SymbianOS/9.2; U; [en]; Series60/3.1 Nokia3250/1.00 ) Profile/MIDP-2.0 Configuration/CLDC-1.1; AppleWebKit/413 (KHTML, like Gecko) Safari/413
        S60 3rd Feature Pack 2: Mozilla/5.0 (SymbianOS/9.2; U; [en]; Series60/3.2 Nokia3250/1.00; Profile/MIDP-2.1 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413
        S60 5th v0.9:           Mozilla/5.0 (SymbianOS/9.2; U; Series60/5.0 Nokia3250/1.00; Profile/MIDP-2.1 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413
        S60 5th v1.0:           Mozilla/5.0 (SymbianOS/9.2; U; Series60/5.0 Nokia3250/1.00; Profile/MIDP-2.1 Configuration/CLDC-1.1 ) AppleWebKit/525 (KHTML, like Gecko) Version/3.0 Safari/525

      He encontrado en www.botsvsbrowsers.com user-agents de una versi�n "previa" a las anteriores, pero no hay ning�n emulador con ella:
        Mozilla/5.0 (SymbianOS/9.1; U; [en]; NokiaN73-1/3.0704.1.0.1 Series60/3.0) AppleWebKit/413 (KHTML, like Gecko) Safari/413
      Lo confirma: http://wiki.forum.nokia.com/index.php/User-Agent_headers_for_Nokia_devices#OSS_Browser_.28Web_browser.29_-_HTML.2C_XHTML-MP

    Como se puede ver el WebKit es en teor�a el mismo (413) en las primeras versiones sin embargo hay algunas diferencias en comportamiento
    por ejemplo el filtrado de comentarios. Por lo que parece Nokia ha cogido el WebKit 413 y lo evoluciona
    a su manera.

    Por tanto la forma de detectar las primeras versiones es a trav�s de "Series60/x.x"

    Sobre versiones de S60: http://en.wikipedia.org/wiki/S60_platform

 */

public class BrowserWebKitS60 extends BrowserWebKitSymbian
{
    protected float s60Version;

    public BrowserWebKitS60(String userAgent)
    {
        super(userAgent,S60WEBKIT);

        try
        {
            int start = userAgent.indexOf("Series60/");
            start += "Series60/".length();
            int end = start;
            while(true)
            {
                char c = userAgent.charAt(end);
                if (c == ' ')
                    break;
                end++;
            }
            String strVer = userAgent.substring(start,end);
            this.s60Version = Float.parseFloat(strVer);
        }
        catch(Exception ex) // Caso de user agent de formato desconocido
        {
            this.s60Version = 3.0f; // Primera versi�n conocida
        }
    }

    public boolean isXHRSyncSupported()
    {
        // http://discussion.forum.nokia.com/forum/showthread.php?111061-Problems-with-synchronous-JavaScript
        // http://discussion.forum.nokia.com/forum/showthread.php?t=122304&highlight=XMLHttpRequest+synchronous
        // http://discussion.forum.nokia.com/forum/showthread.php?183820-XMLHttpRequest-problem
        // La versi�n S60 5th v0.9 todav�a ten�a un WebKit antiguo
        // aunque no est� soportada recordamos ese hecho y chequeamos
        // el soporte a trav�s de webKitVersion que es lo mejor
        return (webKitVersion >= 525);
    }

    public boolean isXHRPostSupported()
    {
        // S�lo GET, problema del emulador y en el hardware real (aunque no esta claro)
        // http://discussion.forum.nokia.com/forum/showthread.php?p=408303#post408303
        // http://discussion.forum.nokia.com/forum/showpost.php?p=388502&postcount=75
        // http://discussion.forum.nokia.com/forum/showthread.php?t=102000
        // http://blogs.forum.nokia.com/blog/hartti-suomelas-forum-nokia-blog/s60/2007/09/27/s60-3rd-edition-fp2-midp-sdk-beta-some-comments
        // https://bugs.webkit.org/show_bug.cgi?id=9116
        // Yo creo que es porque el WebKit es antiguo pues en el emulador de la versi�n 525
        // ya funciona (en la v0.9 no funcionaba).

        return (webKitVersion >= 525);
    }

    public boolean isDOMContentLoadedSupported()
    {
        // Redefinimos el m�todo por defecto, en el S60WebKit 5th v1.0 con WebKit 525
        // TAMPOCO NO FUNCIONA el DOMContentLoaded, lo que rompe la regla del WebKit 525
        return false;
    }

    public boolean isFilteredCommentsInMarkup()
    {
        // Ocurre en WebKit muy antiguos, anteriores a Safari desktop 3.0.
        // Detectado en S60WebKit FP 1 (S60 3.1), se soluciona el problema en el FP 2

        return s60Version <= 3.1f; // La "f" ES FUNDAMENTAL pues de otra manera 3.2 es double y la comparaci�n puede no ser exacta
    }

    public boolean isTextAddedToInsertedHTMLScriptNotExecuted()
    {
        // En S60 5th (5.0) v0.9 este problema ya se solucion� y en v1.0 sigue siendo as�
        return s60Version <= 3.2f; // La "f" ES FUNDAMENTAL pues de otra manera 3.2 es double y la comparaci�n puede no ser exacta
    }

    public boolean isOldEventSystem()
    {
        // En el S60 5th v1.0 el WebKit 525 ya soluciona este problema
        return (webKitVersion < 525);
    }

    public boolean isFocusOrBlurMethodWrong(String methodName,HTMLElement formElem)
    {
        // En principio no hay problema, el focus() es procesado, el problema es que
        // en cuanto el cursor deja el �rea el evento blur se lanza.
        // El problema es que en edici�n inplace puede haber una recolocaci�n de
        // los elementos que hace al salir el control
        // de edici�n no salga debajo del cursor por lo que el blur se lanza.
        // Lo mejor por tanto es no ejecutar el focus().
        // El S60 5th v0.9 no tiene este problema pero por si acaso hacemos lo mismo, as� evitamos
        // que salga autom�ticamente el editor de texto.
        return true;
    }

    public boolean isChangeNotFiredHTMLSelectWithSizeOrMultiple(HTMLSelectElement elem)
    {
        return false;
    }

    public boolean canNativelyRenderOtherNSInXHTMLDoc()
    {
        return false; // No soporta SVG por ejemplo.
    }

    public boolean isInsertedSVGScriptNotExecuted()
    {
        return false; // Por poner algo pues SVG no est� soportado
    }
}
