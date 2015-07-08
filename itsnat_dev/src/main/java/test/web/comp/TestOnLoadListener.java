/*
 * OnUnloadListener.java
 *
 * Created on 7 de noviembre de 2006, 18:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.web.comp;

import org.itsnat.core.ItsNatDocument;
import org.itsnat.core.event.ItsNatDOMStdEvent;
import org.itsnat.core.html.ItsNatHTMLDocument;
import org.itsnat.core.ItsNatSession;
import java.lang.ref.WeakReference;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import test.web.shared.TestBaseHTMLDocument;

/**
 *
 * @author jmarranz
 */
public class TestOnLoadListener extends TestBaseHTMLDocument implements EventListener
{
    /** Creates a new instance of OnUnloadListener */
    public TestOnLoadListener(ItsNatHTMLDocument itsNatDoc)
    {
        super(itsNatDoc);
    }

    public static boolean isAlreadyLoaded(ItsNatHTMLDocument itsNatDoc)
    {
        /* Sirve para probar si podemos evitar que se cargue dos veces
         la misma p�gina para la misma sesi�n y as� testear si se puede evitar el problema
         de la "conversaci�n". S�lo lo podemos hacer en el load de la posible
         copia porque en el Firefox al pulsar el bot�n reload se llama
         al servidor antes de ejecutar el unload de la p�gina actual
         cargada por lo que s�lo sabremos si es un reload o nueva carga cuando en el evento load
         del nuevo documento se comprueba que en la otra p�gina se llam� o no al unload
         pues en reload inmediatamente despu�s de llamar al servidor para recargar
         se ejecuta el unload de la actual (si es nueva carga no estar� invalidada la original)
         En reload con el MSIE se ejecuta el unload antes de llamar al servidor.
        */

        // Inhibimos, hay navegadores que no generan el evento "unload" siempre
        if (true) return false;


        ItsNatSession session = itsNatDoc.getClientDocumentOwner().getItsNatSession();
        @SuppressWarnings("unchecked")
        WeakReference<ItsNatDocument> docRef = (WeakReference<ItsNatDocument>)session.getUserValue(itsNatDoc.getItsNatDocumentTemplate().getName());
        if (docRef == null)
            return false;
        ItsNatDocument itsNatDocReg = docRef.get();
        if (itsNatDocReg == null)
            return false;
        if (itsNatDocReg.isInvalid()) // Ha sido descargado aunque el garbage collector no se lo ha llevado todav�a
            return false;
        return true;
    }

    public static void registerToAvoidConcurrentLoad(ItsNatHTMLDocument itsNatDoc)
    {
        ItsNatSession session = itsNatDoc.getClientDocumentOwner().getItsNatSession();
        session.setUserValue(itsNatDoc.getItsNatDocumentTemplate().getName(),new WeakReference<ItsNatHTMLDocument>(itsNatDoc));
    }

    public void handleEvent(Event evt)
    {
        // Inhibimos, hay navegadores que no generan el evento "unload" siempre
        if (true) return;

        ItsNatDOMStdEvent itsNatEvt = (ItsNatDOMStdEvent)evt;
        ItsNatHTMLDocument itsNatDoc = (ItsNatHTMLDocument)itsNatEvt.getItsNatDocument();

        if (isAlreadyLoaded(itsNatDoc))
            throw new RuntimeException("Document is already loaded by this session");
        registerToAvoidConcurrentLoad(itsNatDoc);
    }

}
