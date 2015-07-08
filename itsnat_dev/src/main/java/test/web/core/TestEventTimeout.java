/*
 * TestAsyncServerTask.java
 *
 * Created on 3 de enero de 2007, 12:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.web.core;

import java.io.Serializable;
import org.itsnat.core.CommMode;
import org.itsnat.core.html.ItsNatHTMLDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public class TestEventTimeout implements EventListener,Serializable
{
    protected static final long eventTimeout = 1000;
    protected ItsNatHTMLDocument itsNatDoc;
    protected Element link;

    /**
     * Creates a new instance of TestAsyncServerTask
     */
    public TestEventTimeout(ItsNatHTMLDocument itsNatDoc)
    {
        this.itsNatDoc = itsNatDoc;

        load();
    }

    public void load()
    {
        Document doc = itsNatDoc.getDocument();
        this.link = doc.getElementById("testEventTimeoutId");
        itsNatDoc.addEventListener((EventTarget)link,"click",this,false,CommMode.XHR_ASYNC,null,null,eventTimeout);  // El timeout s�lo funciona en los modos as�ncronos
    }

    public void handleEvent(final Event evt)
    {
        // El tiempo debe ser mayor que el tiempo del abort.
        // Hay que tener en cuenta de que el request se aborta y por tanto el JavaScript
        // generado para el cliente se pierde porque a nivel de servidor no sabemos que
        // dicho JavaScript no llegar� al cliente (el almac�n de JS pendiente se vac�a normalmente)
        // Esto crea problemas por ejemplo si un elemento ha sido cacheado, la informaci�n del cacheado
        // no llega al cliente.
        long wait = eventTimeout + 2000; // 2 segundos m�s, para que se interrumpa antes en el cliente
        try{ Thread.sleep(wait); } catch(InterruptedException ex) { throw new RuntimeException(ex); }

        itsNatDoc.addCodeToSend("alert('End Wait');"); // No se ver� nunca porque no llega al cliente y se resetea el buffer JavaScript
    }
}
