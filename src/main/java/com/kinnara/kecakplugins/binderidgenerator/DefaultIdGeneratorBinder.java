package com.kinnara.kecakplugins.binderidgenerator;

import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.IdGeneratorField;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultIdGeneratorBinder extends FormBinder implements FormLoadElementBinder {
    public final static String LABEL = "Default ID Generator Binder";
    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        final String elementId = element.getPropertyString(FormUtil.PROPERTY_ID);
        final String value = getGeneratedValue(element, formData);

        FormRowSet rowSet = new FormRowSet();
        FormRow row = new FormRow();
        row.put(elementId, value);
        rowSet.add(row);

        return rowSet;
    }

    @Override
    public String getName() {
        return LABEL;
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
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/DefaultIdGeneratorBinder.json");
    }

    protected String getGeneratedValue(Element element, FormData formData) {
        String value = "";
        if (formData != null) {
            try {
                value = FormUtil.getElementPropertyValue(element, formData);
                if (!(value != null && value.trim().length() > 0)) {
                    String envVariable = getPropertyString("envVariable");
                    AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                    EnvironmentVariableDao environmentVariableDao = (EnvironmentVariableDao) AppUtil.getApplicationContext().getBean("environmentVariableDao");

                    Integer count = environmentVariableDao.getIncreasedCounter(envVariable, "Used for plugin: " + getName(), appDef);

                    String format = getPropertyString("format");
                    value = format;
                    Matcher m = Pattern.compile("(\\?+)").matcher(format);
                    if (m.find()) {
                        String pattern = m.group(1);
                        String formater = pattern.replaceAll("\\?", "0");
                        pattern = pattern.replaceAll("\\?", "\\\\?");

                        DecimalFormat myFormatter = new DecimalFormat(formater);
                        String runningNumber = myFormatter.format(count);
                        value = value.replaceAll(pattern, runningNumber);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(IdGeneratorField.class.getName(), e, "");
            }
        }
        return value;
    }
}
