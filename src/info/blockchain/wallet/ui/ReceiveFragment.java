package info.blockchain.wallet.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import org.json.simple.JSONObject;

import piuk.MyRemoteWallet;
import piuk.blockchain.android.R;
import piuk.blockchain.android.WalletApplication;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.View.OnFocusChangeListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.text.InputType;
import android.util.Log;

import com.google.bitcoin.uri.BitcoinURI;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.encode.QRCodeEncoder;

public class ReceiveFragment extends Fragment   {

	private boolean addressesOn = true;
	private boolean contactsOn = true;
	
	private View rootView = null;

    private EditText edAmount1 = null;
    private TextView tvAmount2 = null;
    private EditText edAddress = null;
    private TextView tvCurrency = null;
//    private LinearLayout summary = null;
//    private LinearLayout summary2 = null;
    
//    private TextView tvAmount = null;
//    private TextView tvAmountBis = null;
//    private TextView tvArrow = null;
    private TextView tvAddress = null;
    private TextView tvAddressBis = null;
    private ImageView ivReceivingQR = null;
	private String strCurrentFiatSymbol = "$";
	private String strCurrentFiatCode = "USD";

	private boolean isMagic = false;
    private View oldView = null;
    private LinearLayout parent = null;
    private LinearLayout magic = null;
    private int children = 0;
    private View childIcons = null;
    private View childList = null;
    private ListView magicList = null;

    private ImageView ivAddresses = null;
    private ImageView ivContacts = null;
    private ImageView ivPhoneContacts = null;

    private ImageView ivCheck = null;

    private List<HashMap<String,String>> magicData = null;
    private List<HashMap<String,String>> filteredDisplayList = null;
	private MagicAdapter adapter = null;
	private String currentSelectedAddress = null;
	
	private List<String> activeAddresses;
	private Map<String,String> labels;
	private List<Map<String, Object>> addressBookMapList;

