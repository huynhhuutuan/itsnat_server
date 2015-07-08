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

import org.itsnat.core.event.ItsNatKeyEvent;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.event.client.dom.domstd.w3c.W3CKeyboardEventSharedImpl;
import org.w3c.dom.events.Event;

/**
 *
 * @author jmarranz
 */
public class JSRenderW3CKeyEventMSIE9Impl extends JSRenderW3CKeyboardEventImpl
{
    public static final JSRenderW3CKeyEventMSIE9Impl SINGLETON = new JSRenderW3CKeyEventMSIE9Impl();
    protected static final W3CKeyboardEventSharedImpl delegate = new W3CKeyboardEventSharedImpl();
    
    /**
     * Creates a new instance of JSWebKitDefaultKeyEventRenderImpl
     */
    public JSRenderW3CKeyEventMSIE9Impl()
    {
    }

    public boolean needsAuxiliarCharCode()
    {
        return true;
    }

    public String toKeyIdentifierByBrowser(int keyCode)
    {
        return delegate.toKeyIdentifier(keyCode);
    }

    protected String getEventGroup(Event evt)
    {
        return "KeyboardEvent"; // SIN s al final
    }

    @Override    
    public String getInitKeyboardEvent(StringBuilder code,ItsNatKeyEvent keyEvt,String evtVarName,String keyIdentifier,int keyLocation,int keyCode,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
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


    public static void addModifier(String name,StringBuilder code)
    {
        if (code.length() > 0) code.append(" " + name);
        else code.append(name);
    }
}
