/*
 * This file is not part of the ItsNat framework.
 *
 * Original source code use and closed source derivatives are authorized
 * to third parties with no restriction or fee.
 * The original source code is owned by the author.
 *
 * This program is distributed AS IS in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * (C) Innowhere Software a service of Jose Maria Arranz Santamaria, Spanish citizen.
 */

package org.itsnat.feashow.features.core.ioeaauto;

import java.io.Serializable;
import org.itsnat.core.ItsNatDocument;
import org.itsnat.core.ItsNatNode;
import org.itsnat.core.event.ItsNatUserEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public class ChildSVGSsrcAutoBindingDocument implements EventListener,Serializable
{
    protected ItsNatDocument itsNatDoc;
    protected Element[] circles = new Element[3];
    protected Element selectedCircle;

    public ChildSVGSsrcAutoBindingDocument(ItsNatDocument itsNatDoc)
    {
        this.itsNatDoc = itsNatDoc;
        load();
    }

    public void load()
    {
        itsNatDoc.setUserValue("user_object",this);

        Document doc = itsNatDoc.getDocument();
        for(int i = 0; i < circles.length; i++)
        {
            this.circles[i] = doc.getElementById("circle" + (i + 1));
            ((EventTarget)circles[i]).addEventListener("click",this,false);
        }

        itsNatDoc.addUserEventListener(null,"update",this);
    }

    public Element getSelectedCircle()
    {
        return selectedCircle;
    }

    public void handleEvent(Event evt)
    {
        if (evt instanceof ItsNatUserEvent)
        {
            // Radio changed notification
            // Nothing specific to do, the client will
            // be automatically updated as response of this event.
        }
        else // Clicked Circle
        {
            Element circle = (Element)evt.getCurrentTarget();
            selectCircle(circle);
        }
    }

    public void selectCircle(Element circle)
    {
        int selected = -1;
        for(int i = 0; i < circles.length; i++)
        {
            if (circle != circles[i])
                circles[i].setAttribute("fill","#0000ff");
            else
            {
                this.selectedCircle = circles[i];
                selected = i;
            }
        }

        circle.setAttribute("fill","#ff0000");

        // Synchronization not needed, parent document is ever synchronized

        Element containerElem = (Element)itsNatDoc.getContainerNode();
        if (containerElem == null)
        {
            itsNatDoc.addCodeToSend("alert('Disconnected from parent!');");
            return;
        }
        ItsNatDocument parentItsNatDoc = ((ItsNatNode)containerElem).getItsNatDocument();

        ObjEmbSVGSsrcAutoBindingTreeNode parentNode =
                  (ObjEmbSVGSsrcAutoBindingTreeNode)parentItsNatDoc.getUserValue("parent_user_object");
        Element circleSelectedNum = parentNode.getCircleSelected();
        Text numNode = (Text)circleSelectedNum.getFirstChild();
        numNode.setData(Integer.toString(selected + 1));

        Element inputRadio = parentNode.getInputRadio();
        inputRadio.setAttribute("value",circle.getAttribute("r"));

        // Notify the client parent document
        StringBuilder code = new StringBuilder();

        code.append("var url = window.location.href;");
        code.append("var pos = url.indexOf('#');"); // #p=n,c=n
        code.append("if (pos != -1)"); // MSIE with Ssrc
        code.append("{");
        code.append("  url = url.substring(0,pos + 3) + 'y' + url.substring(pos + 4);");
        code.append("  window.location.href = url;");
        code.append("}"); // Other browsers (native SVG):
        code.append("else if (window.parent) window.parent.document.getItsNatDoc().fireUserEvent(null,'update');");

        itsNatDoc.addCodeToSend(code.toString());
    }
}
