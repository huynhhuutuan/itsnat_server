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

package org.itsnat.impl.core.jsren.dom.node;

import org.itsnat.impl.core.doc.ItsNatDocumentImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.path.NodeLocationImpl;
import org.w3c.dom.CharacterData;

/**
 *
 * @author jmarranz
 */
public abstract class JSRenderCharacterDataImpl extends JSRenderNotChildrenNodeImpl
{

    /**
     * Creates a new instance of JSTextRender
     */
    public JSRenderCharacterDataImpl()
    {
    }

    public String dataTextToJS(CharacterData node,ClientDocumentStfulImpl clientDoc)
    {
        return dataTextToJS(node.getData(),clientDoc);
    }

    protected String dataTextToJS(String text,ClientDocumentStfulImpl clientDoc)
    {
        ItsNatDocumentImpl itsNatDoc = clientDoc.getItsNatDocumentImpl();
        // Resolvemos los textos cacheados, pasamos resolveEntities=true porque
        // este texto NO va a un innerHTML o a nuestro setInnerXML que pasa por
        // un parse, sino que es un texto que va un text.data = "..." o similar
        // y ah� NO se resuelven los entities tal y como &amp;

        text = itsNatDoc.resolveCachedNodes(text,true);
        return toTransportableStringLiteral(text,clientDoc.getBrowser());
    }

    public abstract String getCharacterDataModifiedCode(CharacterData node,ClientDocumentStfulImpl clientDoc);


    protected String getCharacterDataModifiedCodeDefault(CharacterData node,ClientDocumentStfulImpl clientDoc)
    {
        // Se redefine totalmente en el caso de nodos de texto
        String value = dataTextToJS(node,clientDoc);

        NodeLocationImpl nodeLoc = clientDoc.getNodeLocation(node,true);
        return "itsNatDoc.setCharacterData(" + nodeLoc.toJSArray(true) + "," + value + ");\n";
    }
}
