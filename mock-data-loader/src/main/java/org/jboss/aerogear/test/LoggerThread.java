package org.jboss.aerogear.test;

import org.slf4j.Logger;

/**
 * Logger thread. This is used only to show some progress in the old fashion cli way
 */
public class LoggerThread extends Thread {

    private int totalApps;
    private int totalVariants;
    private int totalTokens;

    private boolean keepPolling = true;

    private final Logger LOG;

    public LoggerThread(Logger logger, final int totalApps, final int totalVariants, final int totalTokens) {
        LoggerThread.this.totalApps = totalApps;
        LoggerThread.this.totalVariants = totalVariants;
        LoggerThread.this.totalTokens = totalTokens;
        this.LOG = logger;
    }

    private int currentAppProgress = 0;
    private int currentAppFailed = 0;

    private int currentVariant = 0;
    private int failedVariants = 0;
    private int currentToken = 0;
    private int failedToken = 0;

    /**
     * Ends the polling loop
     */
    public void shutdown() {
        keepPolling = false;
    }

    /**
     * Increments the count of elaborated variants for the current app
     * @param failed if <code>true</code> increments the counter for failed variant creation
     */
    public synchronized void variantElaborated(boolean failed, Throwable thr) {
        if (failed) {
            LOG.error("Failure creating variant: {}", new Object[]{thr.getMessage()}, thr);
            failedVariants ++;
        } else {
            currentVariant++;
        }
    }

    /**
     * Increments the count of elaborated tokens for the current app
     * @param failed if <code>true</code> increments the counter for failed tokens creation
     */
    public synchronized void tokenElaborated(boolean failed, Throwable thr) {
        if (failed) {
            LOG.error("Failure creating token: {}", new Object[]{thr.getMessage()}, thr);
            failedToken ++;
        } else {
            currentToken++;
        }
    }

    /**
     * Increments the count of elaborated apps
     * @param failed if <code>true</code> increments the counter for failed apps creation
     */
    public synchronized  void appElaborated(boolean failed, Throwable thr) {
        System.out.println();
        if (failed) {
            LOG.error("Failure creating app: {}", new Object[]{thr.getMessage()}, thr);
            currentAppFailed ++;
        } else {
            currentAppProgress++;
        }
        reset();
    }

    /**
     * Resets the counters for tokens and variants
     */
    private synchronized void reset() {
        printUpdate();
        currentToken = currentVariant = failedToken = failedVariants = 0;
    }

    /**
     * Print a progress update
     */
    private synchronized void printUpdate() {
        System.out.printf("\rApps created/failed/total: %3d/%3d/%3d - Variants created/failed/total: %3d/%3d/%3d - Tokens created/failed/total: %5d/%5d/%5d",
            currentAppProgress, currentAppFailed, totalApps,
            currentVariant, failedVariants, totalVariants,
            currentToken, failedToken, totalTokens * totalVariants);
    }

    /**
     * Polls for updates
     */
    @Override
    public void run() {
        while (keepPolling) {
            try {
                Thread.sleep(100);
                printUpdate();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        System.out.println();
    }
}