	private boolean isBTC = false;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_receive, container, false);

        /*
        tvAmount = (TextView)rootView.findViewById(R.id.amount);
        tvAmount.setVisibility(View.INVISIBLE);
        tvAmountBis = (TextView)rootView.findViewById(R.id.amount_bis);
        tvAmountBis.setVisibility(View.INVISIBLE);
        tvArrow = (TextView)rootView.findViewById(R.id.arrow);
        tvArrow.setVisibility(View.INVISIBLE);
        */
        tvAddress = (TextView)rootView.findViewById(R.id.receiving_address);
        tvAddress.setVisibility(View.INVISIBLE);
        tvAddressBis = (TextView)rootView.findViewById(R.id.receiving_address_bis);
        tvAddressBis.setVisibility(View.INVISIBLE);
        
        /*
        summary = (LinearLayout)rootView.findViewById(R.id.summary);
        summary.setVisibility(View.INVISIBLE);
        summary2 = (LinearLayout)rootView.findViewById(R.id.summary2);
        summary2.setVisibility(View.INVISIBLE);
        */

        ivReceivingQR = (ImageView)rootView.findViewById(R.id.qr);
        ivReceivingQR.setVisibility(View.INVISIBLE);
        ivReceivingQR.setOnLongClickListener(new View.OnLongClickListener() {
      	  public boolean onLongClick(View view) {
//    			Toast.makeText(PaymentFragment.this.getActivity(), "Address copied:" + input_address, Toast.LENGTH_LONG).show();
    			
    			android.content.ClipboardManager clipboard = (android.content.ClipboardManager)getActivity().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
    		    android.content.ClipData clip = null;
	        	if (currentSelectedAddress != null) {
	    		    clip = android.content.ClipData.newPlainText("Send address", currentSelectedAddress);
	        	} else {
	    		    clip = android.content.ClipData.newPlainText("Send address", edAddress.getText().toString());
	        	}
    		    clipboard.setPrimaryClip(clip);
    			
    		    String strFileName = getActivity().getCacheDir() + File.separator + "qr.png";
    		    File file = new File(strFileName);
    		    file.setReadable(true, false);
    			FileOutputStream fos = null;
    			try {
        			fos = new FileOutputStream(file);
    			}
    			catch(FileNotFoundException fnfe) {
    				;
    			}
    			
    			if(file != null && fos != null) {
        			Bitmap bitmap = ((BitmapDrawable)ivReceivingQR.getDrawable()).getBitmap();
        	        bitmap.compress(CompressFormat.PNG, 0, fos);
        	        
        			try {
            			fos.close();
        			}
        			catch(IOException ioe) {
        				;
        			}

        	        Intent intent = new Intent(); 
        	        intent.setAction(Intent.ACTION_SEND); 
        	        intent.setType("*/*"); 
        	        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        	        startActivity(Intent.createChooser(intent, "Send payment code"));
    			}
    	        
      	    return true;
      	  }
      	});
        
        tvCurrency = (TextView)rootView.findViewById(R.id.currency);
        tvCurrency.setTypeface(TypefaceUtil.getInstance(getActivity()).getBTCTypeface());
        tvCurrency.setText(Character.toString((char)TypefaceUtil.getInstance(getActivity()).getBTCSymbol()));
        tvCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if(isBTC) {
            		tvCurrency.setText(strCurrentFiatSymbol);
            		String tmp = edAmount1.getText().toString();
            		if(tmp.length() < 1) {
            			tmp = "0.0000";
            		}
            		String tmp2 = tvAmount2.getText().toString().substring(0, tvAmount2.getText().toString().length() - 4);
            		try {
            			double d = Double.parseDouble(tmp2);
            			if(0.0 == d) {
            				tmp2 = "";
            			}
            		}
            		catch(Exception e) {
            			tmp2 = "";
            		}
            		edAmount1.setText(tmp2);
                    tvAmount2.setText(tmp + " BTC");
            	}
            	else {
            	    tvCurrency.setTypeface(TypefaceUtil.getInstance(getActivity()).getBTCTypeface());
            		tvCurrency.setText(Character.toString((char)TypefaceUtil.getInstance(getActivity()).getBTCSymbol()));
            		String tmp = edAmount1.getText().toString(); 
            		if(tmp.length() < 1) {
            			tmp = "0.00";
            		}
            		String tmp2 = tvAmount2.getText().toString().substring(0, tvAmount2.getText().toString().length() - 4);
            		try {
            			double d = Double.parseDouble(tmp2);
            			if(0.0 == d) {
            				tmp2 = "";
            			}
            		}
            		catch(Exception e) {
            			tmp2 = "";
            		}
                    edAmount1.setText(tmp2);
                    tvAmount2.setText(tmp + " " + strCurrentFiatCode);
            	}
            	isBTC = isBTC ? false : true;
                // ivReceivingQR.setVisibility(View.INVISIBLE);
            }
        });

        final ImageView clear_input = (ImageView)rootView.findViewById(R.id.clear);
  
    	LinearLayout divider1 = (LinearLayout)rootView.findViewById(R.id.divider1);
    	divider1.setBackgroundColor(BlockchainUtil.BLOCKCHAIN_GREEN);
    	LinearLayout divider2 = (LinearLayout)rootView.findViewById(R.id.divider2);
    	divider2.setBackgroundColor(BlockchainUtil.BLOCKCHAIN_GREEN);

//        ((TextView)rootView.findViewById(R.id.direction)).setTypeface(TypefaceUtil.getInstance(getActivity()).getGravityBoldTypeface());
//        ((TextView)rootView.findViewById(R.id.direction)).setText(Character.toString((char)0x2192));
        ((ImageView)rootView.findViewById(R.id.direction)).setImageResource(R.drawable.green_arrow);
        ((TextView)rootView.findViewById(R.id.currency)).setText(strCurrentFiatSymbol);
        ((TextView)rootView.findViewById(R.id.currency)).setTypeface(TypefaceUtil.getInstance(getActivity()).getGravityBoldTypeface());
//        ((ImageView)rootView.findViewById(R.id.qr)).setImageBitmap(generateQRCode(BitcoinURI.convertToBitcoinURI("18nkx4epNwy4nEfFWZEtdBucwtj5TdSAm", BigInteger.valueOf(300000L), "", "")));

