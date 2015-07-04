/*
  ItsNat Java Web Application Framework
  Copyright (C) 2007-2014 Jose Maria Arranz Santamaria, Spanish citizen

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

package org.itsnat.impl.core.scriptren.bsren;

import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.scriptren.shared.JSAndBSRenderImpl;

/**
 *
 * @author jmarranz
 */
public class BSRenderImpl
{
    public static String javaToBS(Object value,boolean cacheIfPossible,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return JSAndBSRenderImpl.javaToScript(value, cacheIfPossible, clientDoc);
    }    
    
    public static String toTransportableStringLiteral(String text,Browser browser)
    {
        return toTransportableStringLiteral(text,true,browser);
    }

    public static String toTransportableStringLiteral(String text,boolean addQuotation,Browser browser)
    {
        return JSAndBSRenderImpl.toTransportableStringLiteral(text, addQuotation, browser);
    }    
    
    public static String toLiteralStringBS(String value)
    {
        return JSAndBSRenderImpl.toLiteralStringScript(value);
    }    
    
    public static String getSetPropertyCode(Object object,String propertyName,Object value,boolean endSentence,boolean cacheIfPossible,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return JSAndBSRenderImpl.getSetPropertyCode(object, propertyName, value, endSentence, cacheIfPossible, clientDoc);
    }

    public static String getGetPropertyCode(Object object,String propertyName,boolean endSentence,boolean cacheIfPossible,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return JSAndBSRenderImpl.getGetPropertyCode(object, propertyName, endSentence, cacheIfPossible, clientDoc);
    }    
}
