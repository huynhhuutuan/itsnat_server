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

import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.scriptren.jsren.node.PropertyImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLOptionElement;

/**
 *
 * @author jmarranz
 */
public class JSRenderHTMLAttributeOperaOldImpl extends JSRenderHTMLAttributeW3CImpl
{
    public final static JSRenderHTMLAttributeOperaOldImpl SINGLETON = new JSRenderHTMLAttributeOperaOldImpl();

    /** Creates a new instance of JSRenderHTMLAttributeOperaImpl */
    public JSRenderHTMLAttributeOperaOldImpl()
    {
    }

    public boolean isRenderAttributeAlongsideProperty(String attrName,Element elem)
    {
        return true;
    }

    @Override
    protected String removeAttributeWithProperty(Attr attr,String attrName,Element elem,String elemVarName,PropertyImpl prop,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        if (elem instanceof HTMLOptionElement &&
            prop.getPropertyName().equals("selected"))
        {
            // Solucionamos un problema curioso de Opera 9 tanto desktop (probado 9.63)
            // como mobile (v9.5 beta y 9.7 beta probados) y es que cuando
            // un <select> se oculta con visibility="hidden" a partir de entonces
            // (obviamente ya visible) los elementos seleccionados no son
            // deseleccionados visualmente cuando se cambia el estado selected via JavaScript
            // (incluso cambiando el atributo tambi�n), aunque el estado del DOM
            // es correcto.
            // Esto es importante en ItsNat pues los modal layers ocultan los <select> que est�n
            // "detr�s" en Opera Mobile 9.5+ dej�ndolos entonces en un estado "err�neo".
            // Esto no ocurre con display="none"
            // La soluci�n es reinsertar el elemento el poner como display="none"
            // no funciona.

            StringBuilder code = new StringBuilder();

            code.append( super.removeAttributeWithProperty(attr,attrName,elem,elemVarName,prop,clientDoc) );

            String methodName = "operaFixHTMLSelect";
            if (!clientDoc.isClientMethodBounded(methodName))
                code.append(bindFixHTMLSelectMethod(methodName,clientDoc));

            code.append("itsNatDoc." + methodName + "(" + elemVarName + ");\n");

            return code.toString();
        }
        else
            return super.removeAttributeWithProperty(attr,attrName,elem,elemVarName,prop,clientDoc);
    }

    private static String bindFixHTMLSelectMethod(String methodName,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        StringBuilder code = new StringBuilder();

        code.append( "var func = function (elem)" );
        code.append( "{" );
        code.append( "    var parentNode = elem.parentNode;\n" );
        code.append( "    var clone = elem.cloneNode(true);\n" );
        code.append( "    parentNode.replaceChild(clone,elem);\n" );
        code.append( "    parentNode.replaceChild(elem,clone);\n" );
        code.append( "};" );

        code.append( "itsNatDoc." + methodName + " = func;\n" );

        clientDoc.bindClientMethod(methodName);

        return code.toString();
    }
}
