package com.kinnara.kecakplugins.binderidgenerator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Collection;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        registrationList.add(context.registerService(BinderIdGeneratorField.class.getName(), new BinderIdGeneratorField(), null));
        registrationList.add(context.registerService(DefaultIdGeneratorBinder.class.getName(), new DefaultIdGeneratorBinder(), null));
        registrationList.add(context.registerService(UniqueIdValueValidator.class.getName(), new UniqueIdValueValidator(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}