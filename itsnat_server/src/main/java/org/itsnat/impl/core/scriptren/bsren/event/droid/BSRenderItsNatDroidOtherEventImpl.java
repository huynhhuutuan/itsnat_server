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

package org.itsnat.impl.core.scriptren.bsren.event.droid;

import org.itsnat.core.event.droid.DroidEvent;
import org.itsnat.impl.core.clientdoc.droid.ClientDocumentStfulDelegateDroidImpl;
import org.w3c.dom.events.Event;

/**
 *
 * @author jmarranz
 */
public class BSRenderItsNatDroidOtherEventImpl extends BSRenderItsNatDroidEventImpl
{
    public static final BSRenderItsNatDroidOtherEventImpl SINGLETON = new BSRenderItsNatDroidOtherEventImpl();

    @Override
    public String getCreateEventInstance(Event evt, ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        DroidEvent evt2 = (DroidEvent)evt; // No hay necesidad por ahora de definir un DroidOtherEvent p�blico 
        return "itsNatDoc.createOtherEvent()"; 
    }

}
