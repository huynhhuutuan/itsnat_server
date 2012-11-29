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

package org.itsnat.impl.core.resp;

import java.io.IOException;
import java.io.Writer;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.doc.ItsNatDocumentImpl;
import org.itsnat.impl.core.req.RequestLoadDocImpl;


/**
 *
 * @author jmarranz
 */
public abstract class ResponseLoadDocImpl extends ResponseImpl
{

    /** Creates a new instance of ResponseNormalImpl */
    public ResponseLoadDocImpl(RequestLoadDocImpl request)
    {
        super(request);
    }

    public RequestLoadDocImpl getRequestLoadDoc()
    {
        return (RequestLoadDocImpl)request;
    }

    public String serializeDocument()
    {
        ItsNatDocumentImpl itsNatDoc = getItsNatDocument();
        return itsNatDoc.serializeDocument();
    }

    public void sendMarkupToClient(String docMarkup)
    {
        writeResponse(docMarkup);
    }

    public abstract void dispatchRequestListeners();
}
