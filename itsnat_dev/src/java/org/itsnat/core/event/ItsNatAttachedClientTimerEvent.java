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

package org.itsnat.core.event;

/**
 * This event object is sent to registered {@link ItsNatAttachedClientEventListener} objects
 * to notify the several phases of a remote control process using a timer
 * to notify the client any document change.
 *
 * @author Jose Maria Arranz Santamaria
 */
public interface ItsNatAttachedClientTimerEvent extends ItsNatAttachedClientEvent
{
    /**
     * Returns the refresh interval. If this event is a {@link ItsNatAttachedClientEvent#REQUEST}
     * returned value may be used to validate the request.
     *
     * <p>Returned value is proposed initially by the client but can not change
     * during the remote control life cycle. If a new remote control request
     * was accepted use this value to verify if the accepted
     * refresh interval is the same or lower than the real interval between events
     * (to detect some kind of client manipulation and avoid a server overload).</p>
     *
     * @return the refresh interval.
     * @see #setAccepted(boolean)
     */
    public int getRefreshInterval();
}
