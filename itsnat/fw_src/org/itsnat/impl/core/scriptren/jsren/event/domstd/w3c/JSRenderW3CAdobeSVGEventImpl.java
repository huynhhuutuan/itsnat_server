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
package org.itsnat.impl.core.scriptren.jsren.event.domstd.w3c;


import org.itsnat.core.event.ItsNatDOMStdEvent;
import org.itsnat.core.event.ItsNatKeyEvent;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.event.HTMLEventInternal;
import org.itsnat.impl.core.dompath.NodeLocationImpl;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.events.UIEvent;

/**
 * La versi�n v6 del Adobe SVG Viewer soporta m�s o menos creaci�n/distribuci�n de eventos.
 *
 * La versi�n ASV v3 NO soportan la creaci�n/despacho
 * de eventos manual pero lo simulamos.
 *
 * @author jmarranz
 */
public abstract class JSRenderW3CAdobeSVGEventImpl extends JSRenderW3CEventImpl
{
    public JSRenderW3CAdobeSVGEventImpl()
    {
    }

    public static JSRenderW3CAdobeSVGEventImpl getJSRenderW3CAdobeSVGEvent(ItsNatDOMStdEvent event)
    {
        if (event instanceof MouseEvent)
            return JSRenderW3CAdobeSVGMouseEventImpl.SINGLETON;
        else if (event instanceof ItsNatKeyEvent)
            return JSRenderW3CAdobeSVGKeyEventImpl.SINGLETON;
        else if (event instanceof UIEvent)
            return JSRenderW3CAdobeSVGUIEventGenericImpl.SINGLETON;
        else if (event instanceof HTMLEventInternal)
            return JSRenderW3CAdobeSVGEventDefaultImpl.SINGLETON;
        else if (event instanceof MutationEvent)
            return JSRenderW3CAdobeSVGMutationEventImpl.SINGLETON;
        else
            return JSRenderW3CAdobeSVGEventDefaultImpl.SINGLETON;
    }

    protected String getEventGroup(Event evt)
    {
        // ASV v6
        // Incre�ble pero esta la �nica forma en el que conseguimos
        // hacer disparar eventos. El type pasado al document.createEvent
        // ser� el valor del atributo "type".
        // Como dice la documentaci�n del ASV v6 s�lo podemos crear
        // eventos "custom", afortunadamente el m�todo Element.dispatchEvent
        // admite estos eventos custom por lo que simulamos que son eventos
        // nativos "de verdad".
        return evt.getType();
    }

    @Override
    public String getInitEventSystem(ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        StringBuilder code = new StringBuilder();

        code.append( super.getInitEventSystem(clientDoc) ); // Por si acaso

        // En el caso de ASV v6 a�adiremos m�todos que NO necesitamos
        // pero no podemos distinguir en el servidor cual estamos usando.
        code.append(JSRenderManualDispatchImpl.getInitEventSystem(clientDoc,true));

        // En el caso de ASV v3 simulamos el createEvent de la v6
        // La variable temporal asv6 la utilizaremos en m�s sitios.
        code.append("var asv6 = (navigator.appVersion.indexOf(\"6.\") == 0);"); // Similar: window.getSVGViewerVersion()

        return code.toString();
    }

    @Override    
    public String getCreateEventInstance(Event evt,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        return "asv6 ? " + super.getCreateEventInstance(evt, clientDoc) + " : new Object()";
    }

    @Override    
    public String getInitEvent(Event evt,String evtVarName,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        // ASV v6:
        // No podemos hacer nada aqu�, el atributo "type" afortunadamente
        // est� definido via document.createEvent, pero los
        // dem�s atributos "bubbles" y "cancelable", son s�lo lectura
        // pues ya est�n definidos en el objeto evento "custom".
        // ASV v3:
        // Como el evento es creado manualmente, el type est� ya definido
        // pero no bubble ni cancelabe. Para que sea compatible con la v6
        // que no admite escritura, a�adimos try/catch
        StringBuilder code = new StringBuilder();
        code.append( "if (!asv6)" );
        code.append( "{\n" );
        code.append(   JSRenderManualDispatchImpl.getInitEvent(evt, evtVarName) );
        code.append( "}\n" );
        return code.toString();
    }


    @Override
    public String getCallDispatchEvent(String varResName,NodeLocationImpl nodeLoc,Event evt,String evtVarName,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        StringBuilder code = new StringBuilder();

        code.append("var " + varResName + ";");
        code.append("if (asv6)");
        code.append("{");
        code.append(   super.getCallDispatchEvent(varResName,nodeLoc,evt,evtVarName,clientDoc));
        code.append("}");
        code.append("else");
        code.append("{");
        code.append(   varResName + " = " + JSRenderManualDispatchImpl.getCallDispatchEvent(varResName, nodeLoc, evt, evtVarName,clientDoc) + ";" );
        code.append("}");

        return code.toString();
    }

    @Override
    public String getCallDispatchEvent(String targetRef,Event evt,String evtVarName,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        StringBuilder code = new StringBuilder();

        code.append("if (asv6)");
        code.append("{");
        code.append(   super.getCallDispatchEvent(targetRef,evt,evtVarName,clientDoc) );
        code.append("}");
        code.append("else");
        code.append("{");
        code.append( JSRenderManualDispatchImpl.getCallDispatchEvent(targetRef,evt,evtVarName,clientDoc) + ";");
        code.append("}");

        return code.toString();
    }
}
