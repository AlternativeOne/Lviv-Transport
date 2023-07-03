package com.lexoff.lvivtransport;

import android.location.Address;

import com.grack.nanojson.JsonParserException;
import com.lexoff.lvivtransport.exception.RouteNotFound;
import com.lexoff.lvivtransport.exception.ServiceUnavailableException;
import com.lexoff.lvivtransport.exception.StopNotFound;
import com.lexoff.lvivtransport.extractor.ClosestStopsExtractor;
import com.lexoff.lvivtransport.extractor.ClosestTransportExtractor;
import com.lexoff.lvivtransport.extractor.DynamicTransportExtractor;
import com.lexoff.lvivtransport.extractor.OfflineClosestStopsExtractor;
import com.lexoff.lvivtransport.extractor.OfflineStaticRoutesExtractor;
import com.lexoff.lvivtransport.extractor.OfflineStopStaticExtractor;
import com.lexoff.lvivtransport.extractor.RailroadStationsExtractor;
import com.lexoff.lvivtransport.extractor.StaticRoutesExtractor;
import com.lexoff.lvivtransport.extractor.StopStaticExtractor;
import com.lexoff.lvivtransport.extractor.StopTimetableExtractor;
import com.lexoff.lvivtransport.extractor.VehicleExtractor;
import com.lexoff.lvivtransport.info.ClosestInfo;
import com.lexoff.lvivtransport.info.ClosestTransportsInfo;
import com.lexoff.lvivtransport.info.DynamicRouteInfo;
import com.lexoff.lvivtransport.info.RailroadStationsInfo;
import com.lexoff.lvivtransport.info.RouteInfo;
import com.lexoff.lvivtransport.info.RoutingRoutesInfo;
import com.lexoff.lvivtransport.info.RoutingStopsInfo;
import com.lexoff.lvivtransport.info.StaticRouteInfo;
import com.lexoff.lvivtransport.info.Stop;
import com.lexoff.lvivtransport.info.StopTimetablesInfo;
import com.lexoff.lvivtransport.info.StopsInfo;
import com.lexoff.lvivtransport.info.TransferInfo;
import com.lexoff.lvivtransport.info.TransfersInfo;
import com.lexoff.lvivtransport.info.TransportInfo;
import com.lexoff.lvivtransport.info.VehicleInfo;

import org.osmdroid.bonuspack.location.GeocoderNominatim;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import okhttp3.Response;

public class Api {
    private Client client;

    public Api(){
        client=Client.getInstance();
    }

    public boolean checkIfRouteExist(String shortName){
        if (App.OFFLINE_MODE){
            try {
                return Arrays.asList(App.getApp().getResources().getAssets().list("routes/")).contains(String.format("%s.json", shortName));
            } catch (Exception e) {
                return false;
            }
        }

        String url="https://api.lad.lviv.ua/routes/static/"+shortName;

        try {
            Response response = client.get(url);

            if (response.code()==404) return false;

            return true;
        } catch (IOException e){
            return false;
        }
    }

    public boolean checkIfStopExist(int code){
        if (App.OFFLINE_MODE){
            try {
                return Arrays.asList(App.getApp().getResources().getAssets().list("stops/")).contains(String.format("%d.json", code));
            } catch (Exception e) {
                return false;
            }
        }

        String url="https://api.lad.lviv.ua/stops/"+code+"/static";

        try {
            Response response = client.get(url);

            if (response.code()==404) return false;

            return true;
        } catch (IOException e){
            return false;
        }
    }

    public StaticRouteInfo getStaticRouteInfo(String routeShortName) throws IOException, ServiceUnavailableException, JsonParserException {
        if (App.OFFLINE_MODE){
            OfflineStaticRoutesExtractor srExtractor = new OfflineStaticRoutesExtractor(routeShortName);
            StaticRouteInfo srInfo=(StaticRouteInfo) srExtractor.getInfo();

            return srInfo;
        }

        StaticRoutesExtractor srExtractor = new StaticRoutesExtractor(client, routeShortName);
        StaticRouteInfo srInfo=(StaticRouteInfo) srExtractor.getInfo();

        return srInfo;
    }

