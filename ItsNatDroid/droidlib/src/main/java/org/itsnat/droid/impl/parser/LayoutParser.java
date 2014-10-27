package org.itsnat.droid.impl.parser;

import android.util.Xml;

import org.itsnat.droid.ItsNatDroidException;
import org.itsnat.droid.impl.util.ValueUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;


/**
 * Created by jmarranz on 27/10/14.
 */
public abstract class LayoutParser
{

    public TreeViewParsed inflate(String markup)
    {
        TreeViewParsed treeView = new TreeViewParsed(markup);
        StringReader input = new StringReader(markup);
        return inflate(input,treeView);
    }

    private TreeViewParsed inflate(Reader input,TreeViewParsed treeView)
    {
        try
        {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(input);
            inflate(parser,treeView);
            return treeView;
        }
        catch (XmlPullParserException ex) { throw new ItsNatDroidException(ex); }
        finally
        {
            try
            {
                input.close();
            }
            catch (IOException ex) { throw new ItsNatDroidException(ex); }
        }
    }

    private void inflate(XmlPullParser parser,TreeViewParsed treeView)
    {
        try
        {
            parseRootView(parser,treeView);
        }
        catch (IOException ex) { throw new ItsNatDroidException(ex); }
        catch (XmlPullParserException ex) { throw new ItsNatDroidException(ex); }
    }

    private ViewParsed parseRootView(XmlPullParser parser, TreeViewParsed treeView) throws IOException, XmlPullParserException
    {
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG) // Nodo de texto etc
                continue;

            int nsStart = parser.getNamespaceCount(parser.getDepth()-1);
            int nsEnd = parser.getNamespaceCount(parser.getDepth());
            for (int i = nsStart; i < nsEnd; i++)
            {
                String prefix = parser.getNamespacePrefix(i);
                String ns = parser.getNamespaceUri(i);
                treeView.addNamespace(prefix, ns);
            }

            if (treeView.getAndroidNSPrefix() == null)
                throw new ItsNatDroidException("Missing android namespace declaration in root element");

            String viewName = parser.getName(); // viewName lo normal es que sea un nombre corto por ej RelativeLayout

            ViewParsed rootView = createRootViewObjectAndFillAttributes(viewName,parser,treeView);

            processChildViews(parser,rootView,treeView);

            return rootView;
        }

        throw new ItsNatDroidException("INTERNAL ERROR: NO ROOT VIEW");
    }

    private ViewParsed createRootViewObjectAndFillAttributes(String viewName,XmlPullParser parser,TreeViewParsed treeView)
    {
        ViewParsed rootView = new ViewParsed(viewName,null);

        treeView.setRootView(rootView);

        fillAttributesAndAddView(parser,null,rootView);

        return rootView;
    }

    private ViewParsed parseNextView(XmlPullParser parser,ViewParsed viewParent, TreeViewParsed treeView) throws IOException, XmlPullParserException
    {
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG) // Nodo de texto etc
                continue;

            String viewName = parser.getName(); // viewName lo normal es que sea un nombre corto por ej RelativeLayout

            if (viewName.equals("script"))
            {
                parseScriptElement(parser,viewParent,treeView);
            }
            else
            {
                ViewParsed view = createViewObjectAndFillAttributesAndAdd(viewName,viewParent, parser);

                processChildViews(parser,view,treeView);

                return view;
            }
        }
        return null;
    }

    private ViewParsed createViewObjectAndFillAttributesAndAdd(String viewName,ViewParsed viewParent,XmlPullParser parser)
    {
        // viewParent es null en el caso de parseo de fragment
        ViewParsed view = new ViewParsed(viewName,viewParent);

        fillAttributesAndAddView(parser,viewParent,view);

        return view;
    }

    private void fillAttributesAndAddView(XmlPullParser parser,ViewParsed viewParsedParent,ViewParsed view)
    {
        fillViewAttributes(parser,view);
        if (viewParsedParent != null) viewParsedParent.addChildView(view);
    }

    private void fillViewAttributes(XmlPullParser parser,ViewParsed viewParsed)
    {
        int len = parser.getAttributeCount();
        viewParsed.initAttribList(len);
        for(int i = 0; i < len; i++)
        {
            String namespaceURI = parser.getAttributeNamespace(i);
            if ("".equals(namespaceURI)) namespaceURI = null; // Por estandarizar
            String name = parser.getAttributeName(i); // El nombre devuelto no contiene el namespace
            String value = parser.getAttributeValue(i);
            if (viewParsed.getStyleAttr() == null && (namespaceURI == null) && "style".equals(name))
                viewParsed.setStyleAttr(value);
            else
                viewParsed.addAttribute(namespaceURI, name, value);
        }
    }

    private void processChildViews(XmlPullParser parser,ViewParsed viewParent, TreeViewParsed treeView) throws IOException, XmlPullParserException
    {
        ViewParsed childView = parseNextView(parser, viewParent,treeView);
        while (childView != null)
        {
            childView = parseNextView(parser, viewParent,treeView);
        }
    }

    public static String findAttributeFromParser(String namespaceURI, String name, XmlPullParser parser)
    {
        for(int i = 0; i < parser.getAttributeCount(); i++)
        {
            String currNamespaceURI = parser.getAttributeNamespace(i);
            if ("".equals(currNamespaceURI)) currNamespaceURI = null; // Por estandarizar
            if (!ValueUtil.equalsNullAllowed(currNamespaceURI, namespaceURI)) continue;
            String currName = parser.getAttributeName(i); // El nombre devuelto no contiene el namespace
            if (!name.equals(currName)) continue;
            String value = parser.getAttributeValue(i);
            return value;
        }
        return null;
    }

    protected abstract void parseScriptElement(XmlPullParser parser,ViewParsed viewParent, TreeViewParsed treeView) throws IOException, XmlPullParserException;
}