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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import org.itsnat.core.ItsNatDocument;
import org.itsnat.core.ItsNatException;
import org.itsnat.core.ItsNatServletContext;
import org.itsnat.core.ItsNatServletRequest;
import org.itsnat.core.ItsNatServletResponse;
import org.itsnat.core.ItsNatSession;
import org.itsnat.core.ItsNatVariableResolver;
import org.itsnat.impl.core.*;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedClientImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedServerImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulOwnerImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.util.HasUniqueId;
import org.itsnat.impl.core.util.MapListImpl;
import org.itsnat.impl.core.util.MapUniqueId;
import org.itsnat.impl.core.util.UniqueId;
import org.itsnat.impl.core.util.UniqueIdGenIntList;

/**
 *
 * @author jmarranz
 */
public abstract class ItsNatSessionImpl extends ItsNatUserDataImpl  implements ItsNatSession,HasUniqueId
{
    public static final Comparator<ClientDocumentStfulOwnerImpl> COMPARATOR_STFUL_OWNER = new LastRequestComparator<ClientDocumentStfulOwnerImpl>();    
    public static final Comparator<ClientDocumentAttachedClientImpl> COMPARATOR_ATTACHED_CLIENTS = new LastRequestComparator<ClientDocumentAttachedClientImpl>();
    public static final Comparator<ClientDocumentAttachedServerImpl> COMPARATOR_ATTACHED_SERVERS = new LastRequestComparator<ClientDocumentAttachedServerImpl>();    
        
    
    protected transient ItsNatSessionSerializeContainerImpl serialContainer;
    protected transient ItsNatServletContextImpl context; // No serializamos la instancia pero s� serializaremos el Necesita serializarse porque el generador de ids debe estar en todas las JVMs
    protected transient UniqueId idObj; // No se serializa si no se serializa el contexto pues el generador de ids debe ser el mismo objeto que hay en ItsNatServletContextImpl
    protected final UniqueIdGenIntList idGenerator = new UniqueIdGenIntList(true);
    protected final MapUniqueId<ItsNatStfulDocumentImpl> docsById = new MapUniqueId<ItsNatStfulDocumentImpl>(idGenerator); // Los ItsNatDocument que son propiedad de esta sesi�n. Los ids han sido generados por esta sesi�n. Es auxiliar pues los ClientDocumentOwner de ownerClientsById ya sujetan los ItsNatDocument, sirve para buscar docs por Id
    protected final MapUniqueId<ClientDocumentStfulOwnerImpl> ownerClientsById = new MapUniqueId<ClientDocumentStfulOwnerImpl>(idGenerator);
    protected final MapUniqueId<ClientDocumentAttachedClientImpl> attachedClientsById = new MapUniqueId<ClientDocumentAttachedClientImpl>(idGenerator); // Sirve para retener los attachedClients para que no sean garbage collected hasta que la sesi�n se pierda. Los ids han sido generados por esta sesi�n
    protected final MapUniqueId<ClientDocumentAttachedServerImpl> attachedServersById = new MapUniqueId<ClientDocumentAttachedServerImpl>(idGenerator); // Sirve para guardar provisionalmente datos durante la carga
    protected Browser browser; // El de la primera request, en el ClientDocumentImpl puede cambiar pero nos sirve para los casos en donde no cambia
    protected Referrer referrer;
    protected String token;
    protected transient DeserialPendingTask sessionDeserialPendingTask;
    protected transient MapListImpl<String,DeserialPendingTask> deserialPending;

