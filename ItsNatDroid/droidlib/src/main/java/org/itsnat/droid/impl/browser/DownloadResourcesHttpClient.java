package org.itsnat.droid.impl.browser;

import android.content.res.AssetManager;
import android.os.AsyncTask;

import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.itsnat.droid.ItsNatDroidException;
import org.itsnat.droid.impl.browser.serveritsnat.ItsNatDocImpl;
import org.itsnat.droid.impl.dom.DOMAttrRemote;
import org.itsnat.droid.impl.xmlinflater.XMLInflateRegistry;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by jmarranz on 9/10/14.
 */
public class DownloadResourcesHttpClient extends GenericHttpClientBaseImpl
{
    public DownloadResourcesHttpClient(ItsNatDocImpl itsNatDoc)
    {
        super(itsNatDoc);
    }

    public List<HttpRequestResultImpl> request(DOMAttrRemote attrRemote,boolean async)
    {
        List<DOMAttrRemote> attrRemoteList = new LinkedList<DOMAttrRemote>();
        attrRemoteList.add(attrRemote);

        return request(attrRemoteList,async);
    }

    public List<HttpRequestResultImpl> request(List<DOMAttrRemote> attrRemoteList,boolean async)
    {
        if (async)
        {
            requestAsync(attrRemoteList);
            return null;
        }
        else return requestSync(attrRemoteList);
    }

    public List<HttpRequestResultImpl> requestSync(List<DOMAttrRemote> attrRemoteList)
    {
        PageImpl page = getPageImpl();
        ItsNatDroidBrowserImpl browser = page.getItsNatDroidBrowserImpl();

        // No hace falta clonar porque es síncrona la llamada
        String url = getFinalURL();

        HttpRequestData httpRequestData = new HttpRequestData(page);

        XMLInflateRegistry xmlInflateRegistry = browser.getItsNatDroidImpl().getXMLInflateRegistry();

        AssetManager assetManager = page.getContext().getResources().getAssets();

        List<HttpRequestResultImpl> resultList = new LinkedList<HttpRequestResultImpl>();

        try
        {
            HttpResourceDownloader resDownloader =
                    new HttpResourceDownloader(url,httpRequestData,xmlInflateRegistry,assetManager);
            resDownloader.downloadResources(attrRemoteList,resultList);
        }
        catch (Exception ex)
        {
            ItsNatDroidException exFinal = convertException(ex);
            throw exFinal;

            // No usamos aquí el OnEventErrorListener porque la excepción es capturada por un catch anterior que sí lo hace
        }

        for(HttpRequestResultImpl result : resultList)
            processResult(result,listener,errorMode);

        return resultList;
    }

    public void requestAsync(List<DOMAttrRemote> attrRemoteList)
    {
        String url = getFinalURL();
        AssetManager assetManager = itsNatDoc.getPageImpl().getContext().getResources().getAssets();
        HttpDownloadResourcesAsyncTask task = new HttpDownloadResourcesAsyncTask(attrRemoteList,this,method,url,httpParamsRequest,listener,errorListener,errorMode,assetManager);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // Con execute() a secas se ejecuta en un "pool" de un sólo hilo sin verdadero paralelismo
    }

}
