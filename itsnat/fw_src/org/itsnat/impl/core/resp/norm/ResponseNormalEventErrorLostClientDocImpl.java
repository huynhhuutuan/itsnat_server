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

package org.itsnat.impl.core.resp.norm;

import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.web.BrowserWeb;
import org.itsnat.impl.core.listener.dom.domstd.OnLoadBackForwardListenerImpl;
import org.itsnat.impl.core.req.norm.RequestNormalEventImpl;
import org.itsnat.impl.core.scriptren.shared.JSAndBSRenderImpl;

/**
 *
 * @author jmarranz
 */
public class ResponseNormalEventErrorLostClientDocImpl extends ResponseNormalEventErrorImpl
{
    protected String lostClientId; // No se usa

    /**
     * Creates a new instance of ResponseNormalEventErrorLostClientDocImpl
     */
    public ResponseNormalEventErrorLostClientDocImpl(RequestNormalEventImpl request,String lostClientId)
    {
        super(request);

        this.lostClientId = lostClientId;
    }

    public void processEvent()
    {
        // Antes hemos comprobado que no es un evento unload y que la sesi�n
        // no es la que se ha perdido.

        /*
         Casos:
         1) Ocurre en Opera 9 y BlackBerryOld pudiendo ser un back o forward volviendo
         a una p�gina que sali� haciendo un unload y que por tanto
         ya no existe (la sesi�n est� bien), el problema es que el navegador no recarga la p�gina del servidor
         sino que usa la cach� y no recarga la p�gina desde el servidor,
         pero en lo dem�s es igual generando un evento load,
         de hecho a�adimos un evento load especial precisamente
         para solucionar este problema (de que se vuelva a una p�gina
         en el cliente eliminada en el servidor). En los dem�s navegadores
         esto no ocurre porque se recarga un documento nuevo.

         Si es otro tipo de evento puede ser un evento generado por JavaScript
         como parte del handler onload de la propia p�gina (que se ejecut� antes del listener que
         env�a al servidor el evento) y que tambi�n es escuchado tambi�n desde el servidor.
         Tambi�n puede ser el caso de navegador que no emite el evento load al volver via back
         (t�pico de algunos browser m�viles) y el usuario simplemente ha pulsado algo que emite evento al servidor.
         Por eso no chequeamos el tipo de evento.

         2) Tambi�n ocurre cuando se invalid� en el servidor por c�digo el documento (sin necesariamente cerrar la ventana)
         por alguna raz�n (el programador puede llamar a setInvalid del documento)
         el framework lo hace por ejemplo cuando hay demasiados documentos abiertos.

         Son dos situaciones diferentes, en Opera nos interesa recargar la p�gina
         para conseguir que funcione el back/forward, en el otro caso no hay esa excusa
         y si queremos podemos provocar un error (suponemos evento unload o beforeunload excluidos)
         */

        RequestNormalEventImpl request = getRequestNormalEvent();
        Browser browser = getClientDocumentWithoutDocumentDefault().getBrowser();

        if (browser instanceof BrowserWeb && ((BrowserWeb)browser).isCachedBackForwardExecutedScripts())
        {
            // Esperamos "con seguridad" el evento especial load/DOMContentLoaded marcado con "itsnat_check_reload"
            // con la finalidad de recargar la p�gina, cualquier otro evento lo ignoraremos (no reload)
            // para evitar varios reload, por ejemplo podr�a ser el caso de un evento "load" normal, dicho evento
            // ha podido enviarse despu�s del evento especial pero antes de que el JavaScript de recarga tenga efecto,
            // evitamos por tanto generar otro JavaScript de recarga
            // El problema que introduce es que este reload a�ade una nueva
            // entrada a la cach� del navegador.
            boolean loadBackForwardEvent = OnLoadBackForwardListenerImpl.isLoadBackForwardEvent(request);
            if (loadBackForwardEvent)
            {
                sendReload();
                return;
            }
            else if (request.isLoadEvent())
            {
                // No es un evento load tipo back/forward, lo ignoramos pues antes o despu�s viene el que recarga la p�gina
                return;
            }
            // Si llegamos aqu� es un evento cualquiera como click, no sabemos exactamente qu� ha pasado, delegaremos
            // a los global listeners para que decidan
        }

        if (request.isLoadEvent())
        {
            // No sabemos qu� ha pasado ni que caso de navegador es, lo que
            // parece claro es que la p�gina cliente quiere cargarse
            // Una posible explicaci�n es que la p�gina contenga iframes
            // y que al cargar los iframes hayamos superado el n�mero de
            // documentos abiertos m�ximo por sesi�n y que por tanto
            // este evento load llega cuando el documento ha sido invalidado.
            // La soluci�n es aumentar el n�mero de documentos por sesi�n.
            // Para cualquier otro caso de posible cacheo no previsto, lo
            // propio es recargar.
            sendReload();
            return;
        }

        // Es un evento cualquiera de una p�gina perdida (invalidada en el servidor)
        // Damos una oportunidad a los listeners globales a recargar la p�gina
        // o a redireccionar a otra o lo que sea

        if (!processGlobalListeners())
        {
            // No hay global listeners:
            // Podr�amos lanzar un error, pero queda m�s elegante recargar la p�gina
            sendReload();
        }
    }

    public void sendReload()
    {
        Browser browser = getClientDocumentWithoutDocumentDefault().getBrowser();
        getItsNatServletResponse().addCodeToSend(JSAndBSRenderImpl.getReloadCode(browser));
    }
}
