package org.demo.util;

import io.orkes.conductor.client.ApiClient;

public class ClientUtil {
    private static final String ENV_ROOT_URI = "http://localhost:8080/api";
    private static final String ENV_KEY_ID = "a0d93301-bd35-11ef-b4ec-daddee823c0a";
    private static final String ENV_SECRET = "lAOiw9el3DfmUMwIaIXruncxomfvvWxSi5UmxbIIOK64jd7i";
    private static final ApiClient CLIENT = getClient();

    public static ApiClient getClient() {
        if (CLIENT != null) {
            return CLIENT;
        }
        return new ApiClient(ENV_ROOT_URI, ENV_KEY_ID, ENV_SECRET);
    }
}

