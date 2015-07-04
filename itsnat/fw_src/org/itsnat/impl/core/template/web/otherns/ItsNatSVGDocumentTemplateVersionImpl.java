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

package org.itsnat.impl.core.template.web.otherns;

import org.itsnat.core.ItsNatException;
import org.itsnat.core.ItsNatServletRequest;
import org.itsnat.core.ItsNatServletResponse;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.web.BrowserAdobeSVG;
import org.itsnat.impl.core.browser.web.BrowserMSIEOld;
import org.itsnat.impl.core.doc.ItsNatDocumentImpl;
import org.itsnat.impl.core.doc.web.ItsNatSVGDocumentImpl;
import org.itsnat.impl.core.servlet.ItsNatServletRequestImpl;
import org.itsnat.impl.core.servlet.ItsNatSessionImpl;
import org.itsnat.impl.core.template.ItsNatStfulDocumentTemplateImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 *
 * @author jmarranz
 */
public class ItsNatSVGDocumentTemplateVersionImpl extends ItsNatOtherNSDocumentTemplateVersionImpl
{
    /**
     * Creates a new instance of ItsNatSVGDocumentTemplateVersionImpl
     */
    public ItsNatSVGDocumentTemplateVersionImpl(ItsNatStfulDocumentTemplateImpl docTemplate,InputSource source,long timeStamp,ItsNatServletRequest request,ItsNatServletResponse response)
    {
        super(docTemplate,source,timeStamp,request,response);
    }

    @Override
    public Browser getBrowser(ItsNatServletRequestImpl itsNatRequest)
    {
        Browser browser = super.getBrowser(itsNatRequest);
        if (browser instanceof BrowserMSIEOld)
        {
            // Cuando se carga un documento SVG via iframe o por URL directa se dan dos requests:
            // El primer request lo hace el MSIE, al ver que la respuesta ha sido
            // un MIME SVG delega en el plugin, el plugin a su vez hace un nuevo request.

            // Aunque no est� documentado por Microsoft en iframe
            // este comportamiento es el normal de acuerdo al comportamiento de <embed> (http://msdn.microsoft.com/en-us/library/ms535258%28VS.85%29.aspx)
            // sin atributo type (ver "Performance Note"):
            // http://msdn.microsoft.com/en-us/library/ms535245%28VS.85%29.aspx

            // Yo creo que esta es la raz�n por la que el contentWindow en el caso de <iframe>
            // no es el window del documento SVG sino probablemente el window del primer
            // request, por lo que no es posible acceder desde el documento padre al hijo
            // (s� desde el hijo al padre).

            // S�lo tenemos una forma de distinguir entre un request
            // realizado por MSIE y uno de ASV (ASV v3, v6 ), y es a trav�s del header
            // "accept", en el caso de MSIE incluye "*/*" y m�s cosas (MIMEs de im�genes etc,
            // algunas dependen de componentes instalados tal y como .Net, Office etc),
            // en caso de ASV s�lo es "*/*"
            // Es MUY interesante distinguir entre un request MSIE y uno ASV
            // sobre todo en el caso de SVG cargado via iframe con ASV instalado, pues MSIE como no sabe
            // el MIME hace un primer request, al recibir el MIME SVG delega para el segundo request
            // al plugin SVG.
            // Si evitamos que el primer request se procese de la forma normal, ganamos
            // en rendimiento y evitamos generar y registrar un documento en la sesi�n
            // que no servir� para nada (el primer request).

            // En el caso de <object> tambi�n se producen dos requests, el problema
            // es que ambos se producen a trav�s del plugin. Quiz�s se deba
            // al doble uso de src como atributo/propiedad y como param name="src"

            // Con <embed> s�lo hay un request.

            String accept = itsNatRequest.getHeader("accept");
            if ("*/*".equals(accept))
                browser = new BrowserAdobeSVG(browser.getUserAgent());
        }
        return browser;
    }

    @Override
    protected ItsNatDocumentImpl createItsNatDocument(Document doc,Browser browser,String requestURL,ItsNatSessionImpl session,boolean stateless)
    {
        return new ItsNatSVGDocumentImpl(doc,this,browser,requestURL,session,stateless);
    }

    public static boolean isGeneratedDocumentFake(Browser browser)
    {
        // MSIE no soporta SVG, se genera un documento SVG que es un simple
        // aviso, adem�s hay que evitar que los listeners se ejecuten
        // pues ya no es el documento esperado.
        // Esto tambi�n es �til en el caso de SVG cargado en MSIE via IFRAME o via URL directo
        // existiendo un plugin (ASV) pues el primer request es del MSIE que hay que ignorar
        // para evitar ejecutar los listeners del usuario in�tilmente
        // pues dicha p�gina se pierde pues el MSIE delega en el plugin
        // para un segundo request

        return (browser instanceof BrowserMSIEOld);
    }

    @Override
    public Document loadDocument(Browser browser)
    {
        if (isGeneratedDocumentFake(browser))
        {
            // S�lo clonamos hasta el nodo root sin atributos
            Document cloneDoc = manualCloneDocument(getDocument());

            Element root = cloneDoc.getDocumentElement();

            Element text = cloneDoc.createElement("text");
            text.setAttribute("x","25");
            text.setAttribute("y","50");
            text.setAttribute("font-family","Verdana");
            text.setAttribute("font-size","17");
            text.appendChild(cloneDoc.createTextNode("Microsoft Internet Explorer does not natively support SVG."));
            root.appendChild(text);

            text.setAttribute("x","25");
            text.setAttribute("y","100");
            text.setAttribute("font-family","Verdana");
            text.setAttribute("font-size","17");
            text.appendChild(cloneDoc.createTextNode("Use a SVG plugin like Adobe SVG Viewer v3 or v6."));
            root.appendChild(text);

            return cloneDoc;
        }
        else
            return super.loadDocument(browser);
    }

    public String wrapBodyAsDocument(String source)
    {
        // En un futuro podr�a plantearse el cacheado

        return wrapBodyAsDocument(source,getEncoding(),null,null);
    }

    public static String wrapBodyAsDocument(String source,String encoding,String prefix,String defaultNS)
    {
        // SVG 1.1 es el actual y el soportado por los navegadores soportados por ItsNat.

        if ((prefix == null)&&(defaultNS != null)) throw new ItsNatException("INTERNAL ERROR");

        StringBuilder code = new StringBuilder();
        if (encoding != null) // Si no espeficica es que no es necesaria la cabecera xml (de hecho no la soporta ASV v6)
            code.append( "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>" );
// No funciona en MSIE y loadXML:  code.append( "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">" );
        code.append( "<" );
        if (prefix != null) code.append( prefix + ":" );
        code.append( "svg xmlns" );
        if (prefix != null) code.append( ":" + prefix );
        code.append( "=\"http://www.w3.org/2000/svg\"" );
        if (defaultNS != null)
            code.append( " xmlns=\"" + defaultNS + "\"" );
        code.append( " version=\"1.1\">" );

        code.append( source );

        code.append( "</" );
        if (prefix != null) code.append( prefix + ":" );
        code.append( "svg>" );

        return code.toString();
    }
}
