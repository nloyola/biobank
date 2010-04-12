package edu.ualberta.med.biobank.common;

import java.net.URL;

import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.client.ApplicationServiceProvider;

public class ServiceConnection {

    /**
     * if not null, called to resolved the address
     */
    private static ResourceResolver resourceResolver;

    private static String TRUST_STORE_PROPERTY_NAME = "javax.net.ssl.trustStore";

    private static String KEYSTORE_FILE_PATH = "cert/all.keystore";

    public static WritableApplicationService getAppService(String serverUrl,
        String userName, String password) throws Exception {
        if (serverUrl.startsWith("https")
            && System.getProperty(TRUST_STORE_PROPERTY_NAME) == null) {
            setTrustStore();
        }
        if (userName == null) {
            return (WritableApplicationService) ApplicationServiceProvider
                .getApplicationServiceFromUrl(serverUrl);
        }
        return (WritableApplicationService) ApplicationServiceProvider
            .getApplicationServiceFromUrl(serverUrl, userName, password);
    }

    public static void setTrustStore() throws Exception {
        URL url = ServiceConnection.class.getResource(KEYSTORE_FILE_PATH);
        if (url != null) {
            if (resourceResolver != null) {
                url = resourceResolver.resolveURL(url);
            }
            System.setProperty(TRUST_STORE_PROPERTY_NAME, url.getFile());
        }
    }

    public static WritableApplicationService getAppService(String serverUrl)
        throws Exception {
        return getAppService(serverUrl, null, null);
    }

    public static void setResourceResolver(ResourceResolver resourceResolver) {
        ServiceConnection.resourceResolver = resourceResolver;
    }

    public static ResourceResolver getResourceResolver() {
        return resourceResolver;
    }
}