    public DynamicRouteInfo getDynamicRouteInfo(String routeShortName) throws IOException, ServiceUnavailableException, JsonParserException {
        if (App.OFFLINE_MODE){
            return new DynamicRouteInfo();
        }

        DynamicTransportExtractor dtExtractor = new DynamicTransportExtractor(client, routeShortName);
        DynamicRouteInfo dtInfo=(DynamicRouteInfo) dtExtractor.getInfo();

        return dtInfo;
    }

    public RouteInfo getRouteInfo(String routeShortName) throws IOException, ServiceUnavailableException, RouteNotFound, JsonParserException {
        if (!checkIfRouteExist(routeShortName)) throw new RouteNotFound("");

        RouteInfo info=new RouteInfo();
        info.setStaticRouteInfo(getStaticRouteInfo(routeShortName));
        info.setDynamicRouteInfo(getDynamicRouteInfo(routeShortName));
        return info;
    }

    /*public StopsInfo getStopsInfo() throws IOException, ServiceUnavailableException, JsonParserException {
        StopsExtractor stopsExtractor = new StopsExtractor(client);
        StopsInfo stopsInfo=(StopsInfo) stopsExtractor.getInfo();

        return stopsInfo;
    }*/

    public ClosestTransportsInfo getClosestTransportsInfo(double latitude, double longitude) throws IOException, ServiceUnavailableException, JsonParserException {
        if (App.OFFLINE_MODE){
            return new ClosestTransportsInfo();
        }

        ClosestTransportExtractor ctExtractor = new ClosestTransportExtractor(client, latitude, longitude);
        ClosestTransportsInfo ctInfo=(ClosestTransportsInfo) ctExtractor.getInfo();

        return ctInfo;
    }

    public StopsInfo getClosestStopsInfo(double latitude, double longitude) throws IOException, ServiceUnavailableException, JsonParserException {
        if (App.OFFLINE_MODE){
            OfflineClosestStopsExtractor csExtractor = new OfflineClosestStopsExtractor();
            StopsInfo stopsInfo=(StopsInfo) csExtractor.getInfo();

            return stopsInfo;
        }

        ClosestStopsExtractor csExtractor = new ClosestStopsExtractor(client, latitude, longitude);
        StopsInfo stopsInfo=(StopsInfo) csExtractor.getInfo();

        return stopsInfo;
    }

    public ClosestInfo getClosestInfo(double latitude, double longitude) throws IOException, ServiceUnavailableException, JsonParserException {
        ClosestInfo closestInfo=new ClosestInfo();

        closestInfo.setStopsInfo(getClosestStopsInfo(latitude, longitude));
        closestInfo.setTransportsInfo(getClosestTransportsInfo(latitude, longitude));

        return closestInfo;
    }

