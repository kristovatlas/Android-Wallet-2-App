package info.blockchain.merchant.directory;
 
import info.blockchain.wallet.ui.DeviceUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;

import piuk.blockchain.android.R;

public class ListActivity extends Activity {

	private static final int HEADING_CAFE = 1;
	private static final int HEADING_BAR = 2;
	private static final int HEADING_RESTAURANT = 3;
	private static final int HEADING_SPEND = 4;
	private static final int HEADING_ATM = 5;

	private ArrayList<BTCBusiness> businesses = null;
    private int curSelection = -1;
    private BTCBusinessAdapter adapter = null;
    
    private Handler mHandler = new Handler();
    
    private String strULat = null;
    private String strULon = null;

	//
	//
	//
	private DrawerLayout mDrawerLayout = null;
	private ListView mDrawerList = null;
	private ActionBarDrawerToggle mDrawerToggle = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directory);

	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActionBar actionBar = getActionBar();
        actionBar.hide();
        actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
        
        LinearLayout layout_icons = new LinearLayout(actionBar.getThemedContext());
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
	    if(!DeviceUtil.getInstance(this).isSmallScreen()) {
	        layoutParams.height = 72;
	    }
	    else {
	        layoutParams.height = 30;
	    }

        layoutParams.width = layoutParams.height + 50;
        layout_icons.setLayoutParams(layoutParams);
        layout_icons.setOrientation(LinearLayout.HORIZONTAL);

        ActionBar.LayoutParams imgParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL);
        imgParams.height = layoutParams.height;
        imgParams.width = layoutParams.height;
        imgParams.rightMargin = 5;
        
        final ImageView mapview_icon = new ImageView(actionBar.getThemedContext());
        mapview_icon.setImageResource(R.drawable.mapview_icon);
        mapview_icon.setScaleType(ImageView.ScaleType.FIT_XY);
        mapview_icon.setLayoutParams(imgParams);
        mapview_icon.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	
            	finish();
            	
        		return false;
            }
        });

        layout_icons.addView(mapview_icon);

//        actionBar.setDisplayOptions(actionBar.getDisplayOptions() ^ ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setLogo(R.drawable.masthead);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF1B8AC7")));

        //
        //
        //
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.drawer_list);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
			
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				}

			public void onDrawerOpened(View view) {
				super.onDrawerOpened(view);
			    }

			};

		// hide settings menu
//		invalidateOptionsMenu();

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		ArrayAdapter<String> hAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.drawer_list_item, getResources().getStringArray(R.array.menus_merchantDirectory));
		mDrawerList.setAdapter(hAdapter);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			    switch (position) {
		    	case 0:
		    		doSuggest();
		    		break;
		    	default:
		    		break;
			    }

				// Closing the drawer
				mDrawerLayout.closeDrawer(mDrawerList);
			    invalidateOptionsMenu();

			}
		});
		
        actionBar.setCustomView(layout_icons);
        actionBar.show();

        Bundle extras = getIntent().getExtras();
        if(extras != null)	{
        	strULat = extras.getString("ULAT");
        	strULon = extras.getString("ULON");
        }

        businesses = new ArrayList<BTCBusiness>();
        
        ListView listView = (ListView)findViewById(R.id.listview);
        adapter = new BTCBusinessAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
            	
            	final BTCBusiness b = businesses.get(position);

     			AlertDialog.Builder alert = new AlertDialog.Builder(ListActivity.this);
                alert.setTitle(R.string.merchant_info);
                /*
                alert.setMessage("Correct Answer: "
                        + scoreMode.getCorrectAnswer() + "\n"
                        + "Wrong Answer: " + scoreMode.getWrongAnswer()
                        + "\n" + "Total Score: "
                        + scoreMode.getScorePoint() + "\n"
                        + "Bonus Score: " + scoreMode.getBonusPoint());
                */
                alert.setPositiveButton(R.string.directions,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                     			Intent intent = new Intent(Intent.ACTION_VIEW);
                     			// http://maps.google.com/?saddr=34.052222,-118.243611&daddr=37.322778,-122.031944
                     			intent.setData(Uri.parse("https://maps.google.com/?saddr=" +
                     					strULat + "," + strULon +
                     					"&daddr=" + b.lat + "," + b.lon
                     					));
                     			startActivity(intent);
                            }
                        });
                if(b.tel != null) {
                    alert.setNeutralButton(R.string.call,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                	Intent intent = new Intent(Intent.ACTION_DIAL);
                                	intent.setData(Uri.parse("tel:" +b.tel));
                                	startActivity(intent);
                                }
                            });
                }
                /*
                if(markerValues.get(marker.getId()).web != null) {
                    alert.setNegativeButton("Web",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                         			Intent intent = new Intent(Intent.ACTION_VIEW);
                         			intent.setData(Uri.parse(markerValues.get(marker.getId()).web));
                         			startActivity(intent);
                                }
                            });
                }
                */
                alert.show();

            }
        });

		setAdapterContent();

    }

	public void setAdapterContent() {
		
		businesses.clear();
		
		for(int i = 0; i < MapActivity.btcb.size(); i++) {
			if(Double.parseDouble(MapActivity.btcb.get(i).distance) < 15.0) {
				businesses.add(MapActivity.btcb.get(i));
			}
		}

		adapter.notifyDataSetChanged();

	}
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dir_listmenu, menu);
		return true;
	}
