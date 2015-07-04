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

package org.itsnat.impl.core.scriptren.jsren.node;

import org.itsnat.impl.core.browser.web.BrowserMSIEOld;
import org.itsnat.impl.core.browser.web.BrowserWeb;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.web.SVGWebInfoImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.domutil.DOMUtilHTML;
import org.itsnat.impl.core.scriptren.jsren.node.html.msie.JSRenderHTMLCommentMSIEOldImpl;
import org.itsnat.impl.core.scriptren.jsren.node.otherns.JSRenderSVGCommentSVGWebImpl;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;

/**
 *
 * @author jmarranz
 */
public abstract class JSRenderCommentImpl extends JSRenderCharacterDataImpl
{

    /** Creates a new instance of JSCommentRender */
    public JSRenderCommentImpl()
    {
    }

    public static JSRenderCommentImpl getJSRenderComment(Comment node,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        if (DOMUtilHTML.isHTMLCharacterData(node))
        {
            BrowserWeb browser = clientDoc.getBrowserWeb();
            if (browser instanceof BrowserMSIEOld)
                return JSRenderHTMLCommentMSIEOldImpl.getJSRenderHTMLCommentMSIEOld((BrowserMSIEOld)browser);
            else
                return JSRenderCommentDefaultImpl.SINGLETON;
        }
        else if (SVGWebInfoImpl.isSVGNodeProcessedBySVGWebFlash(node,clientDoc))
            return JSRenderSVGCommentSVGWebImpl.SINGLETON;
        else
            return JSRenderCommentDefaultImpl.SINGLETON;
    }

    public String createNodeCode(Node node,ClientDocumentStfulDelegateImpl clientDoc)
    {
        Comment nodeComm = (Comment)node;
        return "itsNatDoc.doc.createComment(" + dataTextToJS(nodeComm,clientDoc) + ")";
    }
    
    @Override    
    public String getCharacterDataModifiedCode(CharacterData node,ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        return getCharacterDataModifiedCodeDefault(node,clientDoc);
    }
}