    /** Creates a new instance of ItsNatSessionImpl */
    public ItsNatSessionImpl(ItsNatServletContextImpl context,Browser browser)
    {
        super(true);

        this.context = context;
        this.browser = browser;
        this.referrer = Referrer.createReferrer(browser);
        this.token = System.currentTimeMillis() + "_" + context.getNewToken();
        // El token NO es el identificador absoluto de la sesi�n, sirve
        // para asegurar que dos sesiones NO tienen el mismo token
        // ENTRE DIFERENTES CARGAS de la aplicaci�n o del servidor en general.
        // Sirve para detectar que la sesi�n del usuario en el cliente no
        // se corresponde con la del servidor (normalmente porque se ha creado nueva)
        // es decir la sesi�n expir� o el servidor se recarg�. Apenas sirve para
        // gestionar este caso no es tanto un problema de seguridad aunque ayuda
        // (la verdadera identidad segura es la cookie de la sesi�n).
        // No pasa nada porque dos sesiones tengan el mismo token aunque
        // sea casi imposible gracias al n�mero aleatorio (en un supercomputador y millones de usuarios podr�a ser),
        // lo importante es que el token sea CON SEGURIDAD diferente entre sesiones
        // del usuario pues el currentTimeMillis asegura que la sesi�n nueva tenga un token
        // diferente, dos sesiones seguidas creadas para el mismo usuario en el mismo milisegundo
        // es pr�cticamente imposible y con el random es imposible, el random ayuda a que ni por suerte
        // (al intentar adivinar el milisegundo actual) pueda acertarse.
        // Este token se ha demostrado �til tambi�n en GAE con session replication capable desactivado
        // para detectar que un evento ha sido env�ado a un nodo distinto en el que no est� la sesi�n ItsNat
        // pero puede existir una sesi�n de otro usuario con el mismo id (dicho id es local al nodo realmente)
        // el resultado es que al recargarse la p�gina (es lo propio el evento es hu�rfano) se
        // crea un nuevo objeto sesi�n ItsNat en el nuevo nodo.
    }

    public void setItsNatSessionSerializeContainer(ItsNatSessionSerializeContainerImpl serialContainer)
    {
        this.serialContainer = serialContainer;
    }

    public DeserialPendingTask getSessionDeserialPendingTask()
    {
        return sessionDeserialPendingTask;
    }

    public void setSessionDeserialPendingTask(DeserialPendingTask task)
    {
        this.sessionDeserialPendingTask = task;
    }

    public boolean hasDeserialPendingTasks()
    {
        if (deserialPending == null) return false;
        return !deserialPending.isEmpty();
    }

    public MapListImpl<String,DeserialPendingTask> getDeserialPendingTasks()
    {
        if (deserialPending == null) this.deserialPending = new MapListImpl<String,DeserialPendingTask>();
        return deserialPending;
    }

    public void addDeserialPendingTask(String servletName,DeserialPendingTask task)
    {
        getDeserialPendingTasks().add(servletName,task);
    }

    public void clearDeserialPendingTasks()
    {
        if (deserialPending == null) return;
        deserialPending.clear();
        this.deserialPending = null; // Para ahorrar memoria
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        // Esto es para impedir que progrese una serializaci�n de objetos ItsNat expl�cita 
        // en la sesi�n (sea cual sea al final acabar� serializ�ndose este objeto sesi�n)
        // pues no podemos usar "deserial pending tasks",
        // ver notas dentro del m�todo y tambi�n ItsNatSessionObjectInputStream:
        ItsNatSessionObjectOutputStream.castToItsNatSessionObjectOutputStream(out);

        out.writeObject(idObj.getId()); // No podemos serializar el UniqueIdGenerator pues pertenece al ItsNatServletContextImpl no serializado

        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        ItsNatSessionObjectInputStream.setItsNatSession(in, this);

        final String id = (String)in.readObject();

        // Es normal que se deserialice antes de un primer request (antes de inicializar servlets), en el caso
        // de recarga de la JVM deserializa las sesiones salvadas.
        DeserialPendingTask task = new DeserialPendingTask()
        {
            public void process(ItsNatServletImpl itsNatServlet,ItsNatServletRequest request, ItsNatServletResponse response)
            {
                ItsNatSessionImpl.this.context = itsNatServlet.getItsNatServletContextImpl();
                ItsNatSessionImpl.this.idObj = new UniqueId(id,context.getUniqueIdGenerator());
            }
        };
        setSessionDeserialPendingTask(task);

        in.defaultReadObject();
    }

    public abstract void endOfRequestBeforeSendCode();

    // Este m�todo es llamado cuando el request finaliza
    // viene a ser el sim�trico a getItsNatHttpSession(ItsNatHttpServletRequestImpl
    // en la clase derivada Http.
    public abstract void endOfRequest();

