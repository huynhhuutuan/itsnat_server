/*
  ItsNat Java Web Application Framework
  Copyright (C) 2007-2014 Jose Maria Arranz Santamaria, Spanish citizen

  This software is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 3 of
  the License, or (at your option) any later version.
  This software is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details. You should have received
  a copy of the GNU Lesser General Public License along with this program.
  If not, see <http://www.gnu.org/licenses/>.
*/

package org.itsnat.impl.core.resp.shared;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.itsnat.core.ItsNatVariableResolver;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedClientImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.doc.droid.ItsNatStfulDroidDocumentImpl;
import org.itsnat.impl.core.resp.ResponseLoadStfulDocumentValid;
import org.itsnat.impl.core.servlet.ItsNatSessionImpl;
import org.itsnat.impl.core.template.droid.ScriptCode;
import org.itsnat.impl.core.template.droid.ScriptWithSrc;

/**
 *
 * @author jmarranz
 */
public class ResponseDelegateStfulDroidLoadDocImpl extends ResponseDelegateStfulLoadDocImpl
{
    public ResponseDelegateStfulDroidLoadDocImpl(ResponseLoadStfulDocumentValid response)
    {
        super(response);      
    }

    public ItsNatStfulDroidDocumentImpl getItsNatStfulDroidDocument()
    {
        return (ItsNatStfulDroidDocumentImpl)getItsNatStfulDocument();
    }
    
    protected void rewriteClientUIControlProperties()
    {
        // En Android no hay autofill etc
    }

    @Override
    protected String addMarkupToTheEndOfDoc(String docMarkup, String newMarkup)
    {
        if (newMarkup == null || "".equals(newMarkup)) return docMarkup; 
        
        StringBuilder finalMarkup = new StringBuilder();

        int posRootTagEnd = docMarkup.lastIndexOf('<');
        String preScript = docMarkup.substring(0,posRootTagEnd);
        String posScript = docMarkup.substring(posRootTagEnd);

        finalMarkup.append(preScript);
        finalMarkup.append(newMarkup);
        finalMarkup.append(posScript);

        return finalMarkup.toString();
    }
    
    protected String getInitDocumentScriptCode(final int prevScriptsToRemove)    
    {
        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();        
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();        
        ItsNatSessionImpl session = clientDoc.getItsNatSessionImpl();
        String stdSessionId = session.getStandardSessionId();  
        String token = session.getToken(); 
        String sessionId = session.getId();        
        String clientId =  clientDoc.getId();
        String servletPath = delegByBrowser.getServletPathForEvents();
        int errorMode = itsNatDoc.getClientErrorMode(); 
        boolean eventsEnabled = itsNatDoc.isEventsEnabled();
        
        String attachType = null;
        if (clientDoc instanceof ClientDocumentAttachedClientImpl)
        {
            attachType = ((ClientDocumentAttachedClientImpl)clientDoc).getAttachType();
        }        
        
        return "itsNatDoc.init(\"" + stdSessionId + "\",\"" + token + "\",\"" + sessionId + "\",\"" + clientId + "\",\"" + servletPath + "\"," + errorMode + ",\"" + attachType + "\"," + eventsEnabled + ");\n"; // HACER
    }
    
    private ItsNatVariableResolver getItsNatVariableResolverForScriptSrc()
    {
        // Esto es por intentar dar algo de customizaci�n a los scripts teniendo en cuenta que no son accesibles y sobre todo para hacer que un test funcione
        
        HttpServletRequest request = (HttpServletRequest)getResponseLoadDoc().getRequestLoadDoc().getItsNatServletRequest().getServletRequest();            
        String scheme = request.getScheme();
        String authType = request.getAuthType();
        String host = request.getServerName();
        int port = request.getServerPort();
        String pathInfo = request.getPathInfo();
        String pathTranslated = request.getPathTranslated();
        String contextPath = request.getContextPath();
        String queryString = request.getQueryString();

        ItsNatStfulDroidDocumentImpl itsNatDoc = getItsNatStfulDroidDocument();                
        ItsNatVariableResolver resolver = itsNatDoc.createItsNatVariableResolver(false);
        resolver.setLocalVariable("scheme", scheme);
        resolver.setLocalVariable("authType", authType);            
        resolver.setLocalVariable("host", host);
        resolver.setLocalVariable("port", port);
        resolver.setLocalVariable("pathInfo", pathInfo);
        resolver.setLocalVariable("pathTranslated", pathTranslated);
        resolver.setLocalVariable("contextPath", contextPath);
        resolver.setLocalVariable("queryString", queryString); 
        return resolver;
    }
    
    @Override     
    protected String addRequiredMarkupToTheEndOfDoc(String docMarkup)       
    {
        docMarkup = super.addRequiredMarkupToTheEndOfDoc(docMarkup);
                
        ItsNatStfulDroidDocumentImpl itsNatDoc = getItsNatStfulDroidDocument();           
        List<ScriptCode> scriptCodeList = itsNatDoc.getScriptCodeList();
        
        String markupStr = null;
        if (!scriptCodeList.isEmpty())
        {           
            // Hay que tener en cuenta que los quitamos del DOM en el template pero como tenemos los scripts los enviamos "recreando" los script                     
            
            ItsNatVariableResolver resolver = null;
            StringBuilder markup = new StringBuilder();             
            for(ScriptCode script : scriptCodeList)
            {
                if (ItsNatStfulDroidDocumentImpl.PRELOAD_SCRIPTS)
                {
                    markup.append( "<script><![CDATA[ " + script.getCode() + " ]]></script>" );
                }
                else
                {
                    if (script instanceof ScriptWithSrc)
                    {
                        String src = ((ScriptWithSrc)script).getSrc();
                        if (resolver == null) resolver = getItsNatVariableResolverForScriptSrc();
                        String src2 = resolver.resolve(src);
                        if (src2 != null) src = src2;
                        markup.append( "<script src=\"" + src + "\"></script>" );
                    }
                    else
                        markup.append( "<script><![CDATA[ " + script.getCode() + " ]]></script>" );
                }                    
            }
            markupStr = markup.toString();
        }      

        return addMarkupToTheEndOfDoc(docMarkup,markupStr);
    }    
    
    @Override
    protected String generateFinalScriptsMarkup()
    {
        StringBuilder markup = new StringBuilder();        

        // NO CAMBIAR NI UN ESPACIO, este patr�n se usa en el c�digo cliente Android para localizar el script de carga
        markup.append( "<script id=\"itsnat_load_script\"><![CDATA[ " + getInitScriptContentCode(1) + " ]]></script>" );
        
        return markup.toString();
    }
}
