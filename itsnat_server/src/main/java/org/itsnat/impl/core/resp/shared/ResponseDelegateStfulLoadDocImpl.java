/*
  ItsNat Java Web Application Framework
  Copyright (C) 2007-2011 Jose Maria Arranz Santamaria, Spanish citizen

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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.itsnat.impl.core.resp.shared.bybrow.web.ResponseDelegStfulLoadDocByBrowserImpl;
import org.itsnat.impl.core.resp.shared.otherns.ResponseDelegateOtherNSLoadDocImpl;
import org.itsnat.impl.core.resp.shared.html.ResponseDelegateHTMLLoadDocImpl;
import org.itsnat.impl.core.resp.*;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.droid.BrowserDroid;
import org.itsnat.impl.core.browser.web.BrowserWeb;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.servlet.ItsNatServletRequestImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.doc.web.ItsNatHTMLDocumentImpl;
import org.itsnat.impl.core.doc.web.ItsNatOtherNSDocumentImpl;
import org.itsnat.impl.core.doc.droid.ItsNatStfulDroidDocumentImpl;
import org.itsnat.impl.core.mut.doc.DocMutationEventListenerImpl;
import org.itsnat.impl.core.util.IOUtil;
import org.itsnat.impl.res.core.js.LoadScriptImpl;

/**
 *
 * @author jmarranz
 */
public abstract class ResponseDelegateStfulLoadDocImpl extends ResponseDelegateStfulImpl
{
    protected StringBuilder fixDOMCode; // C�digo JavaScript que se ejecutar� al comienzo de todo con la finalidad de arreglar el �rbol DOM cliente antes de que se accedan a los nodos. NO deber�a accederse a los nodos usando paths ItsNat o accediendo a la cache
    protected ResponseDelegStfulLoadDocByBrowserImpl delegByBrowser;

    
    /**
     * Creates a new instance of ResponseDelegateStfulLoadDocImpl
     */
    public ResponseDelegateStfulLoadDocImpl(ResponseLoadStfulDocumentValid response)
    {
        super(response);

        this.delegByBrowser = ResponseDelegStfulLoadDocByBrowserImpl.createResponseDelegStfulLoadDocByBrowser(this);
    }

    public static ResponseDelegateStfulLoadDocImpl createResponseDelegateStfulLoadDoc(ResponseLoadStfulDocumentValid responseParent)
    {
        Browser browser = responseParent.getClientDocument().getBrowser();
        
        ItsNatStfulDocumentImpl itsNatDoc = responseParent.getItsNatStfulDocument();
        if (itsNatDoc instanceof ItsNatStfulDroidDocumentImpl)
        {
            if (!(browser instanceof BrowserDroid))
                throw new ItsNatException("An Android layout only can be loaded by the ItsNat Droid Browser");
            
            return new ResponseDelegateStfulDroidLoadDocImpl(responseParent);
        }
        else
        {
            if (!(browser instanceof BrowserWeb))
                throw new ItsNatException("A web layout cannot be loaded by the ItsNat Droid Browser");             
            
            if (itsNatDoc instanceof ItsNatHTMLDocumentImpl)
                return ResponseDelegateHTMLLoadDocImpl.createResponseDelegateHTMLLoadDoc(responseParent);
            else if (itsNatDoc instanceof ItsNatOtherNSDocumentImpl)
                return ResponseDelegateOtherNSLoadDocImpl.createResponseDelegateOtherNSLoadDoc(responseParent);
        }
        
        return null; // No se llega nunca, futuros tipos
    }

    public ResponseLoadStfulDocumentValid getResponseLoadStfulDocumentValid()
    {
        return (ResponseLoadStfulDocumentValid)responseParent;
    }

    public ResponseLoadDocImpl getResponseLoadDoc()
    {
        return (ResponseLoadDocImpl)responseParent;
    }

    public void addFixDOMCodeToSend(String code)
    {
        getFixDOMCodeToSend().append(code);
    }

    public boolean hasFixDOMCodeToSend()
    {
        return (fixDOMCode != null);
    }

    public StringBuilder getFixDOMCodeToSend()
    {
        if (fixDOMCode == null) this.fixDOMCode = new StringBuilder();
        return fixDOMCode;
    }



    public ItsNatStfulDocumentImpl getItsNatStfulDocument()
    {
        return responseParent.getItsNatStfulDocument();
    }

    public ClientDocumentStfulImpl getClientDocumentStful()
    {
        return getResponseLoadStfulDocumentValid().getClientDocumentStful();
    }

    public ClientDocumentStfulDelegateImpl getClientDocumentStfulDelegate()
    {
        return getClientDocumentStful().getClientDocumentStfulDelegate();
    }
    
    public boolean isSerializeBeforeDispatching()
    {
        return getResponseLoadStfulDocumentValid().isSerializeBeforeDispatching();
    }
    
    protected abstract void rewriteClientUIControlProperties();    
    