    public void destroy()
    {
        // As� no esperamos al garbage collector (de hecho no ser�a necesario guardar los session con weak maps)
        ItsNatServletContextImpl context = getItsNatServletContextImpl();
        context.removeItsNatSession(this);

        // Esta limpieza hay que hacerla por si acaso porque algunos navegadores no tienen
        // asegurado el "unload" (tambi�n pueden haberse colgado, ca�do etc)
        // y porque en casos como los CometNotifiers o los visores remotos
        // de documentos de otras sesiones hay dependencias entre sesiones, hilos etc
        // as� aseguramos que las dependencias detectan que los documentos son inv�lidos

        // Ahora invalidamos los clientes (se invalidar� el documento tambi�n),
        // as� evitamos sincronizar un documento dentro de un bloque sincronizado de la sesi�n (para evitar dead locks)
        // Esto lo hacemos por ejemplo para terminar CometNotifier abiertos
        ClientDocumentStfulOwnerImpl[] clients = getClientDocumentStfulOwnerArray();
        if (clients != null)
        {
            for(int i = 0; i < clients.length; i++)
            {
                ClientDocumentStfulOwnerImpl clientDoc = clients[i];
                ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
                synchronized(itsNatDoc) // No es necesario sincronizar los padres pues esta acci�n s�lo afecta a este documento
                {
                    clientDoc.setInvalid();
                }
            }
        }

        // Por si acaso pero no es necesario
        synchronized(ownerClientsById)
        {
            ownerClientsById.clear();
            docsById.clear();
        }

        // Ahora los observadores (que pueden serlo de otros documentos)

        ClientDocumentAttachedClientImpl[] attachedClients = getClientDocumentAttachedClientArray();
        if (attachedClients != null)
        {
            for(int i = 0; i < attachedClients.length; i++)
            {
                ClientDocumentAttachedClientImpl clientDoc = attachedClients[i];
                ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
                synchronized(itsNatDoc) // No es necesario sincronizar los padres pues esta acci�n s�lo afecta a este documento
                {
                    clientDoc.setInvalid(); // Yo creo que no hace falta pero por si acaso
                }
            }
        }

        // Esto es necesario, porque la invalidaci�n (que puede hacerse el documento de otra sesi�n)
        // no quita el cliente de la sesi�n propietaria
        synchronized(attachedClientsById)
        {
            attachedClientsById.clear();
        }

        // Con los attached server no hace falta invalidar ni nada
        synchronized(attachedServersById)
        {
            attachedServersById.clear();
        }

        referrer.popItsNatStfulDocument(); // Asegura que se vac�a
    }

    public String getToken()
    {
        return token;
    }

    public Browser getBrowser()
    {
        return browser;
    }

    public String getId()
    {
        return idObj.getId();
    }

    public UniqueId getUniqueId()
    {
        return idObj;
    }

    public String getUserAgent()
    {
        // No lo hacemos p�blico porque es m�s bien del mundo Http, lo ponemos en este nivel por razones pr�cticas y en futuro con un caso no HTTP se podr�a consolidar como p�blico
        return browser.getUserAgent();
    }

    protected abstract int getMaxInactiveInterval(); // Idem getUserAgent()

    public long getMaxInactiveIntervalMillisec()
    {
        int maxInterval = getMaxInactiveInterval(); // Devuelve segundos
        if (maxInterval < 0) // No caduca
            return Long.MAX_VALUE; // Equivale a unos 106.000 millones de d�as, nos vale :)
        return 1000 * maxInterval;
    }

    public abstract Object getStandardSessionObject();
    public abstract String getStandardSessionId();

    public ItsNatServletContext getItsNatServletContext()
    {
        return context;
    }

    public ItsNatServletContextImpl getItsNatServletContextImpl()
    {
        return context;
    }

    public Referrer getReferrer()
    {
        return referrer;
    }

    public ClientDocumentStfulImpl getClientDocumentStfulById(String id)
    {
        ClientDocumentStfulImpl clientDoc = getClientDocumentStfulOwnerById(id);
        if (clientDoc == null)
            clientDoc = getClientDocumentAttachedClientById(id);
        return clientDoc;
    }

    public int getClientDocumentStfulOwnerCount()
    {
        synchronized(ownerClientsById)
        {
           return ownerClientsById.size();
        }
    }

