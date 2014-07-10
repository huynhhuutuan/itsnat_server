package org.itsnat.droid.impl.browser.clientdoc;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.itsnat.droid.impl.browser.HttpUtil;
import org.itsnat.droid.impl.browser.ItsNatDroidBrowserImpl;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jmarranz on 10/07/14.
 */
public class EventSender
{
    protected EventManager evtManager;

    public EventSender(EventManager evtManager)
    {
        this.evtManager = evtManager;
    }

    public void requestSyncText(String servletPath,String paramURL)
    {
        ItsNatDroidBrowserImpl browser = evtManager.getItsNatDocImpl().getPageImpl().getItsNatDroidBrowserImpl();

        HttpParams httpParamsRequest = null;

        List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("params", paramURL)); // REVISAR !!!!!!!

        HttpUtil.httpPost(servletPath, browser.getHttpContext(), httpParamsRequest, browser.getHttpParams(),nameValuePairs);
    }
}
