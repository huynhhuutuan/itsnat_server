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

package org.itsnat.impl.core.servlet.http;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.itsnat.impl.core.ItsNatImpl;
import org.itsnat.core.http.ItsNatHttpServlet;
import org.itsnat.impl.core.servlet.ItsNatServletImpl;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.servlet.ItsNatServletRequestImpl;
import org.itsnat.impl.core.servlet.ItsNatSessionImpl;

/**
 *
 * @author jmarranz
 */
public class ItsNatHttpServletImpl extends ItsNatServletImpl implements ItsNatHttpServlet
{
    public static final String ACTION_SERVLET_WEAK_UP = "servlet_weak_up";

    /**
     * Creates a new instance of ItsNatHttpServletImpl
     */
    public ItsNatHttpServletImpl(ItsNatImpl parent,HttpServlet servlet)
    {
        super(parent,servlet);
    }

    public HttpServlet getHttpServlet()
    {
        return (HttpServlet)servlet;
    }

    public ItsNatServletRequestImpl createItsNatServletRequest(ServletRequest request,ServletResponse response,ItsNatSessionImpl itsNatSession)
    {
        return createItsNatHttpServletRequest((HttpServletRequest)request,(HttpServletResponse)response,(ItsNatHttpSessionImpl)itsNatSession);
    }
    
    public ItsNatHttpServletRequestImpl createItsNatHttpServletRequest(HttpServletRequest request,HttpServletResponse response,ItsNatHttpSessionImpl itsNatSession)
    {
        return new ItsNatHttpServletRequestImpl(this,request,response,itsNatSession);            
    }    
    
    @Override
    public ItsNatServletRequestImpl processRequestInternal(ServletRequest request, ServletResponse response,ClientDocumentStfulImpl clientDocStateless) 
    {
        return processRequestInternal((HttpServletRequest)request,(HttpServletResponse)response,clientDocStateless);
    }
    
    public ItsNatServletRequestImpl processRequestInternal(HttpServletRequest request, HttpServletResponse response,ClientDocumentStfulImpl clientDocStateless)
    {
        try
        {
            request.setCharacterEncoding("UTF-8");
            // Lo hacemos antes de tocar nada y llamar a un primer getParameter() o de otra manera es ignorado, es fundamental para caracteres no ASCII (ej. acentos),
            // es el encoding por defecto y adem�s nos viene "encoded" con encodeURIComponent que codifica
            // como Unicode ej. %C3%A1 es la �, sin "UTF-8" el getAttrOrParam devuelve dos caracteres, con "UTF-8" devuelve la �
            // Tenemos el problema de que no podemos poner otro encoding pues depende del documento,
            // y el encoding hay que definirlo antes de obtener valores de par�metros
            // No confundir el encoding del request con el de la respuesta que es el que es configurable en ItsNat
            // En teor�a no ser�a necesaria esta llamada si se especificara el encoding en la request
            // por ejemplo en un POST AJAX tipo x-www-form-urlencoded tendr�amos que especificar: 
            // XMLHttpRequest.setRequestHeader("Content-Type","application/x-www-form-urlencoded,charset=UTF-8");             
            // en el caso del GET no ser�a necesario pues lo normal es que sea el UTF-8 por defecto.
            // pero de esta manera lo imponemos nosotros SIEMPRE.
        }
        catch(UnsupportedEncodingException ex)
        {
            throw new ItsNatException(ex,this);
        }
        
        String action = ItsNatServletRequestImpl.getAttrOrParam(request,"itsnat_action");
        if ((action != null)&& action.equals(ACTION_SERVLET_WEAK_UP))
            return null; // NO HACEMOS ABSOLUTAMENTE NADA, es para que se inicie en servlet y se registren
                    // los templates, �til en de-serializaci�n en GAE,
                    // el mero hecho de crear un ItsNatHttpServletRequestImpl ya carga la sesi�n, lo evitamos

        ItsNatHttpServletRequestImpl itsNatReq = createItsNatHttpServletRequest(request,response,null);
        itsNatReq.process(action,clientDocStateless);
        
        return itsNatReq;
    }
    
    public ServletRequest createServletRequest(ServletRequest request,Map<String,String[]> params)
    {
        // Nota: los tipos gen�ricos <String,String[]> son ya los claramente definidos en la spec servlet 
        // http://docs.oracle.com/javaee/6/api/javax/servlet/ServletRequest.html#getParameterMap()
        return new HttpServletRequestNewParamsImpl((HttpServletRequest)request,params);
    }
}