    public ClientDocumentStfulOwnerImpl[] getClientDocumentStfulOwnerArray()
    {
        // La regla es que con la sesi�n bloqueada NO debe bloquearse nada m�s
        // por eso formamos y devolvemos un array, para evitar iterar el Map con la sesi�n bloqueada
        // pues lo normal es que se necesite bloquear a su vez el documento asociado para acceder al objeto
        synchronized(ownerClientsById)
        {
            int size = ownerClientsById.size();
            if (size == 0) return null;

            ClientDocumentStfulOwnerImpl[] res = new ClientDocumentStfulOwnerImpl[size];
            int i = 0;
            for(Map.Entry<String,ClientDocumentStfulOwnerImpl> entry : ownerClientsById.entrySet())
            {
                res[i] = entry.getValue();
                i++;
            }
            return res;
        }
    }

    public ClientDocumentStfulOwnerImpl getClientDocumentStfulOwnerById(String id)
    {
        synchronized(ownerClientsById)
        {
            return ownerClientsById.get(id);
        }
    }

    public void registerClientDocumentStfulOwner(ClientDocumentStfulOwnerImpl clientDoc)
    {
        ClientDocumentStfulOwnerImpl clientRes;
        ItsNatStfulDocumentImpl docRes;

        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        synchronized(ownerClientsById)
        {
            clientRes = ownerClientsById.put(clientDoc);
            docRes = docsById.put(itsNatDoc);
        }

        if (clientRes != null) throw new ItsNatException("INTERNAL ERROR");
        if (docRes != null) throw new ItsNatException("INTERNAL ERROR");
    }

    public void unregisterClientDocumentStfulOwner(ClientDocumentStfulOwnerImpl clientDoc)
    {
        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        synchronized(ownerClientsById)
        {
            ownerClientsById.remove(clientDoc);
            docsById.remove(itsNatDoc);
        }
    }

    public ItsNatDocument[] getItsNatDocuments()
    {
        return getItsNatStfulDocumentArray();
    }

    public int getItsNatDocumentCount()
    {
        synchronized(ownerClientsById) // docsById est� subyugada y sincronizada a ownerClientsById
        {
           return docsById.size();
        }
    }

    public ItsNatStfulDocumentImpl[] getItsNatStfulDocumentArray()
    {
        // La regla es que con la sesi�n bloqueada NO debe bloquearse nada m�s
        // por eso formamos y devolvemos un array, para evitar iterar el Map con la sesi�n bloqueada
        // pues lo normal es que se necesite bloquear a su vez el documento asociado para acceder al objeto
        synchronized(ownerClientsById) // docsById est� subyugada y sincronizada a ownerClientsById
        {
            ItsNatStfulDocumentImpl[] res = new ItsNatStfulDocumentImpl[docsById.size()];
            int i = 0;
            for(Map.Entry<String,ItsNatStfulDocumentImpl> entry : docsById.entrySet())
            {
                res[i] = entry.getValue();
                i++;
            }
            return res;
        }
    }

    public ItsNatDocument getItsNatDocumentById(String id)
    {
        return getItsNatStfulDocumentById(id);
    }

    public ItsNatStfulDocumentImpl getItsNatStfulDocumentById(String id)
    {
        synchronized(ownerClientsById) // docsById est� subyugada y sincronizada a ownerClientsById
        {
            return docsById.get(id);
        }
    }

    public UniqueIdGenIntList getUniqueIdGenerator()
    {
        return idGenerator;
    }

    public ClientDocumentAttachedClientImpl getClientDocumentAttachedClientById(String id)
    {
        synchronized(attachedClientsById)
        {
            return attachedClientsById.get(id);
        }
    }

    private void addClientDocumentAttachedClient(ClientDocumentAttachedClientImpl clientDoc)
    {
        ClientDocumentAttachedClientImpl res;
        synchronized(attachedClientsById)
        {
            res = attachedClientsById.put(clientDoc);
        }
        if (res != null) throw new ItsNatException("INTERNAL ERROR"); // Asegura el registro una sola vez
    }

    private boolean removeClientDocumentAttachedClient(ClientDocumentAttachedClientImpl clientDoc)
    {
        ClientDocumentAttachedClientImpl res;
        synchronized(attachedClientsById)
        {
            res = attachedClientsById.remove(clientDoc);
        }
        return (res != null);  // Si true es que fue removido
    }

