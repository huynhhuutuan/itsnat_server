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

package org.itsnat.impl.core.scriptren.bsren.listener;

import org.itsnat.impl.core.listener.dom.domext.ItsNatContinueEventListenerWrapperImpl;
import org.itsnat.impl.core.listener.dom.domext.ItsNatDOMExtEventListenerWrapperImpl;
import org.itsnat.impl.core.listener.dom.domext.ItsNatGenericTaskEventListenerWrapperImpl;
import org.itsnat.impl.core.listener.dom.domext.ItsNatTimerEventListenerWrapperImpl;
import org.itsnat.impl.core.listener.dom.domext.ItsNatUserEventListenerWrapperImpl;

/**
 *
 * @author jmarranz
 */
public abstract class BSRenderItsNatDOMExtEventListenerImpl extends BSRenderItsNatNormalEventListenerImpl
{

    /** Creates a new instance of BSRenderItsNatDOMExtEventListenerImpl */
    public BSRenderItsNatDOMExtEventListenerImpl()
    {
    }

    public static BSRenderItsNatDOMExtEventListenerImpl getBSRenderItsNatDOMExtEventListener(ItsNatDOMExtEventListenerWrapperImpl itsNatListener)
    {
        if (itsNatListener instanceof ItsNatTimerEventListenerWrapperImpl)
            return BSRenderItsNatTimerEventListenerImpl.getBSRenderItsNatTimerEventListener();
        else if (itsNatListener instanceof ItsNatContinueEventListenerWrapperImpl)
            return BSRenderItsNatContinueEventListenerImpl.SINGLETON;
        else if (itsNatListener instanceof ItsNatUserEventListenerWrapperImpl)
            return BSRenderItsNatUserEventListenerImpl.SINGLETON;
        else if (itsNatListener instanceof ItsNatGenericTaskEventListenerWrapperImpl)
            return BSRenderItsNatGenericTaskEventListenerImpl.getBSRenderItsNatGenericTaskEventListener((ItsNatGenericTaskEventListenerWrapperImpl)itsNatListener);
        return null;
    }
}
