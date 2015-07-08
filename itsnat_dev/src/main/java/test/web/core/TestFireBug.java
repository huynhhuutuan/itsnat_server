/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.web.core;

import java.io.Serializable;
import org.itsnat.core.ClientDocument;
import org.itsnat.core.domutil.ItsNatTreeWalker;
import org.itsnat.core.event.ItsNatEvent;
import org.itsnat.core.html.ItsNatHTMLDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLStyleElement;
import test.web.shared.EventListenerSerial;

/**
 *
 * @author jmarranz
 */
public class TestFireBug implements EventListener,Serializable
{
    protected ItsNatHTMLDocument itsNatDoc;

    public TestFireBug(ItsNatHTMLDocument itsNatDoc)
    {
        this.itsNatDoc = itsNatDoc;

        // Este test deber�a de ejecutarse con FireBug activado
        // y el tab "HTML" activado tocando algo para que se ilumine la p�gina, esto provoca
        // la inserci�n de un <style> que contiene estilos de FireBug al final del <head> por FireBug.
        // Con DOM Inspector asegurar que se ha a�adido dicho <style> pues con FireBug
        // no se ve.
        // El caso del nodo <div> insertado al final del <body> al activar la Console de FireBug
        // (cuando la p�gina est� ya cargada pues de otra manera lo hace ,
        // no requiere mucho test porque el filtrado consiste en detectar un simple id y se comprueba
        // que es correcto con el ejemplo de los modal layers (pues a�aden nodos al final
        // del body y se acceden a los mismos).

        Document doc = itsNatDoc.getDocument();
        Element link = doc.getElementById("testFireBugId");
        ((EventTarget)link).addEventListener("click",this,false);
    }

    public void handleEvent(Event evt)
    {
        HTMLDocument doc = itsNatDoc.getHTMLDocument();

        final HTMLStyleElement style = (HTMLStyleElement)doc.createElement("style");
        style.setAttribute("id","firebug_test");

        Element head = ItsNatTreeWalker.getFirstChildElement(doc.getDocumentElement());
        head.appendChild(style);

        // La idea es que el nuevo elemento <style> estar� despu�s del <style> de FireBug
        // al calcular el path del nodo si no es filtrado el del FireBug obtendr�amos
        // una referencia al del FireBug detectando as� el error

        StringBuilder code = new StringBuilder();
        code.append("try{ \n");
        code.append("var elem = " + itsNatDoc.getScriptUtil().getNodeReference(style) + ";");
        code.append("if (elem.id != 'firebug_test') alert('ERROR TestFireBug');");
        code.append("else alert('OK TestFireBug');");
        code.append("}catch(ex){ alert('ERROR TestFireBug'); } \n");
        itsNatDoc.addCodeToSend(code.toString());

        EventListener listener = new EventListenerSerial()
        {
            public void handleEvent(Event evt)
            {
                // Limpiamos
                style.getParentNode().removeChild(style);
            }
        };

        ClientDocument clientDoc = ((ItsNatEvent)evt).getClientDocument();
        clientDoc.addContinueEventListener((EventTarget)doc, listener);
    }


}