*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		else {
		    switch (item.getItemId()) {
	    	case R.id.map_view:
	    		finish();
	    		return true;
	    	case R.id.suggest:
	    		doSuggest();
	    		return true;
		    default:
		        return super.onOptionsItemSelected(item);
		    }
		}

	}

    private class BTCBusinessAdapter extends BaseAdapter {
    	
		private LayoutInflater inflater = null;

	    BTCBusinessAdapter() {
	        inflater = (LayoutInflater)ListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return businesses.size();
		}

		@Override
		public String getItem(int position) {
	        return "";
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = null;

	        if (convertView == null) {
	            view = inflater.inflate(R.layout.directory_item, parent, false);
	        } else {
	            view = convertView;
	        }

			BTCBusiness b = businesses.get(position);

            Double distance = Double.parseDouble(b.distance);
            String strDistance = null;
            if(distance < 1.0) {
            	distance *= 1000;
            	DecimalFormat df = new DecimalFormat("###");
            	strDistance = df.format(distance) + " meters";
            }
            else {
            	DecimalFormat df = new DecimalFormat("#####.#");
            	strDistance = df.format(distance) + "km";
            }

	        ((TextView)view.findViewById(R.id.txt1)).setText(b.name + " (" + strDistance + ")");
	        ((TextView)view.findViewById(R.id.txt2)).setText(b.address + ", " + b.city + " " + b.pcode);
	        ((TextView)view.findViewById(R.id.txt3)).setText(b.tel);
	        ((TextView)view.findViewById(R.id.txt4)).setText(b.desc);
	        
	        ImageView ivHeading = (ImageView)view.findViewById(R.id.heading);
			switch(Integer.parseInt(b.hc)) {
			case HEADING_CAFE:
		        ivHeading.setImageResource(b.flag.equals("1") ? R.drawable.marker_cafe_featured : R.drawable.marker_cafe);
				break;
			case HEADING_BAR:
		        ivHeading.setImageResource(b.flag.equals("1") ? R.drawable.marker_drink_featured : R.drawable.marker_drink);
				break;
			case HEADING_RESTAURANT:
		        ivHeading.setImageResource(b.flag.equals("1") ? R.drawable.marker_eat_featured : R.drawable.marker_eat);
				break;
			case HEADING_SPEND:
		        ivHeading.setImageResource(b.flag.equals("1") ? R.drawable.marker_spend_featured : R.drawable.marker_spend);
				break;
			case HEADING_ATM:
		        ivHeading.setImageResource(b.flag.equals("1") ? R.drawable.marker_atm_featured : R.drawable.marker_atm);
				break;
			default:
		        ivHeading.setImageResource(b.flag.equals("1") ? R.drawable.marker_cafe_featured : R.drawable.marker_cafe);
				break;
			}

	        return view;
		}

    }

    private void doMapView() {
    	finish();
    }

    private void doSuggest() {
    	Intent intent = new Intent(ListActivity.this, SuggestMerchant.class);
		startActivity(intent);
    }

}
