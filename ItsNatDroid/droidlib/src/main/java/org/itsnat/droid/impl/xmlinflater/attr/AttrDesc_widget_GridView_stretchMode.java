package org.itsnat.droid.impl.xmlinflater.attr;

import android.widget.GridView;

import org.itsnat.droid.impl.xmlinflater.classtree.ClassDescViewBased;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDesc_widget_GridView_stretchMode extends AttrDescReflecSingleName
{
    static Map<String, Integer> valueMap = new HashMap<String, Integer>();
    static
    {
        valueMap.put("none", GridView.NO_STRETCH);
        valueMap.put("spacingWidth",GridView.STRETCH_SPACING);
        valueMap.put("columnWidth",GridView.STRETCH_COLUMN_WIDTH);
        valueMap.put("spacingWidthUniform",GridView.STRETCH_SPACING_UNIFORM);
    }

    public AttrDesc_widget_GridView_stretchMode(ClassDescViewBased parent)
    {
        super(parent,"stretchMode",valueMap,"columnWidth");
    }

}