    public int getClientDocumentAttachedClientCount()
    {
        synchronized(attachedClientsById)
        {
           return attachedClientsById.size();
        }
    }

    public ClientDocumentAttachedClientImpl[] getClientDocumentAttachedClientArray()
    {
        synchronized(attachedClientsById)
        {
            int size = attachedClientsById.size();
            if (size == 0) return null;

            ClientDocumentAttachedClientImpl[] res = new ClientDocumentAttachedClientImpl[size];
            int i = 0;
            for(Map.Entry<String,ClientDocumentAttachedClientImpl> entry : attachedClientsById.entrySet())
            {
                res[i] = entry.getValue();
                i++;
            }
            return res;
        }
    }

    public void registerClientDocumentAttachedClient(ClientDocumentAttachedClientImpl clientDoc)
    {
        addClientDocumentAttachedClient(clientDoc); // Sujeta el objeto, si ya fue registrado dar� error

        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        itsNatDoc.addClientDocumentAttachedClient(clientDoc); // permite que se notifiquen los cambios
    }

    public void unregisterClientDocumentAttachedClient(ClientDocumentAttachedClientImpl clientDoc)
    {
        // Se supone que el documento asociado, si no se ha perdido, est� sincronizado
        clientDoc.setInvalid(); // Env�a el stop al cliente, si ya es inv�lido no hace nada

        boolean res = removeClientDocumentAttachedClient(clientDoc);
        if (res) // Si res es falso es que ya se desregistr�, pues aqu� hay que evitar hacerlo varias veces
        {
            ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
            itsNatDoc.removeClientDocumentAttachedClient(clientDoc); // El documento no lo retiene pero as� se quita expl�citamente
        }
    }

    public ClientDocumentAttachedServerImpl getClientDocumentAttachedServersById(String id)
    {
        synchronized(attachedServersById)
        {
            return attachedServersById.get(id);
        }
    }

    public void addClientDocumentAttachedServer(ClientDocumentAttachedServerImpl clientDoc)
    {
        ClientDocumentAttachedServerImpl res;
        synchronized(attachedServersById)
        {
            res = attachedServersById.put(clientDoc);
        }
        if (res != null) throw new ItsNatException("INTERNAL ERROR"); // Asegura el registro una sola vez
    }

    public boolean removeClientDocumentAttachedServer(ClientDocumentAttachedServerImpl clientDoc)
    {
        ClientDocumentAttachedServerImpl res;
        synchronized(attachedServersById)
        {
            res = attachedServersById.remove(clientDoc);
        }
        return (res != null);  // Si true es que fue removido
    }

    public int getClientDocumentAttachedServerCount()
    {
        synchronized(attachedServersById)
        {
           return attachedServersById.size();
        }
    }

    public ClientDocumentAttachedServerImpl[] getClientDocumentAttachedServerArray()
    {
        synchronized(attachedServersById)
        {
            int size = attachedServersById.size();
            if (size == 0) return null;

            ClientDocumentAttachedServerImpl[] res = new ClientDocumentAttachedServerImpl[size];
            int i = 0;
            for(Map.Entry<String,ClientDocumentAttachedServerImpl> entry : attachedServersById.entrySet())
            {
                res[i] = entry.getValue();
                i++;
            }
            return res;
        }
    }

    public ItsNatVariableResolver createItsNatVariableResolver()
    {
        return new ItsNatVariableResolverImpl(null,null,null,this,null);
    }

    public Object getVariable(String varName)
    {
        Object value = getAttribute(varName);
        if (value != null)
            return value;

        return getItsNatServletContextImpl().getVariable(varName);
    }

    public void invalidateLostResources()
    {
        // Suponemos que no hay nada sincronizado dependiente de esta sesi�n
        // (ej. documentos de esta sesi�n) cuando este m�todo es llamado.

        long currentTime = System.currentTimeMillis();
        long maxInactiveInterval = getMaxInactiveIntervalMillisec();

        cleanExpiredClients(currentTime,maxInactiveInterval);
        cleanExpiredAttachedServerClients(currentTime,maxInactiveInterval);
        cleanExpiredReferrer(currentTime,maxInactiveInterval);

        cleanExcessClientDocumentStfulOwners();
        cleanExcessClientDocumentAttachedServers();
    }

