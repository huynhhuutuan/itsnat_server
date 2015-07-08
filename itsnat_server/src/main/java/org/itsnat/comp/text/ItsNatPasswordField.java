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

package org.itsnat.comp.text;

/**
 * Is the base interface of text field components containing a password.
 *
 * <p>The user interface typically displays a mask.</p>
 *
 * @author Jose Maria Arranz Santamaria
 */
public interface ItsNatPasswordField extends ItsNatTextField
{
    /**
     * Returns the password value contained in this component as a char array.
     * For stronger security, it is recommended that the returned character array be
     * cleared after use by setting each character to zero.
     *
     * @return the password text as a char array.
     * @see #getText()
     */
    public char[] getPassword();
}
