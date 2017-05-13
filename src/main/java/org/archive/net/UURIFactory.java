package org.archive.net;

import org.apache.commons.httpclient.URIException;
import org.archive.url.UsableURI;
import org.archive.url.UsableURIFactory;

/**
 * Factory that returns UURIs. Mostly wraps {@link UsableURIFactory}.
 * 
 */
/**
 * 返回url链接
 * 
 * @author handongming
 *
 */
public class UURIFactory extends UsableURIFactory {
        
    private static final long serialVersionUID = -7969477276065915936L;
    
    private static final UURIFactory factory = new UURIFactory();

    public static UURI getInstance(String uri) throws URIException {
        return (UURI) UURIFactory.factory.create(uri);
    }

    public static UURI getInstance(UURI base, String relative) throws URIException {
        return (UURI) UURIFactory.factory.create(base, relative);
    }

    @Override
    protected UURI makeOne(String fixedUpUri, boolean escaped, String charset)throws URIException {
        return new UURI(fixedUpUri, escaped, charset);
    }
    
    @Override
    protected UsableURI makeOne(UsableURI base, UsableURI relative) throws URIException {
        return new UURI(base, relative);
    }

}
