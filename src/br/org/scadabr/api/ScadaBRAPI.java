/**
 * ScadaBRAPI.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.org.scadamy.api;

public interface ScadaBRAPI extends java.rmi.Remote {
    public br.org.scadamy.api.config.RemoveFlexProjectResponse removeFlexProject(int id) throws java.rmi.RemoteException;
    public br.org.scadamy.api.config.SetFlexBuilderConfigResponse setFlexBuilderConfig(br.org.scadamy.api.config.SetFlexBuilderConfigParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.config.BrowseFlexProjectsResponse browseFlexProjects() throws java.rmi.RemoteException;
    public br.org.scadamy.api.config.GetFlexBuilderConfigResponse getFlexBuilderConfig(int projectId) throws java.rmi.RemoteException;
    public br.org.scadamy.api.config.ConfigureDataPointResponse configureDataPoint(br.org.scadamy.api.config.ConfigureDataPointParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.config.RemoveDataPointResponse removeDataPoint(br.org.scadamy.api.config.RemoveDataPointParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.config.BrowseDataPointsResponse browseDataPoints(br.org.scadamy.api.config.BrowseDataPointsParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.config.RemoveDataSourceResponse removeDataSource(br.org.scadamy.api.config.RemoveDataSourceParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.config.ConfigureDataSourceResponse configureDataSource(br.org.scadamy.api.config.ConfigureDataSourceParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.config.BrowseDataSourcesResponse browseDataSources(br.org.scadamy.api.config.BrowseDataSourcesParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.da.GetStatusResponse getStatus() throws java.rmi.RemoteException;
    public br.org.scadamy.api.da.WriteDataResponse writeData(br.org.scadamy.api.da.WriteDataParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.da.WriteStringDataResponse writeStringData(br.org.scadamy.api.da.WriteStringDataParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.da.BrowseTagsResponse browseTags(br.org.scadamy.api.da.BrowseTagsParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.hda.GetDataHistoryResponse getDataHistory(br.org.scadamy.api.hda.GetDataHistoryParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.ae.GetActiveEventsResponse getActiveEvents(br.org.scadamy.api.ae.GetActiveEventsParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.ae.GetEventsHistoryResponse getEventsHistory(br.org.scadamy.api.ae.GetEventsHistoryParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.ae.AckEventsResponse ackEvents(br.org.scadamy.api.ae.AckEventsParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.ae.BrowseEventsResponse browseEventsDefinitions(br.org.scadamy.api.ae.BrowseEventsParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.da.ReadDataResponse readData(br.org.scadamy.api.da.ReadDataParams parameters) throws java.rmi.RemoteException;
    public br.org.scadamy.api.ae.AnnotateEventResponse annotateEvent(br.org.scadamy.api.ae.AnnotateEventParams parameters) throws java.rmi.RemoteException;
}
