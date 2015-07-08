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

package org.itsnat.impl.core.scriptren.jsren;

import org.itsnat.impl.core.scriptren.shared.ScriptReference;
import org.itsnat.impl.core.scriptren.shared.ScriptExprImpl;
import java.io.Serializable;
import org.itsnat.core.script.ScriptExpr;
import org.itsnat.core.script.ScriptUtil;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.scriptren.jsren.node.JSRenderNodeImpl;
import org.itsnat.impl.core.dompath.NodeLocationWithParentImpl;
import org.itsnat.impl.core.scriptren.shared.ScriptUtilImpl;

/**
 *
 * @author jmarranz
 */
public abstract class JSScriptUtilImpl extends ScriptUtilImpl implements ScriptUtil,Serializable
{
    /**
     * Creates a new instance of ScriptUtil
     */
    public JSScriptUtilImpl()
    {
    }

    public String encodeURIComponent(String text)
    {
        return JSRenderImpl.encodeURIComponent(text);
    }

    public String encodeURIComponent(char c)
    {
        return JSRenderImpl.encodeURIComponent(c);
    }

    public ScriptReference createScriptReference(Object value)
    {
        // POR AHORA NO ES PUBLICO.
        // Quiz�s m�s adelante cuando se haga un modelo completo de metaprogramaci�n
        // JavaScript en java
        return new JSReferenceImpl(value,this);
    }
  
   
    protected String renderAddNodeToCache(NodeLocationWithParentImpl nodeLoc)
    {
        return JSRenderNodeImpl.addNodeToCache(nodeLoc);
    }
    
    protected String renderGetCallMethodCode(Object obj,String methodName,Object[] params,boolean endSentence,ClientDocumentStfulDelegateImpl clientDoc)
    {      
        return JSRenderMethodCallImpl.getCallMethodCode(obj,methodName,params,endSentence,false,clientDoc);    
    }
    
    protected String renderSetPropertyCode(Object obj,String propertyName,Object value,boolean endSentence,boolean cacheIfPossible,ClientDocumentStfulDelegateImpl clientDoc)
    {          
        return JSRenderImpl.getSetPropertyCode(obj,propertyName,value,endSentence,false,clientDoc);    
    }
    
    protected String renderGetPropertyCode(Object obj,String propertyName,boolean endSentence,ClientDocumentStfulDelegateImpl clientDoc)
    {              
        return JSRenderImpl.getGetPropertyCode(obj,propertyName,endSentence,false,clientDoc); 
    }
    
    protected String javaToScript(Object value,ClientDocumentStfulDelegateImpl clientDoc)
    {
        return JSRenderImpl.javaToJS(value,false,clientDoc);    
    }
}
