package org.jboss.aerogear.test;

import at.ftec.aerogear.api.AerogearAdminService;
import at.ftec.aerogear.api.impl.DefaultAerogearAdminService;
import at.ftec.aerogear.exception.AerogearHelperException;
import at.ftec.aerogear.model.PushServer;
import org.apache.commons.cli.*;
import org.jboss.aerogear.test.builders.AndroidVariantBuilder;
import org.jboss.aerogear.test.builders.PushApplicationBuilder;
import org.jboss.aerogear.test.builders.VariantBuilder;
import org.jboss.aerogear.unifiedpush.api.*;

import java.util.UUID;

/**
 * Utility class to create mock data into the UPS server
 */
public class MockDataLoader {

    private static final String OPTION_APPS = "apps";
    private static final String OPTION_TOKENS = "tokens";
    private static final String OPTION_VARIANTS = "variants";
    private static final String OPTION_USERNAME = "username";
    private static final String OPTION_PASSWORD = "password";
    private static final String OPTION_CLIENTID = "clientid";
    private static final String OPTION_URL = "url";

    private static final String DEFAULT_CLIENT_ID = "unified-push-server-js";
    private static final String DEFAULT_URL = "http://localhost:8080";

    private static final String PUSHAPPID_PATTERN = "PUSHAPPID_%d";
    private static final String APPNAME_PATTERN = "Test App %d";
    private static final String DEVELOPER_PATTERN = "Developer %d";
    private static final String DESCRIPTION_PATTERN = "Test App %d";
    private static final String MASTER_SECRET = "Shhh!!! Don't tell anyone!!";

    private static final String VARIANT_DESC_PATTERN = "Variant %d for %s";
    private static final String VARIANT_DEVELOPER = "Mock Developer";
    private static final String VARIANT_NAME_PATTERN = "Variant-%s-%d";

    private static LoggerThread logger = null;

    /**
     * Logger thread. This is used only to show some progress in the old fashion cli way
     */
    private static class LoggerThread extends Thread {

        private int totalApps;
        private int totalVariants;
        private int totalTokens;

        private boolean keepPolling = true;

