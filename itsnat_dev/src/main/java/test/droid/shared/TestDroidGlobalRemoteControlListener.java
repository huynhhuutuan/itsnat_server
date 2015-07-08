/*
 * TestRemoteControlListener.java
 *
 * Created on 7 de noviembre de 2006, 17:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.droid.shared;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletRequest;
import org.itsnat.core.ItsNatServletResponse;
import org.itsnat.core.event.ItsNatAttachedClientEvent;
import org.itsnat.core.event.ItsNatAttachedClientEventListener;

/**
 *
 * @author jmarranz
 */
public class TestDroidGlobalRemoteControlListener implements ItsNatAttachedClientEventListener
{
    /**
     * Creates a new instance of TestRemoteControlListener
     */
    public TestDroidGlobalRemoteControlListener()
    {
    }

    public void handleEvent(ItsNatAttachedClientEvent event)
    {
        // Este s�lo lo usamos para procesar casos de error
        if (event.getItsNatDocument() != null) return;

        ItsNatServletResponse response = event.getItsNatServletResponse();
        if (event.getPhase() == ItsNatAttachedClientEvent.REQUEST)
        {
            ServletRequest request = event.getItsNatServletRequest().getServletRequest();
            String sessionId = request.getParameter("itsnat_session_id");
            String docId = request.getParameter("itsnat_doc_id");
            try
            {
                response.getServletResponse().setContentType("android/layout;charset=UTF-8");                
                Writer out = response.getServletResponse().getWriter();
                
                out.write("  <TextView xmlns:android=\"http://schemas.android.com/apk/res/android\" ");
                out.write("      android:layout_width=\"match_parent\" ");
                out.write("      android:layout_height=\"wrap_content\" ");
                out.write("      android:text=\"Session/document not found with id: " + sessionId + "/" + docId + "\" ");
                out.write("      android:textSize=\"25dp\" "); 
                out.write("      android:background=\"#00dd00\">");
                out.write("  </TextView>");                 
                
            }
            catch(IOException ex) { throw new RuntimeException(ex); }
        }
        else // ItsNatAttachedClientEvent.REFRESH
        {
           response.addCodeToSend("alert(\"Session is expired or observed doc lost\");");
        }

        event.getItsNatEventListenerChain().stop();
    }
}
