package org.itsnat.droid.impl.xmlinflater.layout.classtree;

import org.itsnat.droid.impl.xmlinflater.layout.ClassDescViewMgr;
import org.itsnat.droid.impl.xmlinflater.layout.attr.AttrDescReflecMethodBoolean;
import org.itsnat.droid.impl.xmlinflater.layout.attr.AttrDescReflecFieldSetBoolean;
import org.itsnat.droid.impl.xmlinflater.layout.attr.widget.AttrDesc_widget_AdapterViewAnimator_inAnimation;
import org.itsnat.droid.impl.xmlinflater.layout.attr.widget.AttrDesc_widget_AdapterViewAnimator_outAnimation;

/**
 * Created by jmarranz on 30/04/14.
 */
public class ClassDesc_widget_AdapterViewAnimator extends ClassDescViewBased
{
    public ClassDesc_widget_AdapterViewAnimator(ClassDescViewMgr classMgr,ClassDescViewBased parentClass)
    {
        super(classMgr,"android.widget.AdapterViewAnimator",parentClass);
    }

    protected void init()
    {
        super.init();

        addAttrDesc(new AttrDescReflecMethodBoolean(this,"animateFirstView",true));
        addAttrDesc(new AttrDesc_widget_AdapterViewAnimator_inAnimation(this));
        addAttrDesc(new AttrDescReflecFieldSetBoolean(this,"loopViews","mLoopViews",false));
        addAttrDesc(new AttrDesc_widget_AdapterViewAnimator_outAnimation(this));
    }
}
