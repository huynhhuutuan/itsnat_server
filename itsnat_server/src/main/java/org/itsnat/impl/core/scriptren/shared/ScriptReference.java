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

package org.itsnat.impl.core.scriptren.shared;

/**
 * Esta interface ser� p�blica cuando se haga un modelo completo
 * de metaprogramaci�n JavaScript en Java.
 *
 * @author jmarranz
 */
public interface ScriptReference
{
    public String getCode();
    public ScriptReference setProperty(String propName,Object newValue,boolean endSentence);
    public ScriptReference getProperty(String propName,boolean endSentence);
    public ScriptReference callMethod(String methodName,Object[] params,boolean endSentence);
}
