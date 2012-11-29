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

package org.itsnat.impl.core.jsren.dom.event.domstd.w3c;

import org.itsnat.core.event.ItsNatKeyEvent;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.w3c.dom.events.Event;

/**
 *
 * @author jmarranz
 */
public class JSRenderW3CKeyEventWebKitDefaultImpl extends JSRenderW3CKeyEventWebKitImpl
{
    public static final JSRenderW3CKeyEventWebKitDefaultImpl SINGLETON = new JSRenderW3CKeyEventWebKitDefaultImpl();

    /**
     * Creates a new instance of JSRenderW3CKeyEventWebKitDefaultImpl
     */
    public JSRenderW3CKeyEventWebKitDefaultImpl()
    {
    }

    public boolean needsAuxiliarCharCode()
    {
        return true;
    }

    protected String getEventTypeGroup(Event evt)
    {
        return "KeyboardEvent"; // Yo creo que vale tambi�n "KeyboardEvents" (en plural)
    }

    public String getInitKeyboardEvent(StringBuffer code,ItsNatKeyEvent keyEvt,String evtVarName,String keyIdentifier,int keyLocation,int keyCode,ClientDocumentStfulImpl clientDoc)
    {
        // http://webkit.org/docs/a00095.html
        // http://www.w3.org/TR/2003/WD-DOM-Level-3-Events-20030331/events.html#Events-KeyboardEvents-Interfaces
        // http://lists.webkit.org/pipermail/webkit-unassigned/2007-April/035697.html
        // http://trac.webkit.org/projects/webkit/browser/trunk/WebCore/dom/KeyboardEvent.cpp
        // http://trac.webkit.org/projects/webkit/browser/trunk/WebCore/dom/KeyboardEvent.h
        // http://www.koders.com/cpp/fid07000D08025239E54B79ABC802B84ED8C27FA066.aspx (PlatformKeyboardEvent.h)
        // http://www.koders.com/cpp/fidCA6C5BC570CBFE6A97F78D2A65217CBECDB4ED88.aspx?s=PlatformKeyboardEvent#L27 ( KeyEventWin.cpp )
        // http://lists.macosforge.org/pipermail/webkit-changes/2005-September/000914.html
        // Las t�cnicas que usan UIEvent no funcionan (en Safari 3) porque los atributos ctrlKey etc
        // son "readonly" de acuerdo con DOM 3 pero no hay problema pues
        // se pasan por initKeyboardEvent
        // Ejemplo: http://developer.yahoo.com/yui/docs/UserAction.js.html

        // El problema keyCode y charCode es que son read only porque se supone que lo genera Safari,
        // keyCode no hay problema pues se obtiene de keyIdentifier pero charCode no.
        // Sin embargo Safari tiene la propiedad
        // de que el objeto evento creado por createEvent es el mismo que reciben
        // los listeners por lo que podemos asociar una propiedad itsnat_charCode
        // al evento. De otra manera tendr�amos que utilizar el shiftKey como �nica
        // manera de poder calcular el charCode al recibir el evento.

        code.append( evtVarName + ".initKeyboardEvent("
                + "\"" + keyEvt.getType() + "\","
                + keyEvt.getBubbles() + ","
                + keyEvt.getCancelable() + ","
                + getViewPath(keyEvt.getView(),clientDoc) + ","
                + "\"" + keyIdentifier + "\"," // keyIdentifier
                + keyLocation + "," // keyLocation
                + keyEvt.getCtrlKey() + ","
                + keyEvt.getAltKey() + ","
                + keyEvt.getShiftKey() + ","
                + keyEvt.getMetaKey() + ","
                + false  // altGraph
                + ");\n" );

        return code.toString();
    }
}
