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
package org.itsnat.impl.core.servlet.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import org.itsnat.core.ItsNatException;
import org.itsnat.core.ItsNatServletResponse;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.servlet.DeserialPendingTask;
import org.itsnat.impl.core.servlet.ItsNatServletContextImpl;
import org.itsnat.impl.core.servlet.ItsNatServletImpl;
import org.itsnat.impl.core.servlet.ItsNatServletRequestImpl;
import org.itsnat.impl.core.servlet.ItsNatServletResponseImpl;
import org.itsnat.impl.core.servlet.ItsNatSessionSerializeContainerImpl;
import org.itsnat.impl.core.util.MapListImpl;

/**
 *
 * @author jmarranz
 */
public class ItsNatHttpSessionReplicationCapableImpl extends ItsNatHttpSessionImpl
{
    public static final String SESSION_ATTRIBUTE_NAME = "itsnat_session";
    public static final String SESSION_ATTRIBUTE_NAME_FRAGMENT_COUNT = "itsnat_session_fragment_count";
    public static final String SESSION_ATTRIBUTE_NAME_FRAGMENT_NUM = "itsnat_session_fragment_";

    protected ItsNatHttpSessionReplicationCapableImpl(HttpSession session,ItsNatServletContextImpl context,Browser browser)
    {
        super(session,context,browser);

        this.serialContainer = new ItsNatSessionSerializeContainerImpl(this);  // Por ahora la serializaci�n s�lo tiene sentido en este contexto (http session en modo replication capable)

        // Es el caso del comienzo del primer request, el atributo de estado probablemente
        // no haya sido definido (caso de un nodo).
        // Yo creo que no es necesario pues se hace tambi�n al final del request
        // De todas maneras no hay problema, la aplicaci�n no se ha usado por parte
        // del usuario y estar� vac�a la sesi�n
        writeItsNatHttpSessionToAttribute();
    }


    protected void writeItsNatHttpSessionToAttribute()
    {
        // http://blog.stringbuffer.com/2009/04/http-sessions-and-google-app-enginej.html
        try
        {
            synchronized(session) // Si requiere serializaci�n no est� mal sincronizar
            {
                if (context.isSessionExplicitSerialize())
                {
                    long size = context.getSessionExplicitSerializeFragmentSize(); // Se supone que no cambia, devuelve siempre el mismo valor

                    // Al deserializar se obtiene siempre una instancia nueva
                    byte[] stream = serializeSession(serialContainer);

                    if (size == ItsNatServletContextImpl.SESSION_EXPLICIT_SERIALIZE_ONE_FRAGMENT)
                    {
                        session.setAttribute(SESSION_ATTRIBUTE_NAME,stream);
                    }
                    else
                    {

                        // Dividimos en trozos de tama�o size (> 0)
                        // size es long por si acaso pero por ahora el m�ximo valor que
                        // serializamos es del tama�o int de acuerdo con el tipo de datos del length
                        // del array
                        int sizeInt = (int)size;
                        int numFrag = stream.length / sizeInt; // Recuerda que es divisi�n entera
                        int modulo = (stream.length % sizeInt);
                        if (modulo != 0) numFrag++; // Uno m�s
                        // Llegados aqu� numFrag nunca ser� cero porque nunca el array estar� vac�o (length == 0)

                        // Antes limpiamos los atributos actuales excedentes para que no queden trozos perdidos
                        Integer numFragObjOld = (Integer)session.getAttribute(SESSION_ATTRIBUTE_NAME_FRAGMENT_COUNT);
                        if (numFragObjOld != null)
                        {
                            // Eliminamos s�lo los excedentes pues con los dem�s siempre puede GAE
                            // evitar su transmisi�n si detecta que el contenido es el mismo
                            int numFragOld = numFragObjOld.intValue();
                            for(int i = numFrag; i < numFragOld; i++)
                                session.removeAttribute(SESSION_ATTRIBUTE_NAME_FRAGMENT_NUM + i);
                        }

                        session.setAttribute(SESSION_ATTRIBUTE_NAME_FRAGMENT_COUNT,new Integer(numFrag));
                        for(int i = 0; i < numFrag; i++)
                        {
                            int sizeFrag = (i != numFrag - 1) ? sizeInt : modulo;
                            byte[] fragment = new byte[sizeFrag];
                            System.arraycopy(stream, i * sizeInt, fragment, 0, sizeFrag);
                            session.setAttribute(SESSION_ATTRIBUTE_NAME_FRAGMENT_NUM + i,fragment);
                        }
                    }
                }
                else  // No serializaci�n expl�cita
                {
                    session.setAttribute(SESSION_ATTRIBUTE_NAME,serialContainer);
                }
            }
        }
        catch(Exception ex)
        {
            // Evitamos guardar en la sesi�n (si el error ocurri� al serializar)
            ItsNatSessionSerializeContainerImpl.showError(ex,false);
        }
    }

