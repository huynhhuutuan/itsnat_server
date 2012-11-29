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

package org.itsnat.impl.core.resp.shared.html;

import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.jsren.dom.node.html.w3c.JSRenderHTMLElementGeckoUCWEBImpl;
import org.itsnat.impl.core.resp.ResponseLoadStfulDocumentValid;
import org.w3c.dom.html.HTMLDocument;

/**
 *
 * @author jmarranz
 */
public class ResponseDelegateHTMLLoadDocGeckoUCWEBImpl extends ResponseDelegateHTMLLoadDocGeckoImpl
{
    public ResponseDelegateHTMLLoadDocGeckoUCWEBImpl(ResponseLoadStfulDocumentValid responseParent)
    {
        super(responseParent);
    }

    public void dispatchRequestListeners()
    {
        //fixSetTimeout();

        boolean serializeBefore = isSerializeBeforeDispatching();
        if (serializeBefore) // Caso fastLoad=false y remote control
            fixHTMLElements();

        super.dispatchRequestListeners();

        if (!serializeBefore)
            fixHTMLElements();
    }

    protected void fixHTMLElements()
    {
        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        if (!clientDoc.isScriptingEnabled())
            return;

        HTMLDocument doc = (HTMLDocument)clientDoc.getItsNatDocumentImpl().getDocument();
        JSRenderHTMLElementGeckoUCWEBImpl.fixTreeHTMLElements(true,doc.getBody(), clientDoc);
    }
}

