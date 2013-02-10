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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import javax.servlet.ServletRequest;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.BrowserMSIEOld;
import org.itsnat.impl.core.browser.webkit.BrowserWebKit;
import org.itsnat.impl.core.clientdoc.ClientDocumentImpl;
import org.itsnat.impl.core.jsren.JSRenderImpl;
import org.itsnat.impl.core.servlet.ItsNatServletResponseImpl;
import org.itsnat.impl.core.req.RequestEventImpl;

/**
 *
 * @author jmarranz
 */
public abstract class ResponseEventImpl extends ResponseAlreadyLoadedDocImpl implements ResponseJavaScript
{
    protected String scriptId = null; // S�lo es no nulo en los modos SCRIPT y SCRIPT_HOLD

    public ResponseEventImpl(RequestEventImpl request)
    {
        super(request);

        ServletRequest servRequest = request.getItsNatServletRequest().getServletRequest();
        this.scriptId = servRequest.getParameter("itsnat_script_evt_id");
    }

    protected void processResponse()
    {
        try
        {
            processEvent();

            sendPendingCode();

            postSendPendingCode();
        }
        catch(RuntimeException ex)
        {
            if (isScriptOrScriptHoldMode())
            {
                // Modos SCRIPT y SCRIPT_HOLD

                // La situaci�n es la siguiente: en modos SCRIPT y SCRIPT_HOLD (no AJAX) si dejamos
                // que la excepci�n llegue al servidor de servlets dar� una respuesta de error al cliente
                // el cual obviamente no cargar�/ejecutar� el texto de la excepci�n via <script>
                // el problema es que tanto en FireFox como Opera el onload
                // no se ejecuta (MSIE no probado, en Chrome funciona) quiz�s porque consideran que
                // el onload s�lo ha de ejecutarse cuando ciertamente el script se ha cargado, la posible
                // alternativa onerror tampoco (no tengo claro ni si existe en general).

                ex.printStackTrace();

                StringWriter writer = new StringWriter();
                PrintWriter str = new PrintWriter(writer);
                ex.printStackTrace(str);
                str.flush();

                sendPendingCode(writer.toString(),true);
            }
            else // AJAX
            {
                throw ex;
            }
        }
    }

    public abstract void processEvent();

    public abstract void postSendPendingCode();

    public void sendPendingCode()
    {
        ItsNatServletResponseImpl itsNatResponse = getItsNatServletResponse();
        String code = itsNatResponse.getCodeToSendAndReset();
        sendPendingCode(code,false);
    }

    public boolean isScriptOrScriptHoldMode()
    {
        return (scriptId != null);
    }

    public void sendPendingCode(String code,boolean error)
    {
        ItsNatServletResponseImpl itsNatResponse = getItsNatServletResponse();

        if (isScriptOrScriptHoldMode())
        {
            // Modos SCRIPT y SCRIPT_HOLD

            Browser browser = getClientDocument().getBrowser();

            StringBuilder codeBuff = new StringBuilder();

            codeBuff.append("var elem = document.getElementById(\"" + scriptId + "\");\n"); // elem es el <script> cargador del script
            codeBuff.append("if (elem != null)"); // elem puede ser null cuando hay un timeout en el cliente y se ha eliminado el <script> y por alguna raz�n (extra�a) se ha cargado y ejecutado el script (REVISAR)
            codeBuff.append("{\n");

            codeBuff.append("  elem.executed = true;\n");
            if (error)
            {
                codeBuff.append("  elem.error = true;\n");
                codeBuff.append("  elem.code = " + JSRenderImpl.toTransportableStringLiteral(code,browser) + ";\n");
            }
            else
            {
                if (browser instanceof BrowserMSIEOld)
                {
                    // Esto es porque al eliminar en el cliente el <script> la funci�n JavaScript
                    // contenida se invalida
                    codeBuff.append("  elem.code = " + JSRenderImpl.toTransportableStringLiteral(code,browser) + ";\n");
                }
                else
                {
                    codeBuff.append("  elem.code = function (event,itsNatDoc)\n"); // Los mismos par�metros que processRespValid
                    codeBuff.append("   {\n");
                    codeBuff.append( code );
                    codeBuff.append("   };\n");
                }
            }

            codeBuff.append("}\n");

            code = codeBuff.toString();
        }

        if (code.length() > 0)
            writeResponse(code);
        else
        {   // Este caso obviamente s�lo se dar� en eventos AJAX
            // por si acaso lo hacemos tambi�n con eventos SCRIPT
            ClientDocumentImpl clientDoc = getClientDocument();
            Browser browser = clientDoc.getBrowser();
            if ((browser instanceof BrowserWebKit) &&
                ((BrowserWebKit)browser).isAJAXEmptyResponseFails())
            {
                writeResponse("          ");
            }
        }
    }

    public boolean isLoadByScriptElement()
    {
        return isScriptOrScriptHoldMode();
    }

}
