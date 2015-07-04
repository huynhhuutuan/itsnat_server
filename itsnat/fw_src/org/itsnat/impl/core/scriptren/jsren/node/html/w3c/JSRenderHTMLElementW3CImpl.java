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

package org.itsnat.impl.core.scriptren.jsren.node.html.w3c;

import org.itsnat.impl.core.browser.web.BrowserAdobeSVG;
import org.itsnat.impl.core.browser.web.BrowserBatik;
import org.itsnat.impl.core.browser.web.BrowserGecko;
import org.itsnat.impl.core.browser.web.BrowserMSIE9;
import org.itsnat.impl.core.browser.web.BrowserW3C;
import org.itsnat.impl.core.browser.web.opera.BrowserOperaOld;
import org.itsnat.impl.core.browser.web.webkit.BrowserWebKit;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.scriptren.jsren.node.html.JSRenderHTMLElementImpl;
import org.itsnat.impl.core.scriptren.jsren.node.otherns.JSRenderOtherNSElementW3CImpl;
import org.itsnat.impl.core.template.MarkupTemplateVersionImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLDocument;

/**
 *
 * @author jmarranz
 */
public abstract class JSRenderHTMLElementW3CImpl extends JSRenderHTMLElementImpl
{
    /** Creates a new instance of JSRenderHTMLElementW3CImpl */
    public JSRenderHTMLElementW3CImpl()
    {
        // Tag names SIN innerHTML

        // El caso de <script> es especial pues innerHTML funciona bien
        // en MIME "text/html" pero no en "application/xhtml+xml" porque el
        // render envuelve el script como "<![DATA[...]]>" y el <![DATA[
        // lo entiende el Gecko como script (incluso FireFox 3.0) y da error de sintaxis
        // (otros navegadores W3C como Opera 9, Safari 3 y Chrome 1.0 lo toleran).
        // Excluyendo el uso de innerHTML se usa un Text node correctamente.
        // En WebKit hay similares problemas por lo menos en Chrome 5.
        // Por otra parte est� la innecesaria conversi�n a HTML del script
        // para aplicar el innerHTML que implica la conversi�n de < en &lt, & en &amp; etc
        // Como este objeto es compartido tanto por HTML como XHTML excluimos <script>
        // en todos los casos y ya est�.
        // Aplicamos a todos los navegadores W3C pues el manejo de <script>
        // es siempre delicado.

        tagNamesWithoutInnerHTML.add("script");


        //---------------------------------------------------
        // TagNames no v�lidos DENTRO de un innerHTML
        //---------------------------------------------------
        /* En FireFox y Safari  el <script> es insertado via innerHTML pero por razones de seguridad
           es ignorado el c�digo del script, no se ejecuta
           Ejemplo:
           var prueba = document.getElementById("prueba");
           prueba.innerHTML = "<div><script>alert('Hola');</" + "script></div>";
           Inserta el <script> pero no lo ejecuta (el truco "</" + "script>" es para evitar
           un cerrado err�neo del parser).
           Sin embargo via DOM funciona correctamente.

           Aunque es posible que funcione en alg�n otro navegador "tipo W3C"
           la inserci�n de <script> via innerHTML es una mala pr�ctica en general
           para la seguridad si se usa texto que proviene del usario
          (aunque este problema no afecta en nada a ItsNat pues el original es DOM)
           no lo permitimos en ning�n navegador.
       */
        tagNamesNotValidInsideInnerHTML.add("script");

        // El caso de <style> dentro de un innerHTML se eval�a para cada navegador.
    }

    public static JSRenderHTMLElementW3CImpl getJSRenderHTMLElementW3C(BrowserW3C browser)
    {
        if (browser instanceof BrowserGecko)
            return JSRenderHTMLElementGeckoImpl.getJSRenderHTMLElementGeckoImpl((BrowserGecko)browser);
        else if (browser instanceof BrowserWebKit)
            return JSRenderHTMLElementWebKitImpl.getJSRenderHTMLElementWebKit((BrowserWebKit)browser);
        else if (browser instanceof BrowserOperaOld)
            return JSRenderHTMLElementOperaOldImpl.getJSRenderHTMLElementOperaOld((BrowserOperaOld)browser);
        else if (browser instanceof BrowserMSIE9)
            return JSRenderHTMLElementMSIE9Impl.SINGLETON;
        else if (browser instanceof BrowserAdobeSVG)
            return JSRenderHTMLElementAdobeSVGImpl.SINGLETON;
        else if (browser instanceof BrowserBatik)
            return JSRenderHTMLElementBatikImpl.SINGLETON;
        else // Desconocido
            return JSRenderHTMLElementGeckoImpl.SINGLETON;
    }

