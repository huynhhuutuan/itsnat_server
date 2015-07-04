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

package org.itsnat.impl.core.scriptren.jsren.node.html.msie;

import java.util.HashMap;
import java.util.Map;
import org.itsnat.impl.core.browser.web.BrowserMSIEOld;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.scriptren.jsren.node.PropertyImpl;
import org.itsnat.impl.core.scriptren.jsren.node.html.JSRenderHTMLAttributeImpl;
import org.itsnat.impl.core.dompath.NodeLocationImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 *
 * @author jmarranz
 */
public class JSRenderHTMLAttributeMSIEOldImpl extends JSRenderHTMLAttributeImpl
{
    public static final JSRenderHTMLAttributeMSIEOldImpl SINGLETON = new JSRenderHTMLAttributeMSIEOldImpl();
    
    // Algunos atributos tal y como "cellSpacing" o "cellPadding" o "rowSpan" o "colSpan" si no se ponen
    // con la letra de en medio en may�scula en el setAttribute() son ignorados en el MSIE
    // es decir considera que son atributos desconocidos, el nombre ha de coincidir
    // exactamente con el de la propiedad entonces funciona el setAttribute().
    // http://www.codingforums.com/showthread.php?t=24525
    // http://msdn2.microsoft.com/en-us/library/ms533055.aspx Lista de Propiedades

    // No es necesario sincronizar esta colecci�n porque va a ser s�lo le�da
    public static final Map<String,String> attributes = new HashMap<String,String>();

    static
    {
        // http://msdn2.microsoft.com/en-us/library/ms533055.aspx
        // http://www.w3.org/TR/html401/index/attributes.html

        // A continuaci�n se listan aquellos atributos del MSIE que no tienen todas
        // las letras may�sculas, aunque algunos atributos s�lo se dan en un
        // tag dado hay otros que son comunes a varios tags por lo que
        // no nos preocupamos del tag que los usan.

        // La lista se obtenido de la siguiente forma:
        // se cogen la propiedades de la lista de Microsoft con mezcla de mays/minus
        // que est�n en la lista de atributos del HTML 4.01
        // (hay que tener en cuenta que el MSIE tiene algunos atributos no est�ndar, no los consideramos)

        // Dudosos no incluidos, casos en donde el nombre de la propiedad
        // no es un simple cambio de may�sculas respecto al atributo:
        // accept-charset/acceptCharset
        // http-equiv/httpEquiv

        // Esto est� corroborado aqu�:
        // http://webbugtrack.blogspot.com/2007/08/bug-242-setattribute-doesnt-always-work.html

        String[] attribs = new String[]
        {
            "accessKey","aLink","allowTransparency","bgColor",
            "cellPadding","cellSpacing","codeBase","codeType",
            "colSpan","dateTime","frameBorder","isMap",
            "marginHeight","marginWidth","maxLength","noHref",
            "noResize","noShade","noWrap","readOnly",
            "rowSpan","tabIndex","useMap","vAlign",
            "valueType","vLink",
        };

        for(int i = 0; i < attribs.length; i++)
            addAttribute(attribs[i]);
    }

    public JSRenderHTMLAttributeMSIEOldImpl()
    {
    }

    public static JSRenderHTMLAttributeMSIEOldImpl getJSRenderHTMLAttributeMSIEOld(BrowserMSIEOld browser)
    {
        return JSRenderHTMLAttributeMSIEOldImpl.SINGLETON;
    }

    private static void addAttribute(String attrName)
    {
        String key = attrName.toLowerCase(); // As� aseguramos la unicidad del nombre a modo de clave y metemos el nombre que ha de usarse
        attributes.put(key,attrName);
    }

    protected String getAttributeName(String attrName)
    {
        String key = attrName.toLowerCase();
        String newAttrName = attributes.get(key);  // Devuelve el que debe ponerse
        if (newAttrName != null)
            return newAttrName;
        else
            return attrName;
    }

