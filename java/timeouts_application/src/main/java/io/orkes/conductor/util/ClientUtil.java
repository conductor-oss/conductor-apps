package io.orkes.conductor.util;

import io.orkes.conductor.client.ApiClient;

public class ClientUtil {
    private static final String ENV_ROOT_URI = "ENV_ROOT_URI";
    private static final String ENV_KEY_ID = "ENV_KEY_ID";
    private static final String ENV_SECRET = "ENV_SECRET";
    private static final ApiClient CLIENT = getClient();

    public static ApiClient getClient() {
        if (CLIENT != null) {
            return CLIENT;
        }
        return new ApiClient(ENV_ROOT_URI, ENV_KEY_ID, ENV_SECRET);
    }
}

