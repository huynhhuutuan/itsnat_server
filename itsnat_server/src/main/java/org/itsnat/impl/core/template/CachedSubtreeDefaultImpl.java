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

package org.itsnat.impl.core.template;

/**
 *
 * @author jmarranz
 */
public class CachedSubtreeDefaultImpl extends CachedSubtreeImpl
{
    public CachedSubtreeDefaultImpl(MarkupTemplateVersionImpl template,String code)
    {
        super(template,code);
    }

    public String getCode(boolean resolveEntities)
    {
        // Ignoramos resolveEntities s�lo tiene sentido en nodos de texto
        // puesto que en sub�rboles con un elemento padre el c�digo cacheado se env�a o bien como markup
        // en carga o bien dentro de innerHTML (o setInnerXML que internamente parsea)
        // por lo que los posibles entities tipo &amp; de los nodos de texto ser�n resueltos.
        // En caso de texto de comentarios no habr� tales entities obviamente.
        return markup;
    }
}
