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

package org.itsnat.impl.core.css.lex;

/**
 *
 * @author jmarranz
 */
public class HexNumber extends Token
{
    protected String value = "";

    /** Creates a new instance of HexNumber */
    public HexNumber(Cursor cursor)
    {
        super(cursor.getCurrentPos());
        parse(cursor);
    }

    public static boolean isHexNumberStart(char c)
    {
        return (c == '#');
    }

    public static boolean isHexNumberPart(char c)
    {
        if (('0' <= c)&&(c <= '9'))
            return true;
        else
        {
            c = Character.toLowerCase(c);
            if (('a' <= c)&&(c <= 'f'))
                return true;
        }
        return false;
    }

    public String toString()
    {
        return value;
    }

    public static int toIntFromHex(String s)
    {
        return Integer.parseInt(s,16);
    }

    public static int toIntFromHex(char c)
    {
        return toIntFromHex(Character.toString(c));
    }

    public void parse(Cursor cursor)
    {
        // cursor apunta a la #
        StringBuilder valueTmp = new StringBuilder();
        valueTmp.append( cursor.getCurrentChar() );
        int i = cursor.inc(); // segunda letra (si hay)
        while(cursor.isValidPosition() &&
              isHexNumberPart(cursor.getCurrentChar()))
        {
            valueTmp.append( cursor.getCurrentChar() );
            i = cursor.inc();
        }

        this.value = valueTmp.toString();

        cursor.dec();
        this.end = cursor.getCurrentPos(); // apunta al �ltimo caracter
    }

}
