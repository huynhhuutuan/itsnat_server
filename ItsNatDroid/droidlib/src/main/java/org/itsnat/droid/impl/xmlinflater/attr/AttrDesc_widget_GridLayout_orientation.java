package org.itsnat.droid.impl.xmlinflater.attr;

import android.widget.GridLayout;

import org.itsnat.droid.impl.xmlinflater.classtree.ClassDescViewBased;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDesc_widget_GridLayout_orientation extends AttrDescReflecSingleName
{
    static Map<String, Integer> valueMap = new HashMap<String, Integer>( 2 );
    static
    {
        valueMap.put("horizontal", GridLayout.HORIZONTAL);
        valueMap.put("vertical",GridLayout.VERTICAL);
    }

    public AttrDesc_widget_GridLayout_orientation(ClassDescViewBased parent)
    {
        super(parent,"orientation",valueMap,"horizontal");
    }

}
