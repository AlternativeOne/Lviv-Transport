package com.lexoff.lvivtransport;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.lexoff.lvivtransport.exception.RouteNotFound;
import com.lexoff.lvivtransport.exception.StopNotFound;
import com.lexoff.lvivtransport.info.ClosestInfo;
import com.lexoff.lvivtransport.info.ClosestTransportInfo;
import com.lexoff.lvivtransport.info.ClosestTransportsInfo;
import com.lexoff.lvivtransport.info.DynamicRouteInfo;
import com.lexoff.lvivtransport.info.Info;
import com.lexoff.lvivtransport.info.RailroadStationInfo;
import com.lexoff.lvivtransport.info.RailroadStationsInfo;
import com.lexoff.lvivtransport.info.RouteInfo;
import com.lexoff.lvivtransport.info.RoutingRoutesInfo;
import com.lexoff.lvivtransport.info.RoutingStopsInfo;
import com.lexoff.lvivtransport.info.StaticRouteInfo;
import com.lexoff.lvivtransport.info.Stop;
import com.lexoff.lvivtransport.info.StopTimetable;
import com.lexoff.lvivtransport.info.StopTimetablesInfo;
import com.lexoff.lvivtransport.info.StopsInfo;
import com.lexoff.lvivtransport.info.TransferInfo;
import com.lexoff.lvivtransport.info.TransfersInfo;
import com.lexoff.lvivtransport.info.TransportInfo;
import com.lexoff.lvivtransport.info.VehicleInfo;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MapFragment extends BackPressableFragment {

    private final double DEFAULT_MAP_ZOOM = 16.5;
    private final double MIN_ZOOM_LEVEL = 16.0, MAX_ZOOM_LEVEL = 18.5;
    private final double NORTH_BOUNDARY = 49.9464, EAST_BOUNDARY = 24.2815, SOUTH_BOUNDARY = 49.7442, WEST_BOUNDARY = 23.7672;
    private final double DEFAULT_MAP_CENTER_LATITUDE = 49.8400829, DEFAULT_MAP_CENTER_LONGITUDE = 24.0319640;
    private long DEFAULT_SERVICE_UPDATE_DELAY = 15000L; //15 secs

    private float POLYLINE_ROUTE_WIDTH=12.0f;
    private double ZOOM_STEP=0.5;

    private boolean minimizedMode=false;

    private View rootView;

    private MapView mapView;

    private LinearLayout infoWindowLayout;
    private boolean isInfoWindowShown = false;

    private LinearLayout searchRouteLayout;
    private LinearLayout searchStopLayout;
    private LinearLayout searchAddressLayout;

    private ImageButton reloadBtn;

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Disposable currentWorker;

    private CompositeDisposable disposables;

    private ResourcesManager rManager;

    private Info currentInfo;

    //TODO: find out something better
    private Stop savedStopInfo;
    private int routeSelectedStopId = -1;
    private String routeSelectedVehicleCode="";

    private boolean notScrollToStopAfterOnBackPressed=false;

    private Overlay addressInSearchOverlay = null;
    private Overlay railroadStationsOverlay=null;
    private Overlay userLocationOverlay=null;

    private EditText inputField, searchStopInputField, searchAddressInputField;
    private Spinner vehicleTypeSpinner;

    private ViewGroup additionalControlsLayout;
    private boolean additionalControlsOpened=false;

    private Timer mainTimer;

    private boolean blockScrollEvent = false;
    private boolean blockItemClick = false;

    /*Routing*/
    private boolean routingMode=false, routingModeLocked=false;
    private Stop startStop=null, endStop=null;
    private RoutingRoutesInfo routingRoutesInfo=null;
    private StopOverlayItem routingSelectedStop=null;

    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private SharedPreferences defaultPreferences;

    private ConnectivityManager connectivityManager;
    private Optional isNetworkAvailable = Optional.empty();

    private NetworkRequest networkRequest = new NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build();

    private ConnectivityManager.NetworkCallback defaultNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NotNull Network network) {
            isNetworkAvailable = Optional.of(true);

            reloadContent();

            mainHandler.post(() -> handleNoNetworkMessage(false));
        }

        @Override
        public void onLost(@NotNull Network network) {
            isNetworkAvailable = Optional.of(false);

            if (currentWorker != null) {
                currentWorker.dispose();
                currentWorker = null;
            }

            if (disposables!=null) {
                disposables.dispose();
                disposables=null;
            }

            isLoading.set(false);

            mainHandler.post(() -> handleNoNetworkMessage(true));
        }
    };

    public MapFragment() {
        //empty
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        defaultPreferences=PreferenceManager.getDefaultSharedPreferences(requireContext());

        connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        rManager = new ResourcesManager(requireContext());
        rManager.init();

        if (defaultPreferences.getBoolean("update_info_more_often", false)){
            DEFAULT_SERVICE_UPDATE_DELAY = 10000L; //10 secs
        }

        IConfigurationProvider configuration = Configuration.getInstance();
        configuration.setOsmdroidBasePath(requireContext().getExternalCacheDir());
        configuration.setOsmdroidTileCache(new File(requireContext().getExternalCacheDir(), "tiles"));

        configuration.load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mapView != null) {
            IGeoPoint center = mapView.getMapCenter();
            outState.putDouble("center_latitutde", center.getLatitude());
            outState.putDouble("center_longtitude", center.getLongitude());
        }

        outState.putString("route_input", inputField.getText().toString());
        outState.putString("stop_input", searchStopInputField.getText().toString());
        outState.putString("address_input", searchAddressInputField.getText().toString());
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();

        //reloadContent();

        registerNetworkCallback();
    }

    @Override
    public void onPause() {
        super.onPause();

        mapView.onPause();

        if (currentWorker != null) {
            currentWorker.dispose();
            currentWorker = null;
        }

        if (disposables!=null){
            disposables.dispose();
            disposables=null;
        }

        cancelMainTimer();

        unregisterNetworkCallback();
    }

    private boolean isNetworkAvailable() {
        if (isNetworkAvailable.isPresent()) return (Boolean) isNetworkAvailable.get();

        return connectivityManager != null && connectivityManager.getActiveNetwork() != null;
    }

    private void registerNetworkCallback() {
        if (connectivityManager != null)
            connectivityManager.registerNetworkCallback(networkRequest, defaultNetworkCallback);
    }

    private void unregisterNetworkCallback() {
        if (connectivityManager != null)
            connectivityManager.unregisterNetworkCallback(defaultNetworkCallback);
    }

    private void reloadContent() {
        reloadContentInternal();
    }

    private void reloadContentInternal() {
        mainHandler.post(() -> {
            if (currentInfo instanceof RouteInfo) {
                loadRoute(Utils.getShortRouteNameFromInfo(currentInfo));
            } else if (currentInfo instanceof Stop) {
                loadStop(((Stop) currentInfo).getCode());
            } else if (currentInfo instanceof VehicleInfo) {
                loadVehicle(((VehicleInfo) currentInfo).getTransportInfo().getId());
            } if (currentInfo instanceof ClosestInfo) {
                IGeoPoint center = mapView.getMapCenter();
                loadClosest(center.getLatitude(), center.getLongitude());
            }

            loadRailroadStations();
        });
    }

    private void cancelMainTimer() {
        if (mainTimer != null) {
            mainTimer.cancel();
            mainTimer = null;
        }
    }

    private void scheduleOnMainTimer(TimerTask task, long delay) {
        mainTimer = new Timer();
        mainTimer.schedule(task, delay);
    }

    private void showLoading() {
        showInfoWindow(getString(R.string.loading_title), null);
    }

    private void hideLoading() {
        hideInfoWindow();
    }

    private void loadRailroadStations(){
        if (railroadStationsOverlay!=null) return;

        if (disposables==null){
            disposables=new CompositeDisposable();
        }

        Disposable d=Single.fromCallable(() -> (new Api()).getRailroadStationsInfo())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final RailroadStationsInfo result) -> {
                    ArrayList<OverlayItem> items=new ArrayList<>();

                    for (RailroadStationInfo info : result.getItems()){
                        OverlayItem item=new OverlayItem("", "", new GeoPoint(info.getLatitutde(), info.getLongitude()));
                        item.setMarker(rManager.getRailwayStationMarker());
                        items.add(item);
                    }

                    railroadStationsOverlay=new ItemizedIconOverlay<>(items, null, requireContext());
                }, (@NonNull final Throwable throwable) -> {

                });

        disposables.add(d);
    }

    private void loadRoute(String routeShortName) {
        loadRoute(routeShortName, false);
    }

    private void loadRoute(String routeShortName, boolean asService) {
        if (!isNetworkAvailable() && !App.OFFLINE_MODE) {
            Toast.makeText(requireContext(), R.string.reconnect_message, Toast.LENGTH_SHORT).show();
            return;
        }

        cancelMainTimer();

        isLoading.set(true);

        updateReloadButtonDrawable();

        if (!asService) showLoading();

        if (currentWorker != null) {
            currentWorker.dispose();
            currentWorker = null;
        }

        currentWorker = Single.fromCallable(() -> (new Api()).getRouteInfo(routeShortName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final RouteInfo result) -> {
                    isLoading.set(false);

                    updateReloadButtonDrawable();

                    if (asService) {
                        updateResult(result);
                    } else handleResult(result);

                    scheduleOnMainTimer(new TimerTask() {
                        @Override
                        public void run() {
                            loadRoute(result.getStaticRouteInfo().getRouteShortName(), true);
                        }
                    }, DEFAULT_SERVICE_UPDATE_DELAY);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    updateReloadButtonDrawable();

                    if (asService) {
                        scheduleOnMainTimer(new TimerTask() {
                            @Override
                            public void run() {
                                loadRoute(Utils.getShortRouteNameFromInfo(currentInfo), true);
                            }
                        }, DEFAULT_SERVICE_UPDATE_DELAY);
                    } else handleError(throwable);
                });
    }

    private void handleResult(RouteInfo info) {
        currentInfo = info;

        routeSelectedStopId = -1;
        routeSelectedVehicleCode="";

        mapView.getOverlays().clear();
        setStaticRoute(info.getStaticRouteInfo());
        setDynamicRoute(info.getDynamicRouteInfo());

        if (addressInSearchOverlay != null) {
            mapView.getOverlays().add(addressInSearchOverlay);
        }

        if (railroadStationsOverlay != null) {
            mapView.getOverlays().add(railroadStationsOverlay);
        }

        if (userLocationOverlay!=null){
            mapView.getOverlays().add(userLocationOverlay);
        }

        mapView.invalidate();

        /*
        double north=Double.MAX_VALUE, south=Double.MIN_VALUE, west=Double.MIN_VALUE, east=Double.MAX_VALUE;

        for (Stop stop:info.getStaticRouteInfo().getForwardStops()){
            GeoPoint point=stop.getPoint();

            if (point.getLatitude()<north) north=point.getLatitude();
            else if (point.getLatitude()>south) south=point.getLatitude();

            if (point.getLongitude()<east) east=point.getLongitude();
            else if (point.getLongitude()>west) west=point.getLongitude();
        }

        for (Stop stop:info.getStaticRouteInfo().getBackwardStops()){
            GeoPoint point=stop.getPoint();

            if (point.getLatitude()<north) north=point.getLatitude();
            else if (point.getLatitude()>south) south=point.getLatitude();

            if (point.getLongitude()<east) east=point.getLongitude();
            else if (point.getLongitude()>west) west=point.getLongitude();
        }

        BoundingBox box=new BoundingBox();
        box.set(north, east, south, west);

        mapView.zoomToBoundingBox(box, true);
        */

        hideLoading();
        mainHandler.postDelayed(() -> showInfoWindowAboutRoute(), 100);

        setRouteProgress(false);
    }

    private void updateResult(RouteInfo info) {
        currentInfo = info;

        mapView.getOverlays().clear();
        setStaticRoute(info.getStaticRouteInfo());
        setDynamicRoute(info.getDynamicRouteInfo());

        if (addressInSearchOverlay != null) {
            mapView.getOverlays().add(addressInSearchOverlay);
        }

        if (railroadStationsOverlay != null) {
            mapView.getOverlays().add(railroadStationsOverlay);
        }

        if (userLocationOverlay!=null){
            mapView.getOverlays().add(userLocationOverlay);
        }

        mapView.invalidate();
    }

    private void setStaticRoute(StaticRouteInfo info) {
        ArrayList<OverlayItem> items = new ArrayList<>();

        for (Stop stop : info.getForwardStops()) {
            String stopName = Localization.localizeStopName(stop.getName(), Utils.getAppLocaleAsString(requireContext()));

            StopOverlayItem item = new StopOverlayItem(String.format("<font color='%s'><b>%s</b></font>", info.getForwardColor(), getString(R.string.stop_item_title, stop.getCode())), stopName, stop.getPoint());
            item.setMarker(stop.getCode() == routeSelectedStopId ? rManager.getSelectedStopMarker() : rManager.getStopMarker());
            item.setStopId(stop.getCode());
            items.add(item);
        }

        for (Stop stop : info.getBackwardStops()) {
            String stopName = Localization.localizeStopName(stop.getName(), Utils.getAppLocaleAsString(requireContext()));

            StopOverlayItem item = new StopOverlayItem(String.format("<font color='%s'><b>%s</b></font>", info.getBackwardColor(), getString(R.string.stop_item_title, stop.getCode())), stopName, stop.getPoint());
            item.setMarker(stop.getCode() == routeSelectedStopId ? rManager.getSelectedStopMarker() : rManager.getStopMarker());
            item.setStopId(stop.getCode());
            items.add(item);
        }

        ItemizedIconOverlay<OverlayItem> staticRouteOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        mapView.getController().animateTo(item.getPoint());

                        item.setMarker(rManager.getSelectedStopMarker());

                        routeSelectedStopId = ((StopOverlayItem) item).getStopId();

                        Spanned title = Html.fromHtml(item.getTitle(), 0);
                        String snippet = item.getSnippet();
                        showInfoWindow(title, snippet);

                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, requireContext());

        Polyline forwardRouteLine = new Polyline();
        forwardRouteLine.setPoints(info.getForwardShapes());
        forwardRouteLine.setColor(Color.parseColor(info.getForwardColor()));
        forwardRouteLine.setWidth(POLYLINE_ROUTE_WIDTH);
        mapView.getOverlays().add(forwardRouteLine);

        Polyline backwardRouteLine = new Polyline();
        backwardRouteLine.setPoints(info.getBackwardShapes());
        backwardRouteLine.setColor(Color.parseColor(info.getBackwardColor()));
        backwardRouteLine.setWidth(POLYLINE_ROUTE_WIDTH);
        mapView.getOverlays().add(backwardRouteLine);

        mapView.getOverlays().add(staticRouteOverlay);
    }

    private void setDynamicRoute(DynamicRouteInfo info) {
        StaticRouteInfo srInfo = ((RouteInfo) currentInfo).getStaticRouteInfo();

        ArrayList<OverlayItem> items = new ArrayList<>();

        for (TransportInfo tInfo : info.getTransportInfos()) {
            VehicleOverlayItem item = new VehicleOverlayItem(srInfo.getRouteShortName() + " #" + tInfo.getId(), "", tInfo.getLocation());
            item.setMarker(rManager.getRotatedTransportMarker(tInfo.getDirection(), (int)(tInfo.getBearing()/*+mapView.getMapOrientation()*/)));
            item.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            item.setCode(tInfo.getId());
            //item.setBearing(tInfo.getBearing());
            items.add(item);

            if (routeSelectedVehicleCode.equals(tInfo.getId())){
                mapView.getController().animateTo(item.getPoint());

                //mapView.setMapOrientation(tInfo.getBearing());
            }
        }

        ItemizedIconOverlay<OverlayItem> dynamicRouteOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        mapView.getController().animateTo(item.getPoint());

                        routeSelectedVehicleCode=((VehicleOverlayItem) item).getCode();

                        //mapView.setMapOrientation(0f-((VehicleOverlayItem) item).getBearing());

                        String title = item.getTitle();
                        String snippet = Localization.localizeRouteName(Utils.getLongRouteNameFromInfo(currentInfo), Utils.getAppLocaleAsString(requireContext()));
                        showInfoWindow(title, /*snippet*/ ""); //no need to show full route

                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, requireContext());

        mapView.getOverlays().add(dynamicRouteOverlay);
    }

    private void showInfoWindowAboutRoute() {
        if (!(currentInfo instanceof RouteInfo)) return;

        RouteInfo info = (RouteInfo) currentInfo;

        StaticRouteInfo srInfo = info.getStaticRouteInfo();
        DynamicRouteInfo drInfo = info.getDynamicRouteInfo();

        setStaticRoute(srInfo);
        setDynamicRoute(drInfo);

        String routeLongName = Localization.localizeRouteName(srInfo.getRouteLongName(), Utils.getAppLocaleAsString(requireContext()));
        List<Stop> forwardStops=srInfo.getForwardStops(), backwardStops=srInfo.getBackwardStops();
        String routeForward=Localization.localizeRouteName(forwardStops.get(0).getName()+" - "+forwardStops.get(forwardStops.size()-1).getName(), Utils.getAppLocaleAsString(requireContext()));
        String routeBackward=Localization.localizeRouteName(backwardStops.get(0).getName()+" - "+backwardStops.get(backwardStops.size()-1).getName(), Utils.getAppLocaleAsString(requireContext()));

        String title = routeLongName + " (" + srInfo.getRouteShortName() + ")";
        String snippet = String.format("<font color='%s'><b>%s</b></font><br><font color='%s'><b>%s</b></font><br>", srInfo.getForwardColor(), routeForward, srInfo.getBackwardColor(), routeBackward)+(!App.OFFLINE_MODE ? getString(R.string.available_vehicles, drInfo.countTransport()) : "");
        showInfoWindow(title, Html.fromHtml(snippet, 0));
    }

    private void reshowInfoWindowAboutRoute() {
        if (!(currentInfo instanceof RouteInfo)) return;

        RouteInfo info = (RouteInfo) currentInfo;

        StaticRouteInfo srInfo = info.getStaticRouteInfo();
        DynamicRouteInfo drInfo = info.getDynamicRouteInfo();

        String routeLongName = Localization.localizeRouteName(srInfo.getRouteLongName(), Utils.getAppLocaleAsString(requireContext()));
        List<Stop> forwardStops=srInfo.getForwardStops(), backwardStops=srInfo.getBackwardStops();
        String routeForward=Localization.localizeRouteName(forwardStops.get(0).getName()+" - "+forwardStops.get(forwardStops.size()-1).getName(), Utils.getAppLocaleAsString(requireContext()));
        String routeBackward=Localization.localizeRouteName(backwardStops.get(0).getName()+" - "+backwardStops.get(backwardStops.size()-1).getName(), Utils.getAppLocaleAsString(requireContext()));

        String title = routeLongName + " (" + srInfo.getRouteShortName() + ")";
        String snippet = String.format("<font color='%s'><b>%s</b></font><br><font color='%s'><b>%s</b></font><br>", srInfo.getForwardColor(), routeForward, srInfo.getBackwardColor(), routeBackward)+(!App.OFFLINE_MODE ? getString(R.string.available_vehicles, drInfo.countTransport()) : "");
        showInfoWindow(title, Html.fromHtml(snippet, 0));
    }

    private void reshowInfoWindowAboutStop() {
        if (!(currentInfo instanceof Stop)) return;

        Stop info = (Stop) currentInfo;

        String title = getString(R.string.stop_item_title, info.getCode());
        String snippet = Localization.localizeStopName(info.getName(), Utils.getAppLocaleAsString(requireContext())) + "\n";

        String s=info.getRoutesAvailableAsString();
        List<TransferInfo> transfers=info.getTransfersInfo().getTransfers();
        for (TransferInfo tinfo : info.getTransfersInfo().getTransfers()){
            s=s.replace(tinfo.getRouteShortName(), "<font color='"+Utils.getRouteColorCode(transfers.indexOf(tinfo), true)+"'><b>"+tinfo.getRouteShortName()+"</b></font>");
        }
        snippet+=s;

        snippet=snippet.replaceAll("\n", "<br>");

        String[][] timetable = info.getTimetableAsArray();
        if (timetable[0].length == 0 || timetable[1].length == 0) {
            if (!App.OFFLINE_MODE) {
                snippet += "<br><br>" + getString(R.string.stop_no_available_vehicles);
            }

            showInfoWindow(title, Html.fromHtml(snippet, 0));
        } else {
            showInfoWindowTable(title, Html.fromHtml(snippet, 0), timetable);
        }
    }

    private void loadClosest(double latitude, double longitude) {
        loadClosest(latitude, longitude, false);
    }

    private void loadClosest(double latitude, double longitude, boolean asService) {
        if (routingModeLocked) return;

        if (isLoading.get()) return;

        cancelMainTimer();

        isLoading.set(true);

        updateReloadButtonDrawable();

        if (currentWorker != null) {
            currentWorker.dispose();
            currentWorker = null;
        }

        currentWorker = Single.fromCallable(() -> (new Api()).getClosestInfo(latitude, longitude))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final ClosestInfo result) -> {
                    isLoading.set(false);

                    updateReloadButtonDrawable();

                    if (asService) {
                        updateResult(result);
                    } else {
                        handleResult(result);
                    }

                    scheduleOnMainTimer(new TimerTask() {
                        @Override
                        public void run() {
                            loadClosest(latitude, longitude, true);
                        }
                    }, DEFAULT_SERVICE_UPDATE_DELAY);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    updateReloadButtonDrawable();

                    if (asService) {
                        scheduleOnMainTimer(new TimerTask() {
                            @Override
                            public void run() {
                                loadClosest(latitude, longitude, true);
                            }
                        }, DEFAULT_SERVICE_UPDATE_DELAY);
                    } else {
                        handleError(throwable);
                    }
                });
    }

    private void handleResult(ClosestInfo info) {
        currentInfo = info;

        mapView.getOverlays().clear();
        setClosestTransport(info.getTransportsInfo());
        setClosestStops(info.getStopsInfo());

        if (addressInSearchOverlay != null) {
            mapView.getOverlays().add(addressInSearchOverlay);
        }

        if (railroadStationsOverlay != null) {
            mapView.getOverlays().add(railroadStationsOverlay);
        }

        if (userLocationOverlay!=null){
            mapView.getOverlays().add(userLocationOverlay);
        }

        mapView.invalidate();
    }

    private void updateResult(ClosestInfo info) {
        currentInfo = info;

        mapView.getOverlays().clear();
        setClosestTransport(info.getTransportsInfo());
        setClosestStops(info.getStopsInfo());

        if (addressInSearchOverlay != null) {
            mapView.getOverlays().add(addressInSearchOverlay);
        }

        if (railroadStationsOverlay != null) {
            mapView.getOverlays().add(railroadStationsOverlay);
        }

        if (userLocationOverlay!=null){
            mapView.getOverlays().add(userLocationOverlay);
        }

        mapView.invalidate();
    }

    private void setClosestStops(StopsInfo stopsInfo) {
        int routingStopsAdded=0;

        ArrayList<OverlayItem> items = new ArrayList<>();

        for (Stop stop : stopsInfo.getStops()) {
            String stopName = Localization.localizeStopName(stop.getName(), Utils.getAppLocaleAsString(requireContext()));

            StopOverlayItem item = new StopOverlayItem(getString(R.string.stop_item_title, stop.getCode()), stopName, stop.getPoint());

            if ((startStop==null || stop.getCode()!=startStop.getCode()) || (endStop==null || stop.getCode()!=endStop.getCode())) {
                item.setMarker(rManager.getStopMarker());
            } else {
                item.setMarker(rManager.getRoutingSelectedStopMarker());

                routingStopsAdded++;
            }

            item.setStopId(stop.getCode());
            items.add(item);
        }

        ItemizedIconOverlay<OverlayItem> stopsOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        if (routingMode){
                            if (startStop==null) {
                                startStop = ((ClosestInfo) currentInfo).getStopsInfo().getStops().get(index);

                                item.setMarker(rManager.getRoutingSelectedStopMarker());
                                mapView.invalidate();

                                showInfoWindow(getString(R.string.routing_title), Localization.localizeStopName(startStop.getName(), Utils.getAppLocaleAsString(requireContext()))+" - "+getString(R.string.select_finish_stop));
                            } else if (((StopOverlayItem) item).getStopId()!=startStop.getCode()) { //TODO: rework
                                endStop = ((ClosestInfo) currentInfo).getStopsInfo().getStops().get(index);

                                item.setMarker(rManager.getRoutingSelectedStopMarker());
                                mapView.invalidate();

                                routingModeLocked=true;

                                buildRouting();
                            }

                            return true;
                        }

                        if (blockItemClick) return false;

                        blockItemClick = true;

                        loadStop(((StopOverlayItem) item).getStopId());

                        mapView.getController().animateTo(item.getPoint());

                        item.setMarker(rManager.getSelectedStopMarker());

                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, requireContext());

        mapView.getOverlays().add(stopsOverlay);

        //routing
        //TODO: wtf is this?
        if (startStop!=null && routingStopsAdded<2) {
            ArrayList<OverlayItem> routingItems = new ArrayList<>();

            if (startStop!=null) {
                StopOverlayItem firstStop = new StopOverlayItem(getString(R.string.stop_item_title, startStop.getCode()), startStop.getName(), startStop.getPoint());
                firstStop.setMarker(rManager.getRoutingSelectedStopMarker());
                firstStop.setStopId(startStop.getCode());

                routingItems.add(firstStop);
            }

            if (endStop!=null) {
                StopOverlayItem secondStop = new StopOverlayItem(getString(R.string.stop_item_title, endStop.getCode()), endStop.getName(), endStop.getPoint());
                secondStop.setMarker(rManager.getRoutingSelectedStopMarker());
                secondStop.setStopId(endStop.getCode());

                routingItems.add(secondStop);
            }

            ItemizedIconOverlay<OverlayItem> routingOverlay = new ItemizedIconOverlay<>(routingItems,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                            return false;
                        }

                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                            return false;
                        }
                    }, requireContext());

            mapView.getOverlays().add(routingOverlay);
        }
    }

    private void setClosestTransport(ClosestTransportsInfo closestTransportsInfo) {
        ArrayList<OverlayItem> items = new ArrayList<>();

        for (ClosestTransportInfo tInfo : closestTransportsInfo.getTransportsInfos()) {
            VehicleOverlayItem item = new VehicleOverlayItem(tInfo.getShortRouteName() + " #" + tInfo.getId(), null, tInfo.getLocation());
            item.setCode(tInfo.getId());
            item.setMarker(rManager.getRotatedTransportMarker(1, tInfo.getBearing()));
            item.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            items.add(item);
        }

        ItemizedIconOverlay<OverlayItem> transportInfo = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        if (blockItemClick) return false;

                        loadVehicle(((VehicleOverlayItem) item).getCode());

                        mapView.getController().animateTo(item.getPoint());

                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, requireContext());

        mapView.getOverlays().add(transportInfo);
    }

    private void loadStop(int code) {
        loadStop(code, false);
    }

    private void loadStop(int code, boolean asService) {
        if (!isNetworkAvailable() && !App.OFFLINE_MODE) {
            blockItemClick = false;

            Toast.makeText(requireContext(), R.string.reconnect_message, Toast.LENGTH_SHORT).show();
            return;
        }

        cancelMainTimer();

        isLoading.set(true);

        updateReloadButtonDrawable();

        if (!asService) showLoading();

        blockScrollEvent = true;

        if (currentWorker != null) {
            currentWorker.dispose();
            currentWorker = null;
        }

        currentWorker = Single.fromCallable(() -> (new Api()).getStopInfo(code))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final Stop result) -> {
                    isLoading.set(false);

                    updateReloadButtonDrawable();

                    if (asService) {
                        updateResult(result);
                    } else {
                        hideLoading();

                        handleResult(result);

                        blockItemClick = false;
                    }

                    scheduleOnMainTimer(new TimerTask() {
                        @Override
                        public void run() {
                            loadStop(code, true);
                        }
                    }, DEFAULT_SERVICE_UPDATE_DELAY);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    updateReloadButtonDrawable();

                    if (asService) {
                        scheduleOnMainTimer(new TimerTask() {
                            @Override
                            public void run() {
                                loadStop(code, true);
                            }
                        }, DEFAULT_SERVICE_UPDATE_DELAY);
                    } else {
                        hideLoading();

                        handleError(throwable);

                        blockItemClick = false;
                    }
                });
    }

    private void handleResult(Stop info) {
        currentInfo = info;
        savedStopInfo = info;

        mapView.getOverlays().clear();

        String title = getString(R.string.stop_item_title, info.getCode());
        String snippet = Localization.localizeStopName(info.getName(), Utils.getAppLocaleAsString(requireContext())) + "\n";

        String s=info.getRoutesAvailableAsString();
        List<TransferInfo> transfers=info.getTransfersInfo().getTransfers();
        for (TransferInfo tinfo : info.getTransfersInfo().getTransfers()){
            s=s.replace(tinfo.getRouteShortName(), "<font color='"+Utils.getRouteColorCode(transfers.indexOf(tinfo), true)+"'><b>"+tinfo.getRouteShortName()+"</b></font>");
        }
        snippet+=s;

        snippet=snippet.replaceAll("\n", "<br>");

        String[][] timetable = info.getTimetableAsArray();
        if (timetable[0].length == 0 || timetable[1].length == 0) {
            if (!App.OFFLINE_MODE) {
                snippet += "<br><br>" + getString(R.string.stop_no_available_vehicles);
            }

            showInfoWindow(title, Html.fromHtml(snippet, 0));
        } else {
            showInfoWindowTable(title, Html.fromHtml(snippet, 0), timetable);
        }

        setStopTransfers(info.getTransfersInfo());
        setStop(info);
        setComingTransport(info.getStopTimetablesInfo());

        if (addressInSearchOverlay != null) {
            mapView.getOverlays().add(addressInSearchOverlay);
        }

        if (railroadStationsOverlay != null) {
            mapView.getOverlays().add(railroadStationsOverlay);
        }

        if (userLocationOverlay!=null){
            mapView.getOverlays().add(userLocationOverlay);
        }

        mapView.invalidate();

        setStopProgress(false);

        if (!notScrollToStopAfterOnBackPressed) {
            mapView.getController().animateTo(info.getPoint());
        }

        notScrollToStopAfterOnBackPressed=false;
    }

    private void updateResult(Stop info) {
        currentInfo = info;
        savedStopInfo = info;

        mapView.getOverlays().clear();

        String title = getString(R.string.stop_item_title, info.getCode());
        String snippet = Localization.localizeStopName(info.getName(), Utils.getAppLocaleAsString(requireContext())) + "\n";

        String s=info.getRoutesAvailableAsString();
        List<TransferInfo> transfers=info.getTransfersInfo().getTransfers();
        for (TransferInfo tinfo : info.getTransfersInfo().getTransfers()){
            s=s.replace(tinfo.getRouteShortName(), "<font color='"+Utils.getRouteColorCode(transfers.indexOf(tinfo), true)+"'><b>"+tinfo.getRouteShortName()+"</b></font>");
        }
        snippet+=s;

        snippet=snippet.replaceAll("\n", "<br>");

        String[][] timetable = info.getTimetableAsArray();
        if (timetable[0].length == 0 || timetable[1].length == 0) {
            if (!App.OFFLINE_MODE) {
                snippet += "\n\n" + getString(R.string.stop_no_available_vehicles);
            }

            showInfoWindow(title, Html.fromHtml(snippet, 0));
        } else {
            showInfoWindowTable(title, Html.fromHtml(snippet, 0), timetable);
        }

        setStopTransfers(info.getTransfersInfo());
        setStop(info);
        setComingTransport(info.getStopTimetablesInfo());

        if (addressInSearchOverlay != null) {
            mapView.getOverlays().add(addressInSearchOverlay);
        }

        if (railroadStationsOverlay != null) {
            mapView.getOverlays().add(railroadStationsOverlay);
        }

        if (userLocationOverlay!=null){
            mapView.getOverlays().add(userLocationOverlay);
        }

        mapView.invalidate();
    }

    private void setStop(Stop stop) {
        ArrayList<OverlayItem> items = new ArrayList<>();

        String stopName = Localization.localizeStopName(stop.getName(), Utils.getAppLocaleAsString(requireContext()));

        StopOverlayItem item = new StopOverlayItem(getString(R.string.stop_item_title, stop.getCode()), stopName, stop.getPoint());
        item.setMarker(rManager.getSelectedStopMarker());
        item.setStopId(stop.getCode());
        items.add(item);

        ItemizedIconOverlay<OverlayItem> stopsOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        handleResult((Stop) currentInfo);

                        mapView.getController().animateTo(item.getPoint());

                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, requireContext());

        mapView.getOverlays().add(stopsOverlay);
    }

    private void setStopTransfers(TransfersInfo info) {
        List<TransferInfo> transfers = info.getTransfers();

        for (TransferInfo tInfo : transfers) {
            Polyline transferLine = new Polyline();
            transferLine.setPoints(tInfo.getShapes());
            transferLine.setColor(Utils.getRouteColor(transfers.indexOf(tInfo), true));
            transferLine.setWidth(POLYLINE_ROUTE_WIDTH);
            transferLine.setOnClickListener((polyline, mapView, eventPos) -> {
                loadRoute(tInfo.getRouteShortName());

                return true;
            });

            mapView.getOverlays().add(transferLine);
        }
    }

    private void setComingTransport(StopTimetablesInfo info) {
        ArrayList<OverlayItem> items = new ArrayList<>();

        for (StopTimetable stopTimetable : info.getTimetables()) {
            TransportInfo tInfo = stopTimetable.getTransportInfo();

            VehicleOverlayItem item = new VehicleOverlayItem("", "", tInfo.getLocation());
            item.setCode(tInfo.getId());
            item.setMarker(rManager.getRotatedTransportMarker(tInfo.getDirection(), tInfo.getBearing()));
            item.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            items.add(item);
        }

        ItemizedIconOverlay<OverlayItem> comingTransportOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        loadVehicle(((VehicleOverlayItem) item).getCode());

                        mapView.getController().animateTo(item.getPoint());

                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, requireContext());

        mapView.getOverlays().add(comingTransportOverlay);
    }

    private void loadVehicle(String code) {
        loadVehicle(code, false);
    }

    private void loadVehicle(String code, boolean asService) {
        cancelMainTimer();

        isLoading.set(true);

        updateReloadButtonDrawable();

        if (!asService) showLoading();

        blockScrollEvent = true;

        if (currentWorker != null) {
            currentWorker.dispose();
            currentWorker = null;
        }

        currentWorker = Single.fromCallable(() -> (new Api()).getVehicleInfo(code))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final VehicleInfo result) -> {
                    isLoading.set(false);

                    updateReloadButtonDrawable();

                    if (asService) {
                        updateResult(result);
                    } else {
                        hideLoading();

                        handleResult(result);

                        blockItemClick = false;
                    }

                    scheduleOnMainTimer(new TimerTask() {
                        @Override
                        public void run() {
                            loadVehicle(code, true);
                        }
                    }, DEFAULT_SERVICE_UPDATE_DELAY);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    updateReloadButtonDrawable();

                    if (asService) {
                        scheduleOnMainTimer(new TimerTask() {
                            @Override
                            public void run() {
                                loadVehicle(code, true);
                            }
                        }, DEFAULT_SERVICE_UPDATE_DELAY);
                    } else {
                        hideLoading();

                        handleError(throwable);

                        blockItemClick = false;
                    }
                });
    }

    private void handleResult(VehicleInfo info) {
        currentInfo = info;

        TransportInfo transportInfo = info.getTransportInfo();
        StaticRouteInfo staticRouteInfo = info.getStaticRouteInfo();

        mapView.getOverlays().clear();

        String routeLongName = Localization.localizeRouteName(staticRouteInfo.getRouteLongName(), Utils.getAppLocaleAsString(requireContext()));

        String title = transportInfo.getShortRouteName() + " #" + transportInfo.getId();
        String snippet = routeLongName;

        showInfoWindow(title, snippet);

        setStaticRoute(staticRouteInfo);
        setVehicle(info);

        if (addressInSearchOverlay != null) {
            mapView.getOverlays().add(addressInSearchOverlay);
        }

        if (railroadStationsOverlay != null) {
            mapView.getOverlays().add(railroadStationsOverlay);
        }

        if (userLocationOverlay!=null){
            mapView.getOverlays().add(userLocationOverlay);
        }

        mapView.invalidate();
    }

    private void updateResult(VehicleInfo info) {
        currentInfo = info;

        TransportInfo transportInfo = info.getTransportInfo();
        StaticRouteInfo staticRouteInfo = info.getStaticRouteInfo();

        mapView.getOverlays().clear();

        setStaticRoute(staticRouteInfo);
        setVehicle(info);

        if (addressInSearchOverlay != null) {
            mapView.getOverlays().add(addressInSearchOverlay);
        }

        if (railroadStationsOverlay != null) {
            mapView.getOverlays().add(railroadStationsOverlay);
        }

        if (userLocationOverlay!=null){
            mapView.getOverlays().add(userLocationOverlay);
        }

        mapView.invalidate();
    }

    private void setVehicle(VehicleInfo info) {
        TransportInfo tInfo = info.getTransportInfo();

        ArrayList<OverlayItem> items = new ArrayList<>();

        OverlayItem item = new OverlayItem("", "", tInfo.getLocation());
        item.setMarker(rManager.getRotatedTransportMarker(tInfo.getDirection(), tInfo.getBearing()));
        items.add(item);

        ItemizedIconOverlay<OverlayItem> dynamicRouteOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, requireContext());

        mapView.getOverlays().add(dynamicRouteOverlay);
    }

    @SuppressLint("CheckResult")
    private void loadAddress(String address) {
        Locale locale = Utils.getAppLocale(requireContext());

        Single.fromCallable(() -> (new Api()).findAddress(address, locale))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final List<Address> addresses) -> {
                    setupSearchAddressLayout(addresses);
                }, (@NonNull final Throwable throwable) -> {
                    //do nothing?
                });
    }

    private void setupSearchAddressLayout(List<Address> addresses) {
        ListView listView = searchAddressLayout.findViewById(R.id.address_listview);

        if (addresses == null || addresses.size() == 0) {
            setAddressProgress(false);

            //available nonnull adapter means address view has some records
            setupArrowButton(listView.getAdapter() != null);

            Toast.makeText(requireContext(), getString(R.string.address_not_found), Toast.LENGTH_SHORT).show();

            return;
        }

        ArrayList<Address> clearedAddresses=new ArrayList<>(addresses);
        if (defaultPreferences.getBoolean("show_only_city_addresses", true)) {
            for (Address address : addresses) {
                String locality=address.getLocality();

                if (locality==null || (!locality.equalsIgnoreCase("lviv") && !locality.equalsIgnoreCase("львів"))) {
                    clearedAddresses.remove(address);
                }
            }
        }

        listView.setOnItemClickListener((parent, view, position, id) -> {
            animateAddressView(false, null, () -> {
                navigateToAddress((Address) parent.getAdapter().getItem(position));
            });
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            animateAddressView(false, null, () -> {
                Address address=(Address) parent.getAdapter().getItem(position);
                GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());
                mapView.getController().animateTo(point);
            });

            return true;
        });

        AddressAdapter adapter = new AddressAdapter(requireContext(), clearedAddresses);
        listView.setAdapter(adapter);

        if (listView.getVisibility() == View.GONE) {
            animateAddressView(true, () -> setAddressProgress(false), null);
        } else {
            setAddressProgress(false);
        }
    }

    private void navigateToAddress(Address address) {
        if (addressInSearchOverlay != null) {
            mapView.getOverlays().remove(addressInSearchOverlay);
        }

        GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());

        Marker addressMarker=new Marker(mapView);
        addressMarker.setTitle(address.getAddressLine(0));
        addressMarker.setIcon(rManager.getAddressMarker());
        addressMarker.setPosition(point);
        addressMarker.setDraggable(false);
        //addressMarker.showInfoWindow();

        addressMarker.setOnMarkerClickListener((marker, mapView) -> {
            /*if (marker.isInfoWindowShown()){
                marker.closeInfoWindow();
            } else {
                marker.showInfoWindow();
            }*/

            if (addressInSearchOverlay != null) {
                //marker.closeInfoWindow();

                mapView.getOverlays().remove(addressInSearchOverlay);
                mapView.invalidate();

                addressInSearchOverlay = null;
            }

            return true;
        });

        mapView.getOverlays().add(addressMarker);

        addressInSearchOverlay=addressMarker;

        mapView.getController().animateTo(point);
    }

    private void buildRouting() {
        if (isLoading.get()) return;

        cancelMainTimer();

        isLoading.set(true);

        showInfoWindow(getString(R.string.routing_title), Localization.localizeStopName(startStop.getName(), Utils.getAppLocaleAsString(requireContext()))+" - "+Localization.localizeStopName(endStop.getName(), Utils.getAppLocaleAsString(requireContext()))+"\n"+getString(R.string.building_route_message));

        ArrayList<OverlayItem> routingItems = new ArrayList<>();

        StopOverlayItem firstStop = new StopOverlayItem(getString(R.string.stop_item_title, startStop.getCode()), startStop.getName(), startStop.getPoint());
        firstStop.setMarker(rManager.getRoutingSelectedStopMarker());
        firstStop.setStopId(startStop.getCode());

        routingItems.add(firstStop);

        StopOverlayItem secondStop = new StopOverlayItem(getString(R.string.stop_item_title, endStop.getCode()), endStop.getName(), endStop.getPoint());
        secondStop.setMarker(rManager.getRoutingSelectedStopMarker());
        secondStop.setStopId(endStop.getCode());

        routingItems.add(secondStop);

        ItemizedIconOverlay<OverlayItem> routingOverlay = new ItemizedIconOverlay<>(routingItems,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, requireContext());

        mapView.getOverlays().clear();
        mapView.getOverlays().add(routingOverlay);
        mapView.invalidate();

        if (currentWorker != null) {
            currentWorker.dispose();
            currentWorker = null;
        }

        currentWorker = Single.fromCallable(() -> (new Api()).getRoutingStopsInfo(startStop.getCode(), endStop.getCode()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final RoutingStopsInfo result) -> {
                    isLoading.set(false);

                    handleResult(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    handleError(throwable);
                });
    }

    private void handleResult(RoutingStopsInfo info) {
        Stop info1 = info.getStartStop(), info2 = info.getEndStop();

        ArrayList<TransferInfo> rinfo1 = new ArrayList<>(), rinfo2 = new ArrayList<>();

        for (TransferInfo tinfo1 : info1.getTransfersInfo().getTransfers()) {
            for (TransferInfo tinfo2 : info2.getTransfersInfo().getTransfers()) {
                if (info1.getRoutesAvailable().contains(tinfo1.getRouteShortName()) && info2.getRoutesAvailable().contains(tinfo1.getRouteShortName())) {
                    rinfo1.add(tinfo1);

                    continue;
                }

                if (info1.getRoutesAvailable().contains(tinfo2.getRouteShortName()) && info2.getRoutesAvailable().contains(tinfo2.getRouteShortName())) {
                    rinfo1.add(tinfo2);

                    continue;
                }
            }
        }

        if (rinfo1.size()==0) {
            for (TransferInfo tinfo1 : info1.getTransfersInfo().getTransfers()) {
                for (TransferInfo tinfo2 : info2.getTransfersInfo().getTransfers()) {
                    List<GeoPoint> points1 = tinfo1.getShapes();
                    List<GeoPoint> points2 = tinfo2.getShapes();

                    for (int a = 0; a < points1.size() - 1; a++) {
                        for (int b = 0; b < points2.size() - 1; b++) {
                            if (a>=(points1.size()-1) || b>=(points2.size()-1)){
                                break;
                            }

                            //first line
                            GeoPoint p1 = points1.get(a);
                            GeoPoint p2 = points1.get(a + 1);

                            //second line
                            GeoPoint p3 = points2.get(b);
                            GeoPoint p4 = points2.get(b + 1);

                            if (lineLineIntersection(p1, p2, p3, p4)) {
                                if (!rinfo1.contains(tinfo1) && !rinfo2.contains(tinfo2)) {
                                    rinfo1.add(tinfo1);
                                    rinfo2.add(tinfo2);
                                }
                            }

                            a++;
                            b++;
                        }
                    }
                }
            }
        }

        //probably, unreachable branch
        if (rinfo1.size()==0 && rinfo2.size()==0) {
            Toast.makeText(requireContext(), getString(R.string.cant_routing_message), Toast.LENGTH_SHORT).show();

            //TODO: rework
            onBackPressed();

            return;
        }

        ArrayList<String> names1=new ArrayList<>(), names2=new ArrayList<>();

        for (int i=0; i<rinfo1.size(); i++){
            names1.add(rinfo1.get(i).getRouteShortName());
            if (rinfo2.size()>0) names2.add(rinfo2.get(i).getRouteShortName());
        }

        buildRouting2(names1, names2);
    }

    private void buildRouting2(List<String> names1, List<String> names2) {
        if (isLoading.get()) return;

        cancelMainTimer();

        isLoading.set(true);

        if (currentWorker != null) {
            currentWorker.dispose();
            currentWorker = null;
        }

        currentWorker = Single.fromCallable(() -> (new Api()).getRoutingRoutesInfo(names1, names2))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull final RoutingRoutesInfo result) -> {
                    isLoading.set(false);

                    handleResult2(result);
                }, (@NonNull final Throwable throwable) -> {
                    isLoading.set(false);

                    handleError(throwable);
                });
    }

    private void handleResult2(RoutingRoutesInfo info) {
        routingRoutesInfo=info;

        List<StaticRouteInfo> sInfo1 = info.getStaticRoute1();
        List<DynamicRouteInfo> dInfo1=info.getDynamicRoute1();
        List<StaticRouteInfo> sInfo2 = info.getStaticRoute2();
        List<DynamicRouteInfo> dInfo2=info.getDynamicRoute2();

        StaticRouteInfo info1=null, info2=null;

        int index=0;

        if (sInfo2.size()==0){
            info1=sInfo1.get(0);

            for (StaticRouteInfo sinfo:sInfo1){
                int newIndex=sInfo1.indexOf(sinfo);

                if (dInfo1.get(newIndex).countTransport()>dInfo1.get(index).countTransport()){
                    info1=sinfo;

                    index=newIndex;
                }
            }
        } else {
            //first possible variant as fallback
            int bestIndex = 0;
            double bestAvg = 0;

            for (int i = 0; i < sInfo1.size(); i++) {
                double newAvg = (dInfo1.get(i).countTransport() + dInfo2.get(i).countTransport()) / 2;
                if (newAvg > bestAvg) {
                    bestAvg = newAvg;

                    bestIndex = i;
                }
            }

            info1 = sInfo1.get(bestIndex);
            info2 = sInfo2.get(bestIndex);
        }

        routingRoutesInfo = new RoutingRoutesInfo();
        routingRoutesInfo.addStaticRoute1(info1);
        routingRoutesInfo.addStaticRoute2(info2);

        if (info1 != null) {
            Polyline line1 = new Polyline();
            line1.setPoints(info1.getForwardShapes());
            line1.setColor(Color.parseColor(info1.getForwardColor()));
            line1.setWidth(POLYLINE_ROUTE_WIDTH);

            Polyline line2 = new Polyline();
            line2.setPoints(info1.getBackwardShapes());
            line2.setColor(Utils.getRouteColor(0, true));
            line2.setWidth(POLYLINE_ROUTE_WIDTH);

            mapView.getOverlays().add(line1);
            mapView.getOverlays().add(line2);
        }

        if (info2 != null) {
            Polyline line3 = new Polyline();
            line3.setPoints(info2.getForwardShapes());
            line3.setColor(Color.parseColor(info2.getForwardColor()));
            line3.setWidth(POLYLINE_ROUTE_WIDTH);

            Polyline line4 = new Polyline();
            line4.setPoints(info2.getBackwardShapes());
            line4.setColor(Utils.getRouteColor(1, true));
            line4.setWidth(POLYLINE_ROUTE_WIDTH);

            mapView.getOverlays().add(line3);
            mapView.getOverlays().add(line4);
        }

        if (info1 != null) {
            ArrayList<OverlayItem> items = new ArrayList<>();

            for (Stop stop : info1.getForwardStops()) {
                String stopName = Localization.localizeStopName(stop.getName(), Utils.getAppLocaleAsString(requireContext()));

                StopOverlayItem item = new StopOverlayItem(Utils.getRouteArrow(0, false) + getString(R.string.stop_item_title, stop.getCode()), stopName, stop.getPoint());
                item.setMarker(startStop.getCode() == stop.getCode() ? rManager.getRoutingSelectedStopMarker() : (endStop.getCode() == stop.getCode() ? rManager.getRoutingSelectedStopMarker() : rManager.getStopMarker()));
                item.setStopId(stop.getCode());
                items.add(item);
            }

            for (Stop stop : info1.getBackwardStops()) {
                String stopName = Localization.localizeStopName(stop.getName(), Utils.getAppLocaleAsString(requireContext()));

                StopOverlayItem item = new StopOverlayItem(Utils.getRouteArrow(1, false) + getString(R.string.stop_item_title, stop.getCode()), stopName, stop.getPoint());
                item.setMarker(startStop.getCode() == stop.getCode() ? rManager.getRoutingSelectedStopMarker() : (endStop.getCode() == stop.getCode() ? rManager.getRoutingSelectedStopMarker() : rManager.getStopMarker()));
                item.setStopId(stop.getCode());
                items.add(item);
            }

            ItemizedIconOverlay<OverlayItem> staticRouteOverlay = new ItemizedIconOverlay<>(items,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                            routingSelectedStop=(StopOverlayItem) item;

                            item.setMarker(rManager.getSelectedStopMarker());

                            mapView.invalidate();

                            String title = item.getTitle();
                            String snippet = item.getSnippet();
                            showInfoWindow(title, snippet);

                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                            return false;
                        }
                    }, requireContext());

            mapView.getOverlays().add(staticRouteOverlay);
        }

        if (info2 != null) {
            ArrayList<OverlayItem> items = new ArrayList<>();

            for (Stop stop : info2.getForwardStops()) {
                String stopName = Localization.localizeStopName(stop.getName(), Utils.getAppLocaleAsString(requireContext()));

                StopOverlayItem item = new StopOverlayItem(Utils.getRouteArrow(0, false) + getString(R.string.stop_item_title, stop.getCode()), stopName, stop.getPoint());
                item.setMarker(startStop.getCode() == stop.getCode() ? rManager.getRoutingSelectedStopMarker() : (endStop.getCode() == stop.getCode() ? rManager.getRoutingSelectedStopMarker() : rManager.getStopMarker()));
                item.setStopId(stop.getCode());
                items.add(item);
            }

            for (Stop stop : info2.getBackwardStops()) {
                String stopName = Localization.localizeStopName(stop.getName(), Utils.getAppLocaleAsString(requireContext()));

                StopOverlayItem item = new StopOverlayItem(Utils.getRouteArrow(1, false) + getString(R.string.stop_item_title, stop.getCode()), stopName, stop.getPoint());
                item.setMarker(startStop.getCode() == stop.getCode() ? rManager.getRoutingSelectedStopMarker() : (endStop.getCode() == stop.getCode() ? rManager.getRoutingSelectedStopMarker() : rManager.getStopMarker()));
                item.setStopId(stop.getCode());
                items.add(item);
            }

            ItemizedIconOverlay<OverlayItem> staticRouteOverlay = new ItemizedIconOverlay<>(items,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                            routingSelectedStop=(StopOverlayItem) item;

                            item.setMarker(rManager.getSelectedStopMarker());

                            mapView.invalidate();

                            String title = item.getTitle();
                            String snippet = item.getSnippet();
                            showInfoWindow(title, snippet);

                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                            return false;
                        }
                    }, requireContext());

            mapView.getOverlays().add(staticRouteOverlay);
        }

        mapView.invalidate();

        CharSequence snippet="";
        if (info1!=null && info2!=null){
            snippet=Html.fromHtml(String.format("%s<br>%s (<font color='%s'><b>%s</b></font>)<br>%s<br>%s (<font color='%s'><b>%s</b></font>)", getString(R.string.route_built_prefix), info1.getRouteLongName(), Utils.getRouteColorCode(0, true), info1.getRouteShortName(), getString(R.string.and), info2.getRouteLongName(), Utils.getRouteColorCode(1, true), info2.getRouteShortName()), 0);
        } else if (info1!=null){
            snippet=Html.fromHtml(String.format("%s<br>%s (<font color='%s'><b>%s</b></font>)", getString(R.string.route_built_prefix), info1.getRouteLongName(), Utils.getRouteColorCode(0, true), info1.getRouteShortName()), 0);
        }

        showInfoWindow(getString(R.string.routing_title), snippet);
    }

    private boolean lineLineIntersection(GeoPoint A, GeoPoint B, GeoPoint C, GeoPoint D) {
        double a1 = B.getLongitude() - A.getLongitude();
        double b1 = A.getLatitude() - B.getLatitude();
        //double c1 = a1 * (A.getLatitude()) + b1 * (A.getLongitude());

        double a2 = D.getLongitude() - C.getLongitude();
        double b2 = C.getLatitude() - D.getLatitude();
        //double c2 = a2 * (C.getLatitude()) + b2 * (C.getLongitude());

        double determinant = a1 * b2 - a2 * b1;

        if (determinant == 0) {
            //lines don't intersect
            return false;
        } else {
            //double x = (b2 * c1 - b1 * c2) / determinant;
            //double y = (a1 * c2 - a2 * c1) / determinant;
            //return new GeoPoint(x, y);

            return true;
        }
    }

    private void handleNoNetworkMessage(boolean show) {
        LinearLayout messageBox = rootView.findViewById(R.id.no_internet_layout);
        messageBox.setVisibility(show ? View.VISIBLE : View.GONE);

        setNetworkMessagePosition();
    }

    private void setNetworkMessagePosition() {
        LinearLayout messageBox = rootView.findViewById(R.id.no_internet_layout);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) messageBox.getLayoutParams();
        if (searchAddressLayout.getVisibility() == View.VISIBLE) {
            params.bottomToTop = R.id.search_address_layout;
        } else if (searchStopLayout.getVisibility() == View.VISIBLE) {
            params.bottomToTop = R.id.search_stop_layout;
        } else {
            params.bottomToTop = R.id.search_route_layout;
        }
        messageBox.setLayoutParams(params);

        setLocationButtonPosition();
    }

    private void setLocationButtonPosition() {
        LinearLayout messageBox = rootView.findViewById(R.id.no_internet_layout);

        ImageButton locBtn = rootView.findViewById(R.id.location_btn);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) locBtn.getLayoutParams();
        if (messageBox.getVisibility() == View.VISIBLE) {
            params.bottomToTop = R.id.no_internet_layout;
        } else {
            if (searchAddressLayout.getVisibility() == View.VISIBLE) {
                params.bottomToTop = R.id.search_address_layout;
            } else if (searchStopLayout.getVisibility() == View.VISIBLE) {
                params.bottomToTop = R.id.search_stop_layout;
            } else {
                params.bottomToTop = R.id.search_route_layout;
            }
        }
        locBtn.setLayoutParams(params);
    }

    private void handleError(Throwable err) {
        hideInfoWindow();

        if (err instanceof RouteNotFound) {
            setRouteProgress(false);

            Toast.makeText(requireContext(), getString(R.string.route_not_found), Toast.LENGTH_SHORT).show();

            reshowInfoWindowAboutRoute();
            reshowInfoWindowAboutStop();
        } else if (err instanceof StopNotFound) {
            setStopProgress(false);

            Toast.makeText(requireContext(), getString(R.string.stop_not_found), Toast.LENGTH_SHORT).show();

            reshowInfoWindowAboutRoute();
            reshowInfoWindowAboutStop();
        } else if (Utils.isNetworkRelated(err)) {
            //Toast.makeText(requireContext(), getString(R.string.network_connected_error), Toast.LENGTH_SHORT).show();

            //do nothing
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_has_happened) + (Utils.isNullOrEmpty(err.getMessage()) ? "" : ":\n" + err.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void showInfoWindow(CharSequence title, CharSequence snippet) {
        isInfoWindowShown = true;

        TextView titleView = infoWindowLayout.findViewById(R.id.title);
        titleView.setText(title);
        TextView snippetView = infoWindowLayout.findViewById(R.id.snippet);
        snippetView.setText(snippet);

        //very dirty hack to prevent infowindow's width from jumping
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) snippetView.getLayoutParams();
        params.width = mapView.getWidth() / 2;
        snippetView.setLayoutParams(params);

        ListView listView = infoWindowLayout.findViewById(R.id.listview);
        listView.setVisibility(View.GONE);

        if (Utils.isNullOrEmpty(snippet)) {
            snippetView.setVisibility(View.GONE);
        } else {
            snippetView.setVisibility(View.VISIBLE);
        }

        infoWindowLayout.setVisibility(View.VISIBLE);
        infoWindowLayout.post(() -> {
            if (snippetView.getWidth() > titleView.getWidth()) {
                params.width = infoWindowLayout.getWidth();
                snippetView.setLayoutParams(params);
            }
        });
    }

    private void showInfoWindowTable(CharSequence title, CharSequence snippet, String[][] timetable) {
        showInfoWindow(title, snippet);

        String[] routes = timetable[0];
        String[] timeLefts = timetable[1];

        if (routes.length > 0 && timeLefts.length > 0) {
            ListView listView = infoWindowLayout.findViewById(R.id.listview);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) listView.getLayoutParams();
            params.width = (int) (mapView.getWidth() / 2.25); //magic number
            listView.setLayoutParams(params);
            listView.setVisibility(View.VISIBLE);

            TimetableAdapter adapter = new TimetableAdapter(requireContext(), timeLefts, routes);
            listView.setAdapter(adapter);

            //inaudibly dirty hack, but didn't find a better solution
            listView.post(() -> {
                int height = listView.getChildAt(0).getHeight() * (adapter.getCount() > 5 ? 5 : adapter.getCount());
                params.height = height;
                listView.setLayoutParams(params);
            });
        }
    }

    private void hideInfoWindow() {
        isInfoWindowShown = false;

        infoWindowLayout.setVisibility(View.GONE);

        Utils.closeKeyboard(requireActivity());
    }

    private void toggleInfoWindowVisibility(boolean show) {
        infoWindowLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setAddressProgress(boolean show) {
        ImageButton addressImgBtn = searchAddressLayout.findViewById(R.id.address_img_btn);
        ImageButton arrowImgBtn = searchAddressLayout.findViewById(R.id.address_updown_img_btn);
        ProgressBar addressProgressbar = searchAddressLayout.findViewById(R.id.address_progressbar);

        addressImgBtn.setVisibility(show ? View.GONE : View.VISIBLE);
        arrowImgBtn.setVisibility(show ? View.GONE : View.VISIBLE);
        addressProgressbar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setRouteProgress(boolean show) {
        ImageButton routeImgBtn = rootView.findViewById(R.id.route_img_btn);
        ProgressBar routeProgressbar = rootView.findViewById(R.id.route_progressbar);

        routeImgBtn.setVisibility(show ? View.GONE : View.VISIBLE);
        routeProgressbar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setStopProgress(boolean show) {
        ImageButton routeImgBtn = rootView.findViewById(R.id.stop_img_btn);
        ProgressBar routeProgressbar = rootView.findViewById(R.id.stop_progressbar);

        routeImgBtn.setVisibility(show ? View.GONE : View.VISIBLE);
        routeProgressbar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void animateAddressView(boolean show, Runnable openPostCallback, Runnable closePostCallback) {
        ListView listView = searchAddressLayout.findViewById(R.id.address_listview);

        Animator.AnimatorListener openAnimationCallback = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mainHandler.post(() -> {
                    if (isInfoWindowShown) toggleInfoWindowVisibility(false);

                    listView.setVisibility(View.VISIBLE);
                    setupArrowButton(true);

                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) searchAddressLayout.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    searchAddressLayout.setLayoutParams(params);

                    if (openPostCallback != null) openPostCallback.run();
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        Animator.AnimatorListener closeAnimationCallback = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mainHandler.post(() -> {
                    listView.setVisibility(View.GONE);
                    setupArrowButton(true);

                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) searchAddressLayout.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    searchAddressLayout.setLayoutParams(params);

                    if (isInfoWindowShown) toggleInfoWindowVisibility(true);

                    if (closePostCallback != null) closePostCallback.run();
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        if (show) {
            searchAddressLayout
                    .animate()
                    .translationY(mapView.getY() + Utils.getStatusBarHeight(requireContext()))
                    .setDuration(500)
                    .setListener(openAnimationCallback);
        } else {
            searchAddressLayout
                    .animate()
                    .translationY(0)
                    .setDuration(500)
                    .setListener(closeAnimationCallback);
        }
    }

    private boolean isAddressViewExpanded() {
        return searchAddressLayout.findViewById(R.id.address_listview).getVisibility() == View.VISIBLE;
    }

    private void setupArrowButton(boolean show) {
        ImageButton imgBtn = searchAddressLayout.findViewById(R.id.address_img_btn);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imgBtn.getLayoutParams();
        params.setMarginEnd(show ? imgBtn.getWidth() : 0);
        imgBtn.setLayoutParams(params);

        EditText addressInputField = searchAddressLayout.findViewById(R.id.sa_input_field);
        FrameLayout.LayoutParams params2 = (FrameLayout.LayoutParams) addressInputField.getLayoutParams();
        params2.setMarginEnd(show ? imgBtn.getWidth() * 2 : imgBtn.getWidth());
        addressInputField.setLayoutParams(params2);

        ImageButton button = searchAddressLayout.findViewById(R.id.address_updown_img_btn);
        button.setOnClickListener(v -> animateAddressView(!isAddressViewExpanded(), null, null));

        Drawable d = isAddressViewExpanded() ?
                AppCompatResources.getDrawable(requireContext(), R.drawable.ic_kb_arrow_down_black)
                : AppCompatResources.getDrawable(requireContext(), R.drawable.ic_kb_arrow_up_black);
        button.setImageDrawable(d);

        button.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateReloadButtonDrawable(){
        if (isLoading.get()){
            reloadBtn.setImageResource(R.drawable.ic_cancel_black);
        } else {
            reloadBtn.setImageResource(R.drawable.ic_reload_black);
        }
    }

    private void showOrHideAdditionalControls(boolean show){
        if (show){
            View toggleableControlsLayout=additionalControlsLayout.findViewById(R.id.toggleable_controls_layout);
            AnimationUtils.expandAdditionalControls(toggleableControlsLayout);

            ImageButton expandBtn=additionalControlsLayout.findViewById(R.id.expand_btn);
            expandBtn.setImageResource(R.drawable.ic_expand_less_black);
        } else {
            View toggleableControlsLayout=additionalControlsLayout.findViewById(R.id.toggleable_controls_layout);
            AnimationUtils.collapseAdditionalControls(toggleableControlsLayout);

            ImageButton expandBtn=additionalControlsLayout.findViewById(R.id.expand_btn);
            expandBtn.setImageResource(R.drawable.ic_expand_more_black);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        this.rootView=rootView;

        mapView = rootView.findViewById(R.id.map_view);

        infoWindowLayout = rootView.findViewById(R.id.infowindow_layout);
        Utils.fixLayoutTopPadding(infoWindowLayout);
        infoWindowLayout.setOnTouchListener((v, event) -> {
            //prevents clicking on views that are under infowindow

            return true;
        });

        searchRouteLayout = rootView.findViewById(R.id.search_route_layout);
        searchStopLayout = rootView.findViewById(R.id.search_stop_layout);
        searchAddressLayout = rootView.findViewById(R.id.search_address_layout);

        inputField = rootView.findViewById(R.id.input_field);
        inputField.setOnEditorActionListener((v, actionId, event) -> {
            EditText editText = (EditText) v;

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!isNetworkAvailable() && !App.OFFLINE_MODE) {
                    Toast.makeText(requireContext(), R.string.reconnect_message, Toast.LENGTH_SHORT).show();
                    return false;
                }

                String query = editText.getText().toString();

                if (Utils.isNullOrEmpty(query) || !query.matches("[0-9]+")) {
                    return false;
                }

                if (query.length()==1) {
                    query="0"+query;
                }

                setRouteProgress(true);

                Utils.closeKeyboard(requireActivity());
                editText.clearFocus();

                if (Utils.isNullOrEmpty(query)) {
                    Toast.makeText(requireContext(), getString(R.string.empty_input), Toast.LENGTH_SHORT).show();
                    return false;
                }

                long type = vehicleTypeSpinner.getSelectedItemId();
                if (type == 0) {
                    query = "A" + query;
                } else if (type == 1) {
                    query = "T" + query;
                } else if (type == 2) {
                    query = "T" + query;
                }

                blockScrollEvent = true;

                savedStopInfo = null;

                cancelMainTimer();

                loadRoute(query);

                return true;
            }

            return false;
        });

        ImageButton routeImgBtn = rootView.findViewById(R.id.route_img_btn);
        routeImgBtn.setOnClickListener(v -> inputField.setText(""));

        vehicleTypeSpinner = rootView.findViewById(R.id.vehicle_type_spinner);
        String[] items = new String[]{getString(R.string.bus), getString(R.string.tram), getString(R.string.trolleybus)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
        vehicleTypeSpinner.setAdapter(adapter);

        searchStopInputField = rootView.findViewById(R.id.stop_input_field);
        searchStopInputField.setOnEditorActionListener((v, actionId, event) -> {
            EditText editText = (EditText) v;

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!isNetworkAvailable() && !App.OFFLINE_MODE) {
                    Toast.makeText(requireContext(), R.string.reconnect_message, Toast.LENGTH_SHORT).show();
                    return false;
                }

                String query = editText.getText().toString();

                if (Utils.isNullOrEmpty(query) || !query.matches("[0-9]+")) {
                    return false;
                }

                setStopProgress(true);

                Utils.closeKeyboard(requireActivity());
                editText.clearFocus();

                if (Utils.isNullOrEmpty(query)) {
                    Toast.makeText(requireContext(), getString(R.string.empty_input), Toast.LENGTH_SHORT).show();
                    return false;
                }

                blockScrollEvent = true;

                savedStopInfo = null;

                cancelMainTimer();

                int code = -1;

                try {
                    code = Integer.parseInt(query);
                } catch (Exception e) {
                    handleError(new StopNotFound(""));
                    return true;
                }

                loadStop(code);

                return true;
            }

            return false;
        });

        ImageButton stopImgBtn = rootView.findViewById(R.id.stop_img_btn);
        stopImgBtn.setOnClickListener(v -> searchStopInputField.setText(""));

        searchAddressInputField = searchAddressLayout.findViewById(R.id.sa_input_field);
        searchAddressInputField.setOnEditorActionListener((v, actionId, event) -> {
            EditText editText = (EditText) v;

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(requireContext(), R.string.reconnect_message, Toast.LENGTH_SHORT).show();
                    return false;
                }

                String query = editText.getText().toString();

                if (Utils.isNullOrEmpty(query)) return false;

                Utils.closeKeyboard(requireActivity());
                editText.clearFocus();

                setAddressProgress(true);

                loadAddress(query);

                return true;
            }
            return false;
        });

        ImageButton addressImgBtn = searchAddressLayout.findViewById(R.id.address_img_btn);
        addressImgBtn.setOnClickListener(v -> {
            searchAddressInputField.setText("");
        });

        BottomNavigationView bottomNavigationView = rootView.findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.search_route_item) {
                if (isAddressViewExpanded()) {
                    animateAddressView(false, null, () -> {
                        searchAddressLayout.setVisibility(View.GONE);
                        searchRouteLayout.setVisibility(View.VISIBLE);

                        setNetworkMessagePosition();
                    });
                } else {
                    searchStopLayout.setVisibility(View.GONE);
                    searchAddressLayout.setVisibility(View.GONE);
                    searchRouteLayout.setVisibility(View.VISIBLE);

                    setNetworkMessagePosition();
                }
            } else if (id == R.id.search_stop_item) {
                searchStopLayout.setVisibility(View.VISIBLE);
                searchRouteLayout.setVisibility(View.GONE);
                searchAddressLayout.setVisibility(View.GONE);

                setNetworkMessagePosition();
            } else if (id == R.id.search_address_item) {
                searchRouteLayout.setVisibility(View.GONE);
                searchStopLayout.setVisibility(View.GONE);
                searchAddressLayout.setVisibility(View.VISIBLE);

                setNetworkMessagePosition();
            }

            return true;
        });

        ImageButton settingsBtn = rootView.findViewById(R.id.settings_btn);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) settingsBtn.getLayoutParams();
        params.topMargin = Utils.getStatusBarHeight(requireContext());
        settingsBtn.setLayoutParams(params);
        settingsBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                buildAndShowSettingsDialog();
            });
        });

        additionalControlsLayout=rootView.findViewById(R.id.additional_controls_layout);

        ImageButton expandBtn=additionalControlsLayout.findViewById(R.id.expand_btn);
        expandBtn.setOnClickListener(v -> {
            additionalControlsOpened=!additionalControlsOpened;

            showOrHideAdditionalControls(additionalControlsOpened);
        });

        ImageButton routingBtn=rootView.findViewById(R.id.routing_btn);
        routingBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                if (!(currentInfo instanceof ClosestInfo)) {
                    return;
                }

                if (!routingMode) {
                    routingMode = true;

                    showInfoWindow(getString(R.string.routing_title), getString(R.string.select_start_stop) + " - " + getString(R.string.select_finish_stop));
                }
            });
        });

        ImageButton offlineBtn=rootView.findViewById(R.id.offline_btn);
        offlineBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                App.OFFLINE_MODE = !App.OFFLINE_MODE;

                ((ImageButton) v).setImageResource(App.OFFLINE_MODE ? R.drawable.ic_offline_enabled_black : R.drawable.ic_offline_disabled_black);

                if (App.OFFLINE_MODE) {
                    Toast.makeText(requireContext(), getString(R.string.offline_mode_enabled_toast_message), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.offline_mode_disabled_toast_message), Toast.LENGTH_SHORT).show();
                }

                hideInfoWindow();

                cancelMainTimer();

                if (currentWorker != null) {
                    currentWorker.dispose();
                }

                currentWorker = null;

                mapView.getOverlays().clear();
                mapView.invalidate();

                IGeoPoint center = mapView.getMapCenter();
                loadClosest(center.getLatitude(), center.getLongitude());
            });
        });

        reloadBtn=rootView.findViewById(R.id.reload_btn);
        reloadBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                if (currentWorker != null) {
                    currentWorker.dispose();
                    currentWorker = null;
                }

                cancelMainTimer();

                boolean val=isLoading.get();

                isLoading.set(false);

                hideInfoWindow();

                setRouteProgress(false);
                setStopProgress(false);

                updateReloadButtonDrawable();

                if (!val) {
                    reloadContent();

                    Toast.makeText(requireContext(), getString(R.string.reloading_toast_message), Toast.LENGTH_SHORT).show();
                }
            });
        });

        ImageButton oneHandBtn=rootView.findViewById(R.id.one_hand_btn);
        oneHandBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                MainActivity activity = (MainActivity) requireActivity();

                if (minimizedMode) {
                    activity.resizeFragment(false, true);
                } else {
                    activity.resizeFragment(true, true);
                }

                minimizedMode = !minimizedMode;

                oneHandBtn.setImageResource(minimizedMode ? R.drawable.ic_maximize_black : R.drawable.ic_minimize_black);
            });
        });

        ImageButton zoomInBtn=rootView.findViewById(R.id.zoom_in_btn);
        zoomInBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                mapView.getController().zoomTo(mapView.getZoomLevelDouble()+ZOOM_STEP);
            });
        });

        ImageButton zoomOutBtn=rootView.findViewById(R.id.zoom_out_btn);
        zoomOutBtn.setOnClickListener(v -> {
            Utils.animateClickOnImageButton(v, () -> {
                mapView.getController().zoomTo(mapView.getZoomLevelDouble()-ZOOM_STEP);
            });
        });

        ImageButton locBtn = rootView.findViewById(R.id.location_btn);
        locBtn.setOnClickListener(v -> Utils.animateClickOnImageButton(v, this::findAndScrollToLocation));

        GeoPoint startPoint = new GeoPoint(DEFAULT_MAP_CENTER_LATITUDE, DEFAULT_MAP_CENTER_LONGITUDE);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("center_latitutde") && savedInstanceState.containsKey("center_longtitude")) {
                startPoint = new GeoPoint(
                        savedInstanceState.getDouble("center_latitutde"),
                        savedInstanceState.getDouble("center_longtitude")
                );
            }

            inputField.setText(savedInstanceState.getString("route_input", ""));
            searchStopInputField.setText(savedInstanceState.getString("stop_input", ""));
            searchAddressInputField.setText(savedInstanceState.getString("address_input", ""));

            bottomNavigationView.post(() -> {
                int selectedItemId = bottomNavigationView.getSelectedItemId();
                bottomNavigationView.setSelectedItemId(selectedItemId);
            });
        }

        setupDefaultMap(startPoint);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDefaultMap(GeoPoint startPoint) {
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

        //TODO: maybe better change MIN_ZOOM_LEVEL to DEFAULT_MAP_ZOOM?
        mapView.setMinZoomLevel(MIN_ZOOM_LEVEL);
        mapView.setMaxZoomLevel(MAX_ZOOM_LEVEL);

        //TODO: maybe better to decrease available zone?
        BoundingBox mapBounds = new BoundingBox();
        mapBounds.set(NORTH_BOUNDARY, EAST_BOUNDARY, SOUTH_BOUNDARY, WEST_BOUNDARY);
        mapView.setScrollableAreaLimitDouble(mapBounds);

        IMapController mapController = mapView.getController();
        mapController.setZoom(DEFAULT_MAP_ZOOM);
        mapController.setCenter(startPoint);

        mapView.setMultiTouchControls(true);

        mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                if (blockScrollEvent) return false;

                IGeoPoint center = event.getSource().getMapCenter();
                loadClosest(center.getLatitude(), center.getLongitude());

                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        });

        //all this boilerplate code just to intercept double tap lol
        GestureDetector gestureDetector=new GestureDetector(requireContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
        gestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });

        mapView.setOnTouchListener(new View.OnTouchListener() {
            private int CLICK_ACTION_THRESHOLD = 100;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)){
                    return true;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //reject moving if in following mode
                        if (!routeSelectedVehicleCode.equals("")) {
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        if (isAClick(startX, endX, startY, endY)) {
                            if (routingMode) {
                                if (routingSelectedStop != null) {
                                    routingSelectedStop.setMarker(rManager.getStopMarker());

                                    mapView.invalidate();
                                }

                                routingSelectedStop = null;

                                if (routingRoutesInfo != null) {
                                    StaticRouteInfo info1 = routingRoutesInfo.getStaticRoute1().get(0), info2 = routingRoutesInfo.getStaticRoute2().get(0);

                                    CharSequence snippet = "";
                                    if (info1 != null && info2 != null) {
                                        snippet = Html.fromHtml(String.format("%s<br>%s (<font color='%s'><b>%s</b></font>)<br>%s<br>%s (<font color='%s'><b>%s</b></font>)", getString(R.string.route_built_prefix), info1.getRouteLongName(), Utils.getRouteColorCode(0, true), info1.getRouteShortName(), getString(R.string.and), info2.getRouteLongName(), Utils.getRouteColorCode(1, true), info2.getRouteShortName()), 0);
                                    } else if (info1 != null) {
                                        snippet = Html.fromHtml(String.format("%s<br>%s (<font color='%s'><b>%s</b></font>)", getString(R.string.route_built_prefix), info1.getRouteLongName(), Utils.getRouteColorCode(0, true), info1.getRouteShortName()), 0);
                                    }

                                    showInfoWindow(getString(R.string.routing_title), snippet);
                                }

                                return false;
                            }

                            if (!(currentInfo instanceof Stop) && !(currentInfo instanceof VehicleInfo)) {
                                hideInfoWindow();

                                //the easiest way to change selected stop's icon back to non-selected one
                                mapView.invalidate();
                            }

                            if (currentInfo instanceof RouteInfo) {
                                showInfoWindowAboutRoute();

                                //TODO: maybe, simplify?
                                for (Overlay overlay : mapView.getOverlays()) {
                                    if (overlay instanceof ItemizedIconOverlay) {
                                        ItemizedIconOverlay itemizedOverlay = (ItemizedIconOverlay) overlay;
                                        for (int i = 0; i < itemizedOverlay.size(); i++) {
                                            OverlayItem item = itemizedOverlay.getItem(i);
                                            if (item instanceof StopOverlayItem) {
                                                StopOverlayItem stopOverlayItem = (StopOverlayItem) item;
                                                if (stopOverlayItem.getStopId() == routeSelectedStopId) {
                                                    stopOverlayItem.setMarker(rManager.getStopMarker());

                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                                routeSelectedStopId = -1;
                                routeSelectedVehicleCode = "";
                            } else if (currentInfo instanceof VehicleInfo) {
                                TransportInfo transportInfo = ((VehicleInfo) currentInfo).getTransportInfo();
                                StaticRouteInfo staticRouteInfo = ((VehicleInfo) currentInfo).getStaticRouteInfo();

                                String routeLongName = Localization.localizeRouteName(staticRouteInfo.getRouteLongName(), Utils.getAppLocaleAsString(requireContext()));

                                String title = transportInfo.getShortRouteName() + " #" + transportInfo.getId();
                                String snippet = routeLongName;

                                showInfoWindow(title, snippet);
                            }

                            if (searchRouteLayout.getVisibility() == View.VISIBLE
                                    || searchAddressLayout.getVisibility() == View.VISIBLE) {
                                Utils.closeKeyboard(requireActivity());

                                if (searchRouteLayout.hasFocus()) searchRouteLayout.clearFocus();
                                if (searchAddressLayout.hasFocus())
                                    searchAddressLayout.clearFocus();
                            }
                        }
                        break;
                }

                return false;
            }

            private boolean isAClick(float startX, float endX, float startY, float endY) {
                float differenceX = Math.abs(startX - endX);
                float differenceY = Math.abs(startY - endY);
                return !(differenceX > CLICK_ACTION_THRESHOLD || differenceY > CLICK_ACTION_THRESHOLD);
            }
        });

        //actually, it can work without this call
        //because closest info is loaded on scroll
        //and when you set the center of the map - this causes onScroll
        //but for some reason with new NetworkCallback (probably, idk for 100%)
        //it doesn't work as written, so this to be sure
        //this won't mess anything up, because loadClosest() has isLoading check
        loadClosest(startPoint.getLatitude(), startPoint.getLongitude());
    }

    private void buildAndShowSettingsDialog() {
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int langInt = defaultPrefs.getInt(Utils.LANG_KEY, 1);

        AtomicInteger shouldRecreate = new AtomicInteger(0);

        View dialogView = View.inflate(requireContext(), R.layout.settings_dialog, null);

        TextView aboutTextView = dialogView.findViewById(R.id.about_textview);
        String info = String.format(getString(R.string.about_version), BuildConfig.VERSION_NAME) + "\n"
                + String.format(getString(R.string.about_developer), getString(R.string.developer_name)) + "\n\n"
                + getString(R.string.about_lad) + "\n\n"
                + getString(R.string.about_map) + "\n\n"
                + getString(R.string.about_feather_icons) + "\n\n"
                + getString(R.string.about_logo) + "\n"
                + getString(R.string.about_stop_icon) + "\n"
                + getString(R.string.about_railway_station_icon) + "\n"
                + getString(R.string.about_address_icon) + "\n"
                + getString(R.string.about_location_icon);
        aboutTextView.setText(info);

        Spinner langSpinner=dialogView.findViewById(R.id.lang_spinner);
        String[] items =new String[]{getString(R.string.lang_switch_en_title), getString(R.string.lang_switch_ua_title)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, items);
        langSpinner.setAdapter(adapter);
        langSpinner.setSelection(langInt-1);
        langSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                defaultPrefs
                        .edit()
                        .putInt(Utils.LANG_KEY, position + 1)
                        .apply();

                if (langInt != (position + 1)) {
                    shouldRecreate.incrementAndGet();
                } else {
                    if (shouldRecreate.get() > 0)
                        shouldRecreate.decrementAndGet();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        boolean filterAddresses=defaultPrefs.getBoolean("show_only_city_addresses", true);

        Switch filterAddressesSwitch=dialogView.findViewById(R.id.filter_addresses_switch);
        filterAddressesSwitch.setChecked(filterAddresses);
        filterAddressesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            defaultPrefs.edit().putBoolean("show_only_city_addresses", isChecked).commit();
        });

        boolean updateMoreOften=defaultPrefs.getBoolean("update_info_more_often", false);

        Switch updateMoreOftenSwitch=dialogView.findViewById(R.id.update_more_often_switch);
        updateMoreOftenSwitch.setChecked(updateMoreOften);
        updateMoreOftenSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            defaultPrefs.edit().putBoolean("update_info_more_often", isChecked).commit();

            if (isChecked != updateMoreOften) {
                shouldRecreate.incrementAndGet();
            } else {
                if (shouldRecreate.get() > 0)
                    shouldRecreate.decrementAndGet();
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .setOnDismissListener(dialogInterface -> {
                    if (shouldRecreate.get()>0) {
                        shouldRecreate.set(0);

                        ((MainActivity) requireActivity()).recreateThisActivity();
                    }
                })
                .create();

        if (dialog.getWindow()!=null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        View closeBtn=dialogView.findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(v -> dialog.dismiss());

        /*Licenses*/
        View axRow=dialogView.findViewById(R.id.ax_row);
        axRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));

        View jsRow=dialogView.findViewById(R.id.js_row);
        jsRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.MIT))));

        View mcRow=dialogView.findViewById(R.id.mc_row);
        mcRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));

        View njRow=dialogView.findViewById(R.id.nj_row);
        njRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.MIT))));

        View ohRow=dialogView.findViewById(R.id.oh_row);
        ohRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));

        View osmbRow=dialogView.findViewById(R.id.osmb_row);
        osmbRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.LGPL))));

        View osmdRow=dialogView.findViewById(R.id.osmd_row);
        osmdRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));

        View raRow=dialogView.findViewById(R.id.ra_row);
        raRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));

        View rjRow=dialogView.findViewById(R.id.rj_row);
        rjRow.setOnClickListener(v -> Utils.animateClickOnItem(v, () -> showLicenseDialog(LicensesHelper.getLicenseText(requireContext(), LicensesHelper.License.APACHE2))));
        /**********/

        dialog.show();
    }

    private void showLicenseDialog(Spanned text){
        new AlertDialog.Builder(requireContext())
                .setMessage(text)
                .setPositiveButton(getString(R.string.close_button_title), null)
                .create()
                .show();
    }

    private void findAndScrollToLocation() {
        if (!LocationHelper.checkPermissions()) {
            LocationHelper.requestPermissions(requireActivity());

            return;
        }

        if (!LocationHelper.isLocationEnabled()) {
            Toast.makeText(requireContext(), getString(R.string.location_disabled_message), Toast.LENGTH_SHORT).show();

            return;
        }

        LocationHelper.getLocation(new OnLocationFound() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    Toast.makeText(requireContext(), getString(R.string.no_location_message), Toast.LENGTH_SHORT).show();

                    return;
                }

                GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());

                if (userLocationOverlay != null) {
                    mapView.getOverlays().remove(userLocationOverlay);
                }

                Marker addressMarker = new Marker(mapView);
                addressMarker.setTitle("");
                addressMarker.setIcon(rManager.getLocationMarker());
                addressMarker.setPosition(point);
                addressMarker.setDraggable(false);
                userLocationOverlay = addressMarker;

                mapView.getOverlays().add(userLocationOverlay);

                mapView.invalidate();

                mapView.getController().animateTo(point);
            }
        });
    }

    private void showExitSnackbar(){
        Snackbar.make(rootView, getString(R.string.exit_dialog_title), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.exit_dialog_yes_button_title), v -> requireActivity().finish())
                .show();
    }

    //TODO: stop main timer here?
    @Override
    public boolean onBackPressed() {
        if (isAddressViewExpanded()) {
            animateAddressView(false, null, null);

            return true;
        }

        if (routingMode){
            routingMode=false;
            routingModeLocked=false;

            startStop=null;
            endStop=null;

            routingRoutesInfo=null;
            routingSelectedStop=null;

            hideInfoWindow();

            mapView.getOverlays().clear();
            mapView.invalidate();

            IGeoPoint center=mapView.getMapCenter();
            loadClosest(center.getLatitude(), center.getLongitude());

            return true;
        }

        if (currentInfo instanceof RouteInfo) {
            if (savedStopInfo != null) {
                mapView.getOverlays().clear();
                mapView.invalidate();

                notScrollToStopAfterOnBackPressed=true;

                loadStop(savedStopInfo.getCode());
                return true;
            }

            inputField.clearFocus();

            //inputField.setText("");
            hideInfoWindow();

            blockScrollEvent = false;

            mapView.getOverlays().clear();
            mapView.invalidate();

            IGeoPoint center = mapView.getMapCenter();
            loadClosest(center.getLatitude(), center.getLongitude());

            return true;
        } else if (currentInfo instanceof Stop) {
            hideInfoWindow();

            savedStopInfo = null;

            blockScrollEvent = false;

            mapView.getOverlays().clear();
            mapView.invalidate();

            IGeoPoint center = mapView.getMapCenter();
            loadClosest(center.getLatitude(), center.getLongitude());

            return true;
        } else if (currentInfo instanceof VehicleInfo) {
            hideInfoWindow();

            if (savedStopInfo != null) {
                mapView.getOverlays().clear();
                mapView.invalidate();

                loadStop(savedStopInfo.getCode());
                return true;
            }

            blockScrollEvent = false;

            mapView.getOverlays().clear();
            mapView.invalidate();

            IGeoPoint center = mapView.getMapCenter();
            loadClosest(center.getLatitude(), center.getLongitude());

            return true;
        }

        showExitSnackbar();

        return true;
    }

}
