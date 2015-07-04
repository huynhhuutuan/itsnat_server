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

package org.itsnat.impl.core.event;

import java.io.Serializable;
import java.util.LinkedHashSet;
import org.itsnat.core.event.CodeToSendListener;

/**
 *
 * @author jmarranz
 */
public class CodeToSendListenersImpl implements Serializable
{
    protected LinkedHashSet<CodeToSendListener> codeToSendListener = new LinkedHashSet<CodeToSendListener>();
    protected Object target; // ClientDocument o ItsNatDocument

    /** Creates a new instance of CodeToSendListenersImpl */
    public CodeToSendListenersImpl(Object target)
    {
        this.target = target;
    }

    public boolean hasCodeToSendListeners()
    {
        return !codeToSendListener.isEmpty();
    }

    private LinkedHashSet<CodeToSendListener> getCodeToSendListeners()
    {
        return codeToSendListener;
    }

    public void addCodeToSendListener(CodeToSendListener listener)
    {
        getCodeToSendListeners().add(listener);
    }

    public void removeCodeToSendListener(CodeToSendListener listener)
    {
        getCodeToSendListeners().remove(listener);
    }

    public CodeToSendEventImpl preProcessCodeToSend(Object code)
    {
        LinkedHashSet<CodeToSendListener> listenerList = getCodeToSendListeners();
        CodeToSendEventImpl event = new CodeToSendEventImpl(code,target);
        for(CodeToSendListener listener : listenerList)
        {
            code = listener.preSendCode(event);
            event.setCode(code);
            if (code == null)
                return event; // No procesar mas, ha sido rechazado
        }
        return event;
    }

    public void postProcessCodeToSend(CodeToSendEventImpl event)
    {
        // Si se llega hasta aqu� es que hay listenerList (y si no hay porque se quitaron as� mismos al procesarse, pues nada)
        LinkedHashSet<CodeToSendListener> listeners = getCodeToSendListeners();
        for(CodeToSendListener listener : listeners)
        {
            listener.postSendCode(event);
        }
    }
}
