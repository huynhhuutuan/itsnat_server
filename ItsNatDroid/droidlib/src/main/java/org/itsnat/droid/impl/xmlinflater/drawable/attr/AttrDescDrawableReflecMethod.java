package org.itsnat.droid.impl.xmlinflater.drawable.attr;

import android.graphics.drawable.Drawable;

import org.itsnat.droid.impl.xmlinflater.MethodContainer;
import org.itsnat.droid.impl.xmlinflater.drawable.classtree.ClassDescDrawable;

/**
 * Created by jmarranz on 30/04/14.
 */
public abstract class AttrDescDrawableReflecMethod extends AttrDescDrawable
{
    protected MethodContainer method;

    public AttrDescDrawableReflecMethod(ClassDescDrawable parent, String name, String methodName, Class classParam)
    {
        super(parent,name);
        this.method = new MethodContainer(parent,methodName,classParam != null ? new Class[]{classParam} : null);
    }

    public AttrDescDrawableReflecMethod(ClassDescDrawable parent, String name, Class classParam)
    {
        this(parent,name,genMethodName(name),classParam);
    }

    public static String genMethodName(String name)
    {
        return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    protected void callMethod(Drawable drawable, Object convertedValue)
    {
        method.invoke(drawable, convertedValue);
    }

}
