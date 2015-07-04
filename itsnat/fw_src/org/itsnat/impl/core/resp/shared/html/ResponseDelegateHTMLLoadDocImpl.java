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

package org.itsnat.impl.core.resp.shared.html;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.web.BrowserMSIEOld;
import org.itsnat.impl.core.browser.web.BrowserW3C;
import org.itsnat.impl.core.browser.web.BrowserWeb;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.web.SVGWebInfoImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.doc.web.ItsNatHTMLDocumentImpl;
import org.itsnat.impl.core.domimpl.html.HTMLTextAreaElementImpl;
import org.itsnat.impl.core.domutil.DOMUtilHTML;
import org.itsnat.impl.core.domutil.DOMUtilInternal;
import org.itsnat.impl.core.domutil.NamespaceUtil;
import org.itsnat.impl.core.scriptren.jsren.JSRenderImpl;
import org.itsnat.impl.core.scriptren.jsren.node.JSRenderPropertyImpl;
import org.itsnat.impl.core.scriptren.jsren.node.PropertyImpl;
import org.itsnat.impl.core.resp.ResponseLoadStfulDocumentValid;
import org.itsnat.impl.core.resp.shared.*;
import org.itsnat.impl.core.template.web.html.HTMLTemplateVersionDelegateImpl;
import org.itsnat.impl.res.core.js.LoadScriptImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLBodyElement;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLHeadElement;
import org.w3c.dom.html.HTMLMetaElement;
import org.w3c.dom.html.HTMLTextAreaElement;

/**
 *
 * @author jmarranz
 */
public abstract class ResponseDelegateHTMLLoadDocImpl extends ResponseDelegateStfulWebLoadDocImpl
{

    /**
     * Creates a new instance of ResponseDelegateHTMLLoadDocImpl
     */
    public ResponseDelegateHTMLLoadDocImpl(ResponseLoadStfulDocumentValid responseParent)
    {
        super(responseParent);
    }

    public static ResponseDelegateHTMLLoadDocImpl createResponseDelegateHTMLLoadDoc(ResponseLoadStfulDocumentValid responseParent)
    {
        Browser browser = responseParent.getClientDocument().getBrowser();
        if (browser instanceof BrowserMSIEOld)
            return ResponseDelegateHTMLLoadDocMSIEOldImpl.createResponseDelegateHTMLLoadDocMSIEOld((BrowserMSIEOld)browser,responseParent);
        else
            return ResponseDelegateHTMLLoadDocW3CImpl.createResponseDelegateHTMLLoadDocW3C((BrowserW3C)browser,responseParent);
    }

    public ClientDocumentStfulDelegateWebImpl getClientDocumentStfulDelegateWeb()    
    {
        return (ClientDocumentStfulDelegateWebImpl)getClientDocumentStfulDelegate();
    }
    
    public ItsNatHTMLDocumentImpl getItsNatHTMLDocument()
    {
        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        return (ItsNatHTMLDocumentImpl)clientDoc.getItsNatDocument();
    }

    public String getJavaScriptMIME()
    {
        return NamespaceUtil.MIME_JAVASCRIPT;
    }

    public void setScriptURLAttribute(Element scriptElem,String url)
    {
        DOMUtilInternal.setAttribute(scriptElem,"src",url);
    }

    public void setScriptContent(Element scriptElem,String code)
    {
        ItsNatHTMLDocumentImpl itsNatDoc = getItsNatHTMLDocument();
        Document doc = scriptElem.getOwnerDocument();
        if (itsNatDoc.isMIME_HTML())
            scriptElem.appendChild(doc.createTextNode(code));  // No usamos CDATA porque hay problemas en MSIE y te�ricamente no est� soportado en HTML
        else // XHTML MIME
            scriptElem.appendChild(doc.createCDATASection(code));
    }

