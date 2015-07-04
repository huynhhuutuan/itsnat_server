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

package org.itsnat.impl.core.listener.dom.domstd;

import java.io.Serializable;
import org.itsnat.core.event.CustomParamTransport;
import org.itsnat.core.event.ItsNatEvent;
import org.itsnat.core.event.ParamTransport;
import org.itsnat.impl.core.clientdoc.ClientDocumentImpl;
import org.itsnat.impl.core.req.norm.RequestNormalEventImpl;
import org.itsnat.impl.core.scriptren.shared.JSAndBSRenderImpl;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

/**
 *
 * @author jmarranz
 */
public class OnLoadBackForwardListenerImpl implements EventListener,Serializable
{
    protected boolean loaded = false;

    /**
     * Creates a new instance of OnLoadBackForwardListenerImpl
     */
    public OnLoadBackForwardListenerImpl()
    {
    }

    public static ParamTransport[] createExtraParams()
    {
        return new ParamTransport[] { new CustomParamTransport("itsnat_check_reload","true") };
    }

    public static boolean isLoadBackForwardEvent(RequestNormalEventImpl request)
    {
        return request.isLoadEvent() && (request.getAttrOrParam("itsnat_check_reload") != null);
    }

    public void handleEvent(Event evt)
    {
        // En Opera 9 este listener tiene la finalidad
        // de detectar que la p�gina del navegador no ha sido
        // cargada desde el servidor sino de la cache normalmente
        // ante un back/forward unido a que Opera no siempre genera unload
        // por lo que la p�gina puede seguir "viva" en el servidor al volver via back/forward.

        // En Opera 9 gracias a window.history.navigationMode = "compatible" (y la otra forma)
        // el evento load y DOMContentLoaded se env�an aunque la p�gina
        // se lea de la cache. 

        // Si ya pas� por aqu� significa que se han enviado dos eventos
        // load o DOMContentLoaded, esto es posible si el evento unload
        // no se emiti� y en el servidor la p�gina sigue vida.
        // El caso es que cuando el navegador al rescatar la p�gina de la cache
        // env�a el evento load/DOMContentLoaded detectamos que realmente
        // se cerr� sin enviar el unload (ocurre cuando se sale de la p�gina con
        // back/forward) y hemos vuelto a ella via back/forward.
        // Para ItsNat salir de la p�gina supone que dicha p�gina "ha de morir"
        // pues las siguientes p�ginas han podido cambiar el estado del servidor
        // por lo que el estado de la p�gina en cliente y servidor pueden no corresponderse
        // con el estado de la base de datos por poner un ejemplo, por ello
        // nuestro objetivo es recargarla.

        // Por otra parte si la p�gina se elimin� con unload y hemos vuelto
        // a ella via back/forward, no pasaremos por aqu� obviamente (el documento ya no existe)
        // ser� el caso de recarga que procesar� ResponseNormalFixCacheImpl

        if (loaded)
        {
            // Se est� ejecutando dos veces por lo que es el caso de vuelta por back/forward
            ItsNatEvent itsNatEvt = (ItsNatEvent)evt;
            itsNatEvt.getItsNatDocument().setInvalid();
            ClientDocumentImpl clientDoc = (ClientDocumentImpl)itsNatEvt.getClientDocument();
            itsNatEvt.getItsNatServletResponse().addCodeToSend(JSAndBSRenderImpl.getReloadCode(clientDoc.getBrowser()));
        }
        else this.loaded = true;
    }

}
