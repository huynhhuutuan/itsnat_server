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

import java.util.LinkedList;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.web.BrowserW3C;
import org.itsnat.impl.core.resp.shared.ResponseDelegateStfulWebLoadDocImpl;


/**
 *
 * @author jmarranz
 */
public abstract class ResponseDelegStfulLoadDocByWebBrowserImpl extends ResponseDelegStfulLoadDocByBrowserImpl
{
    public ResponseDelegStfulLoadDocByWebBrowserImpl(ResponseDelegateStfulWebLoadDocImpl parent)
    {
        super(parent);
    }

    public static ResponseDelegStfulLoadDocByWebBrowserImpl createResponseDelegStfulLoadDocByWebBrowser(ResponseDelegateStfulWebLoadDocImpl parent)
    {
        Browser browser = parent.getClientDocumentStful().getBrowser();
        if (browser instanceof BrowserW3C)
            return ResponseDelegStfulLoadDocByBW3CImpl.createResponseDelegStfulLoadDocByBW3C(parent);
        else
            return new ResponseDelegStfulLoadDocByBMSIEOldImpl(parent);
    }    
    
    public ResponseDelegateStfulWebLoadDocImpl getResponseDelegateStfulWebLoadDoc()
    {
        return (ResponseDelegateStfulWebLoadDocImpl)parent;
    }
    

    public boolean getRevertJSChanges()
    {
        // Revertir cambios hechos a trav�s de JavaScript y que el navegador ha podido memorizar para el autofill, por ejemplo la etiqueta de un bot�n que cambia
        // Se redefine en varios casos en los que hay auto-form-fill
        // Llamar s�lo una vez (por el caso de BrowserOperaDesktop)

        // Caso de revertir las acciones del usuario en
        // navegadores sin "autofill no desactivable": FireFox, MSIE etc.
        // (pues al revertir el autofill solucionamos ya el problema de las acciones del usuario)
        // Esto no quiere decir que no tengan autofill sino que �ste no act�a si la p�gina
        // es recargada desde el servidor (pues suponen acertadamente que la p�gina puede haber cambiado).

        // En tiempo de carga de la p�gina, antes de que se ejecute el script de iniciaci�n de la p�gina,
        // el usuario tiene la oportunidad de tocar los formularios cambiando su estado, dicho
        // nuevo estado no se propaga al servidor porque los formularios todav�a no
        // tienen listeners asociados porque todav�a no se ha ejecutado el script de la p�gina.
        // De esta manera el usuario puede tocar lo que quiera pues por una parte
        // los botones etc no hacen nada pues no hay listeners, y por otro los checkboxes
        // radio buttons y select son reinicializados despu�s.
        // Esto lo hacemos DESPUES del proceso de la p�gina porque en "fast load"
        // los cambios se manifiestan s�lo en el �rbol DOM y no se env�an por JavaScript
        // por lo que el usuario ya tiene el �rbol DOM final de la p�gina inicial antes de que se ejecute el c�digo
        // JavaScript de iniciaci�n de la p�gina (que fundamentalmente a�ade listeners)
        // En caso de no fast load probablemente no ser�a necesario pues lo normal es que los
        // elementos del formulario est�n o vac�os o con el elemento patr�n por lo que el usuario no puede tocar mucho
        // y lo que toque ser� sobreescrito al ejecutarse el JavaScript inicial, pero por si acaso
        // ejecutamos el forzado de los formularios en su estado final, sobre todo tambi�n por el caso de los controles markup driven
        // en donde el markup inicial son los datos iniciales.
        // No consideramos los controles que no pueden ser cambiados por el usuario (el value de botones etc).

        // En el caso de Opera 9 el auto-fill existe pero no act�a en tiempo
        // de carga sino tras el onload.

        return false;
    }    
    
    public abstract String getJSMethodInitName(); 
    public abstract void fillFrameworkScriptFileNamesOfBrowser(LinkedList<String> list);    
}