    protected void cleanExpiredClients(long currentTime,long maxInactiveInterval)
    {
        // Aunque este m�todo es fundamentalmente �til para navegadores que
        // no siempre env�an el evento unload (ejemplo Opera 9 y muchos otros),
        // puede ocurrir que una p�gina cargada por un navegador que s� lo hace
        // haya sido parada la carga antes de que se ejecute el script de inicio,
        // por lo que el evento "unload" no se ejecutar� de todas formas. Por ello ejecutamos
        // esta limpieza para cualquier navegador.

        ClientDocumentStfulOwnerImpl[] clientOwnerList = getClientDocumentStfulOwnerArray();
        if (clientOwnerList != null)
        {
            for(int i = 0; i < clientOwnerList.length; i++)
            {
                ClientDocumentStfulOwnerImpl clientDoc = clientOwnerList[i];
                long lastRequestTime = clientDoc.getLastRequestTime();  // No hace falta sincronizar
                long interval = currentTime - lastRequestTime;
                if (interval > maxInactiveInterval)
                {
                    ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();  // No hace falta sincronizar
                    synchronized(itsNatDoc) // No es necesario sincronizar los padres pues esta acci�n s�lo afecta a este documento
                    {
                        clientDoc.setInvalid();
                    }
                }
                else
                {
                    // Es posible que haya clientes de control remoto zombies asociados al documento de este cliente y que no pertenezcan
                    // a esta sesi�n, es posible que el usuario cerrara el cliente control remoto
                    // pero el navegador no notificara este cierre (ocurre en algunos), si el usuario sigue
                    // activo en otra p�gina la sesi�n seguir� viva por lo que el cliente zombie seguir�
                    // recibiendo c�digo JavaScript indefinidamente.
                    // Por tanto los intentamos limpiar aqu�:
                    ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();  // No hace falta sincronizar
                    synchronized(itsNatDoc) // No es necesario sincronizar los padres pues esta acci�n s�lo afecta a este documento
                    {
                        if (itsNatDoc.hasClientDocumentAttachedClient())
                        {
                            ClientDocumentAttachedClientImpl[] clientAttachList = itsNatDoc.getClientDocumentAttachedClientArray();
                            for(int j = 0; j < clientAttachList.length; j++)
                            {
                                ClientDocumentAttachedClientImpl clientDocAttached = clientAttachList[j];
                                ItsNatSessionImpl attachedSession = clientDocAttached.getItsNatSessionImpl();
                                if (attachedSession == this) continue; // No merece la pena, se procesar� despu�s
                                cleanExpiredClientDocumentAttachedClient(clientDocAttached,currentTime); // currentTime vale pero el maxInactiveInterval depende de la sesi�n
                            }
                        }

                        // Ya que estamos aprovechamos para limpiar los attached client excedentes
                        // As� eliminamos los clientes attached excedentes de los documentos
                        // guardados en esta sesi�n est�n expirados o no (cada sesi�n har� lo suyo aunque sean clientes en diferentes sesiones)
                        // obviamente eliminaremos primero los que no han sido tocados durante m�s tiempo

                        cleanExcessClientDocumentAttachedClients(itsNatDoc);
                    }
                }
            }
        }

        // Eliminamos los ClientDocument observadores que pueden estar zombies
        // porque el navegador no ha notificado que se cerr� la ventana.
        // Esta limpieza es redundante (pero necesaria) pues
        // esta impieza ya la hace el cliente propietario del documento (antes de llegar aqu�).
        // De hecho el propietario no hace la limpieza de sus auto-observadores (misma sesi�n) pues
        // ahora se hace aqu� (ver "if (attachedSession == this) continue;")
        ClientDocumentAttachedClientImpl[] clientAttachedList = getClientDocumentAttachedClientArray();
        if (clientAttachedList != null)
        {
            for(int i = 0; i < clientAttachedList.length; i++)
            {
                ClientDocumentAttachedClientImpl clientDoc = clientAttachedList[i];
                cleanExpiredClientDocumentAttachedClient(clientDoc,currentTime);
            }
        }
    }