    public Stop getStopInfo(int code) throws IOException, ServiceUnavailableException, StopNotFound, JsonParserException {
        if (!checkIfStopExist(code)) throw new StopNotFound("");

        if (App.OFFLINE_MODE){
            OfflineStopStaticExtractor ssExtractor=new OfflineStopStaticExtractor(code);
            Stop stopInfo=(Stop) ssExtractor.getInfo();

            stopInfo.setStopTimetablesInfo(new StopTimetablesInfo());

            List<TransferInfo> transferInfos=stopInfo.getTransfersInfo().getTransfers();

            TransfersInfo transfersInfo=new TransfersInfo();

            for (int i=0; i<transferInfos.size(); i++) {
                String route = stopInfo.getRoutesAvailable().get(i);
                TransferInfo transferInfo = transferInfos.get(i);

                OfflineStaticRoutesExtractor srExtractor = new OfflineStaticRoutesExtractor(route);
                StaticRouteInfo srInfo = (StaticRouteInfo) srExtractor.getInfo();

                if (transferInfo.getDirection() == 0) {
                    transferInfo.setShapes(srInfo.getForwardShapes());
                } else {
                    transferInfo.setShapes(srInfo.getBackwardShapes());
                }

                transfersInfo.addTransferInfo(transferInfo);
            }

            stopInfo.setTransfersInfo(transfersInfo);

            return stopInfo;
        }

        StopStaticExtractor ssExtractor=new StopStaticExtractor(client, code);
        Stop stopInfo=(Stop) ssExtractor.getInfo();

        StopTimetableExtractor stExtractor=new StopTimetableExtractor(client, code);
        StopTimetablesInfo stopTimetablesInfo=(StopTimetablesInfo) stExtractor.getInfo();

        stopInfo.setStopTimetablesInfo(stopTimetablesInfo);

        List<TransferInfo> transferInfos=stopInfo.getTransfersInfo().getTransfers();

        TransfersInfo transfersInfo=new TransfersInfo();

        for (int i=0; i<transferInfos.size(); i++){
            String route=stopInfo.getRoutesAvailable().get(i);
            TransferInfo transferInfo=transferInfos.get(i);

            StaticRoutesExtractor srExtractor=new StaticRoutesExtractor(client, route);
            StaticRouteInfo srInfo=(StaticRouteInfo) srExtractor.getInfo();

            if (transferInfo.getDirection()==0){
                transferInfo.setShapes(srInfo.getForwardShapes());
            } else {
                transferInfo.setShapes(srInfo.getBackwardShapes());
            }

            transfersInfo.addTransferInfo(transferInfo);
        }

        stopInfo.setTransfersInfo(transfersInfo);

        return stopInfo;
    }

    public TransportInfo getTransportInfo(String code) throws IOException, ServiceUnavailableException, JsonParserException {
        VehicleExtractor vehicleExtractor=new VehicleExtractor(client, code);
        TransportInfo transportInfo=(TransportInfo) vehicleExtractor.getInfo();

        return transportInfo;
    }

    public VehicleInfo getVehicleInfo(String code) throws IOException, ServiceUnavailableException, JsonParserException {
        VehicleInfo info=new VehicleInfo();

        TransportInfo transportInfo=getTransportInfo(code);
        StaticRouteInfo staticRouteInfo=getStaticRouteInfo(transportInfo.getShortRouteName());
        transportInfo.setShortRouteName(staticRouteInfo.getRouteShortName());

        info.setTransportInfo(transportInfo);
        info.setStaticRouteInfo(staticRouteInfo);

        return info;
    }

    public List<Address> findAddress(String address, Locale locale) throws IOException {
        address="Львів, "+address;

        GeocoderNominatim decoder=new GeocoderNominatim(locale, Client.USER_AGENT);
        return decoder.getFromLocationName(address, 5);
    }

    public RoutingStopsInfo getRoutingStopsInfo(int code1, int code2) throws StopNotFound, JsonParserException, ServiceUnavailableException, IOException {
        RoutingStopsInfo info=new RoutingStopsInfo();

        info.setStartStop(getStopInfo(code1));
        info.setEndStop(getStopInfo(code2));

        return info;
    }

    public RoutingRoutesInfo getRoutingRoutesInfo(List<String> names1, List<String> names2) throws JsonParserException, IOException, ServiceUnavailableException {
        RoutingRoutesInfo info = new RoutingRoutesInfo();

        for (String name : names1) {
            info.addStaticRoute1(getStaticRouteInfo(name));
            info.addDynamicRoute1(getDynamicRouteInfo(name));
        }

        if (names2.size() > 0) {
            for (String name : names2) {
                info.addStaticRoute2(getStaticRouteInfo(name));
                info.addDynamicRoute2(getDynamicRouteInfo(name));
            }
        }

        return info;
    }

    public RailroadStationsInfo getRailroadStationsInfo() throws JsonParserException, IOException, ServiceUnavailableException {
        RailroadStationsExtractor extractor=new RailroadStationsExtractor(client);
        RailroadStationsInfo info=(RailroadStationsInfo) extractor.getInfo();

        return info;
    }

}
