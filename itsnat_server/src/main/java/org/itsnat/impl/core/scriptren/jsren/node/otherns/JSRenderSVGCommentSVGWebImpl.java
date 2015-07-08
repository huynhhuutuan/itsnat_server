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
package org.itsnat.impl.core.scriptren.jsren.node.otherns;

import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.scriptren.jsren.node.JSRenderCommentImpl;
import org.w3c.dom.Node;
import org.w3c.dom.CharacterData;

/**
 * En SVGWeb por ahora los comentarios son tolerados en carga pero no se reflejan
 * en el DOM y no soporta la inserci�n y borrado de comentarios.
 *
 * @author jmarranz
 */
public class JSRenderSVGCommentSVGWebImpl extends JSRenderCommentImpl
{
    public static final JSRenderSVGCommentSVGWebImpl SINGLETON = new JSRenderSVGCommentSVGWebImpl();

    /** Creates a new instance of JSRenderSVGCommentSVGWebImpl */
    public JSRenderSVGCommentSVGWebImpl()
    {
    }

    @Override
    public String getAppendCompleteChildNode(Node parent,Node newNode,String parentVarName,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return "";
    }

    @Override
    public String getInsertCompleteNodeCode(Node newNode,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return "";
    }

    @Override
    public String getRemoveNodeCode(Node removedNode,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        return "";
    }

    @Override
    public String getCharacterDataModifiedCode(CharacterData node,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        return "";
    }

}