        public LoggerThread(final int totalApps, final int totalVariants, final int totalTokens) {
            LoggerThread.this.totalApps = totalApps;
            LoggerThread.this.totalVariants = totalVariants;
            LoggerThread.this.totalTokens = totalTokens;
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
        public synchronized void variantElaborated(boolean failed) {
            if (failed) {
                failedVariants ++;
            } else {
                currentVariant++;
            }
        }

        /**
         * Increments the count of elaborated tokens for the current app
         * @param failed if <code>true</code> increments the counter for failed tokens creation
         */
        public synchronized void tokenElaborated(boolean failed) {
            if (failed) {
                failedToken ++;
            } else {
                currentToken++;
            }
        }

        /**
         * Increments the count of elaborated apps
         * @param failed if <code>true</code> increments the counter for failed apps creation
         */
        public synchronized  void appElaborated(boolean failed) {
            System.out.println();
            if (failed) {
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

    /**
     * Generate a unique token id for android devices
     * @return the token id
     */
    private static String generateAndroidToken() {

        String raw = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
        raw = raw.replaceAll("-","");

        return raw;
    }

    /**
     * Generate a unique token id for ios devices
     * @return teh token id
     */
    private static String generateiOSToken() {

        String raw = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        raw = raw.replaceAll("-","");

        return raw;
    }

    /**
     * Generate the tokens
     * @param aerogearAdminService aerogear admin interface
     * @param variantId the variant id for this token
     * @param variandSecret teh variant secret
     * @param count Number of tokens to be generated
     * @throws Exception on any error
     */
    private static void generateTokens(final AerogearAdminService aerogearAdminService, final String variantId, final String variandSecret, final int count) throws Exception {

        final String DEVICE_ALIAS = "TEST_TOKEN";

        for (int i = 0; i < count; i++) {

            try {
                Installation installation = new Installation();
                installation.setDeviceToken(generateAndroidToken());
                installation.setAlias(DEVICE_ALIAS);

                aerogearAdminService.registerDevice(installation, variantId, variandSecret);
                logger.tokenElaborated(false);
            } catch (Exception e) {
                logger.tokenElaborated(true);
            }
        }
    }

    /**
     * Gneerate variants for the given push application
     * @param appId the push application id
     * @param appName the application name
     * @param cmd parameters received on the command line
     * @throws Exception on any error
     */
    private static void generateVariants(final String appId, final String appName, final CommandLine cmd) throws Exception {
        for (int variantNumber = 0; variantNumber < getIntOptionValue(cmd, OPTION_VARIANTS); variantNumber++) {

            String variantID = UUID.randomUUID().toString();
            String variantSecret = UUID.randomUUID().toString();

            Variant v = VariantBuilder.<AndroidVariantBuilder>forVariant(VariantType.ANDROID)
                .withVariantId(variantID)
                .withDescription(String.format(VARIANT_DESC_PATTERN, variantNumber, appName))
                .withDeveloper(VARIANT_DEVELOPER)
                .withId(variantID)
                .withName(String.format(VARIANT_NAME_PATTERN, appName, variantNumber))
                .withSecret(variantSecret)
                .withGoogleKey("googlekey")
                .withProjectNumber("123456")
                .build();

            try {
                DefaultAerogearAdminService aerogearAdminService = getAdminService(cmd);
                v = aerogearAdminService.createVariant(v, appId);
                logger.variantElaborated(false);
                generateTokens(aerogearAdminService, v.getVariantID(), v.getSecret(), getIntOptionValue(cmd, OPTION_TOKENS));
            } catch (Exception e) {
                logger.variantElaborated(true);
            }
        }
    }

    /**
     * Logs in into the UPS amdin console
     * @param cmd parsed command line
     * @return admin interface
     */
    private static DefaultAerogearAdminService getAdminService(final CommandLine cmd) {
        PushServer pushServer = new PushServer(cmd.getOptionValue(OPTION_URL, DEFAULT_URL));
        pushServer.setKeycloakCredentials(cmd.getOptionValue(OPTION_USERNAME), cmd.getOptionValue(OPTION_PASSWORD), cmd.getOptionValue(OPTION_CLIENTID, DEFAULT_CLIENT_ID));

        return new DefaultAerogearAdminService(pushServer);
    }

    /**
     * Generate push applications accoring to received command line parameters
     * @param cmd parsed command line
     * @throws Exception on any error
     */
    private static void generateApplications(final CommandLine cmd) throws Exception {

        for (int i = 0; i < getIntOptionValue(cmd, OPTION_APPS); i++) {

            String appId = UUID.randomUUID().toString();
            String appName = String.format(APPNAME_PATTERN, i);

            try {

                PushApplicationBuilder builder =
                    PushApplicationBuilder.forApplication(appId, appName)
                    .withDescription(String.format(DESCRIPTION_PATTERN, i))
                    .withDeveloper(String.format(DEVELOPER_PATTERN, i))
                    .withMasterSecret(MASTER_SECRET)
                    .withPushApplicationID(String.format(PUSHAPPID_PATTERN, i));

                PushApplication app = getAdminService(cmd).createPushApplication(builder.build());
                logger.appElaborated(false);

                generateVariants(app.getPushApplicationID(), app.getName(), cmd);

            } catch (AerogearHelperException re) {
                logger.appElaborated(true);
            }
        }
    }

    /**
     * Return a command line parameter as int
     * @param cmd parsed command line
     * @param optionName option to be returned
     * @return the option value
     * @throws ParseException on any error parsing the option
     */
    private static int getIntOptionValue(final CommandLine cmd, final String optionName) throws ParseException {
        return ((Number) cmd.getParsedOptionValue(optionName)).intValue();
    }


    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(Option.builder("a").longOpt(OPTION_APPS).hasArg(true).argName("total").type(Number.class).desc("Number of apps to be generated").required(true).build());
        options.addOption(Option.builder("v").longOpt(OPTION_VARIANTS).hasArg(true).argName("total").type(Number.class).desc("Number of variants to be generated").required(true).build());
        options.addOption(Option.builder("t").longOpt(OPTION_TOKENS).hasArg(true).argName("total").type(Number.class).desc("Number of tokens to be generated").required(true).build());
        options.addOption(Option.builder("u").longOpt(OPTION_USERNAME).hasArg(true).argName("username").desc("Username to be used to authenticate to the UPS").required(true).build());
        options.addOption(Option.builder("p").longOpt(OPTION_PASSWORD).hasArg(true).argName("password").desc("Username to be used to authenticate to the UPS").required(true).build());
        options.addOption(Option.builder("c").longOpt(OPTION_CLIENTID).hasArg(true).argName("id").desc("Client id used to create the apps. Defaults to <" + DEFAULT_CLIENT_ID + ">").required(false).build());
        options.addOption(Option.builder("U").longOpt(OPTION_URL).hasArg(true).argName("UPS URL").desc("URL to the UPS server. Defaults to <" + DEFAULT_URL + ">").required(false).build());

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            logger = new LoggerThread(getIntOptionValue(cmd, OPTION_APPS), getIntOptionValue(cmd, OPTION_VARIANTS), getIntOptionValue(cmd, OPTION_TOKENS));
            logger.start();

            try {
                generateApplications(cmd);
            } finally {
                logger.shutdown();
            }

        } catch (Exception e) {
            System.out.println ("Command line parsing error : " + e.getMessage());

            final String syntax = "mock-data-loader.sh " +
                "-u|--username <username>" +
                "-u|--password <password>" +
                "-a|--apps <TOTAL> " +
                "-t|--tokens <TOTAL> " +
                "-v|--variants <TOTAL> " +
                "[-c|--clientid <CLIENTID> " +
                " -U|--url <UPS URL>]";


            new HelpFormatter().printHelp(syntax, options);
        }

    }
}

