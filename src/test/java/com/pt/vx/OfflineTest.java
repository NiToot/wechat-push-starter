package com.pt.vx;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import java.security.Permission;

/** Fail closed: a missing test stub must never contact real services. */
public abstract class OfflineTest {
    private static SecurityManager previous;
    @BeforeClass public static void blockNetwork() {
        // Initialize JVM instrumentation before the guard; no business code runs here.
        org.mockito.Mockito.mock(Object.class);
        previous = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {
            @Override public void checkPermission(Permission permission) { }
            @Override public void checkConnect(String host, int port) {
                throw new AssertionError("Offline test attempted network: " + host);
            }
        });
    }
    @AfterClass public static void restoreNetworkPolicy() {
        System.setSecurityManager(previous);
    }
}