//      initMagicList();
      initAddressBookList();

        tvAmount2 = ((TextView)rootView.findViewById(R.id.amount2));
        tvAmount2.setText("0.0000 BTC");
        edAmount1 = ((EditText)rootView.findViewById(R.id.amount1));
        edAmount1.setOnEditorActionListener(new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if(actionId == EditorInfo.IME_ACTION_DONE) {

//		        	summary.setVisibility(View.VISIBLE);
//		        	summary2.setVisibility(View.VISIBLE);
		        	tvAddress.setVisibility(View.VISIBLE);
		        	tvAddressBis.setVisibility(View.VISIBLE);
//		        	tvArrow.setVisibility(View.VISIBLE);
//		        	tvAmount.setVisibility(View.VISIBLE);
//		        	tvAmountBis.setVisibility(View.VISIBLE);
		        	ivReceivingQR.setVisibility(View.VISIBLE);

//		        	final WalletApplication application = (WalletApplication)getActivity().getApplication();
// 		    		MyRemoteWallet wallet = application.getRemoteWallet();
// 		    		Map<String,String> labels = wallet.getLabelMap();
 		            if(currentSelectedAddress != null) {
// 		            	tvAddressBis.setText(destination.substring(0,  15) + "...");
 		            	tvAddressBis.setText(currentSelectedAddress);
 		            }
 		            else {
 		            	tvAddressBis.setVisibility(View.GONE);
 		            }
 //					Toast.makeText(application, "BTC going to:" + destination, Toast.LENGTH_LONG).show();

		        	if(edAddress.getText().toString().length() > 15) {
//			        	tvAddress.setText(edAddress.getText().toString().subSequence(0, 15) + "...");
			        	tvAddress.setText(edAddress.getText().toString());
		        	}
		        	else {
			        	tvAddress.setText(edAddress.getText().toString());
		        	}

//		        	tvArrow.setText(Character.toString((char)0x2192));

		        	String amount1 = edAmount1.getText().toString();
		        	if(amount1 == null || amount1.length() < 1) {
		        		amount1 = "0.00";
		        	}
		        	String amount2 = tvAmount2.getText().toString().substring(0, tvAmount2.getText().toString().length() - 4);
		        	long btcValue;
		        	double value;
		        	if(isBTC) {
		            	value = Math.round(Double.parseDouble(amount1) * 100000000.0);
		            	btcValue = (Double.valueOf(value)).longValue();
		        		amount1 += " BTC";
		        		amount2 += " " + strCurrentFiatCode;
		        	}
		        	else {
		            	value = Math.round(Double.parseDouble(amount2) * 100000000.0);
		            	btcValue = (Double.valueOf(value)).longValue();
		        		amount1 += " " + strCurrentFiatCode;
		        		amount2 += " BTC";
		        	}
		        	/*
		        	SpannableStringBuilder a1 = new SpannableStringBuilder(amount1);
		        	SpannableStringBuilder a2 = new SpannableStringBuilder(amount2);
		        	a1.setSpan(new SuperscriptSpan(), amount1.length() - 4, amount1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        	a1.setSpan(new RelativeSizeSpan((float)0.75), amount1.length() - 4, amount1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        	a2.setSpan(new SuperscriptSpan(), amount2.length() - 4, amount2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        	a2.setSpan(new RelativeSizeSpan((float)0.75), amount2.length() - 4, amount2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        	tvAmount.setText(a1);
		        	tvAmountBis.setText(a2);
		        	*/

		        	if (currentSelectedAddress != null) {
		        		Log.d("currentSelectedAddress", "currentSelectedAddress " + currentSelectedAddress);
			            ivReceivingQR.setImageBitmap(generateQRCode(BitcoinURI.convertToBitcoinURI(currentSelectedAddress, BigInteger.valueOf(btcValue), "", "")));		        		
		        	} else {
			            ivReceivingQR.setImageBitmap(generateQRCode(BitcoinURI.convertToBitcoinURI(edAddress.getText().toString(), BigInteger.valueOf(btcValue), "", "")));		        		
		        	}
		        }
		        return false;
		    }
		});

        edAmount1 = ((EditText)rootView.findViewById(R.id.amount1));
        edAmount1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	
            	if(ivReceivingQR.getVisibility() == View.VISIBLE) {
            		clearReceive();
            	}
            		
            }
        });

        edAmount1.addTextChangedListener(new TextWatcher()	{

        	public void afterTextChanged(Editable s) {
        		if((edAddress.getText().toString() != null && edAddress.getText().toString().length() > 0) || (edAmount1.getText().toString() != null && edAmount1.getText().toString().length() > 0)) {
        			
        			if(isBTC)	{
            			tvAmount2.setText(BlockchainUtil.BTC2Fiat(edAmount1.getText().toString()) + " " + strCurrentFiatCode);
        			}
        			else	{
//                		tvAmount2.setTypeface(TypefaceUtil.getInstance(getActivity()).getBTCTypeface());
        				tvAmount2.setText(BlockchainUtil.Fiat2BTC(edAmount1.getText().toString()) + " BTC");
        			}

        			clear_input.setVisibility(View.VISIBLE);
        		}
        		else {
        			clear_input.setVisibility(View.INVISIBLE);
        		}
        	}

        	public void beforeTextChanged(CharSequence s, int start, int count, int after)	{ ; }
        
        	public void onTextChanged(CharSequence s, int start, int before, int count)	{ ; }
        });

        edAmount1.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    if(edAmount1.getText().toString() != null && edAmount1.getText().toString().length() > 0) {
            			edAmount1.setText("");
                    	/*
                    	try {
                    		Double val = Double.parseDouble(edAmount1.getText().toString());
                    		if(val == 0.0) {
                    			edAmount1.setText("");
                    		}
                    	}
                    	catch(NumberFormatException nfe) {
                			edAmount1.setText("");
                    	}
                    	*/
                    }
                }
            }
        });

        edAddress = ((EditText)rootView.findViewById(R.id.address));
        edAddress.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	
            	if(ivReceivingQR.getVisibility() == View.VISIBLE) {
            		clearReceive();
            	}

            	if(!isMagic) {
            		
            		displayMagicList();

            	}
            	else {
//            		removeMagicList();
            	}
            		
            }
        });

        edAddress.addTextChangedListener(new TextWatcher()	{

        	public void afterTextChanged(Editable s) {
        		if((edAddress.getText().toString() != null && edAddress.getText().toString().length() > 0) || (edAmount1.getText().toString() != null && edAmount1.getText().toString().length() > 0)) {
        			clear_input.setVisibility(View.VISIBLE);
        		}
        		else {
        			clear_input.setVisibility(View.INVISIBLE);
        		}
        	}

        	public void beforeTextChanged(CharSequence s, int start, int count, int after)	{ ; }
        
        	public void onTextChanged(CharSequence s, int start, int before, int count)	{        		
        		String inputAddress = edAddress.getText().toString();
        		int len = edAddress.getText().length();
        		List<HashMap<String,String>> filtered = new ArrayList<HashMap<String,String>>();
        		
        		for (HashMap<String,String> row : magicData) {
        			String labelOrAddress = row.get("labelOrAddress");
        		    if (len <= labelOrAddress.length()) {
            			if(inputAddress.equalsIgnoreCase((String) labelOrAddress.subSequence(0, len))) {
            				filtered.add(row);
            			}
        		    }
        		}
        		
        		if (BitcoinAddressCheck.isValidAddress(inputAddress)) {
            		currentSelectedAddress = inputAddress;                        			
        		} else {
            		currentSelectedAddress = null;                        			
        		}
        		filteredDisplayList = filtered;
                if(adapter != null)	{
            		adapter.notifyDataSetChanged();
                }
            }
        });

        edAddress.setOnEditorActionListener(new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if(actionId == EditorInfo.IME_ACTION_NEXT) {
		        	
		        	if(isMagic) {
		        		removeMagicList();
		        	}

	                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(edAddress.getWindowToken(), 0);
	                edAmount1.requestFocus();
	                edAmount1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
	                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

		        }
		        return false;
		    }
		});

        clear_input.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	edAddress.setText("");
            	if(isBTC) {
                	edAmount1.setText("0.00");
                	tvAmount2.setText("0.00");
            	}
            	else {
                	edAmount1.setText("0.00");
                	tvAmount2.setText("0.00");
            	}

            	/*
                summary.setVisibility(View.INVISIBLE);
                summary2.setVisibility(View.INVISIBLE);
                tvAmount.setText("");
                tvAmount.setVisibility(View.INVISIBLE);
                tvAmountBis.setText("");
                tvAmountBis.setVisibility(View.INVISIBLE);
                tvArrow.setText("");
                tvArrow.setVisibility(View.INVISIBLE);
                */
                tvAddress.setText("");
                tvAddress.setVisibility(View.INVISIBLE);
                tvAddressBis.setText("");
                tvAddressBis.setVisibility(View.INVISIBLE);
                
