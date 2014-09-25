package org.itsnat.droid.impl.xmlinflater.attr.widget;

import android.widget.AbsListView;

import org.itsnat.droid.impl.xmlinflater.attr.AttrDescReflecMethodSingleName;
import org.itsnat.droid.impl.xmlinflater.classtree.ClassDescViewBased;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDesc_widget_AbsListView_transcriptMode extends AttrDescReflecMethodSingleName
{
    static Map<String, Integer> valueMap = new HashMap<String, Integer>( 3 );
    static
    {
        valueMap.put("disabled", AbsListView.TRANSCRIPT_MODE_DISABLED);
        valueMap.put("normal",AbsListView.TRANSCRIPT_MODE_NORMAL);
        valueMap.put("alwaysScroll",AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
    }

    public AttrDesc_widget_AbsListView_transcriptMode(ClassDescViewBased parent)
    {
        super(parent,"transcriptMode",int.class,valueMap,"disabled");
    }


}