    @Override
    protected boolean isChildNotValidInsideInnerHTMLHTMLElement(Element elem,MarkupTemplateVersionImpl template)
    {
        if (super.isChildNotValidInsideInnerHTMLHTMLElement(elem, template))
            return true; // No se puede meter dentro de un innerHTML, nada m�s que decir

        if (JSRenderOtherNSElementW3CImpl.isElementWithSomethingOtherNSInMIMEHTML(elem,template))
            return true; // Requiere inserci�n via DOM pues via innerHTML se ignoran los namespaces en MIME text/html

        return false;
    }

    protected boolean isChildNotValidInsideInnerHTMLElementNotHTML(Element elem,MarkupTemplateVersionImpl template)
    {
        // En navegadores W3C tal como FireFox, Opera, WebKit...
        // en un documento con MIME "application/xhtml+xml" el innerHTML
        // inserta correctamente elementos no X/HTML, pero con MIME "text/html"
        // es insertado como elementos HTML desconocidos,
        // es decir el namespaceURI no es el esperado (es nulo), es decir pasa lo mismo
        // que en carga, si evitamos insertar via innerHTML obligaremos a usar correctamente
        // la inserci�n via DOM por lo menos del nodo padre no HTML luego en otro lugar
        // se decide si los hijos pueden insertarse via setInnerXML de ItsNat.
        // Tambi�n est� el caso de contener un atributo con namespace no XHTML

        if (JSRenderOtherNSElementW3CImpl.isElementWithSomethingOtherNSInMIMEHTML(elem,template))
            return true; // Requiere inserci�n via DOM pues via innerHTML se ignoran los namespaces en MIME text/html

        return false; // Caso de documento XHTML con MIME application/xhtml+xml o documento SVG etc
    }

    protected boolean isChildNotValidInsideInnerHTMLNotElementOrText(Node node)
    {
        /* Caso <![CDATA[ ]]> (caso Node.CDATA_SECTION_NODE)
           FireFox no los procesa bien dentro de innerHTML
           pues los convierte en comentarios seguramente
           porque Document.createCDataSection no est� soportado en HTML, por eso mismo
           lo tragamos como nodo v�lido dentro de un innerHTML porque
           de otra manera habr� error en el cliente al generar una llamada createCDataSection
           En Safari directamente los filtra pero aceptamos el uso de innerHTML
          para evitar el error de usar Document.createCDataSection
        */

        return false;
    }

    @Override
    protected String createElement(Element nodeElem,String tagName,ClientDocumentStfulDelegateImpl clientDoc)
    {
        // En p�ginas con MIME text/html los elementos creados con
        // document.createElement() son consideramos HTML.
        // He comprobado que incluso con MIME XHTML el elemento creado
        // con document.createElement() tiene namespace no nulo y es XHTML
        // (probado en FireFox 3.5, Opera 9.6, Safari 3.1 y Chrome 2.0)
        // por lo que usar createElementNS() genera m�s c�digo in�tilmente
        // (hay que enviar el namespace).
        // En ese caso s�lo consideramos el caso RARISIMO de elemento X/HTML con prefijo
        // El namespace sabemos que en el servidor no es nulo y es XHTML

        if (nodeElem.getOwnerDocument() instanceof HTMLDocument)
        {
            if (nodeElem.getPrefix() != null) // Caso MUY MUY RARO
                return "itsNatDoc.doc.createElementNS(\"" + nodeElem.getNamespaceURI() + "\",\"" + tagName + "\")";
            else
                return super.createElement(nodeElem,tagName,clientDoc);
        }
        else  // Caso de XHTML embebido en documentos no X/HTML
            return "itsNatDoc.doc.createElementNS(\"" + nodeElem.getNamespaceURI() + "\",\"" + tagName + "\")";
    }



}
