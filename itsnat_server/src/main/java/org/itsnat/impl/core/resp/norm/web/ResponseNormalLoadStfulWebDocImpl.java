/*
  ItsNat Java Web Application Framework
  Copyright (C) 2007-2014 Jose Maria Arranz Santamaria, Spanish citizen

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

package org.itsnat.impl.core.resp.norm.web;

import org.itsnat.core.CommMode;
import org.itsnat.impl.core.CommModeImpl;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.doc.web.ItsNatHTMLDocumentImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.listener.dom.domstd.OnUnloadListenerImpl;
import org.itsnat.impl.core.listener.dom.domstd.RegisterThisDocAsReferrerListenerImpl;
import org.itsnat.impl.core.req.norm.RequestNormalLoadDocImpl;
import org.itsnat.impl.core.resp.norm.ResponseNormalLoadStfulDocImpl;
import org.w3c.dom.Document;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.views.AbstractView;
import org.w3c.dom.views.DocumentView;

/**
 *
 * @author jmarranz
 */
public abstract class ResponseNormalLoadStfulWebDocImpl extends ResponseNormalLoadStfulDocImpl
{
    public ResponseNormalLoadStfulWebDocImpl(RequestNormalLoadDocImpl request)
    {
        super(request);
    }
    
    public static ResponseNormalLoadStfulWebDocImpl createResponseNormalLoadStfulWebDoc(RequestNormalLoadDocImpl request)
    {
        ItsNatStfulDocumentImpl itsNatDoc = (ItsNatStfulDocumentImpl)request.getItsNatDocument();
        if (itsNatDoc instanceof ItsNatHTMLDocumentImpl)
            return new ResponseNormalLoadHTMLDocImpl(request);
        else
            return new ResponseNormalLoadOtherNSDocImpl(request);
    }    
    
    @Override
    public void dispatchRequestListeners()
    {
        // Caso de carga del documento por primera vez, el documento est� reci�n creado

        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        Document doc = itsNatDoc.getDocument();
        AbstractView view = ((DocumentView)doc).getDefaultView();
        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        Browser browser = clientDoc.getBrowser();

        if (isReferrerEnabled())
        {
            EventTarget target;
            String eventType;
            int commMode;
            if (browser.isClientWindowEventTarget())
            {
                target = (EventTarget)view;
                if ( CommModeImpl.isXHRDefaultMode(clientDoc) &&
                     browser.hasBeforeUnloadSupport(itsNatDoc) &&
                     itsNatDoc.isUseXHRSyncOnUnloadEvent())
                {
                    // Si no se soporta el modo s�ncrono corremos el riesgo de que no se env�e el evento en el proceso de cerrado de la p�gina
                    // lo cual normalmente ocurre en el evento "unload"
                    eventType = "beforeunload";
                    commMode = CommMode.XHR_SYNC; // As� aseguramos que se env�a pues por ejemplo no hay seguridad en modo as�ncrono en MSIE 6 desktop
                }
                else
                {
                    // Intentamos soportar referrers tambi�n aunque de forma menos elegante.
                    // Registramos en el evento load y no cuando se carga el documento
                    // para evitar solapamiento con posibles iframes
                    eventType = "load";
                    commMode = clientDoc.getCommMode();
                }
            }
            else
            {
                target = (EventTarget)doc.getDocumentElement();
                eventType = "SVGLoad";
                commMode = clientDoc.getCommMode();
            }

            clientDoc.addEventListener(target,eventType,RegisterThisDocAsReferrerListenerImpl.SINGLETON,false,commMode);
        }

        // Es necesario usar siempre el modo s�ncrono con unload para asegurar que llega al servidor
        // sobre todo con FireFox, total es destrucci�n
        // En FireFox a veces el unload se env�a pero no llega al servidor en el caso de AJAX as�ncrono,
        // la culpa la tiene quiz�s el enviar por red as�ncronamente algo en el proceso de destrucci�n de la p�gina
        // Curiosamente esto s�lo ocurre cuando se abre un visor remoto Comet y se cierra la p�gina principal.
        // En teor�a "beforeunload" deber�a dar menos problemas que unload en FireFox
        // pero sin embargo tambi�n ocurri� con beforeunload as�ncrono (adem�s beforeunload es cancelable).
        // NOTA: es posible que en versiones recientes est� solucionado esto.
        // De todas formas es �til el modo s�ncrono porque si hubiera alg�n
        // JavaScript pendiente de enviar, pues evita que de error al haberse perdido la p�gina
        // (pues el navegador ha de esperarse, no destruye la p�gina), si fuera asincrono
        // seguir�a destruyendo la p�gina antes de retornar el evento (comprobado en MSIE y FireFox).

        super.dispatchRequestListeners();

        // En W3C en addEventListener el orden de dispatch es el mismo que el orden de inserci�n
        // y en MSIE hemos simulado lo mismo (lo natural es primero el �ltimo)
        // por ello insertamos despu�s de los listeners del usuario tal que
        // nuestro unload "destructor" (invalida/desregistra el documento) sea el �ltimo

        // Si se puede, los eventos de descarga deben enviarse como s�ncronos

        EventTarget target;
        String eventType;
        int commMode;
        int defaultCommMode = clientDoc.getCommMode();
        if (CommModeImpl.isXHRMode(defaultCommMode))
        {
            if (!itsNatDoc.isUseXHRSyncOnUnloadEvent()) // Este problema no se ha estudiado para SVGUnLoad pero por si acaso tambi�n lo consideramos
                commMode = CommMode.XHR_ASYNC;
            else
                commMode = CommMode.XHR_SYNC;
        }
        else commMode = defaultCommMode; // Caso SCRIPT o SCRIPT_HOLD, siempre as�ncronos

        if (browser.isClientWindowEventTarget())
        {
            target = (EventTarget)view;
            eventType = "unload";
        }
        else
        {
            // En algunos plugins no se dispara por ejemplo ASV (v3 y v6) o Batik.
            target = (EventTarget)doc.getDocumentElement();
            eventType = "SVGUnload";
        }

        clientDoc.addEventListener(target,eventType,OnUnloadListenerImpl.SINGLETON,false,commMode);
    }    
}