    public static void cleanExpiredClientDocumentAttachedClient(ClientDocumentAttachedClientImpl clientDoc,long currentTime)
    {
        ItsNatSessionImpl session = clientDoc.getItsNatSessionImpl(); // Nos aseguramos as� que la sesi�n en la que se evalua es la propietaria del cliente (por eso el m�todo es est�tico)
        long maxInactiveInterval = session.getMaxInactiveIntervalMillisec();

        long lastRequestTime = clientDoc.getLastRequestTime();  // No hace falta sincronizar
        long interval = currentTime - lastRequestTime;
        if (interval > maxInactiveInterval)
        {
            ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
            synchronized(itsNatDoc) // No es necesario sincronizar los padres pues esta acci�n s�lo afecta a este documento
            {
                clientDoc.invalidateAndUnregister(); // Desregistra de la sesi�n que puede no ser esta
            }
        }
    }


    public void cleanExpiredAttachedServerClients(long currentTime,long maxInactiveInterval)
    {
        // En teor�a los clientes attached server son temporales durante el proceso
        // de carga, que se hace en m�ltiples requests, cuando la carga termina
        // se desregistra. Sin embargo es posible que por errores en el proceso
        // o por un cliente enga�oso o lo que sea se quede a medias y por tanto quede
        // registrado.

        ClientDocumentAttachedServerImpl[] clientAttachedList = getClientDocumentAttachedServerArray();
        if (clientAttachedList != null)
        {
            for(int i = 0; i < clientAttachedList.length; i++)
            {
                ClientDocumentAttachedServerImpl clientDoc = clientAttachedList[i];

                // Realmente el getLastRequestTime es el getCreationTime pues se supone que si no hay error
                // los requests del proceso de carga son muy seguidos y no merece la pena actualizar, pero por si acaso.
                // Aunque es una barbaridad de tiempo usar el tiempo de inactividad de la sesi�n
                // no tenemos otro tiempo en qu� basarnos y no merece la pena pedir al usuario
                // un nuevo tiempo cuando esto s�lo va a ocurrir en caso de error o de usuario malicioso
                long lastRequestTime = clientDoc.getLastRequestTime();  // No hace falta sincronizar
                long interval = currentTime - lastRequestTime;
                if (interval > maxInactiveInterval)
                {
                    clientDoc.setInvalid();  // Se desregistra de la sesi�n
                }
            }
        }
    }

    protected void cleanExpiredReferrer(long currentTime,long maxInactiveInterval)
    {
        Referrer referer = getReferrer();
        if (referrer instanceof ReferrerStrong)
        {
            // Caso Opera y similares
            // Referrer es de tipo ReferrerStrong y en el unload
            // de la p�gina la llamada cleanItsNatStfulDocument no hace nada
            // para que el destino pueda hacer pop al referrer
            // por lo que es posible que nadie haya querido el referrer
            // y haya quedado indefinidamente con una referencia strong.

            ItsNatStfulDocumentImpl itsNatDocRef = referer.getItsNatStfulDocument();
            if (itsNatDocRef != null)
            {
                ClientDocumentStfulOwnerImpl clientRef = itsNatDocRef.getClientDocumentStfulOwner();  // No hace falta sincronizar
                long lastRequestTime = clientRef.getLastRequestTime();  // No hace falta sincronizar
                long interval = currentTime - lastRequestTime;
                if (interval > maxInactiveInterval)
                    referer.popItsNatStfulDocument(); // Lo quitamos
            }
        }
    }

    protected void cleanExcessClientDocumentStfulOwners()
    {
        // Eliminamos los documentos excedentes est�n expirados o no,
        // obviamente eliminaremos primero los que no han sido tocados durante m�s tiempo

        int maxLiveDocs = getItsNatServletContextImpl().getMaxOpenDocumentsBySession();
        if ((maxLiveDocs >= 0) && (getClientDocumentStfulOwnerCount() > maxLiveDocs))
        {
            // Solamente maxLiveDocs live documents allowed in this session
            // probablemente es un robot que admite cookies o un navegador no soportado
            // que da error y no recibe el evento unload o bien JavaScript
            // est� desactivado aunque sea un navegador permitido
            ClientDocumentStfulOwnerImpl[] clients = getClientDocumentStfulOwnerArray();
            if (clients != null) // Por si acaso hay que tener en cuenta que es multihilo
            {
                int excess = clients.length - maxLiveDocs;

                // Invalidamos los que llevan m�s tiempo sin usar
                Arrays.sort(clients,COMPARATOR_STFUL_OWNER);
                for(int i = 0; i < excess; i++)
                {
                    ClientDocumentStfulOwnerImpl clientDoc = clients[i];
                    ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
                    synchronized(itsNatDoc) // No es necesario sincronizar los padres pues esta acci�n s�lo afecta a este documento
                    {
                        clientDoc.setInvalid();
                    }
                }
            }
        }
    }

