/*
 * TestAsyncServerResponse.java
 *
 * Created on 3 de enero de 2007, 12:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.web.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.itsnat.core.html.ItsNatHTMLDocument;
import org.itsnat.impl.core.template.ItsNatDocumentTemplateImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLDocument;
import test.web.shared.EventListenerSerial;

/**
 *
 * @author jmarranz
 */
public class TestEventListenerGC implements EventListener,Serializable
{
    protected ItsNatHTMLDocument itsNatDoc;
    protected Element link;
    protected transient WeakHashMap<EventListener,Object> weakMap = new WeakHashMap<EventListener,Object>();

    /**
     * Creates a new instance of TestAsyncServerResponse
     */
    public TestEventListenerGC(ItsNatHTMLDocument itsNatDoc)
    {
        this.itsNatDoc = itsNatDoc;

        load();
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        Map<EventListener,Object> mapTmp = new HashMap<EventListener,Object>(weakMap);
        out.writeObject(mapTmp);

        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        @SuppressWarnings("unchecked")
        Map<EventListener,Object> mapTmp = (Map<EventListener,Object>)in.readObject();
        this.weakMap = new WeakHashMap<EventListener,Object>(mapTmp);

        in.defaultReadObject();
    }

    public void load()
    {
        Document doc = itsNatDoc.getDocument();
        this.link = doc.getElementById("listenerGCTestId");
        ((EventTarget)link).addEventListener("click",this,false);
    }

    public void handleEvent(Event evt)
    {
        boolean forcedDisable = false;

        if (itsNatDoc.getItsNatDocumentTemplate().isAutoCleanEventListeners())
        {
            // Si AutoCleanEventListeners est� activado no podemos saber si funciona el sistema de referencias weak
            ItsNatDocumentTemplateImpl template = (ItsNatDocumentTemplateImpl)itsNatDoc.getItsNatDocumentTemplate();
            template.setAutoCleanEventListenersTESTING(false);
            forcedDisable = true;
        }

        // Este test lo que busca es ver que aunque se pulse el bot�n
        // muchas veces el n�mero de listeners registrados en el servidor
        // y en el en el cliente
        // no aumenta continuamente. Eso se traduce en que ni la memoria
        // Java ni la del navegador aumentan continuamente.

        HTMLDocument doc = itsNatDoc.getHTMLDocument();

        Element elem = doc.getElementById("listenerGCTestId2");
        Element child = doc.createElement("b");
        elem.appendChild(child);

        for(int i = 0; i < 1000; i++)
        {
            EventListener listener = new EventListenerSerial()
            {
                public void handleEvent(Event evt) { }
            };
            ((EventTarget)child).addEventListener("click",listener,false);
            weakMap.put(listener,null);
        }

        elem.removeChild(child);

        // Al eliminarse el nodo dicho nodo no est� sujeto ni por el registro
        // ni por el propio listener wrapper interno que se usa por tanto en alg�n
        // momento via GC el registro del listener se elimina al usar un weak map
        // Se evitan memory leaks en el servidor aunque NO se evitan en el cliente
        // porque como el nodo se pierde en el servidor es que no est� unido al documento
        // y por tanto no podemos encontrarlo en el cliente.
        // Para conseguir ambas cosas es necesario activar el AutoCleanEventListeners
        // que elimina el registro del listener autom�ticamente cuando se quita el nodo del documento.

        // En resumen: el n�mero de listeners en el servidor NO crecer� continuamente
        // cuando se pulse varias veces, en el cliente s�.

        System.gc();
        System.gc();
        System.gc();

        String code = "";
        code += "var i = 0; var list = itsNatDoc.domListeners; \n"; // domListeners no es p�blica
        code += "for(var id in list) i++; \n";
        code += "alert('Server:' + " + weakMap.size() + " + ' Client:' + i); \n";
        itsNatDoc.addCodeToSend(code);

        if (forcedDisable)
        {
            ItsNatDocumentTemplateImpl template = (ItsNatDocumentTemplateImpl)itsNatDoc.getItsNatDocumentTemplate();
            template.setAutoCleanEventListenersTESTING(true);
        }
    }
}
