package com.kinnarastudio.kecakplugins.binderidgenerator;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.IdGeneratorField;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadElementBinder;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author aristo
 */
public class BinderIdGeneratorField extends IdGeneratorField {
    @Override
    public String getFormBuilderCategory() {
        return "Kecak";
    }

    @Override
    public int getFormBuilderPosition() {
        return 100;
    }

    @Override
    protected String getGeneratedValue(FormData formData) {
        ApplicationContext applicationContext = AppUtil.getApplicationContext();
        PluginManager pluginManager = (PluginManager) applicationContext.getBean("pluginManager");
        FormLoadElementBinder formLoadBinder = pluginManager.getPlugin((Map<String, Object>) getProperty("idLoadBinder"));

        return Optional.ofNullable(formLoadBinder)
                .map(s -> s.load(this, formData.getPrimaryKeyValue(), formData))
                .stream()
                .flatMap(Collection::stream)
                .findFirst()
                .flatMap(r -> Optional.of(getFieldName()).map(r::getProperty))
                .map(String::trim)
                .filter(s -> !s.isEmpty())

                // testing value, default set to current date
                .orElseGet(() -> {
                    LogUtil.warn(getClassName(), "Error retrieving value from load binder ["+Optional.ofNullable(formLoadBinder)
                            .map(FormLoadElementBinder::getClass)
                            .map(Class::getName).orElse("null")+"]");

                    if(isValidateError()) {
                        formData.addFormError(FormUtil.getElementParameterName(this), "Error retrieving value");
                        return null;
                    } else {
                        return UUID.randomUUID().toString();
                    }
                });
    }

    private String getFieldName() {
        return Optional.of("fieldName")
                .map(this::getPropertyString)
                .filter(s -> !s.isEmpty())
                .orElseGet(() -> getPropertyString(FormUtil.PROPERTY_ID));
    }

    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
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
        final String[] args = new String[] {DefaultIdGeneratorBinder.class.getName()};
        return Optional.ofNullable(AppUtil.readPluginResource(getClassName(), "/properties/BinderIdGeneratorField.json", args, true, "/messages/BinderIdGeneratorField"))
                .orElse("")
                .replaceAll("\"", "'");
    }

    @Override
    public String renderTemplate(FormData formData, @SuppressWarnings("rawtypes") Map dataModel) {
        String template = "BinderIdGeneratorField.ftl";
        return renderTemplate(template,formData,dataModel);
    }

    protected String renderTemplate(String template, FormData formData, @SuppressWarnings("rawtypes") Map dataModel){
        String value = FormUtil.getElementPropertyValue(this, formData);
        dataModel.put("value", value);
        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    private boolean isValidateError() {
        return "true".equalsIgnoreCase(getPropertyString("validateError"));
    }
}
