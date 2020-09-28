package com.kinnara.kecakplugins.binderidgenerator;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.IdGeneratorField;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.PluginManager;
import org.springframework.context.ApplicationContext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author aristo
 */
public class BinderIdGeneratorField extends IdGeneratorField {
    final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Override
    protected String getGeneratedValue(FormData formData) {
        ApplicationContext applicationContext = AppUtil.getApplicationContext();
        PluginManager pluginManager = (PluginManager) applicationContext.getBean("pluginManager");
        FormLoadElementBinder formLoadBinder = pluginManager.getPluginObject((Map<String, Object>) getProperty("idLoadBinder"));

        return Optional.ofNullable(formLoadBinder)
                .map(s -> s.load(this, formData.getPrimaryKeyValue(), formData))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .flatMap(r -> Optional.of(getFieldName()).map(r::getProperty))
                .filter(s -> !s.isEmpty())

                // testing value, default set to current date
                .orElse(UUID.randomUUID().toString());
    }

    private String getFieldName() {
        return Optional.of("fieldName")
                .map(this::getPropertyString)
                .orElse("");
    }

    @Override
    public String getName() {
        return getLabel() + getVersion();
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return "Binder ID Generator";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return Optional.ofNullable(AppUtil.readPluginResource(getClassName(), "/properties/BinderIdGeneratorField.json", null, true, null)).orElse("").replaceAll("\"", "'");
    }
}
