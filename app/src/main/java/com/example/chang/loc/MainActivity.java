package com.example.chang.loc;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.esri.android.map.Callout;
import com.esri.android.map.CalloutStyle;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

public class MainActivity extends AppCompatActivity {
    Callout callout;
    MapView mMapView;

    LocationDisplayManager lDisplayManager = null;
    GraphicsLayer gpsGraphicsLayer;
    Polyline mPolyline;
    int pointCount = 0;
    Button btnPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);
        //ArcGISTiledMapServiceLayer tile = new ArcGISTiledMapServiceLayer("https://services8.arcgis.com/qURMYZyUXQm3d6wk/arcgis/rest/services/test/FeatureServer");
        //mMapView.addLayer(tile);

        mMapView.setOnSingleTapListener(new OnSingleTapListener() {

            @Override
            public void onSingleTap(float x, float y) {
                //屏幕坐标转地图坐标
                Point point = mMapView.toMapPoint(x,y);
                //设置显示位置
                callout.show(point);
            }
        });
        gpsGraphicsLayer = new GraphicsLayer();
        mMapView.addLayer(gpsGraphicsLayer);
        mPolyline = new Polyline();

        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
            @Override
            public void onStatusChanged(Object source, STATUS status) {
                if (source == mMapView && status == STATUS.INITIALIZED) {
                    lDisplayManager = mMapView.getLocationDisplayManager();//获取LocationDisplayManager
                    lDisplayManager.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
                    lDisplayManager.setShowLocation(false);//不显示当前位置，坐标系不一致坐标偏移严重
                    lDisplayManager.setShowPings(true);
                    lDisplayManager.setAccuracyCircleOn(true);
                    lDisplayManager.setAllowNetworkLocation(true);
                    lDisplayManager.setLocationListener(new LocationListener() {
                        @Override
                        public void onLocationChanged(Location loc) {
                            //火星坐标转换
                            //double[] gcj = CoordinateConvert.wgs2GCJ(loc.getLatitude(), loc.getLongitude());

                            Point wgspoint = new Point(loc.getLongitude(), loc.getLatitude());
                            Point p = (Point) GeometryEngine.project(wgspoint,
                                    SpatialReference.create(SpatialReference.WKID_WGS84),
                                    mMapView.getSpatialReference());
                            SimpleMarkerSymbol ptSym = new SimpleMarkerSymbol(Color.BLUE, 15,
                                    SimpleMarkerSymbol.STYLE.CIRCLE);
                            Graphic graphic = new Graphic(p, ptSym, null);
                            initCallout(wgspoint);
                            if (pointCount == 2) {
                                mPolyline.startPath(p.getX(), p.getY());
                                mMapView.zoomTo(p, 18);
                            }
                            if (pointCount >= 2) {
                                mPolyline.lineTo(p.getX(), p.getY());//点画线
                                mMapView.centerAt(p, true);
                            }


                            gpsGraphicsLayer.removeAll();
                            SimpleLineSymbol lineSym = new SimpleLineSymbol(Color.RED, 5);
                            Graphic g = new Graphic(mPolyline, lineSym);
                            gpsGraphicsLayer.addGraphic(g);
                            pointCount++;

                            gpsGraphicsLayer.addGraphic(graphic);
                        }

                        @Override
                        public void onProviderDisabled(String arg0) {
                        }

                        @Override
                        public void onProviderEnabled(String arg0) {
                        }

                        @Override
                        public void onStatusChanged(String arg0, int arg1,
                                                    Bundle arg2) {

                        }
                    });  // Actionlistener
                    lDisplayManager.start();
                }
            }
            });
}
    private void initCallout(Point p) {
        //获取一个气泡
        callout = mMapView.getCallout();
        //设置最大的长宽
        callout.setMaxWidth(1200);
        callout.setMaxHeight(300);
//        TextView tv = new TextView(this);
//        tv.setText("这是一个气泡");
//        callout.setContent(tv);
        // create a textview for the callout
        TextView calloutContent = new TextView(getApplicationContext());
        calloutContent.setTextColor(Color.BLACK);
        calloutContent.setSingleLine();
        // format coordinates to 4 decimal places
        calloutContent.setText("Lat:" + String.format("%.4f", p.getY()) +
                ", Lon: " + String.format("%.4f", p.getX()));

        callout.setContent(calloutContent);
        callout.show();
        CalloutStyle calloutStyle = new CalloutStyle();
        //设置尖尖角的位置，尖尖显示在气泡的左下角，
        calloutStyle.setAnchor(Callout.ANCHOR_POSITION_LOWER_LEFT_CORNER);
        callout.setStyle(calloutStyle);
    }
}
