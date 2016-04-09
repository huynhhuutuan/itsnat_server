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
public class TestDroidStyleAttrAndViewInsertion2 extends TestDroidBase implements EventListener
{
    protected Element testStyleAttr;
    
    
    public TestDroidStyleAttrAndViewInsertion2(ItsNatDocument itsNatDoc)
    {
        super(itsNatDoc);
        Document doc = itsNatDoc.getDocument();
        this.testStyleAttr = doc.getElementById("testStyleAttrId2");
        ((EventTarget)testStyleAttr).addEventListener("click", this, false);
    }
    
    @Override
    public void handleEvent(Event evt)
    {
        ((EventTarget)testStyleAttr).removeEventListener("click", this, false); // Evitamos ejecutar varias veces para evitar que falle textView.setAttribute("android:id","@+id/testStyleAttrTextId"); pues al reutilizarse el id varios elementos tendr�n el mismo id (lo cual es correcto) y se devuelve el primero en el test y por tanto fallar�
        
        
        Document doc = itsNatDoc.getDocument();        
        Element testStyleAttrHidden = doc.getElementById("testStyleAttrHiddenId2");  
        
        Element textView = doc.createElement("TextView");        
        // Test definir atributos antes de insertar
        textView.setAttribute("id", "testTextViewStyleAttrHiddenId2");        
        textView.setAttribute("android:id", "@remote:+id/droid/res/values/test_values_remote.xml:test_id");        
        textView.setAttribute("android:text", "HOLA OK if left/right padding, background=pink, width=match_parent, textSize=10.3dp, textColor=red");         
        textView.setAttribute("style","@remote:style/droid/res/values/test_values_remote.xml:test_style_textview_remote");
        
        testStyleAttrHidden.getParentNode().insertBefore(textView, testStyleAttrHidden);
        
        itsNatDoc.addCodeToSend("var id = itsNatDoc.getResourceIdentifier(\"testTextViewStyleAttrHiddenId2\");");
        itsNatDoc.addCodeToSend("var view = itsNatDoc.findViewByXMLId(\"testTextViewStyleAttrHiddenId2\");");        
        itsNatDoc.addCodeToSend("if (id != view.getId()) alert(\"FAIL TEST\");");
        
        // Test definir atributos despu�s de insertar            
        textView.setAttribute("android:layout_width", "match_parent");        
        textView.setAttribute("android:layout_height", "wrap_content");        
        textView.setAttribute("android:background", "#ffdddd");         
        textView.setAttribute("android:textSize", "@remote:dimen/droid/res/values/test_values_remote.xml:test_dimen_textSize");  
        textView.setAttribute("android:textColor", "@remote:color/droid/res/values/test_values_remote.xml:test_color_textColor");                
       
        Element textViewPrevious = textView;
        
        textView = doc.createElement("TextView");        
        // Test definir atributos antes de insertar
        textView.setAttribute("android:text", "OK if left/right padding, background=pink, width=match_parent, textSize=default, textColor=red");         
        textView.setAttribute("style","@remote:style/droid/res/values/test_values_remote.xml:test_style_textview_remote");
        
        testStyleAttrHidden.getParentNode().insertBefore(textView, textViewPrevious);
        
        // Test definir atributos despu�s de insertar            
        textView.setAttribute("android:layout_width", "match_parent");        
        textView.setAttribute("android:layout_height", "wrap_content");        
        textView.setAttribute("android:background", "#ffdddd");         
        // textView.setAttribute("android:textSize", "@remote:dimen/droid/res/values/test_values_remote.xml:test_dimen_textSize");  
        textView.setAttribute("android:textColor", "@remote:color/droid/res/values/test_values_remote.xml:test_color_textColor");         
    }
    
}