//                btReceive.setVisibility(View.INVISIBLE);
                ivReceivingQR.setVisibility(View.INVISIBLE);

            	if(!isMagic) {
                	displayMagicList();
            	}

                clear_input.setVisibility(View.INVISIBLE);

                return false;
            }
        });
        clear_input.setVisibility(View.INVISIBLE);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        
        Log.d("BlockchainWallet", "setUserVisible");

        /*
        if(isVisibleToUser) {
        	if(isMagic) {
        		removeMagicList();
        	}
        	displayMagicList();
        	doSimpleSend();
        }
        else {
        	;
        }
        */

    }

    @Override
    public void onResume() {
    	super.onResume();

        Log.d("BlockchainWallet", "onResume");

		removeMagicList();
    	displayMagicList();

    }

    private Bitmap generateQRCode(String uri) {

        Bitmap bitmap = null;
        int qrCodeDimension = 280;

        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(uri, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimension);

    	try {
            bitmap = qrCodeEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            e.printStackTrace();
        }
    	
    	return bitmap;
    }

    public static void sendViewToBack(final View child) {
    	if(child != null) {
            final ViewGroup parent = (ViewGroup)child.getParent();
            if (null != parent) {
                parent.removeView(child);
                parent.addView(child, 0);
            }
    	}
    }

    private class MagicAdapter extends BaseAdapter {
    	
		private LayoutInflater inflater = null;

	    MagicAdapter() {
	        inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			if(filteredDisplayList != null) {
				return filteredDisplayList.size();
			}
			else {
				return 0;
			}
		}

		@Override
		public String getItem(int position) {
			HashMap<String,String> row = filteredDisplayList.get(position);
			return row.get("labelOrAddress");
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view;
	        
	        if (convertView == null) {
	            view = inflater.inflate(R.layout.magic_entry, parent, false);
	        } else {
	            view = convertView;
	        }

	        ((TextView)view.findViewById(R.id.p1)).setTypeface(TypefaceUtil.getInstance(getActivity()).getGravityBoldTypeface());
	        HashMap<String,String> row = filteredDisplayList.get(position);
	        if(row.get("label") != null) {
		        ((TextView)view.findViewById(R.id.p1)).setTypeface(TypefaceUtil.getInstance(getActivity()).getGravityBoldTypeface());
	        }
	        else {
		        ((TextView)view.findViewById(R.id.p1)).setTypeface(TypefaceUtil.getInstance(getActivity()).getGravityLightTypeface());
	        }

	        // Names, Labels in black, addresses in GREY
	        if(row.get("label") != null) {
		        ((TextView)view.findViewById(R.id.p1)).setTextColor(Color.BLACK);
	        }
	        else {
		        ((TextView)view.findViewById(R.id.p1)).setTextColor(0xFF616161);
	        }

	        String labelOrAddress = BlockchainUtil.formatAddress(row.get("labelOrAddress"), 15) ;
	        ((TextView)view.findViewById(R.id.p1)).setText(labelOrAddress);
	        
	        if (contactsOn) {
		        String address = BlockchainUtil.formatAddress(row.get("address"), 15) ;
		        ((TextView)view.findViewById(R.id.p2)).setText(address);
	        } else {
		        ((TextView)view.findViewById(R.id.p2)).setText(row.get("amount"));	        	
	        }

	        return view;
		}

    }

    private void initMagicList() {

		final WalletApplication application = (WalletApplication)getActivity().getApplication();
		MyRemoteWallet wallet = application.getRemoteWallet();
		activeAddresses = Arrays.asList(wallet.getActiveAddresses());
		labels = wallet.getLabelMap();
        
        magicData =  new ArrayList<HashMap<String,String>>();
        
        filteredDisplayList = new ArrayList<HashMap<String,String>>();

        for(int i = 0; i < activeAddresses.size(); i++) {
		    String address = activeAddresses.get(i);
        	String amount = "0.000";
		    BigInteger finalBalance = wallet.getBalance(address);	
		    if (finalBalance != null)
		    	amount = BlockchainUtil.formatBitcoin(finalBalance);

		        HashMap<String,String> row = new HashMap<String,String>();
		        
		        String label = labels.get(address);
		        String labelOrAddress;
		        if (label != null) {
		            row.put("label", label.toString());	
		            labelOrAddress = label;
		        } else {
		        	labelOrAddress = address;
		        }
		        row.put("address", address.toString());
		        row.put("amount", amount);
		        row.put("labelOrAddress", labelOrAddress);

				magicData.add(row);    
						
	        	filteredDisplayList.add(row);
        }

    }

    private void initAddressBookList() {
 		final WalletApplication application = (WalletApplication)getActivity().getApplication();
 		MyRemoteWallet wallet = application.getRemoteWallet();
 		
        magicData =  new ArrayList<HashMap<String,String>>();

        addressBookMapList = wallet.getAddressBookMap();
        filteredDisplayList = new ArrayList<HashMap<String,String>>();

        if (addressBookMapList != null) {
  		    for (Iterator<Map<String, Object>> iti = addressBookMapList.iterator(); iti.hasNext();) {
 		    	Map<String, Object> addressBookMap = iti.next();
 		    	Object address = addressBookMap.get("addr");
 		    	Object label = addressBookMap.get("label");

 		        HashMap<String,String> row = new HashMap<String,String>();
 		        row.put("label", label.toString());
 		        row.put("address", address.toString());
		        row.put("labelOrAddress", label.toString());

    			magicData.add(row);
	         	filteredDisplayList.add(row);
 		    }

        }
        
     }

    private void displayMagicList() {
    	LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    	isMagic = true;
		
		final int colorOn = 0xFF9d9d9d;
		final int colorOff = 0xFFb6b6b6;

		//
		//
		//
		if(rootView == null) {
	        rootView = inflater.inflate(R.layout.fragment_receive, null, false);
		}
        ((TextView)rootView.findViewById(R.id.currency)).setText("");
        ((TextView)rootView.findViewById(R.id.currency)).setBackgroundColor(0xFF232223);

		//
        // add view with my_addresses and contacts
        //
        magic = ((LinearLayout)rootView.findViewById(R.id.magic_input));
        oldView = ((LinearLayout)magic.findViewById(R.id.magic_bis));
        parent = (LinearLayout)oldView.getParent();
        oldView.setVisibility(View.GONE);
		childIcons = inflater.inflate(R.layout.magic, null);
        ivAddresses = (ImageView)childIcons.findViewById(R.id.addresses);
        ivAddresses.setImageResource(R.drawable.my_addresses);
        ivAddresses.setBackgroundColor(colorOn);
        ivContacts = (ImageView)childIcons.findViewById(R.id.contacts);
        ivContacts.setVisibility(View.VISIBLE);
        ivContacts.setImageResource(R.drawable.address_book);
        ivContacts.setBackgroundColor(colorOff);
        ivPhoneContacts = (ImageView)childIcons.findViewById(R.id.phone_contacts);
        ivPhoneContacts.setVisibility(View.GONE);
        addressesOn = true;
        contactsOn = false;
        ivAddresses.setOnClickListener(new View.OnClickListener() {        
            @Override
                public void onClick(View view) {
            		if(!addressesOn) {
            			addressesOn = true;
            			contactsOn = false;
                        ivAddresses.setBackgroundColor(colorOn);
                        ivContacts.setBackgroundColor(colorOff);
            		}
            		initMagicList();
            		adapter.notifyDataSetChanged();                            		
                }
        });
        ivContacts.setOnClickListener(new View.OnClickListener() {        
            @Override
                public void onClick(View view) {
            		if(!contactsOn) {
            			contactsOn = true;
            			addressesOn = false;
                        ivAddresses.setBackgroundColor(colorOff);
                        ivContacts.setBackgroundColor(colorOn);
            		}
            		initAddressBookList();
            		adapter.notifyDataSetChanged();                            		
                }
        });

        final ImageView qr_scan = (ImageView)childIcons.findViewById(R.id.qr_icon);
        qr_scan.setVisibility(View.INVISIBLE);

        //	    parent.addView(child, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	    parent.addView(childIcons);
	    children++;
	    
    	LinearLayout divider1 = (LinearLayout)childIcons.findViewById(R.id.divider1);
    	divider1.setBackgroundColor(BlockchainUtil.BLOCKCHAIN_GREEN);

        //
        // add view with list
        //
		childList = inflater.inflate(R.layout.magic2, null);
    	divider1 = (LinearLayout)childList.findViewById(R.id.divider1);
    	divider1.setBackgroundColor(BlockchainUtil.BLOCKCHAIN_GREEN);
        magicList = ((ListView)childList.findViewById(R.id.magicList));
        magicList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    parent.addView(childList);
	    children++;
	    
        magicList.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)	{
//                Toast.makeText(getActivity(), keys.get(position), Toast.LENGTH_SHORT).show();
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edAddress.getWindowToken(), 0);

                HashMap<String, String> map = filteredDisplayList.get(position);
                String labelOrAddress = map.get("labelOrAddress");
            	edAddress.setText(labelOrAddress);            	                	               
            	currentSelectedAddress = map.get("address");
            	
                removeMagicList();
                edAmount1.requestFocus();
                edAmount1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

            	if(isBTC) {
            	    tvCurrency.setTypeface(TypefaceUtil.getInstance(getActivity()).getBTCTypeface());
            		tvCurrency.setText(Character.toString((char)TypefaceUtil.getInstance(getActivity()).getBTCSymbol()));
            	}
            	else {
            	    tvCurrency.setTypeface(TypefaceUtil.getInstance(getActivity()).getBTCTypeface());
            		tvCurrency.setText(strCurrentFiatSymbol);
            	}
                
            }
        });

        adapter = new MagicAdapter();
        magicList.setAdapter(adapter);

        LinearLayout container = ((LinearLayout)rootView.findViewById(R.id.qr_container));
        sendViewToBack(container);
        
	    parent.bringToFront();
	    parent.requestLayout();
	    parent.invalidate();
    }

    private void removeMagicList() {
		isMagic = false;

        ((TextView)rootView.findViewById(R.id.currency)).setBackgroundColor(0xFFFFFFFF);
        ((TextView)rootView.findViewById(R.id.currency)).setText(strCurrentFiatSymbol);
        
        if(parent != null) {
            parent.removeViews(parent.getChildCount() - children, children);
            children = 0;
            oldView.setVisibility(View.VISIBLE);
        }
        
        if(addressesOn) {
            initMagicList();
        }
        else {
    		initAddressBookList();
        }
    }


    private void clearReceive()	{
    	/*
        summary.setVisibility(View.INVISIBLE);
        summary2.setVisibility(View.INVISIBLE);
        tvAmount.setText("");
        tvAmount.setVisibility(View.INVISIBLE);
        tvAmountBis.setText("");
        tvAmountBis.setVisibility(View.INVISIBLE);
        tvArrow.setText("");
        tvArrow.setVisibility(View.INVISIBLE);
        */
        tvAddress.setText("");
        tvAddress.setVisibility(View.INVISIBLE);
        tvAddressBis.setText("");
        tvAddressBis.setVisibility(View.INVISIBLE);
        ivReceivingQR.setVisibility(View.INVISIBLE);
    }

}
