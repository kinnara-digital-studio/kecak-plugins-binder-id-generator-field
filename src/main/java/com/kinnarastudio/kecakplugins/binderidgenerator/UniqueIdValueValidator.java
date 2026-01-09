package com.kinnarastudio.kecakplugins.binderidgenerator;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.PluginManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class UniqueIdValueValidator extends FormValidator {
    @Override
    public boolean validate(Element element, FormData formData, String[] values) {
        FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
        Form form = FormUtil.findRootForm(element);

        String fieldId = element.getPropertyString("id");

        List<String> args = Optional.ofNullable(values)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .collect(Collectors.toList());

        String primaryKey = formData.getPrimaryKeyValue();
        StringBuilder conditions = new StringBuilder(getCondition(fieldId, values));
        if(primaryKey != null) {
            args.add(primaryKey);
            conditions.append(" AND id <> ?");
        }

        long count = Optional.ofNullable(formDataDao.count(form, conditions.toString(), args.toArray(new String[0]))).orElse(0L);
        if(count > 0) {
            formData.addFormError(fieldId, "Value already exists");
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "Unique Value Validator";
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
        return "Unique Value Validator";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    public String getCondition(String fieldId, String[] values) {
        String questions = Optional.ofNullable(values)
                .stream()
                .flatMap(Arrays::stream)
                .filter(s -> !s.isEmpty())
                .map(s -> "?")
                .collect(Collectors.joining(", "));

        if(questions.isEmpty()) {
            return " 1=1 ";
        }
        return "WHERE e.customProperties." + fieldId + " IN (" + questions + ")";
    }
}
