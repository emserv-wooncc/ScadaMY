/*
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.web.mvc;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;

public abstract class SimpleFormRedirectController implements Controller {
    private String successUrl;
    private String commandName = "form";
    private Class<?> commandClass = Object.class;
    private String formView;

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public void setCommandClass(Class<?> commandClass) {
        this.commandClass = commandClass;
    }

    public void setFormView(String formView) {
        this.formView = formView;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object command = formBackingObject(request);
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            ServletRequestDataBinder binder = new ServletRequestDataBinder(command, commandName);
            binder.bind(request);
            BindException errors = new BindException(binder.getBindingResult());
            onBindAndValidate(request, command, errors);
            if (isFormChangeRequest(request) || errors.hasErrors()) {
                Map<String, Object> model = errors.getModel();
                Map<String, Object> ref = referenceData(request, command, errors);
                if (ref != null) {
                    model.putAll(ref);
                }
                return new ModelAndView(formView, model);
            }
            return onSubmit(request, response, command, errors);
        } else {
            BindException errors = new BindException(command, commandName);
            Map<String, Object> model = errors.getModel();
            Map<String, Object> ref = referenceData(request, command, errors);
            if (ref != null) {
                model.putAll(ref);
            }
            return new ModelAndView(formView, model);
        }
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return commandClass.newInstance();
    }

    @SuppressWarnings("rawtypes")
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        return null;
    }

    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
    }

    protected boolean isFormChangeRequest(HttpServletRequest request) {
        return false;
    }

    protected boolean isFormSubmission(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod());
    }

    protected abstract ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
            BindException errors) throws Exception;

    public ModelAndView getSuccessRedirectView() {
        return getSuccessRedirectView(null);
    }

    public ModelAndView getSuccessRedirectView(String queryString) {
        String url = successUrl;
        if (queryString != null && queryString.trim().length() > 0) {
            if (queryString.charAt(0) != '?')
                url += '?' + queryString;
            else
                url += queryString;
        }
        RedirectView redirectView = new RedirectView(url, true);
        return new ModelAndView(redirectView);
    }

    public boolean hasSubmitParameter(HttpServletRequest request, String name) {
        return WebUtils.hasSubmitParameter(request, name);
    }
}
