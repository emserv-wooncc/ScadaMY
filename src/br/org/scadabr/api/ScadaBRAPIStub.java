/**
 * ScadaBRAPIStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.org.scadamy.api;

public class ScadaBRAPIStub extends org.apache.axis.client.Stub implements br.org.scadamy.api.ScadaBRAPI {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[21];
        _initOperationDesc1();
        _initOperationDesc2();
        _initOperationDesc3();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("removeFlexProject");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "id"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">RemoveFlexProjectResponse"));
        oper.setReturnClass(br.org.scadamy.api.config.RemoveFlexProjectResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "RemoveFlexProjectResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("setFlexBuilderConfig");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "SetFlexBuilderConfigParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">SetFlexBuilderConfigParams"), br.org.scadamy.api.config.SetFlexBuilderConfigParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">SetFlexBuilderConfigResponse"));
        oper.setReturnClass(br.org.scadamy.api.config.SetFlexBuilderConfigResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "SetFlexBuilderConfigResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getFlexBuilderConfig");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "projectId"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">GetFlexBuilderConfigResponse"));
        oper.setReturnClass(br.org.scadamy.api.config.GetFlexBuilderConfigResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "GetFlexBuilderConfigResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("browseFlexProjects");
        oper.setReturnType(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">BrowseFlexProjectsResponse"));
        oper.setReturnClass(br.org.scadamy.api.config.BrowseFlexProjectsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "BrowseFlexProjectsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("configureDataPoint");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "ConfigureDataPointParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">ConfigureDataPointParams"), br.org.scadamy.api.config.ConfigureDataPointParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">ConfigureDataPointResponse"));
        oper.setReturnClass(br.org.scadamy.api.config.ConfigureDataPointResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "ConfigureDataPointResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("removeDataPoint");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "RemoveDataPointParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">RemoveDataPointParams"), br.org.scadamy.api.config.RemoveDataPointParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">RemoveDataPointResponse"));
        oper.setReturnClass(br.org.scadamy.api.config.RemoveDataPointResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "RemoveDataPointResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("browseDataPoints");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "BrowseDataPointsParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">BrowseDataPointsParams"), br.org.scadamy.api.config.BrowseDataPointsParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">BrowseDataPointsResponse"));
        oper.setReturnClass(br.org.scadamy.api.config.BrowseDataPointsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "BrowseDataPointsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("removeDataSource");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "RemoveDataSourceParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">RemoveDataSourceParams"), br.org.scadamy.api.config.RemoveDataSourceParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">RemoveDataSourceResponse"));
        oper.setReturnClass(br.org.scadamy.api.config.RemoveDataSourceResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "RemoveDataSourceResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("configureDataSource");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "ConfigureDataSourceParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">ConfigureDataSourceParams"), br.org.scadamy.api.config.ConfigureDataSourceParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">ConfigureDataSourceResponse"));
        oper.setReturnClass(br.org.scadamy.api.config.ConfigureDataSourceResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "ConfigureDataSourceResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("browseDataSources");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "BrowseDataSourcesParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">BrowseDataSourcesParams"), br.org.scadamy.api.config.BrowseDataSourcesParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">BrowseDataSourcesResponse"));
        oper.setReturnClass(br.org.scadamy.api.config.BrowseDataSourcesResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://config.api.scadamy.org.br", "BrowseDataSourcesResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[9] = oper;

    }

    private static void _initOperationDesc2(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getStatus");
        oper.setReturnType(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">GetStatusResponse"));
        oper.setReturnClass(br.org.scadamy.api.da.GetStatusResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "GetStatusResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("readData");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "ReadDataParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">ReadDataParams"), br.org.scadamy.api.da.ReadDataParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">ReadDataResponse"));
        oper.setReturnClass(br.org.scadamy.api.da.ReadDataResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "ReadDataResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("writeData");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "WriteDataParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">WriteDataParams"), br.org.scadamy.api.da.WriteDataParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">WriteDataResponse"));
        oper.setReturnClass(br.org.scadamy.api.da.WriteDataResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "WriteDataResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("writeStringData");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "WriteStringDataParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">WriteStringDataParams"), br.org.scadamy.api.da.WriteStringDataParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">WriteStringDataResponse"));
        oper.setReturnClass(br.org.scadamy.api.da.WriteStringDataResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "WriteStringDataResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("browseTags");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "BrowseTagsParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">BrowseTagsParams"), br.org.scadamy.api.da.BrowseTagsParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">BrowseTagsResponse"));
        oper.setReturnClass(br.org.scadamy.api.da.BrowseTagsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "BrowseTagsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getDataHistory");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://hda.api.scadamy.org.br", "GetDataHistoryParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://hda.api.scadamy.org.br", ">GetDataHistoryParams"), br.org.scadamy.api.hda.GetDataHistoryParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://hda.api.scadamy.org.br", ">GetDataHistoryResponse"));
        oper.setReturnClass(br.org.scadamy.api.hda.GetDataHistoryResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://hda.api.scadamy.org.br", "GetDataHistoryResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getActiveEvents");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "GetActiveEventsParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">GetActiveEventsParams"), br.org.scadamy.api.ae.GetActiveEventsParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">GetActiveEventsResponse"));
        oper.setReturnClass(br.org.scadamy.api.ae.GetActiveEventsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "GetActiveEventsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[16] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getEventsHistory");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "GetEventsHistoryParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">GetEventsHistoryParams"), br.org.scadamy.api.ae.GetEventsHistoryParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">GetEventsHistoryResponse"));
        oper.setReturnClass(br.org.scadamy.api.ae.GetEventsHistoryResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "GetEventsHistoryResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[17] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("ackEvents");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "AckEventsParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">AckEventsParams"), br.org.scadamy.api.ae.AckEventsParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">AckEventsResponse"));
        oper.setReturnClass(br.org.scadamy.api.ae.AckEventsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "AckEventsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[18] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("browseEventsDefinitions");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "BrowseEventsParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">BrowseEventsParams"), br.org.scadamy.api.ae.BrowseEventsParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">BrowseEventsResponse"));
        oper.setReturnClass(br.org.scadamy.api.ae.BrowseEventsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "BrowseEventsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[19] = oper;

    }

    private static void _initOperationDesc3(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("annotateEvent");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "AnnotateEventParams"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">AnnotateEventParams"), br.org.scadamy.api.ae.AnnotateEventParams.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">AnnotateEventResponse"));
        oper.setReturnClass(br.org.scadamy.api.ae.AnnotateEventResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "AnnotateEventResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[20] = oper;

    }

    public ScadaBRAPIStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public ScadaBRAPIStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public ScadaBRAPIStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">AckEventsParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.AckEventsParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">AckEventsResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.AckEventsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">AnnotateEventParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.AnnotateEventParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">AnnotateEventResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.AnnotateEventResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">BrowseEventsParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.BrowseEventsParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">BrowseEventsResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.BrowseEventsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">GetActiveEventsParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.GetActiveEventsParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">GetActiveEventsResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.GetActiveEventsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">GetEventsHistoryParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.GetEventsHistoryParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", ">GetEventsHistoryResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.GetEventsHistoryResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "AckEventsOptions");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.AckEventsOptions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "ActiveEventsOptions");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.ActiveEventsOptions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "BrowseEventsOptions");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.BrowseEventsOptions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ae.api.scadamy.org.br", "EventsHistoryOptions");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.ae.EventsHistoryOptions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">BrowseDataPointsParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.BrowseDataPointsParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">BrowseDataPointsResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.BrowseDataPointsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">BrowseDataSourcesParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.BrowseDataSourcesParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">BrowseDataSourcesResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.BrowseDataSourcesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">BrowseFlexProjectsResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.BrowseFlexProjectsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">ConfigureDataPointParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.ConfigureDataPointParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">ConfigureDataPointResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.ConfigureDataPointResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">ConfigureDataSourceParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.ConfigureDataSourceParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">ConfigureDataSourceResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.ConfigureDataSourceResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">GetFlexBuilderConfigResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.GetFlexBuilderConfigResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">RemoveDataPointParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.RemoveDataPointParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">RemoveDataPointResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.RemoveDataPointResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">RemoveDataSourceParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.RemoveDataSourceParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">RemoveDataSourceResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.RemoveDataSourceResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">RemoveFlexProjectResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.RemoveFlexProjectResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">SetFlexBuilderConfigParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.SetFlexBuilderConfigParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://config.api.scadamy.org.br", ">SetFlexBuilderConfigResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.config.SetFlexBuilderConfigResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://constants.api.scadamy.org.br", "AlarmLevel");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.constants.AlarmLevel.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://constants.api.scadamy.org.br", "DataSourceType");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.constants.DataSourceType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://constants.api.scadamy.org.br", "DataType");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.constants.DataType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://constants.api.scadamy.org.br", "ErrorCode");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.constants.ErrorCode.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://constants.api.scadamy.org.br", "EventType");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.constants.EventType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://constants.api.scadamy.org.br", "ModbusDataType");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.constants.ModbusDataType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://constants.api.scadamy.org.br", "ModbusRegisterRange");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.constants.ModbusRegisterRange.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://constants.api.scadamy.org.br", "QualityCode");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.constants.QualityCode.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://constants.api.scadamy.org.br", "ServerStateCode");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.constants.ServerStateCode.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">BrowseTagsParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.BrowseTagsParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">BrowseTagsResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.BrowseTagsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">GetStatusResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.GetStatusResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">ReadDataParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.ReadDataParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">ReadDataResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.ReadDataResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">WriteDataParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.WriteDataParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">WriteDataResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.WriteDataResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">WriteStringDataParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.WriteStringDataParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", ">WriteStringDataResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.WriteStringDataResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "BrowseTagsOptions");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.BrowseTagsOptions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "ReadDataOptions");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.ReadDataOptions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "WriteDataOptions");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.WriteDataOptions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://da.api.scadamy.org.br", "WriteStringDataOptions");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.da.WriteStringDataOptions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://hda.api.scadamy.org.br", ">GetDataHistoryParams");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.hda.GetDataHistoryParams.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://hda.api.scadamy.org.br", ">GetDataHistoryResponse");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.hda.GetDataHistoryResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://hda.api.scadamy.org.br", "GetDataHistoryOptions");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.hda.GetDataHistoryOptions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", ">Authentication");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.Authentication.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "APIError");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.APIError.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "EventDefinition");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.EventDefinition.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "EventMessage");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.EventMessage.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "EventNotification");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.EventNotification.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "FlexProject");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.FlexProject.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "ItemInfo");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.ItemInfo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "ItemStringValue");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.ItemStringValue.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "ItemValue");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.ItemValue.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "ModbusIPConfig");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.ModbusIPConfig.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "ModbusPointConfig");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.ModbusPointConfig.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "ModbusSerialConfig");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.ModbusSerialConfig.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "ReplyBase");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.ReplyBase.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vo.api.scadamy.org.br", "ServerStatus");
            cachedSerQNames.add(qName);
            cls = br.org.scadamy.api.vo.ServerStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public br.org.scadamy.api.config.RemoveFlexProjectResponse removeFlexProject(int id) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "removeFlexProject"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {new java.lang.Integer(id)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.config.RemoveFlexProjectResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.config.RemoveFlexProjectResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.config.RemoveFlexProjectResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.config.SetFlexBuilderConfigResponse setFlexBuilderConfig(br.org.scadamy.api.config.SetFlexBuilderConfigParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "setFlexBuilderConfig"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.config.SetFlexBuilderConfigResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.config.SetFlexBuilderConfigResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.config.SetFlexBuilderConfigResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.config.GetFlexBuilderConfigResponse getFlexBuilderConfig(int projectId) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "getFlexBuilderConfig"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {new java.lang.Integer(projectId)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.config.GetFlexBuilderConfigResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.config.GetFlexBuilderConfigResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.config.GetFlexBuilderConfigResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.config.BrowseFlexProjectsResponse browseFlexProjects() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "browseFlexProjects"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.config.BrowseFlexProjectsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.config.BrowseFlexProjectsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.config.BrowseFlexProjectsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.config.ConfigureDataPointResponse configureDataPoint(br.org.scadamy.api.config.ConfigureDataPointParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "configureDataPoint"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.config.ConfigureDataPointResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.config.ConfigureDataPointResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.config.ConfigureDataPointResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.config.RemoveDataPointResponse removeDataPoint(br.org.scadamy.api.config.RemoveDataPointParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "removeDataPoint"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.config.RemoveDataPointResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.config.RemoveDataPointResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.config.RemoveDataPointResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.config.BrowseDataPointsResponse browseDataPoints(br.org.scadamy.api.config.BrowseDataPointsParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "browseDataPoints"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.config.BrowseDataPointsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.config.BrowseDataPointsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.config.BrowseDataPointsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.config.RemoveDataSourceResponse removeDataSource(br.org.scadamy.api.config.RemoveDataSourceParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "removeDataSource"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.config.RemoveDataSourceResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.config.RemoveDataSourceResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.config.RemoveDataSourceResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.config.ConfigureDataSourceResponse configureDataSource(br.org.scadamy.api.config.ConfigureDataSourceParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "configureDataSource"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.config.ConfigureDataSourceResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.config.ConfigureDataSourceResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.config.ConfigureDataSourceResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.config.BrowseDataSourcesResponse browseDataSources(br.org.scadamy.api.config.BrowseDataSourcesParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "browseDataSources"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.config.BrowseDataSourcesResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.config.BrowseDataSourcesResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.config.BrowseDataSourcesResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.da.GetStatusResponse getStatus() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "getStatus"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.da.GetStatusResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.da.GetStatusResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.da.GetStatusResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.da.ReadDataResponse readData(br.org.scadamy.api.da.ReadDataParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "readData"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.da.ReadDataResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.da.ReadDataResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.da.ReadDataResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.da.WriteDataResponse writeData(br.org.scadamy.api.da.WriteDataParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "writeData"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.da.WriteDataResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.da.WriteDataResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.da.WriteDataResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.da.WriteStringDataResponse writeStringData(br.org.scadamy.api.da.WriteStringDataParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "writeStringData"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.da.WriteStringDataResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.da.WriteStringDataResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.da.WriteStringDataResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.da.BrowseTagsResponse browseTags(br.org.scadamy.api.da.BrowseTagsParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "browseTags"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.da.BrowseTagsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.da.BrowseTagsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.da.BrowseTagsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.hda.GetDataHistoryResponse getDataHistory(br.org.scadamy.api.hda.GetDataHistoryParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "getDataHistory"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.hda.GetDataHistoryResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.hda.GetDataHistoryResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.hda.GetDataHistoryResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.ae.GetActiveEventsResponse getActiveEvents(br.org.scadamy.api.ae.GetActiveEventsParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "getActiveEvents"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.ae.GetActiveEventsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.ae.GetActiveEventsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.ae.GetActiveEventsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.ae.GetEventsHistoryResponse getEventsHistory(br.org.scadamy.api.ae.GetEventsHistoryParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[17]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "getEventsHistory"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.ae.GetEventsHistoryResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.ae.GetEventsHistoryResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.ae.GetEventsHistoryResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.ae.AckEventsResponse ackEvents(br.org.scadamy.api.ae.AckEventsParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[18]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "ackEvents"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.ae.AckEventsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.ae.AckEventsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.ae.AckEventsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.ae.BrowseEventsResponse browseEventsDefinitions(br.org.scadamy.api.ae.BrowseEventsParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[19]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "browseEventsDefinitions"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.ae.BrowseEventsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.ae.BrowseEventsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.ae.BrowseEventsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.org.scadamy.api.ae.AnnotateEventResponse annotateEvent(br.org.scadamy.api.ae.AnnotateEventParams parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[20]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "annotateEvent"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.org.scadamy.api.ae.AnnotateEventResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.org.scadamy.api.ae.AnnotateEventResponse) org.apache.axis.utils.JavaUtils.convert(_resp, br.org.scadamy.api.ae.AnnotateEventResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
