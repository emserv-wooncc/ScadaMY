package com.serotonin.web.util;

import com.serotonin.web.util.PagingDataForm;
import com.serotonin.web.util.PaginatedData;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public abstract class PaginatedListController implements Controller {
    private String viewName;
    private String commandName = "form";
    private Class<?> commandClass = PagingDataForm.class;

    public PaginatedListController() {
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandClass(Class<?> commandClass) {
        this.commandClass = commandClass;
    }

    public Class<?> getCommandClass() {
        return commandClass;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object command = getCommand(request);
        if (command == null) {
            command = commandClass.newInstance();
        }
        ServletRequestDataBinder binder = new ServletRequestDataBinder(command, commandName);
        binder.bind(request);
        BindException errors = new BindException(binder.getBindingResult());
        return handle(request, response, command, errors);
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        return null;
    }

    @SuppressWarnings("unchecked")
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        PagingDataForm paging = (PagingDataForm) command;
        PaginatedData<?> data = getData(request, paging, paging.getOrderByClause(), paging.getOffset(), paging.getItemsPerPage(), errors);
        paging.setData(data.getData());
        paging.setNumberOfItems(data.getRowCount());
        Map<String, Object> model = errors.getModel();
        model.put(getCommandName(), command);
        referenceData(request, command, model);
        return new ModelAndView(viewName, model);
    }

    protected PaginatedData<?> getData(HttpServletRequest request, PagingDataForm paging, String orderBy, int offset, int itemsPerPage, BindException errors) throws Exception {
        return getData(request, paging, errors);
    }

    protected PaginatedData<?> getData(HttpServletRequest request, PagingDataForm paging, BindException errors) throws Exception {
        throw new RuntimeException("A getData method must be overridden");
    }

    @SuppressWarnings("rawtypes")
    protected void referenceData(HttpServletRequest request, Object command, Map model) throws Exception {
    }
}
