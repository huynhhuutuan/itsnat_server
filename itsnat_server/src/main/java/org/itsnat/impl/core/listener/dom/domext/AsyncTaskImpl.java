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

package org.itsnat.impl.core.listener.dom.domext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.itsnat.impl.core.listener.*;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.doc.ItsNatDocSynchronizerImpl;
import org.itsnat.impl.core.doc.ItsNatDocumentImpl;
import org.itsnat.impl.core.domimpl.ItsNatNodeInternal;
import org.w3c.dom.Element;

/**
 *
 * @author jmarranz
 */
public class AsyncTaskImpl extends GenericTaskImpl implements Runnable
{
    protected boolean lockDoc;
    protected long maxWait;
    protected transient Thread thread;
    protected boolean started = false;
    protected Runnable task;
    protected ClientDocumentStfulImpl clientDoc;

    /**
     * Creates a new instance of AsyncTask
     */
    public AsyncTaskImpl(Runnable task,boolean lockDoc,long maxWait,ClientDocumentStfulImpl clientDoc)
    {
        if (task instanceof Thread) throw new ItsNatException("Runnable object must not be a Thread",task);

        this.task = task;

        this.lockDoc = lockDoc;
        this.maxWait = maxWait;

        this.clientDoc = clientDoc;

        this.thread = new Thread(this);
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        this.thread = new Thread(this); // No se puede propagar, esto es simplemente para que no de error al de-serializar
    }    
    
    public ItsNatDocumentImpl getItsNatDocument()
    {
        return clientDoc.getItsNatStfulDocument();
    }

    public boolean isStarted()
    {
        return started;
    }

    public Runnable getTask()
    {
        return task;
    }

    public boolean locksDocument()
    {
        return lockDoc;
    }

    public void start()
    {
        thread.start();

        if (!started)
        {
            // Para asegurarnos de que ha empezado pues podr�a darse la paradoja de
            // que el evento que recoje los resultados llegara antes de que empiece el hilo que tiene que hacerlos
            while(!started)
                Thread.yield();
        }
    }

    @Override
    public void dispose()
    {
        this.thread = null;
    }

    @Override
    public boolean isDisposed()
    {
        return (thread == null);
    }

    @Override
    public boolean mustWait()
    {
        Thread thread = this.thread;
        if (thread == null) return false; // disposed
        return thread.isAlive();
    }

    @Override
    public void run()
    {
        this.started = true;

        final Runnable task = getTask();
        if (lockDoc)
        {
            ItsNatDocumentImpl itsNatDoc = getItsNatDocument();
            // Es una rutina del usuario, no sabemos si acceder� a los documentos padre as� que por si acaso sincronizamos los padres
            ItsNatDocSynchronizerImpl syncTask = new ItsNatDocSynchronizerImpl()
            {
                @Override
                protected void syncMethod()
                {
                    task.run();
                }
            };
            syncTask.exec(itsNatDoc);
        }
        else
        {
            task.run();
        }
    }

    public Thread getThread()
    {
        return thread;
    }

    @Override
    public void waitToFinish()
    {
        Thread thread = this.thread;
        if (thread == null) return; // Es disposed
        if (thread.isAlive())
        {
            // Para que espere tranquilito (parado) dando oportunidad a otros hilos
            try {  thread.join(maxWait); } catch(InterruptedException ex) { }
        }
    }
}
