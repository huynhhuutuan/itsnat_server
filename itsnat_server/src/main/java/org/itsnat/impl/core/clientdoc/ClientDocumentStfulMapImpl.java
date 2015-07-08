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

package org.itsnat.impl.core.clientdoc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;

/**
 * Esta clase es debido a que en la gran mayor�a de las ocasiones s�lo habr�
 * un cliente, el ClientDocumentStfulOwner, y no habr� control remoto.
 *
 * Evitamos por tanto usar un Map normal (o WeakHashMap) pues normalmente contendr�a
 * �nicamente al ClientDocumentStfulOwner reservando mucha memoria "a lo tonto".
 * Por otra parte los "attached" necesitan un WeakHashMap pero el ClientDocumentStfulOwner
 * no pues es siempre referenciado por el ItsNatStfulDocumentImpl
 *
 * As�, al tratar el owner aparte podemos evitar el crear el WeakHashMap vac�o
 * si no se usa (no hay observadores/"invitados").
 *
 * @author jmarranz
 */
public class ClientDocumentStfulMapImpl extends ClientDocumentMapImpl
{
    protected transient WeakHashMap<ClientDocumentImpl,Object> clientDocAttachedMap; // No se utilizan ids (pues pueden ser generados por otras sesiones), se utiliza la identidad del objeto

    public ClientDocumentStfulMapImpl(ItsNatStfulDocumentImpl itsNatDoc)
    {
        super(itsNatDoc);
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        Map<ClientDocumentImpl,Object> mapTmp = null;
        if (clientDocAttachedMap != null)
            mapTmp = new HashMap<ClientDocumentImpl,Object>(clientDocAttachedMap);
        out.writeObject(mapTmp);

        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        @SuppressWarnings("unchecked")
        Map<ClientDocumentImpl,Object> mapTmp = (Map<ClientDocumentImpl,Object>)in.readObject();
        if (mapTmp != null)
            getClientDocumentAttachedClientMap().putAll(mapTmp);

        in.defaultReadObject();
    }

    private boolean hasClientDocumentAttachedClient()
    {
        if (clientDocAttachedMap == null) return false;
        return !clientDocAttachedMap.isEmpty();
    }

    private Map<ClientDocumentImpl,Object> getClientDocumentAttachedClientMap()
    {
        if (clientDocAttachedMap == null)
            this.clientDocAttachedMap = new WeakHashMap<ClientDocumentImpl,Object>();
        return clientDocAttachedMap;
    }

    @Override
    public Object get(ClientDocumentImpl clientDoc)
    {
        Object res = super.get(clientDoc);
        if (res != null) return res;

        // Es muy raro que no est� el client en el Map
        Map<ClientDocumentImpl,Object> map = getClientDocumentAttachedClientMap();
        return map.get(clientDoc);
    }

    @Override
    public Object put(ClientDocumentImpl clientDoc,Object value)
    {
        if (clientDoc == getClientDocumentOwner())
            return super.put(clientDoc, value);
        else
        {
            Map<ClientDocumentImpl,Object> map = getClientDocumentAttachedClientMap();
            return map.put(clientDoc, value);
        }
    }

    public Object remove(ClientDocumentImpl clientDoc)
    {
        // No puede ser el owner, pues este no se puede quitar.
        Map<ClientDocumentImpl,Object> map = getClientDocumentAttachedClientMap();
        return map.remove(clientDoc);
    }

    @Override
    public int size()
    {
        int size = super.size(); // Ser� 1
        if (!hasClientDocumentAttachedClient())
            return size;
        else
            return size + getClientDocumentAttachedClientMap().size();
    }

    @Override
    public void fillAllValues(Object[] values)
    {
        super.fillAllValues(values);

        if (!hasClientDocumentAttachedClient())
            return;

        Map<ClientDocumentImpl,Object> observerClients = getClientDocumentAttachedClientMap();
        int i = 1;
        for(Iterator<Object> it = observerClients.values().iterator(); it.hasNext(); i++)
        {
            Object value = it.next();
            values[i] = value;
        }
    }

    @Override
    public void execAction(ClientDocumentMapAction action)
    {
        super.execAction(action);

        if (!hasClientDocumentAttachedClient())
            return;

        Map<ClientDocumentImpl,Object> observerClients = getClientDocumentAttachedClientMap();
        for(Map.Entry<ClientDocumentImpl,Object> entry : observerClients.entrySet())
        {
            action.exec(entry.getKey(),entry.getValue());
        }
    }
}
