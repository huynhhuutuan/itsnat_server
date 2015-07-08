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

package org.itsnat.impl.comp.inplace;

import org.itsnat.comp.ItsNatComponent;
import org.itsnat.impl.core.browser.web.BrowserWeb;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author jmarranz
 */
public class ItsNatHTMLCellEditorClientOperaOldMiniImpl extends ItsNatCellEditorClientImpl
{
    protected static final ItsNatHTMLCellEditorClientOperaOldMiniImpl SINGLETON = new ItsNatHTMLCellEditorClientOperaOldMiniImpl();

    public ItsNatHTMLCellEditorClientOperaOldMiniImpl()
    {
    }

    @Override
    public void registerEventListeners(ItsNatCellEditorImpl compParent,ClientDocumentStfulDelegateWebImpl clientDocDeleg)
    {
        super.registerEventListeners(compParent,clientDocDeleg);

        // Opera Mini no recibe clicks a nivel de documento, por ello a�adimos adem�s el siguiente truco.
        // El definir el z-index no vale para nada en Opera Mini (el position:absolute s�) pero con as� tambi�n funcionar�a con FireFox etc
        // por ello lo dejamos por si en futuras versiones fuera m�s est�ndar el Opera Mini.
        // Aunque Opera Mini permite editar controles form distintos al editor inplace aunque haya un layer en teor�a "encima" (ya sabemos que el z-index es ignorado)
        // antes de emitir el change se emite antes un click en el layer y por tanto se ejecuta el blur
        // antes de que se ejecute el change. Si se detectara alg�n caso en que no fuera as�
        // podr�amos usar un event listener global a nivel de documento en el servidor para quitar
        // el editor antes de procesar el evento.
        // No es necesario c�digo de "desregistro" porque si el editor
        // se ha eliminado por otra v�a el editor.dispatchEvent() no hace nada y el registro de DOMNodeRemovedFromDocument
        // obviamente se pierde (pues el elemento en el cliente no se reutiliza de acuerdo con las reglas de ItsNat aunque no sea as� en el servidor)
        // y al ejecutarse el mutation se elimina el layer de utilidad en cualquier caso.        

        // El recibir eventos a nivel de documento es �til para salir del edit inplace por ejemplo        
        
        StringBuilder code = new StringBuilder();
        String method = "operaMiniInitInplaceEditor";
        if (!clientDocDeleg.isClientMethodBounded(method))
        {
            code.append( "var func = function (editor)" );
            code.append( "{" );
            code.append( "  var topElem = itsNatDoc.doc.documentElement;" );
            code.append( "  var layer = itsNatDoc.doc.createElement('span');" );
            code.append( "  var zIndex = 999999;");
            code.append( "  layer.setAttribute('style','position:absolute; z-index:' + zIndex + '; margin:0; padding:0; top:0; left:0; width:' + topElem.scrollWidth + 'px; height:' + topElem.scrollHeight + 'px;');" ); // Para testeo:  border:3px solid blue;
            code.append( "  editor.style.position = 'absolute';" );
            code.append( "  editor.style.zIndex = zIndex + 1;" );         
            code.append( "  itsNatDoc.doc.body.appendChild(layer);" );             
            code.append( "  var listener = function (evt)" );
            code.append( "  {" );
            code.append( "    if (evt.target != editor)" );
            code.append( "    {" );
            code.append( "       var evtTmp = itsNatDoc.doc.createEvent('HTMLEvents');" );
            code.append( "       evtTmp.initEvent('blur',true,true);" );
            code.append( "       editor.dispatchEvent(evtTmp);" );
            code.append( "    }" );
            code.append( "  };" );
            code.append( "  layer.setAttribute('onclick','');" ); // Esto es para evitar el error de Opera Mini de los listeners-atributos            
            code.append( "  layer.addEventListener('click',listener,true);" );
            code.append( "  var listener = function (evt)" );
            code.append( "  {" );
            code.append( "    itsNatDoc.doc.body.removeChild(layer);" );
            code.append( "  };" );
            code.append( "  editor.addEventListener('DOMNodeRemovedFromDocument',listener,false);" );
            code.append( "};" );
            code.append( "itsNatDoc." + method + " = func;\n" );
            clientDocDeleg.bindClientMethod(method);
        }
        
        ItsNatComponent compEditor = compParent.getCellEditorComponent();     
        
        Element nodeEditor = (Element)compEditor.getNode(); // S�lo admitimos elementos por ahora        
        
        code.append( "var nodeEditor = " + clientDocDeleg.getNodeReference(nodeEditor,true,true) + ";" );        
        
        code.append( "itsNatDoc." + method + "(nodeEditor);\n" );

        clientDocDeleg.addCodeToSend(code.toString());
    }
}
