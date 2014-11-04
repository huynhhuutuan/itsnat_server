package org.itsnat.droid.impl.xmlinflater.layout.attr;

import android.view.View;

import org.itsnat.droid.impl.xmlinflater.layout.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.layout.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescReflecMethodString extends AttrDescReflecMethod
{
    protected String defaultValue;

    public AttrDescReflecMethodString(ClassDescViewBased parent, String name, String methodName,String defaultValue)
    {
        super(parent,name,methodName,getClassParam());
        this.defaultValue = defaultValue;
    }

    public AttrDescReflecMethodString(ClassDescViewBased parent, String name,String defaultValue)
    {
        super(parent,name,getClassParam());
        this.defaultValue = defaultValue;
    }

    protected static Class<?> getClassParam()
    {
        return String.class;
    }

    public void setAttribute(View view, String value, OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        String convValue = getString(value, view.getContext());
        callMethod(view, convValue);
    }

    public void removeAttribute(View view)
    {
        callMethod(view, "");
    }

}