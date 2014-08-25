package org.itsnat.droid.impl.browser;

import android.content.Context;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;

import org.itsnat.droid.AttrCustomInflaterListener;
import org.itsnat.droid.ItsNatDroidException;
import org.itsnat.droid.impl.xmlinflater.InflatedLayoutImpl;
import org.itsnat.droid.impl.browser.clientdoc.DroidEventGroupInfo;
import org.itsnat.droid.impl.browser.clientdoc.ItsNatViewImpl;
import org.itsnat.droid.impl.browser.clientdoc.ItsNatViewNotNullImpl;
import org.itsnat.droid.impl.util.MapLight;
import org.itsnat.droid.impl.util.ValueUtil;
import org.itsnat.droid.impl.xmlinflater.ClassDescViewMgr;
import org.itsnat.droid.impl.xmlinflater.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.classtree.ClassDescViewBased;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jmarranz on 20/08/14.
 */
public class InflatedLayoutPageImpl extends InflatedLayoutImpl
{
    protected PageImpl page;

    public InflatedLayoutPageImpl(PageImpl page, AttrCustomInflaterListener inflateListener, Context ctx)
    {
        super(page.getItsNatDroidBrowserImpl().getItsNatDroidImpl(), inflateListener, ctx);
        this.page = page;
    }

    public PageImpl getPageImpl()
    {
        return page;
    }


    public void insertFragment(View parentView, String markup, String[] loadScript, List<String> scriptList)
    {
        if (page == null) throw new ItsNatDroidException("INTERNAL ERROR");

        // Preparamos primero el markup añadiendo un false parentView que luego quitamos, el false parentView es necesario
        // para declarar el namespace android, el false parentView será del mismo tipo que el de verdad para que los
        // LayoutParams se hagan bien

        StringBuilder newMarkup = new StringBuilder();

        newMarkup.append("<" + parentView.getClass().getName());

        MapLight<String, String> namespaceMap = getNamespacesByPrefix();
        for (Iterator<Map.Entry<String, String>> it = namespaceMap.getEntryList().iterator(); it.hasNext(); )
        {
            Map.Entry<String, String> entry = it.next();
            newMarkup.append(" xmlns:" + entry.getKey() + "=\"" + entry.getValue() + "\">");
        }
        newMarkup.append(">");
        newMarkup.append(markup);
        newMarkup.append("</" + parentView.getClass().getName() + ">");

        markup = newMarkup.toString();

        XmlPullParser parser = Xml.newPullParser();
        try
        {
            StringReader input = new StringReader(markup);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(input);

            ViewGroup falseParentView = (ViewGroup) parseNextView(parser, null, loadScript, scriptList); // Los XML ids, los inlineHandlers etc habrán quedado memorizados
            while (falseParentView.getChildCount() > 0)
            {
                View child = falseParentView.getChildAt(0);
                falseParentView.removeViewAt(0);
                ((ViewGroup) parentView).addView(child);
            }
        }
        catch (XmlPullParserException ex)
        {
            throw new ItsNatDroidException(ex);
        }
        catch (IOException ex)
        {
            throw new ItsNatDroidException(ex);
        }
    }

    protected void parseScriptElement(XmlPullParser parser, View viewParent, String[] loadScript, List<String> scriptList) throws IOException, XmlPullParserException
    {
        boolean isLoadScript = parser.getAttributeCount() == 1 &&
                "id".equals(parser.getAttributeName(0)) &&
                "itsnat_load_script".equals(parser.getAttributeValue(0));

        while (parser.next() != XmlPullParser.TEXT) /*nop*/ ;

        String code = parser.getText();
        if (isLoadScript) loadScript[0] = code;
        else scriptList.add(code);

        while (parser.next() != XmlPullParser.END_TAG) /*nop*/ ;
    }

    public void setAttribute(View view, String namespaceURI, String name, String value)
    {
        ClassDescViewMgr classDescViewMgr = getXMLLayoutInflateService().getClassDescViewMgr();
        ClassDescViewBased viewClassDesc = classDescViewMgr.get(view);
        setAttribute(viewClassDesc, view, namespaceURI, name, value, null);
    }

    public void removeAttribute(View view, String namespaceURI, String name)
    {
        ClassDescViewMgr viewMgr = page.getInflatedLayoutPageImpl().getXMLLayoutInflateService().getClassDescViewMgr();
        ClassDescViewBased viewClassDesc = viewMgr.get(view);
        removeAttribute(viewClassDesc, view, namespaceURI, name);
    }

    public boolean setAttribute(ClassDescViewBased viewClassDesc, View view, String namespaceURI, String name, String value, OneTimeAttrProcess oneTimeAttrProcess)
    {
        if (ValueUtil.isEmpty(namespaceURI))
        {
            String type = getTypeInlineEventHandler(name);
            if (type != null)
            {
                ItsNatViewImpl viewData = getItsNatViewOfInlineHandler(type,view);
                viewData.setOnTypeInlineCode(name, value);
                if (viewData instanceof ItsNatViewNotNullImpl)
                    ((ItsNatViewNotNullImpl) viewData).registerEventListenerViewAdapter(type);

                return true;
            }
            else
                return super.setAttribute(viewClassDesc, view, namespaceURI, name, value, oneTimeAttrProcess);
        }
        else
        {
            return super.setAttribute(viewClassDesc, view, namespaceURI, name, value, oneTimeAttrProcess);
        }
    }

    protected boolean removeAttribute(ClassDescViewBased viewClassDesc, View view, String namespaceURI, String name)
    {
        if (ValueUtil.isEmpty(namespaceURI))
        {
            String type = getTypeInlineEventHandler(name);
            if (type != null)
            {
                ItsNatViewImpl viewData = getItsNatViewOfInlineHandler(type,view);
                viewData.removeOnTypeInlineCode(name);

                return true;
            }
            else return viewClassDesc.removeAttribute(view, namespaceURI, name, page.getInflatedLayoutPageImpl());
        }
        else
        {
            return viewClassDesc.removeAttribute(view, namespaceURI, name, page.getInflatedLayoutPageImpl());
        }
    }

    private ItsNatViewImpl getItsNatViewOfInlineHandler(String type,View view)
    {
        if (type.equals("load") || type.equals("unload"))
        {
            // El handler inline de load o unload sólo se puede poner una vez por layout por lo que obligamos
            // a que sea el View root de forma similar al <body> en HTML
            if (view != getRootView())
                throw new ItsNatDroidException("onload/onunload handlers only can be defined in the view root of the layout");
        }
        return page.getItsNatViewImpl(view);
    }

    private String getTypeInlineEventHandler(String name)
    {
        if (!name.startsWith("on")) return null;
        String type = name.substring(2);
        DroidEventGroupInfo eventGroup = DroidEventGroupInfo.getEventGroupInfo(type);
        if (eventGroup.getEventGroupCode() == DroidEventGroupInfo.UNKNOWN_EVENT)
            return null;
        return type;
    }
}