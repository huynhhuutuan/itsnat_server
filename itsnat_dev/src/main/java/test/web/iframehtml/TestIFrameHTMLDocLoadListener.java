/*
 * TestMobileLoadListener.java
 *
 * Created on 5 de octubre de 2006, 11:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.web.iframehtml;

import org.itsnat.core.ItsNatServletRequest;
import org.itsnat.core.ItsNatServletResponse;
import org.itsnat.core.event.ItsNatServletRequestListener;
import org.itsnat.core.html.ItsNatHTMLDocument;

/**
 *
 * @author jmarranz
 */
public class TestIFrameHTMLDocLoadListener implements ItsNatServletRequestListener
{

    /**
     * Creates a new instance of TestMobileLoadListener
     */
    public TestIFrameHTMLDocLoadListener()
    {
    }

    public void processRequest(ItsNatServletRequest request, ItsNatServletResponse response)
    {
        ItsNatHTMLDocument itsNatDoc = (ItsNatHTMLDocument)request.getItsNatDocument();

        new TestIFrameHTMLDocument(itsNatDoc,request);
    }

}