    public void endOfRequestBeforeSendCode()
    {
        // No se usa, antes se usaba (err�neamente) en la simulaci�n de serializaci�n
        // eliminar en el futuro, lo dejamos por si acaso
    }

    public void endOfRequest()
    {
        // Es el caso por ejemplo de GAE, se supone que GAE "serializa" las
        // requests incluso entre JVMs

        // De esta manera notificamos a la sesi�n nativa que serialice
        // de nuevo la sesi�n, por ejemplo en GAE, pues GAE s�lo serializa
        // cuando se actualiza via setAttribute de acuerdo con un uso interno de HttpSessionBindingListener
        // http://groups.google.com/group/google-appengine/browse_thread/thread/94b6e2b38ddfe59

        writeItsNatHttpSessionToAttribute();
    }

    protected static ItsNatSessionSerializeContainerImpl readItsNatSessionSerializeContainerFromSessionAttribute(HttpSession session,ItsNatServletContextImpl context)
    {
        // Puede devolver null, no se ha guardado todav�a en el atributo (nueva sesi�n)

        try
        {
            if (context.isSessionExplicitSerialize())
            {
                long size = context.getSessionExplicitSerializeFragmentSize(); // Se supone que no cambia, devuelve siempre el mismo valor

                byte[] stream;
                if (size == ItsNatServletContextImpl.SESSION_EXPLICIT_SERIALIZE_ONE_FRAGMENT)
                {
                    stream = (byte[])session.getAttribute(SESSION_ATTRIBUTE_NAME);
                    if (stream == null) return null;
                }
                else
                {
                    // size > 0
                    Integer numFragObj = (Integer)session.getAttribute(SESSION_ATTRIBUTE_NAME_FRAGMENT_COUNT);
                    if (numFragObj == null) return null; // Nueva sesi�n, no se ha guardado
                    int numFrag = numFragObj.intValue();
                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                    for(int i = 0; i < numFrag; i++)
                    {
                        byte[] fragment = (byte[])session.getAttribute(SESSION_ATTRIBUTE_NAME_FRAGMENT_NUM + i);
                        byteArray.write(fragment);
                    }
                    stream = byteArray.toByteArray();
                }

                return deserializeSession(stream);
            }
            else // No serializaci�n expl�cita
            {
                return (ItsNatSessionSerializeContainerImpl)session.getAttribute(SESSION_ATTRIBUTE_NAME);
            }
        }
        catch(Exception ex)
        {
            // Este error puede darse por ejemplo a causa de un cambio de configuraci�n del ItsNatServletContextImpl
            // existiendo sesiones por ejemplo ya serializadas a archivo en el contenedor de servlets (por un rearranque).
            // En ese caso es posible que sea el cast el que falle por ejemplo (por cambio de m�todo de serializaci�n)
            // As� devolviendo null desecharemos esta sesi�n "silenciosamente" sin provocar un error.
            ItsNatSessionSerializeContainerImpl.showError(ex,false);
            return null;
        }
    }

