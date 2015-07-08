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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.itsnat.core.ItsNatException;

/**
    La finalidad de esta clase es poder evitar que se registre la sesi�n cuando hay
    un problema de carga de la sesi�n guardada en el datastore/memcached en
    Google App Engine, por ejemplo cuando alguna clase de ItsNat serializable
    ha cambiado su esquema, o las clases del usuario.
    Si no capturamos la excepci�n (InvalidClassException ser� lo normal) GAE
    lo intentar� de nuevo.
    La forma manual ser�a destruir las sesiones via web de administraci�n (Datastore Viewer),
    el problema (y esto es un fallo de GAE) es que tambi�n pueden estar en el memcached
    y no hay forma via admin de borrarla, la soluci�n es eliminar la cookie del navegador
    pero esto NO se le puede pedir al usuario final.
    Evitando que la excepci�n llegue a GAE le decimos a GAE que hemos le�do correctamente la sesi�n
    aunque realmente la descartemos.

 * @author jmarranz
 */
public class ItsNatSessionSerializeContainerImpl implements Serializable
{
    public static final long serialVersionUID = 1L; // ��NO CAMBIAR!!

    protected transient ItsNatSessionImpl itsNatSession;

    public ItsNatSessionSerializeContainerImpl(ItsNatSessionImpl itsNatSession)
    {
        this.itsNatSession = itsNatSession;
    }

    public ItsNatSessionImpl getItsNatSession()
    {
        return itsNatSession; // Puede ser null
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        try
        {
            byte[] stream = serializeSession(itsNatSession);

            ItsNatServletContextImpl itsNatCtx = itsNatSession.getItsNatServletContextImpl();
            boolean compressed = itsNatCtx.isSessionSerializeCompressed(); // Se supone que no cambia nunca
            if (compressed)
                stream = compressByteArray(stream);

            out.writeBoolean(compressed);

            out.writeInt(stream.length);
            out.write(stream);
        }
        catch(Exception ex)
        {
            // La serializaci�n ser� err�nea pero as� conseguimos que la de-serializaci�n
            // sea err�nea tambi�n y se pueda recrear la sesi�n, de otra manera se queda
            // atascada la aplicaci�n con una sesi�n err�nea indefinidamente
            showError(ex,false);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        // Para que quede claro
        this.itsNatSession = null;
        try
        {
            // Esta es la raz�n del guardar el compressed, pues no tenemos acceso al ItsNatServletContextImpl
            // en esta fase pues es posible que est� deserializando antes de cargar alg�n servlet.
            boolean compressed = in.readBoolean();

            int total = in.readInt();
            byte[] stream = new byte[total];
            in.readFully(stream);

            if (compressed)
                stream = decompressByteArray(stream);            

            this.itsNatSession = deserializeSession(stream);
            itsNatSession.setItsNatSessionSerializeContainer(this); // Para que al serializar detecte que es el mismo objeto que se ley�
            // Todo ha ido bien, itsNatSession ser� distinto de null
            // y est� creado correctamente
        }
        catch(Exception ex)
        {
            // Mal, GAE creer� que lo hemos cargado bien pero no es verdad
            // seguramente ha cambiado el esquema
            this.itsNatSession = null;

            showError(ex,false);
        }
    }

    public static void showError(Exception ex,boolean write)
    {
        if (write)
            System.err.println("Something was wrong serializing");
        else
            System.err.println("Something was wrong de-serializing, stored session was discarded");

        // GAE tiende a ocultar las salidas por pantalla
        // de un ex.printStackTrace(); por eso hacemos esto, nos interesa
        // saber qu� ha pasado exactamente.
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        System.err.println(sw.toString());
    }

    public static byte[] serializeSession(ItsNatSessionImpl itsNatSession)
    {
        ByteArrayOutputStream ostream = null;
        try
        {
           ostream = new ByteArrayOutputStream();
           ObjectOutputStream p = new ItsNatSessionObjectOutputStream(ostream);
           p.writeObject(itsNatSession); // Write the tree to the stream.
           p.flush();
           ostream.close();
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

    public static ItsNatSessionImpl deserializeSession(byte[] stream)
    {
        ItsNatSessionImpl itsNatSession;
        ByteArrayInputStream istream = null;
        try
        {
            istream = new ByteArrayInputStream(stream);
            ObjectInputStream q = new ItsNatSessionObjectInputStream(istream);
            itsNatSession = (ItsNatSessionImpl)q.readObject();
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

        return itsNatSession;
    }

    protected static byte[] compressByteArray(byte[] stream)
    {
        ByteArrayOutputStream inByteArray = new ByteArrayOutputStream();
        GZIPOutputStream output = null;
        try
        {
            output = new GZIPOutputStream(inByteArray);
            output.write(stream);
        }
        catch(IOException ex)
        {
            throw new ItsNatException(ex);
        }
        finally
        {
            if (output != null)
                try{ output.close(); }
                catch(IOException ex) { throw new ItsNatException(ex); }
        }

        stream = inByteArray.toByteArray();
        return stream;
    }

    protected static byte[] decompressByteArray(byte[] stream)
    {
        ByteArrayOutputStream outByteArray = new ByteArrayOutputStream();

        GZIPInputStream input = null;
        try
        {
            input = new GZIPInputStream(new ByteArrayInputStream(stream));
            byte[] buffer = new byte[stream.length]; // Al expandir se har� m�s grande (el doble m�s o menos) por lo que as� el buffer es en torno a la mitad
            int readed;
            while((readed = input.read(buffer)) != -1)
                outByteArray.write(buffer, 0, readed);
        }
        catch(IOException ex)
        {
            throw new ItsNatException(ex);
        }
        finally
        {
            if (input != null)
                try{ input.close(); }
                catch(IOException ex) { throw new ItsNatException(ex); }
        }

        stream = outByteArray.toByteArray();
        return stream;
    }

}
