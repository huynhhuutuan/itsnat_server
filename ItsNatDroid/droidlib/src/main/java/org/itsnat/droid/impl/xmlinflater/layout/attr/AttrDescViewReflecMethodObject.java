package org.itsnat.droid.impl.xmlinflater.layout.attr;

import android.view.View;

import org.itsnat.droid.impl.xmlinflater.layout.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.layout.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescViewReflecMethodObject extends AttrDescViewReflecMethod
{
    public AttrDescViewReflecMethodObject(ClassDescViewBased parent, String name, String methodName)
    {
        super(parent,name,methodName,getClassParam());
    }

    public AttrDescViewReflecMethodObject(ClassDescViewBased parent, String name)
    {
        super(parent,name,getClassParam());
    }

    protected static Class<?> getClassParam()
    {
        return Object.class;
    }

    public void setAttribute(View view, String value, OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        // El único caso que usa AttrDescReflecMethodObject es el atributo android:tag y sólo veo el caso de uso de ser una cadena
        CharSequence convValue = getText(value, view.getContext());
        callMethod(view, convValue);
    }

    public void removeAttribute(View view)
    {
        callMethod(view, null);
    }

}