    public String addMarkupToTheEndOfDoc(String docMarkup,String scriptsMarkup)
    {
        StringBuilder finalMarkup = new StringBuilder();

        int posHTMLEnd = docMarkup.lastIndexOf('<');
        int posBODYEnd = docMarkup.lastIndexOf('<',posHTMLEnd - 1);
        String preScript = docMarkup.substring(0,posBODYEnd);
        String posScript = docMarkup.substring(posBODYEnd);

        finalMarkup.append(preScript);
        finalMarkup.append(scriptsMarkup);
        finalMarkup.append(posScript);

        return finalMarkup.toString();
    }

    public String getDocumentNamespace()
    {
        return NamespaceUtil.XHTML_NAMESPACE;
    }

    protected void rewriteClientUIControlProperties(Element elem,boolean revertJSChanges,StringBuilder code)
    {
        // Obviamente los documentos XHTML contienen elementos XHTML :)
        rewriteClientHTMLUIControlProperties(elem,revertJSChanges,code);
    }

    @Override
    protected String serializeDocument()
    {
        // En este punto SVGWeb debe de estar ya detectado o no
        boolean useSVGWeb = SVGWebInfoImpl.isSVGWebEnabled(getClientDocumentStfulDelegateWeb());

        // Primero los que no son procesados por SVGWeb (aunque da igual)
        LinkedList<Attr> otherNSElemsAttribs = null;
        LinkedList<Element> otherNSRootElemsInHTML = fixOtherNSElementsInHTMLFindRootElems();
        if (otherNSRootElemsInHTML != null)
            otherNSElemsAttribs = fixOtherNSElementsInHTMLSaveValidNames(otherNSRootElemsInHTML);

        Map<Element,Element> svgRootElems = null;
        if (useSVGWeb)
        {
            HTMLDocument doc = getItsNatHTMLDocument().getHTMLDocument();
            svgRootElems = processTreeSVGWebElements(doc);
        }

        String docMarkup = super.serializeDocument();

        if (useSVGWeb)
        {
            if ((svgRootElems != null) && !svgRootElems.isEmpty())
                restoreSVGWebElements(svgRootElems);
        }

        if (otherNSRootElemsInHTML  != null)
        {
            fixOtherNSElementsInHTMLCleanAuxAttribs(otherNSElemsAttribs);
            fixOtherNSElementsInHTMLGenCode(otherNSRootElemsInHTML);
        }

        return docMarkup;
    }

    public void detectSVGWeb()
    {
        // Usamos m�s o menos el mismo tipo de chequeos que hace SVGWeb en el cliente
        // S�lo soportaremos SVGWeb si su presencia en la p�gina es clara,
        // debe de existir un <meta name="svg.render.forceflash"> o estar "svg.render.forceflash" en el URL
        // pues a trav�s del <script> que incluye el archivo "svg.js" no est� claro que
        // dicho archivo sea el SVGWeb
        // Da igual si estamos en fastLoad o no pues la inserci�n din�mica del <meta>
        // y del <script> del SVGWeb via JavaScript en carga no creo que funcionen bien,
        // por tanto suponemos que est�n ah� como markup desde el principio.
        // ES FUNDAMENTAL que el <meta> NO est� cacheado (usar itsnat:nocache="true" por ejemplo)
        // de otra manera el <meta> NO se refleja en el DOM.

        ItsNatHTMLDocumentImpl itsNatDoc = getItsNatHTMLDocument();
        HTMLDocument doc = itsNatDoc.getHTMLDocument();

        boolean svgWeb = false;
        boolean forceFlash = false; // El valor se impondr� (lo exigimos de otra manera no reconocemos SVGWeb)
        int metaDecPos = -1;

        // El valor de svg.render.forceflash en el URL manda sobre el valor del <meta>
        String queryString = getResponseLoadDoc().getRequest().getItsNatServletRequest().getQueryStringInternal();
        if (queryString == null) return; // No hay query string, caso por ejemplo de Pretty URLs
        if (queryString.indexOf("svg.render.forceflash=true") != -1)
        {
            svgWeb = true;
            forceFlash = true;
        }
        else if (queryString.indexOf("svg.render.forceflash=false") != -1)
        {
            svgWeb = true;
            forceFlash = false;
        }

        if (!svgWeb)
        {
            // No est� svg.render.forceflash en el URL vamos a ver si est� en el <meta>
            HTMLHeadElement head = DOMUtilHTML.getHTMLHead(doc);
            LinkedList<Node> metaList = DOMUtilInternal.getChildElementListWithTagName(head,"meta",true);
            if (metaList != null)
            {
                int i = 0;
                for(Iterator<Node> it = metaList.iterator(); it.hasNext(); )
                {
                    HTMLMetaElement elem = (HTMLMetaElement)it.next();
                    if (HTMLTemplateVersionDelegateImpl.isSVGWebMetaDeclarationStatic(elem))
                    {
                        svgWeb = true;
                        forceFlash = elem.getContent().toLowerCase().equals("true");
                        metaDecPos = i;
                        break;
                    }
                    i++;
                }
            }
        }

        if (svgWeb)
        {
            ClientDocumentStfulDelegateWebImpl clientDoc = getClientDocumentStfulDelegateWeb();
            clientDoc.setSVGWebInfo(forceFlash,metaDecPos);
            addScriptFileToLoad(LoadScriptImpl.ITSNAT_SVGWEB);
        }
    }

