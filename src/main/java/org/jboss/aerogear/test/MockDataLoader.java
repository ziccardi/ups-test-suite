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
 * Created by ziccardi on 20/12/2016.
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

        public void shutdown() {
            keepPolling = false;
        }


        public synchronized void variantElaborated(boolean failed) {
            if (failed) {
                failedVariants ++;
            } else {
                currentVariant++;
            }
        }

        public synchronized void tokenElaborated(boolean failed) {
            if (failed) {
                failedToken ++;
            } else {
                currentToken++;
            }
        }


        public synchronized  void appElaborated(boolean failed) {
            System.out.printf("\n");
            if (failed) {
                currentAppFailed ++;
            } else {
                currentAppProgress++;
            }
            reset();
        }

        private synchronized void reset() {
            printUpdate();
            currentToken = currentVariant = failedToken = failedVariants = 0;
        }

        private synchronized void printUpdate() {
            System.out.printf("\rApps created/failed/total: %3d/%3d/%3d - Variants created/failed/total: %3d/%3d/%3d - Tokens created/failed/total: %5d/%5d/%5d",
                currentAppProgress, currentAppFailed, totalApps,
                currentVariant, failedVariants, totalVariants,
                currentToken, failedToken, totalTokens * totalVariants);
        }

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
            System.out.printf("\n");
        }
    }


    private static String generateAndroidToken() {

        String raw = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
        raw = raw.replaceAll("-","");

        return raw;
    }

    private static String generateiOSToken() {

        String raw = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        raw = raw.replaceAll("-","");

        return raw;
    }


    private static void generateTokens(final AerogearAdminService aerogearAdminService, final String variantId, final String variandSecret, final int count) throws Exception {

        final String DEVICE_ALIAS = "TEST_TOKEN";

        for (int i = 0; i < count; i++) {

            try {
                Installation installation = new Installation();
                installation.setDeviceToken(generateAndroidToken());
                installation.setAlias(DEVICE_ALIAS);

                //System.out.printf ("[INFO] - Registering device for %s:%s\n", variantId, variandSecret);

                aerogearAdminService.registerDevice(installation, variantId, variandSecret);
                logger.tokenElaborated(false);
            } catch (Exception e) {
                logger.tokenElaborated(true);
            }
        }
    }

    private static void generateVariants(final String appId, final String appName, final CommandLine cmd) throws Exception {
        for (int variantNumber = 0; variantNumber < getIntOptionValue(cmd, OPTION_VARIANTS); variantNumber++) {

            String variantID = UUID.randomUUID().toString();
            String variantSecret = UUID.randomUUID().toString();

            Variant v = VariantBuilder.<AndroidVariantBuilder>forVariant(VariantType.ANDROID)
                .withVariantId(variantID) // first variant
                .withDescription(String.format(VARIANT_DESC_PATTERN, variantNumber, appName))
                .withDeveloper(VARIANT_DEVELOPER)
                .withId(variantID)
                .withName(String.format(VARIANT_NAME_PATTERN, appName, variantNumber))
                .withSecret(variantSecret)
                .withGoogleKey("googlekey")
                .withProjectNumber("123456")
                .build();

            try {
                //System.out.printf("[INFO] - Creating variant. ID: %s\n", variantID);
                DefaultAerogearAdminService aerogearAdminService = getAdminService(cmd);
                v = aerogearAdminService.createVariant(v, appId);
                logger.variantElaborated(false);
                generateTokens(aerogearAdminService, v.getVariantID(), v.getSecret(), getIntOptionValue(cmd, OPTION_TOKENS));
            } catch (Exception e) {
                logger.variantElaborated(true);
                //System.out.printf("[ERROR] - Failed creating variant. ID: %s\n", variantID);
            }
        }
    }

    private static DefaultAerogearAdminService getAdminService(final CommandLine cmd) {
        PushServer pushServer = new PushServer(cmd.getOptionValue(OPTION_URL, DEFAULT_URL));
        pushServer.setKeycloakCredentials(cmd.getOptionValue(OPTION_USERNAME), cmd.getOptionValue(OPTION_PASSWORD), cmd.getOptionValue(OPTION_CLIENTID, DEFAULT_CLIENT_ID));

        return new DefaultAerogearAdminService(pushServer);
    }

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

