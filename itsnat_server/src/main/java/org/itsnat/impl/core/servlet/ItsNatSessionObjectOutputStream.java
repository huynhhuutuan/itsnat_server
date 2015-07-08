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
package org.itsnat.impl.core.servlet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.itsnat.core.ItsNatException;

/**
 *
 * @author jmarranz
 */
public class ItsNatSessionObjectOutputStream extends ObjectOutputStream
{
    protected static boolean showInfo = false;

    public ItsNatSessionObjectOutputStream(OutputStream out) throws IOException
    {
        super(out);

        if (showInfo)
        {
            enableReplaceObject(true);
            System.out.println("Created DebuggingObjectOutputStream");
        }
    }

    protected Object replaceObject(Object obj) throws IOException
    {
        if (obj != null) System.out.println("Serializing: " + obj.getClass());
	return obj;
    }

    public static ItsNatSessionObjectOutputStream castToItsNatSessionObjectOutputStream(OutputStream out)
    {
        if (out instanceof ItsNatSessionObjectOutputStream)
            return ((ItsNatSessionObjectOutputStream)out);
        else
        {
            // Alguien ha podido guardar expl�citamente el ItsNatDocument en la sesi�n
            // por lo que no hemos tenido control de la serializaci�n,
            // es el caso de los ejemplos degraded (casos no eventos y no JavaScript)
            // No lo permitimos porque las "deserial pending tasks" no se ejecutar�n
            // y dar� problemas m�s tarde aunque no se use el objeto pues al serializar los
            // atributos transient estar�n a null

            // Evitando la escritura con el HttpSession.setAttribute evitamos despu�s
            // tener que eliminar las sesiones manualmente pues dar� error al leer
            // pero los datos seguir�n ah� serializados 
            throw new ItsNatException("Explicit serialization of ItsNat objects like ItsNatDocument (calling HttpSession.setAttribute(String,Object)) is not allowed on session replication");
        }
    }
}
