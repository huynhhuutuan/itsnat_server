package org.itsnat.droid.impl.xmlinflater.classtree;

import org.itsnat.droid.impl.xmlinflater.attr.AttrDescReflecMethodBoolean;

/**
 * Created by jmarranz on 30/04/14.
 */
public class ClassDesc_widget_HorizontalScrollView extends ClassDescViewBased
{
    public ClassDesc_widget_HorizontalScrollView(ClassDesc_widget_FrameLayout parentClass)
    {
        super("android.widget.HorizontalScrollView",parentClass);
    }

    protected void init()
    {
        super.init();

        addAttrDesc(new AttrDescReflecMethodBoolean(this,"fillViewport",false));
    }
}

