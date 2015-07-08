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

import java.security.MessageDigest;
import javax.servlet.http.HttpSession;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.util.*;

/**
 *
 * @author jmarranz
 */
public class ItsNatServletContextUniqueIdGenImpl implements UniqueIdGenerator
{
    protected MessageDigest md;
    protected ItsNatServletContextImpl context;
    protected UniqueIdGenIntList genIntList;
    protected UniqueIdGenMessageDigest genMesDigest;
    protected boolean frozen = false;

    /** Creates a new instance of UniqueIdGenerator */
    public ItsNatServletContextUniqueIdGenImpl(ItsNatServletContextImpl context)
    {
        this.context = context;
        initIdGenerators();
    }

    public void initIdGenerators()
    {
        if (context.isSessionReplicationCapable())
        {
            this.genIntList = null;
            this.genMesDigest = new UniqueIdGenMessageDigest();
        }
        else
        {
            this.genIntList = new UniqueIdGenIntList(true);
            this.genMesDigest = null;
        }
    }

    public void notifySessionReplicationCapableChanged()
    {
        if (frozen) throw new ItsNatException("Too late some servlet has already received events");
        initIdGenerators();
    }

    public UniqueId generateUniqueId(HttpSession session,String prefix)
    {
        this.frozen = true;

        String id;
        if (context.isSessionReplicationCapable())
        {
            // Este m�todo genera un id �nico si el idRef es �nico usando SHA-1
            // Es �til (y es su �nico uso) en la generaci�n del id de sesi�n
            // de ItsNat cuando se usa replicaci�n de sesiones por ejemplo en GAE
            // pues el ItsNatServletContextImpl no puede compartirse entre nodos
            // (se podr�a usando una cach� compartida y alg�n m�todo at�mico)
            // por tanto el enfoque de generaci�n de ids �nicos usando enteros
            // no nos vale pues deben ser �nicos para todos los nodos.
            // El generar un id SHA diferente al id de la sesi�n original y no reversible es importante
            // porque en control remoto este id es p�blico para poder acceder a una sesi�n
            // de un usuario diferente, este id de ItsNat s�lo ser� �til en ItsNat
            // y sus acciones posibles estar�n controladas por ItsNat (autorizaci�n etc)
            // si public�ramos el id nativo de la sesi�n estar�amos dando control
            // ABSOLUTO al usuario controlador m�s all� de ItsNat.

            id = genMesDigest.generateId(session.getId(),prefix);
        }
        else
        {
            id = genIntList.generateId(prefix);
        }

        return new UniqueId(id,this);
    }

}
