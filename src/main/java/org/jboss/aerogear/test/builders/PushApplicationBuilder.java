package org.jboss.aerogear.test.builders;

import org.apache.commons.beanutils.BeanUtils;
import org.jboss.aerogear.unifiedpush.api.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ziccardi on 20/12/2016.
 */
public class PushApplicationBuilder {

    private final PushApplication pushApplication;
    private final List<Variant> variants;

    private PushApplicationBuilder(final String id, final String name) {
        this.pushApplication = new PushApplication();
        this.pushApplication.setId(id);
        this.pushApplication.setName(name);

        this.variants = new ArrayList<Variant>();
    }

    private PushApplicationBuilder(final PushApplication pushApplication) {
        this.pushApplication = pushApplication;
        this.variants = pushApplication.getVariants() != null ? pushApplication.getVariants() : new ArrayList<Variant>();
    }

    public static PushApplicationBuilder forApplication(final String id, final String name) {
        return new PushApplicationBuilder(id, name);
    }

    public PushApplicationBuilder withDescription(final String description) {
        this.pushApplication.setDescription(description);
        return this;
    }

    public PushApplicationBuilder withDeveloper(final String developer) {
        this.pushApplication.setDeveloper(developer);
        return this;
    }

    public PushApplicationBuilder withMasterSecret(final String masterSecret) {
        this.pushApplication.setMasterSecret(masterSecret);
        return this;
    }

    public PushApplicationBuilder withPushApplicationID(final String pushApplicationID) {
        this.pushApplication.setPushApplicationID(pushApplicationID);
        return this;
    }

    public PushApplicationBuilder withVariant(final Variant variant) {
        this.variants.add(variant);
        return this;
    }


    private Variant cloneVariant(Variant variant) throws Exception {
        Variant ret = variant.getClass().newInstance();

//        switch (variant.getType()) {
//            case ADM:
//                ret = new AdmVariant();
//                break;
//            case ANDROID:
//                ret = new AndroidVariant();
//                break;
//            case IOS:
//                ret = new iOSVariant();
//                break;
//            case SIMPLE_PUSH:
//                ret = new SimplePushVariant();
//                break;
//            case WINDOWS_MPNS:
//                ret = new WindowsMPNSVariant();
//                break;
//            case WINDOWS_WNS:
//                ret = new WindowsWNSVariant();
//                break;
//            default:
//                throw new IllegalStateException("Unknown variant type: " + variant.getType());
//        }

        BeanUtils.copyProperties(ret, variant);
        return ret;
    }

    private List<Variant> cloneVariants(List<Variant> variants) throws Exception {
        List<Variant> ret = new ArrayList<Variant>();
        for (Variant variant : variants) {
            ret.add(cloneVariant(variant));
        }

        return ret;
    }

    public PushApplication build(){

        // By contract, built object must be a different object on every invocation
        PushApplication ret = new PushApplication();

        try {
            BeanUtils.copyProperties(ret, this.pushApplication);
            ret.setVariants(cloneVariants(this.variants));
        } catch (Exception  e) {
            // Should never happen...
            throw new IllegalStateException(e);
        }

        this.pushApplication.setVariants(this.variants);
        return pushApplication;
    }
}
