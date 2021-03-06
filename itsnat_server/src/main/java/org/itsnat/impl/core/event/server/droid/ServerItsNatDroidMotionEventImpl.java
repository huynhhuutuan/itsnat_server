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

package org.itsnat.impl.core.event.server.droid;

import org.itsnat.core.event.droid.DroidMotionEvent;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;

/**
 *
 * @author jmarranz
 */
public class ServerItsNatDroidMotionEventImpl extends ServerItsNatDroidEventImpl implements DroidMotionEvent
{
    protected float rawX;
    protected float rawY;    
    protected float x;    
    protected float y;    
    
    public ServerItsNatDroidMotionEventImpl(ItsNatStfulDocumentImpl itsNatDoc)
    {
        super(itsNatDoc);
    }

    public float getRawX()
    {
        return rawX;
    }

    public void setRawX(float value)
    {
        this.rawX = value;
    }

    public float getRawY()
    {
        return rawY;
    }

    public void setRawY(float value)
    {
        this.rawY = value;
    }

    public float getX()
    {
        return x;
    }

    public void setX(float value)
    {
        this.x = value;
    }

    public float getY()
    {
        return y;
    }

    public void setY(float value)
    {
        this.y = value;
    }
    
}
