package org.itsnat.droid.impl.xmlinflater.classtree;

import org.itsnat.droid.impl.xmlinflater.ClassDescViewMgr;
import org.itsnat.droid.impl.xmlinflater.attr.widget.AttrDesc_widget_RadioGroup_checkedButton;
import org.itsnat.droid.impl.xmlinflater.attr.widget.AttrDesc_widget_RadioGroup_orientation;

/**
 * Created by jmarranz on 30/04/14.
 */
public class ClassDesc_widget_RadioGroup extends ClassDescViewBased
{
    public ClassDesc_widget_RadioGroup(ClassDescViewMgr classMgr,ClassDesc_widget_LinearLayout parentClass)
    {
        super(classMgr,"android.widget.RadioGroup",parentClass);
    }

    protected void init()
    {
        super.init();

        addAttrDesc(new AttrDesc_widget_RadioGroup_checkedButton(this));
        addAttrDesc(new AttrDesc_widget_RadioGroup_orientation(this));
    }
}
