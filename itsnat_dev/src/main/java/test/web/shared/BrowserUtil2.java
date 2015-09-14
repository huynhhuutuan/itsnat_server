/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.web.shared;

import javax.servlet.http.HttpServletRequest;
import org.itsnat.core.ItsNatServletRequest;

/**
 * Ponemos aqu� cosas que necesitamos pero que no se necesitan en el Feature Showcase
 *
 * @author jmarranz
 */
public class BrowserUtil2
{

    public static boolean isOperaOld(ItsNatServletRequest request)
    {
        HttpServletRequest httpReq = (HttpServletRequest) request.getServletRequest();
        String userAgent = httpReq.getHeader("User-Agent");
        return (userAgent.contains("Opera") && !isWebKit(userAgent));
    }

    public static boolean isWebKit(String userAgent)
    {
        // Podr�a usarse "Safari" pero alg�n navegador antiguo no la ten�a
        return (userAgent.contains("WebKit"));
    }    
    
    public static boolean isMSIE(ItsNatServletRequest request)
    {
        HttpServletRequest httpReq = (HttpServletRequest) request.getServletRequest();
        String userAgent = httpReq.getHeader("User-Agent");
        return (userAgent.contains("MSIE")) &&
                !isOperaOld(request);
    }

    public static boolean isMSIE6(ItsNatServletRequest request)
    {
        if (isMSIE(request))
        {
            HttpServletRequest httpReq = (HttpServletRequest) request.getServletRequest();
            String userAgent = httpReq.getHeader("User-Agent");
            return (userAgent.contains("MSIE 6.0;"));
        }
        return false;
    }

    public static boolean isBatik(ItsNatServletRequest request)
    {
        HttpServletRequest httpReq = (HttpServletRequest) request.getServletRequest();
        String userAgent = httpReq.getHeader("User-Agent");
        return (userAgent.contains("Batik"));
    }
}
