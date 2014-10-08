package org.itsnat.droid.impl.xmlinflater.classtree;

import org.itsnat.droid.impl.xmlinflater.ClassDescViewMgr;
import org.itsnat.droid.impl.xmlinflater.attr.AttrDescReflecMethodBoolean;
import org.itsnat.droid.impl.xmlinflater.attr.AttrDescReflecMethodCharSequence;
import org.itsnat.droid.impl.xmlinflater.attr.AttrDescReflecMethodDimensionInt;
import org.itsnat.droid.impl.xmlinflater.attr.AttrDescReflecMethodMultipleName;
import org.itsnat.droid.impl.xmlinflater.attr.ImeOptionsUtil;
import org.itsnat.droid.impl.xmlinflater.attr.InputTypeUtil;

/**
 * Created by jmarranz on 30/04/14.
 */
public class ClassDesc_widget_SearchView extends ClassDescViewBased
{
    public ClassDesc_widget_SearchView(ClassDescViewMgr classMgr, ClassDesc_widget_LinearLayout parentClass)
    {
        super(classMgr,"android.widget.SearchView",parentClass);
    }

    protected void init()
    {
        super.init();

        addAttrDesc(new AttrDescReflecMethodBoolean(this,"iconifiedByDefault",true));
        addAttrDesc(new AttrDescReflecMethodMultipleName(this,"imeOptions", ImeOptionsUtil.valueMap,"actionUnspecified"));
        addAttrDesc(new AttrDescReflecMethodMultipleName(this,"inputType", InputTypeUtil.valueMap,"text")); // No estoy seguro que el valor por defecto sea "text" pero parece el más razonable
        addAttrDesc(new AttrDescReflecMethodDimensionInt(this,"maxWidth",0f));
        addAttrDesc(new AttrDescReflecMethodCharSequence(this,"queryHint",""));
    }
}
