/*
 * TestSVGInXHTMLLoadListener.java
 *
 * Created on 5 de octubre de 2006, 11:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.web.svgxhtml;


import org.itsnat.core.ItsNatServletRequest;
import org.itsnat.core.ItsNatServletResponse;
import org.itsnat.core.event.ItsNatServletRequestListener;

/**
 *
 * @author jmarranz
 */
public class TestSVGInXHTMLAdobeSVGDocLoadListener implements ItsNatServletRequestListener
{

    /**
     * Creates a new instance of TestSVGInXHTMLLoadListener
     */
    public TestSVGInXHTMLAdobeSVGDocLoadListener()
    {
    }

    public void processRequest(ItsNatServletRequest request, ItsNatServletResponse response)
    {
        new TestSVGInXHTMLAdobeSVGDocument(request,response);
    }

}
