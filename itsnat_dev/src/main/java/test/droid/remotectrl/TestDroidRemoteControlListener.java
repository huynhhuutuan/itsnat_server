/*
 * TestRemoteControlListener.java
 *
 * Created on 7 de noviembre de 2006, 17:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.droid.remotectrl;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletResponse;
import org.itsnat.core.ClientDocument;
import org.itsnat.core.ItsNatDocument;
import org.itsnat.core.event.ItsNatAttachedClientCometEvent;
import org.itsnat.core.event.ItsNatAttachedClientEvent;
import org.itsnat.core.event.ItsNatAttachedClientEventListener;
import org.itsnat.core.event.ItsNatAttachedClientTimerEvent;

/**
 *
 * @author jmarranz
 */
public class TestDroidRemoteControlListener implements ItsNatAttachedClientEventListener
{
    protected boolean refreshMsg;

    /**
     * Creates a new instance of TestRemoteControlListener
     */
    public TestDroidRemoteControlListener(boolean refreshMsg)
    {
        this.refreshMsg = refreshMsg;
    }

    public void handleEvent(ItsNatAttachedClientEvent event)
    {
        //ItsNatDocument itsNatDoc = event.getItsNatDocument();

        int phase = event.getPhase();

        ClientDocument observer = event.getClientDocument();
        ItsNatDocument itsNatDoc = event.getItsNatDocument();
        ServletResponse response = event.getItsNatServletResponse().getServletResponse();

        switch(phase)
        {
            case ItsNatAttachedClientEvent.REQUEST:
                if (event instanceof ItsNatAttachedClientTimerEvent)
                {
                    ItsNatAttachedClientTimerEvent timerEvent = (ItsNatAttachedClientTimerEvent)event;
                    boolean accepted = (timerEvent.getRefreshInterval() >= 1000);                  
                    if (!accepted)
                    {                       
                        try
                        {
                            response.setContentType("android/layout;charset=UTF-8");                            
                            Writer out = response.getWriter();
                            
                            out.write("<TextView xmlns:android=\"http://schemas.android.com/apk/res/android\" ");
                            out.write("    android:layout_width=\"match_parent\" ");
                            out.write("    android:layout_height=\"wrap_content\" ");
                            out.write("    android:text=\"Remote control request rejected. Interval too short:" + Integer.toString(timerEvent.getRefreshInterval()) + "\" ");
                            out.write("    android:textSize=\"25dp\" "); 
                            out.write("    android:background=\"#00dd00\">");
                            out.write("</TextView>");                              
                        }
                        catch(IOException ex) { throw new RuntimeException(ex); }
                    }
                    event.setAccepted(accepted);
                }
                else if (event instanceof ItsNatAttachedClientCometEvent)
                {
                    event.setAccepted(true); // Nothing to check
                }
                else // "None" refresh mode
                {
                    event.setAccepted(true); // Nothing to check
                }

                if (event.getWaitDocTimeout() > 30000) // Demasiado tiempo de espera
                    event.setAccepted(false);
                
                if (!event.isAccepted())
                    return;

                break;

            case ItsNatAttachedClientEvent.LOAD:
                 //event.setAccepted(false);
                break;
            case ItsNatAttachedClientEvent.REFRESH:
                if (itsNatDoc.isInvalid())
                {
                    observer.addCodeToSend("alert(\"Observed document was destroyed\");");
                }
                else if (event instanceof ItsNatAttachedClientTimerEvent)
                {
                    // Esto no es lo normal (modificar a trav�s de un visor el documento)
                    // pero es para probar.
                    // Lo normal es a partir del estado del DOM hacer algo en consecuencia
                    // junto con la sessi�n del "observado" que tenemos acceso a ella via ItsNatDocument
                    ItsNatAttachedClientTimerEvent timerEvent = (ItsNatAttachedClientTimerEvent)event;

                    long initTime = timerEvent.getItsNatServletRequest().getClientDocument().getCreationTime();
                    long currentTime = System.currentTimeMillis();
                    int limitMilisec = 15*60*1000; // 15 minutos, para evitar que est� indefinidamente
                    if (currentTime - initTime > limitMilisec)
                    {
                        event.setAccepted(false);
                        observer.addCodeToSend("alert(\"Remote Control Timeout\");");
                    }
                    /*
                    else if (refreshMsg)
                    {
                        Document doc = itsNatDoc.getDocument();
                        if (doc instanceof HTMLDocument) // En el ejemplo SVG no funciona obviamente
                        {
                            String phaseStr;
                            if (phase == ItsNatAttachedClientEvent.LOAD)
                                phaseStr = "load";
                            else // if (phase == ItsNatAttachedClientEvent.REFRESH)
                                phaseStr = "refresh";
                            ((HTMLDocument)doc).getBody().appendChild(doc.createTextNode("OK remote ctrl " + phaseStr + " "));
                        }
                    }
                    */
                }
                break;
            case ItsNatAttachedClientEvent.UNLOAD:
                // Nada que hacer
                break;
        }

        if (!event.isAccepted())
            event.getItsNatEventListenerChain().stop();
    }

}