    public void cleanExcessClientDocumentAttachedClients(ItsNatStfulDocumentImpl itsNatDoc)
    {
        // El documento debe estar sincronizado, se ejecutar� en monohilo

        int maxClientAttachedNum;
        int maxClients = itsNatDoc.getMaxOpenClientsByDocument();
        if (maxClients < -1) maxClientAttachedNum = -1; // Ilimitado
        else maxClientAttachedNum = maxClients - 1; // Pues en maxClients se incluye el owner y s�lo vamos a limpiar los attached (y no puede ser cero)
        if ((maxClientAttachedNum >= 0) && (itsNatDoc.getClientDocumentAttachedCount() > maxClientAttachedNum))
        {
            ClientDocumentAttachedClientImpl[] clientList = itsNatDoc.getClientDocumentAttachedClientArray();

            int excess = clientList.length - maxClientAttachedNum;

            // Invalidamos los que llevan m�s tiempo sin usar
            Arrays.sort(clientList,COMPARATOR_ATTACHED_CLIENTS);
            for(int i = 0; i < excess; i++)
            {
                ClientDocumentAttachedClientImpl clientDoc = clientList[i];
                clientDoc.invalidateAndUnregister(); // Se desregistra de su sesi�n (puede no ser esta)
            }
        }
    }

    protected void cleanExcessClientDocumentAttachedServers()
    {
        // Eliminamos los clientes attached server en proceso de carga que sobren est�n
        // expirados o no con el fin de que un usuario malicioso quiera saturarnos la sesi�n
        // iniciando montones de clientes attached server seguidos pero evitando
        // la carga completa y por tanto evitando su destrucci�n.
        // Como un cliente attached server cuando termina su vida acaba siendo un documento
        // normal, es razonable evitar que haya m�s procesos en carga que documentos
        // permitidos en la sesi�n pues de todas formas cuando acaben ser�n invalidados
        // obviamente eliminaremos primero los que no han sido tocados durante m�s tiempo

        int maxLiveDocs = getItsNatServletContextImpl().getMaxOpenDocumentsBySession();
        if ((maxLiveDocs >= 0) && (getClientDocumentAttachedServerCount() > maxLiveDocs))
        {
            // Solamente maxLiveDocs live documents allowed in this session
            // probablemente es un robot que admite cookies o un navegador no soportado
            // que da error y no recibe el evento unload o bien JavaScript
            // est� desactivado aunque sea un navegador permitido
            ClientDocumentAttachedServerImpl[] clientList = getClientDocumentAttachedServerArray();
            if (clientList != null) // Por si acaso hay que tener en cuenta que es multihilo
            {
                int excess = clientList.length - maxLiveDocs;

                // Invalidamos los que llevan m�s tiempo sin usar
                Arrays.sort(clientList,COMPARATOR_ATTACHED_SERVERS);
                for(int i = 0; i < excess; i++)
                {
                    ClientDocumentAttachedServerImpl clientDoc = clientList[i];
                    clientDoc.setInvalid();   // Se desregistra de la sesi�n
                }
            }
        }
    }


}

class LastRequestComparator<T> implements Comparator<T>
{
    public int compare(T o1, T o2)
    {
        ClientDocumentImpl clientDoc1 = (ClientDocumentImpl)o1;
        ClientDocumentImpl clientDoc2 = (ClientDocumentImpl)o2;
        long t1 = clientDoc1.getLastRequestTime();
        long t2 = clientDoc2.getLastRequestTime();
        if (t1 < t2) return -1;
        else if (t1 > t2) return +1;
        else return 0;
    }
}