    protected static ItsNatHttpSessionImpl readItsNatHttpSessionFromAttribute(HttpSession session,ItsNatServletContextImpl itsNatContext,ItsNatHttpServletRequestImpl itsNatRequest)
    {
        try
        {
            ItsNatSessionSerializeContainerImpl serialContainer = readItsNatSessionSerializeContainerFromSessionAttribute(session,itsNatContext);
            if (serialContainer == null) return null; // No se ha guardado todav�a en el atributo (nueva sesi�n)

            ItsNatHttpSessionReplicationCapableImpl itsNatSession = (ItsNatHttpSessionReplicationCapableImpl)serialContainer.getItsNatSession();
            // Puede ser nula la sesi�n, significa que la lectura fue err�nea (as� soportamos cambios en el c�digo sin eliminar manualmente las sesiones guardadas en GAE)
            if (itsNatSession == null) return null;

            // A partir de aqu� itsNatSession NO es nulo

            if (itsNatSession.session == null) // Es una instancia nueva resultado de de-serializar
            {
                // Pasamos por aqu� inmediatamente despu�s de de-serializar

                // Fue obtenido por serializaci�n desde otro nodo o bien al cargar el servidor
                // antes del request, la sesi�n nativa no est� definida todav�a porque es transient
                itsNatSession.session = session;

                itsNatSession.executeDeserialPendingTasks(itsNatContext,itsNatRequest);

                itsNatContext.addItsNatSession(itsNatSession);
            }

            return itsNatSession; // En teor�a devolvemos aqu� una sesi�n bien deserializada y formada
        }
        catch(Exception ex)
        {
            // Hay que excluir cualquier posibilidad de que por cambios de esquema, de registro de templates
            // de lo que sea ocurra una excepci�n en el proceso de deserializaci�n de una sesi�n guardada
            // probablemente antes de los cambios dados, pues lo f�cil es deshechar la sessi�n y punto
            // sin que GAE o ItsNat crea que ha habido un error (lo que ocurre si dejamos propagar la excepci�n)
            ItsNatSessionSerializeContainerImpl.showError(ex,false);
            return null;
        }
    }

