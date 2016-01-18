/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package test.droid.core;

import test.droid.shared.TestDroidBase;
import org.itsnat.core.ItsNatDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public class TestDroidStyleAttrAndViewInsertion extends TestDroidBase implements EventListener
{
    protected Element testStyleAttr;

    public TestDroidStyleAttrAndViewInsertion(ItsNatDocument itsNatDoc)
    {
        super(itsNatDoc);
        Document doc = itsNatDoc.getDocument();
        this.testStyleAttr = doc.getElementById("testStyleAttrId");
        ((EventTarget)testStyleAttr).addEventListener("click", this, false);
    }

    @Override
    public void handleEvent(Event evt)
    {
        ((EventTarget)testStyleAttr).removeEventListener("click", this, false); // Evitamos ejecutar varias veces para evitar que falle textView.setAttribute("android:id","@+id/testStyleAttrTextId"); pues al reutilizarse el id varios elementos tendr�n el mismo id (lo cual es correcto) y se devuelve el primero en el test y por tanto fallar�


        Document doc = itsNatDoc.getDocument();
        Element testStyleAttrHidden = doc.getElementById("testStyleAttrHiddenId");

        Element textView = doc.createElement("TextView");
        // Test definir atributos antes de insertar
        // Los definimos como setAttribute("android:localname","value") porque es m�s simple, consecuentemente debemos obtenerlo con getAttribute("android:localname")
        // En ItsNat Droid client le va a dar igual si se usa setAttribute o setAttributeNS pues si setAttribute tiene prefijo el prefijo se usa para resolver el namespace
        textView.setAttribute("android:text", "OK if text has left/right padding, background pink and width is match_parent");
        textView.setAttribute("style","@style/test_style2");

        testStyleAttrHidden.getParentNode().insertBefore(textView, testStyleAttrHidden);

        String nodeRef = itsNatDoc.getScriptUtil().getNodeReference(textView);
        itsNatDoc.addCodeToSend("var view = " + nodeRef + ";");
        
        // Test definir atributos despu�s de insertar
        textView.setAttribute("android:layout_width", "match_parent");
        textView.setAttribute("android:layout_height","wrap_content");
        textView.setAttribute("android:background", "#000000");
        itsNatDoc.addCodeToSend("if (view.getBackground() == null) alert(\"FAIL setAttribute\");");       
        textView.removeAttribute("android:background");

        itsNatDoc.addCodeToSend("if (view.getBackground() != null) alert(\"FAIL removeAttribute\");");
        textView.setAttribute("android:background", "#ffdddd");  // Rosa
        
        // Test uso del atributo DOM id
        textView.setAttribute("id", "BAD_ID");
        textView.removeAttribute("id");
        itsNatDoc.addCodeToSend("if (itsNatDoc.findViewByXMLId(\"BAD_ID\") != null) alert(\"FAIL removeAttribute XML Id\");");

        textView.setAttribute("id", "testStyleAttrTextId");
        itsNatDoc.addCodeToSend("if (view != itsNatDoc.findViewByXMLId(\"testStyleAttrTextId\")) alert(\"FAIL setAttribute XML Id\");");

        textView.setAttribute("android:id","@+id/testStyleAttrTextId");
        itsNatDoc.addCodeToSend("if (view != view.getParent().findViewById(view.getId())) alert(\"FAIL setAttribute XML Id native \");");
    }

}
