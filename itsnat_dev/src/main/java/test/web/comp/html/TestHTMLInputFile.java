/*
 * TestSelectComboBoxListener.java
 *
 * Created on 26 de octubre de 2006, 16:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.web.comp.html;

import org.itsnat.core.html.ItsNatHTMLDocument;
import org.itsnat.comp.text.ItsNatHTMLInputFile;
import javax.swing.text.PlainDocument;
import org.itsnat.comp.ItsNatComponentManager;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.html.HTMLInputElement;

/**
 *
 * @author jmarranz
 */
public class TestHTMLInputFile extends TestHTMLInputTextBased implements EventListener
{

    /**
     * Creates a new instance of TestSelectComboBoxListener
     */
    public TestHTMLInputFile(ItsNatHTMLDocument itsNatDoc)
    {
        super(itsNatDoc);

        initInputFile();
    }

    public void initInputFile()
    {
        org.w3c.dom.Document doc = itsNatDoc.getDocument();
        HTMLInputElement inputElem = (HTMLInputElement)doc.getElementById("fileInputId");
        ItsNatComponentManager componentMgr = itsNatDoc.getItsNatComponentManager();
        ItsNatHTMLInputFile input = (ItsNatHTMLInputFile)componentMgr.findItsNatComponent(inputElem);
        PlainDocument dataModel = new PlainDocument();
        input.setDocument(dataModel);

        input.setText("Initial Value");
        // Esto lo hacemos para ver si falla algun navegador (BlackBerry sobre todo) pero no deber�a hacerse
        // pues s�lo se admite poner el valor
        // a trav�s del GUI por el usuario, cosa de los navegadores por
        // seguridad para que sin que el usuario se de cuenta se subieran archivos
        // a trav�s de formularios ocultos por parte de webs malotas

        input.addEventListener("change",this);

        dataModel.addDocumentListener(this);
    }

    public void handleEvent(Event evt)
    {
        // No tratamos de modificar el value por lo que se dice m�s arriba
        boolean updateAgainToTest = false;
        handleEvent(evt,updateAgainToTest);
    }
}