    protected void executeDeserialPendingTasks(ItsNatServletContextImpl itsNatContext,ItsNatServletRequestImpl itsNatRequest) throws Exception
    {
        // Es posible que la deserializaci�n se produjera no ahora sino al cargarse
        // el servidor o al restaurar una sesi�n que no se ha tocado
        // desde hace tiempo, en ese caso el servlet no estaba
        // iniciado y algunas tareas quedaron pendientes, ahora que est�
        // el servlet iniciado podemos hacerlo
        // Ahora bien, este request pertenece a un servlet concreto y puede haber varios
        // servlets, pero tenemos que despertar a todos los servlets AHORA porque de otra forma
        // al terminar este request se intentar�n serializar los documentos de otros servlets
        // que ni siquiera est�n correctamente de-serializados

        DeserialPendingTask sessionTask = getSessionDeserialPendingTask();
        if (sessionTask != null)
        {
            ItsNatServletImpl itsNatServlet = itsNatRequest.getItsNatServletImpl();
            ItsNatServletResponse itsNatResponse = itsNatRequest.getItsNatServletResponse();

            sessionTask.process(itsNatServlet,itsNatRequest,itsNatResponse);

            setSessionDeserialPendingTask(null); // Para liberar memoria
        }

        if (hasDeserialPendingTasks())
        {
            ItsNatServletImpl itsNatServlet = itsNatRequest.getItsNatServletImpl();
            String servletName = itsNatServlet.getName();

            ItsNatServletResponse itsNatResponse = itsNatRequest.getItsNatServletResponse();

            ServletContext context = itsNatContext.getServletContext();

            MapListImpl<String,DeserialPendingTask> pendingTasks = getDeserialPendingTasks();

            for(Map.Entry<String,LinkedList<DeserialPendingTask>> entry : pendingTasks.getMap().entrySet() )
            {
                String currServletName = entry.getKey();
                LinkedList<DeserialPendingTask> pendingTasksOfServlet = entry.getValue();
                if (pendingTasksOfServlet == null) continue; // Por si acaso pero es raro que sea nulo

                if (servletName.equals(currServletName))
                {
                    // El servlet que est� haciendo la request de verdad
                    for(DeserialPendingTask task : pendingTasksOfServlet)
                    {
                        task.process(itsNatServlet,itsNatRequest,itsNatResponse);
                    }
                }
                else
                {
                    ItsNatServletImpl currItsNatServlet = ItsNatServletImpl.getItsNatServletByName(currServletName);

                    ServletRequest servRequest = itsNatRequest.getServletRequest();
                    ServletResponse servResponse = itsNatResponse.getServletResponse();

                    if (currItsNatServlet == null)
                    {
                        // Despertamos al servlet para que se inicie y se registren los templates etc
                        RequestDispatcher servletDisp = context.getNamedDispatcher(currServletName);
                        // No chequeamos si es null, caso de eliminaci�n de servlet o similar en una nueva versi�n de la app.
                        // no merece la pena porque la deserializaci�n ser� err�nea, dejamos fallar
                        // aunque perdamos la sessi�n entera
                        Object currItsNatAction = servRequest.getAttribute("itsnat_action");
                        servRequest.setAttribute("itsnat_action", ItsNatHttpServletImpl.ACTION_SERVLET_WEAK_UP);

                        servletDisp.include(servRequest,servResponse); // Aseguramos as� que se inicializa
                        // Lo dejamos como estaba
                        servRequest.removeAttribute("itsnat_action");
                        if (currItsNatAction != null)
                            servRequest.setAttribute("itsnat_action",currItsNatAction);

                        // Ahora deber�a de estar
                        currItsNatServlet = ItsNatServletImpl.getItsNatServletByName(currServletName);
                    }


                    // Porque este servlet es diferente al que recibe la request, no pasamos
                    // los objetos request y response originales pues los de ItsNat est�n vinculados
                    // al servlet, tenemos que crear un par "falsos"
                    // el �nico caso problem�tico son los templates basados en TemplateSource que son los �nicos que necesitan estos objetos
                    ItsNatServletRequestImpl currItsNatServReq = currItsNatServlet.createItsNatServletRequest(servRequest,servResponse,this); // Pasando la sesi�n como par�metro evitamos que se intente cargar de nuevo
                    ItsNatServletResponseImpl currItsNatServResp = currItsNatServReq.getItsNatServletResponseImpl();

                    for(DeserialPendingTask task : pendingTasksOfServlet)
                    {
                        task.process(currItsNatServlet,currItsNatServReq,currItsNatServResp);
                    }
                }
            }

            clearDeserialPendingTasks(); // Para liberar memoria
        }
    }

    
    public static byte[] serializeSession(ItsNatSessionSerializeContainerImpl serialContainer)
    {
        ByteArrayOutputStream ostream = null;
        try
        {
           ostream = new ByteArrayOutputStream();
           ObjectOutputStream p = new ObjectOutputStream(ostream);
           p.writeObject(serialContainer); // Write the tree to the stream.
           p.flush();
           ostream.close();    // close the file.
        }
        catch (Exception ex)
        {
            try
            {
                if (ostream != null) ostream.close();
            }
            catch(IOException ex2)
            {
                ex.printStackTrace();
                throw new ItsNatException(ex2);
            }
            throw new ItsNatException(ex);
        }

        return ostream.toByteArray();
    }

    public static ItsNatSessionSerializeContainerImpl deserializeSession(byte[] stream)
    {
        ItsNatSessionSerializeContainerImpl serialContainer;
        ByteArrayInputStream istream = null;
        try
        {
            istream = new ByteArrayInputStream(stream);
            ObjectInputStream q = new ObjectInputStream(istream);
            serialContainer = (ItsNatSessionSerializeContainerImpl)q.readObject();
            istream.close();
        }
        catch (Exception ex)
        {
            try
            {
                if (istream != null) istream.close();
            }
            catch(IOException ex2)
            {
                ex.printStackTrace();
                throw new ItsNatException(ex2);
            }
            throw new ItsNatException(ex);
        }

        return serialContainer;
    }
}