    public String setStyleMSIECode(Element elem,String valueJS,ClientDocumentStfulDelegateImpl clientDoc)
    {
        NodeLocationImpl nodeLoc = clientDoc.getNodeLocation(elem,true);
        return "itsNatDoc.setCSSStyle(" + nodeLoc.toScriptNodeLocation(true) + "," + valueJS + ");\n";
    }

    public String setStyleMSIECode(String elemVarName,String valueJS)
    {
        return elemVarName + ".style.cssText = " + valueJS + ";\n";
    }

    public static boolean isStyleAttr(String attName)
    {
        return attName.equals("style");
    }

    @Override
    protected String setAttributeCode(Attr attr,String attrName,String jsValue,Element elem,boolean newElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        if (isStyleAttr(attrName))
            return setStyleMSIECode(elem,jsValue,clientDoc);
        else
        {
            // Incluso aunque newElem = true hay ciertos atributos que se tienen que definir como propiedades
            JSRenderHTMLPropertyMSIEOldImpl render = JSRenderHTMLPropertyMSIEOldImpl.getJSRenderHTMLPropertyMSIEOld((BrowserMSIEOld)clientDoc.getBrowser());
            PropertyImpl prop = render.getGlobalProperty(elem,attrName);
            if (prop != null)
            {
                return setAttributeWithProperty(attr,attrName,jsValue,elem,newElem,prop,clientDoc);
            }
            else
            {
                attrName = getAttributeName(attrName);

                return super.setAttributeCode(attr,attrName,jsValue,elem,newElem,clientDoc);
            }
        }
    }

    @Override
    public String setAttributeCode(Attr attr,String attrName,String jsValue,Element elem,String elemVarName,boolean newElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        if (isStyleAttr(attrName))
            return setStyleMSIECode(elemVarName,jsValue);
        else
        {
            // Incluso aunque newElem = true hay ciertos atributos que se tienen que definir como propiedades
            JSRenderHTMLPropertyMSIEOldImpl render = JSRenderHTMLPropertyMSIEOldImpl.getJSRenderHTMLPropertyMSIEOld((BrowserMSIEOld)clientDoc.getBrowser());
            PropertyImpl prop = render.getGlobalProperty(elem,attrName); // Como estamos en creaci�n propiedades tal y como "selected" etc no necesitan definirse, nos interesan atributos problem�ticos en el MSIE tal y como "class" "onclick" etc
            if (prop != null)
                return setAttributeWithProperty(attr,attrName,jsValue,elem,elemVarName,newElem,prop,clientDoc);
            else
            {
                attrName = getAttributeName(attrName);

                return super.setAttributeCode(attr,attrName,jsValue,elem,elemVarName,newElem,clientDoc);
            }
        }
    }

    @Override
    protected String removeAttributeCode(Attr attr,String attrName,Element elem,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        if (isStyleAttr(attrName))
            return setStyleMSIECode(elem,"\"\"",clientDoc);
        else
        {
            JSRenderHTMLPropertyMSIEOldImpl render = JSRenderHTMLPropertyMSIEOldImpl.getJSRenderHTMLPropertyMSIEOld((BrowserMSIEOld)clientDoc.getBrowserWeb());
            PropertyImpl prop = render.getGlobalProperty(elem,attrName); // Como estamos en creaci�n propiedades tal y como "selected" etc no necesitan definirse, nos interesan atributos problem�ticos en el MSIE tal y como "class" "onclick" etc
            if (prop != null)
                return removeAttributeWithProperty(attr,attrName,elem,prop,clientDoc);
            else
            {
                attrName = getAttributeName(attrName);

                return super.removeAttributeCode(attr,attrName,elem,clientDoc);
            }
        }
    }

    public boolean isRenderAttributeAlongsideProperty(String attrName,Element elem)
    {
        // MSIE no distingue atributos de propiedades
        return false;
    }
}