    public String getServletPath()
    {
        ResponseLoadDocImpl parent = getResponseLoadDoc();
        boolean useAbsoluteURL = false;
        if (getClientDocumentStful().getBrowser().isNeededAbsoluteURL())
            useAbsoluteURL = true;  // El browser lo pide
        else if (getResponseLoadStfulDocumentValid().isNeededAbsoluteURL())
            useAbsoluteURL = true;  // El tipo de proceso lo pide

        ItsNatServletRequestImpl itsNatRequest = parent.getRequestLoadDoc().getItsNatServletRequest();
        return itsNatRequest.getServletPath(useAbsoluteURL, true);
    }        
    
    public void processResponse()
    {
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        DocMutationEventListenerImpl mutListener = itsNatDoc.getDocMutationEventListener();

        String docMarkup = null;

        boolean serializeBefore = isSerializeBeforeDispatching();
        if (serializeBefore) // Caso fastLoad=false y remote control
        {
            mutListener.setEnabled(false); // Desactivamos totalmente pues hay muchos casos en los que antes de serializar necesitamos hacer cambios temporales en el DOM que no queremos que generen c�digo JavaScript
            preSerializeDocument();
            docMarkup = serializeDocument();
            mutListener.setEnabled(true); // Restauramos

            rewriteClientUIControlProperties();
        }

        dispatchRequestListeners();

        if (!serializeBefore) // Caso fast load
        {
            // En caso Fast Load DEBE ejecutarse despu�s de los listeners del
            // usuario porque el estado del DOM servidor en carga es el resultado
            // de ejecutar dichos listeners en el servidor que deben tambi�n
            // ejecutarse en JavaScript.
            rewriteClientUIControlProperties();

            mutListener.setEnabled(false); // Desactivamos totalmente pues hay muchos casos en los que antes de serializar necesitamos hacer cambios temporales en el DOM que no queremos que generen c�digo JavaScript
            preSerializeDocument();
            docMarkup = serializeDocument();
            mutListener.setEnabled(true); // Restauramos
        }

        docMarkup = addRequiredMarkupToTheEndOfDoc(docMarkup);

        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        if (clientDoc.isScriptingEnabled())
        {
            String scriptsMarkup = generateFinalScriptsMarkup();
            if (getResponseLoadStfulDocumentValid().isOnlyReturnMarkupOfFinalScripts())
                docMarkup = scriptsMarkup;
            else
                docMarkup = addMarkupToTheEndOfDoc(docMarkup,scriptsMarkup);
        }         
        
        getResponseLoadDoc().sendMarkupToClient(docMarkup);
    }

    protected String addRequiredMarkupToTheEndOfDoc(String docMarkup)
    {
        return docMarkup; // Se redefine en un caso
    }
    
    public void dispatchRequestListeners()
    {
        getResponseLoadDoc().dispatchRequestListeners();
    }



    protected String serializeDocument()
    {
        return getResponseLoadDoc().serializeDocument();
    }

    protected void preSerializeDocument()
    {
        getResponseLoadStfulDocumentValid().preSerializeDocumentStful();
    }

    protected String getLoadCodeUsingDocument()
    {
        // Llamar despu�s de serializar el markup as� en el proceso de serializar el documento hay una �ltima oportunidad de generar c�digo JavaScript
        return getResponseLoadDoc().getCodeToSendAndReset();
    }

    protected abstract String generateFinalScriptsMarkup();


    protected String getInitScriptContentCode(final int prevScriptsToRemove)
    {
        StringBuilder code = new StringBuilder();

        // Llamamos antes de llamar a getInitDocumentAndLoadJSCode pues es la �ltima oportunidad de enviar c�digo "FixDOM" antes de iniciar el documento ItsNat
        code.append( getPreInitDocumentScriptCode() );

        code.append( getInitDocumentAndLoadScriptCode(prevScriptsToRemove) );

        return code.toString();
    }

    protected String getPreInitDocumentScriptCode()
    {
        StringBuilder code = new StringBuilder();

        if (hasFixDOMCodeToSend())
            code.append( getFixDOMCodeToSend() );

        String byBrowserCode = delegByBrowser.getOnInitScriptContentCodeFixDOMCode();
        if (byBrowserCode != null)
            code.append( byBrowserCode );

        return code.toString();
    }

    protected String getInitDocumentAndLoadScriptCode(final int prevScriptsToRemove)
    {
        StringBuilder code = new StringBuilder();

        code.append( getInitDocumentScriptCode(prevScriptsToRemove) );

        code.append( getLoadCodeUsingDocument() );

        return code.toString();
    }

    protected abstract String getInitDocumentScriptCode(final int prevScriptsToRemove);


    public static String loadScriptList(String scriptNameList)
    {
        StringBuilder code = new StringBuilder();
        String[] scriptNameArray = scriptNameList.split(",");
        for(int i = 0; i < scriptNameArray.length; i++)
        {
            String scriptName = scriptNameArray[i];
            loadScript(scriptName,code);
        }
        return code.toString();
    }

    public static void loadScript(String scriptName,StringBuilder code)
    {
        LoadScriptImpl.checkFileName(scriptName);
        InputStream input = LoadScriptImpl.class.getResourceAsStream(scriptName);        
        IOUtil.readTextStream(input,"UTF-8",code);
    }

    protected abstract String addMarkupToTheEndOfDoc(String docMarkup,String codeToAdd);
}