    protected boolean isSVGRootElementProcessedBySVGWeb(Element elem)
    {
        ClientDocumentStfulDelegateWebImpl clientDoc = getClientDocumentStfulDelegateWeb();
        return SVGWebInfoImpl.isSVGRootElementProcessedBySVGWebFlash(elem,clientDoc);
    }

    protected Map<Element,Element> processTreeSVGWebElements(HTMLDocument doc)
    {
        HTMLBodyElement body = (HTMLBodyElement)doc.getBody();
        Map<Element,Element> svgwebElems = new HashMap<Element,Element>();
        processTreeSVGWebElements(body,svgwebElems);
        if (svgwebElems.isEmpty())
            svgwebElems = null;
        return svgwebElems;
    }

    protected Node processTreeSVGWebElements(Node node,Map<Element,Element> svgwebElems)
    {
        if (node == null) return null;
        if (node.getNodeType() != Node.ELEMENT_NODE) return node;

        Element elem = (Element)node;
        if (isSVGRootElementProcessedBySVGWeb(elem))
        {
            // Hay el deseo expl�cito de que sea SVGWeb quien procese este <svg>
            Element script = processSVGRootElemForSVGWeb(elem);
            svgwebElems.put(script, elem);
            return script; // script ha reemplazado a elem
        }
        else if (elem.hasChildNodes())
        {
            Node child = elem.getFirstChild();
            while(child != null)
            {
                child = processTreeSVGWebElements(child,svgwebElems);

                child = child.getNextSibling();
            }
        }
        return elem;
    }

    protected Element processSVGRootElemForSVGWeb(Element elem)
    {
        // Envolvemos temporalmente el elemento root <svg> en un
        // <script type="image/svg+xml">
        // Esta es la �nica opci�n viable compatible con ItsNat porque el uso de <object>
        // con data/src="data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg">...</svg>"
        // da lugar a la generaci�n de un <iframe> al final de la p�gina por cada <object>
        // lo cual nos fastidia el DOM.
        // Usando <script type="image/svg+xml"> no se genera nada al final de la p�gina
        // en el caso de navegadores no MSIE, en el caso de MSIE se genera un UNICO <div>
        // este <div> es b�sicamente para generar el <?import namespace="svg" ..?>
        // y declaraciones necesarias de cada trozo de SVG inclu�do, este elemento
        // es tratado autom�ticamente de forma adecuada por ItsNat para que no estropee el DOM.
        // Otra cosa positiva de usar la t�cnica <script type="image/svg+xml"> es que
        // en el c�digo fuente se ve el c�digo SVG, con object y data/src inline se ve todo
        // en una l�nea y adem�s no se tolera cargar la p�gina con "localhost" (da error en MSIE y FireFox
        // de diferente naturaleza) ha de usarse 127.0.0.1

        Document doc = elem.getOwnerDocument();
        Element script = doc.createElement("script");
        script.setAttribute("type","image/svg+xml");
        elem.getParentNode().replaceChild(script,elem);
        script.appendChild(elem);
        return script; // Ha reemplazado a elem
    }

