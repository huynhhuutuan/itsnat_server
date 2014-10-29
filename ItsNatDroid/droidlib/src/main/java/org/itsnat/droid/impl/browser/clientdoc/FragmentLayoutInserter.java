package org.itsnat.droid.impl.browser.clientdoc;

import android.view.View;
import android.view.ViewGroup;

import org.itsnat.droid.impl.parser.LayoutParser;
import org.itsnat.droid.impl.parser.LayoutParserPage;
import org.itsnat.droid.impl.parser.TreeViewParsed;
import org.itsnat.droid.impl.parser.TreeViewParsedCache;
import org.itsnat.droid.impl.util.MapLight;
import org.itsnat.droid.impl.xmlinflater.page.InflatedLayoutPageImpl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jmarranz on 29/10/14.
 */
public class FragmentLayoutInserter
{
    public ItsNatDocImpl itsNatDoc;

    public FragmentLayoutInserter(ItsNatDocImpl itsNatDoc)
    {
        this.itsNatDoc = itsNatDoc;
    }

    public void insertFragment(ViewGroup parentView, String markup,View viewRef, List<String> scriptList)
    {
        // Preparamos primero el markup añadiendo un false parentView que luego quitamos, el false parentView es necesario
        // para declarar el namespace android, el false parentView será del mismo tipo que el de verdad para que los
        // LayoutParams se hagan bien.

        InflatedLayoutPageImpl pageLayout = itsNatDoc.getPageImpl().getInflatedLayoutPageImpl();

        StringBuilder newMarkup = new StringBuilder();

        newMarkup.append("<" + parentView.getClass().getName());

        MapLight<String, String> namespaceMap = pageLayout.getNamespacesByPrefix();
        for (Iterator<Map.Entry<String, String>> it = namespaceMap.getEntryList().iterator(); it.hasNext(); )
        {
            Map.Entry<String, String> entry = it.next();
            newMarkup.append(" xmlns:" + entry.getKey() + "=\"" + entry.getValue() + "\">");
        }

        newMarkup.append(">");
        newMarkup.append(markup);
        newMarkup.append("</" + parentView.getClass().getName() + ">");

        markup = newMarkup.toString();

        TreeViewParsed treeViewParsed;

        TreeViewParsedCache treeViewParsedCache = pageLayout.getItsNatDroidImpl().getXMLLayoutInflateService().getTreeViewParsedCache();
        TreeViewParsed cachedTreeView = treeViewParsedCache.get(markup);
        if (cachedTreeView != null)
            treeViewParsed = cachedTreeView;
        else
        {
            boolean loadingPage = false;
            LayoutParser layoutParser = new LayoutParserPage(pageLayout.getPageImpl().getItsNatServerVersion(),loadingPage);
            treeViewParsed = layoutParser.inflate(markup);
            treeViewParsedCache.put(markup,treeViewParsed);
        }

        InflatedLayoutPageImpl.fillScriptList(treeViewParsed,scriptList);

        ViewGroup falseParentView = (ViewGroup) pageLayout.insertFragment(treeViewParsed.getRootView()); // Los XML ids, los inlineHandlers etc habrán quedado memorizados
        int indexRef = viewRef != null ? InflatedLayoutPageImpl.getChildIndex(parentView,viewRef) : -1;
        while (falseParentView.getChildCount() > 0)
        {
            View child = falseParentView.getChildAt(0);
            falseParentView.removeViewAt(0);
            if (indexRef >= 0)
            {
                parentView.addView(child, indexRef);
                indexRef++;
            }
            else parentView.addView(child);
        }
    }

}