    protected static void restoreSVGWebElements(Map<Element,Element> svgRootElems)
    {
        if (svgRootElems != null)
        {
            for(Map.Entry<Element,Element> entry : svgRootElems.entrySet())
            {
                Element script = entry.getKey();
                Element elem = entry.getValue();
                script.getParentNode().replaceChild(elem, script);
            }
        }
    }

    protected void rewriteClientHTMLTextAreaProperties(HTMLTextAreaElement elem,StringBuilder code)
    {
        // Se redefine en el caso de Opera 9 Desktop
        ClientDocumentStfulDelegateWebImpl clientDoc = getClientDocumentStfulDelegateWeb();        
        //ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        BrowserWeb browser = clientDoc.getBrowserWeb();

        // Hay dos casos, fast load mode y no fast load, pero la acci�n es la misma
        // - Caso Fast Load
        //   El contenido que manda est� en el nodo de texto del <textarea>
        //   no en el atributo value, salvo que expl�citamente se sepa que se ha definido
        //   Para m�s detalles ver m�todos:
        //   ItsNatHTMLTextAreaImpl.afterRender(...) y
        //   DocMutationEventListenerHTMLImpl.beforeAfterRenderAndSendMutationCode(...)

        // - Caso NO fast load
        //  En modo no fast load el reseteo (esta llamada) se hace tras serializar
        //  pero ANTES de que se despachen los listeners. El valor del atributo value
        //  no ha podido ser cambiado respecto al inicial en el template
        //  (ya sea page template o fragmento incluido via <include> da igual)
        //  y sabemos que no pinta nada inicialmente (ignorado) en el valor del textarea
        //  por lo que s�lo cuenta el nodo de texto hijo. Sabemos que en este
        // caso el value no ha sido definido expl�citamente por c�digo del usuario.

        // En remote control tenemos que saber si el atributo fue definido expl�citamente
        // por c�digo o no.

        // Recuerda que en Opera 9 se pasa por aqu� dos veces (la segunda tras la carga)
        // para evitar el auto-fill

        String explicitValue = ((HTMLTextAreaElementImpl)elem).getValueProperty();
        if (explicitValue != null) // Ser� el mismo valor que el atributo value
        {
            processUIControlProperty(elem,"value",code,clientDoc);
        }
        else // Manda el nodo de texto (el atributo puede tener un valor inicial cualquiera pero este no pinta nada)
        {
            code.append( "var elem = " + clientDoc.getNodeReference(elem,true,true) + ";\n" );
            String content = DOMUtilInternal.getTextContent(elem,false); // No puede ser nulo
            String valueJS = JSRenderImpl.toTransportableStringLiteral(content,browser);
            JSRenderPropertyImpl render = JSRenderPropertyImpl.getJSRenderProperty(elem,clientDoc.getBrowserWeb());
            PropertyImpl prop = render.getProperty(elem,"value");
            code.append( render.renderSetProperty(prop, elem, "elem", valueJS, content, clientDoc) );
        }
    }

    protected abstract LinkedList<Element> fixOtherNSElementsInHTMLFindRootElems();
    protected abstract LinkedList<Attr> fixOtherNSElementsInHTMLSaveValidNames(LinkedList<Element> otherNSRootElemsInHTML);
    protected abstract void fixOtherNSElementsInHTMLCleanAuxAttribs(LinkedList<Attr> attribs);
    protected abstract void fixOtherNSElementsInHTMLGenCode(LinkedList<Element> otherNSElemsInHTML);
}
