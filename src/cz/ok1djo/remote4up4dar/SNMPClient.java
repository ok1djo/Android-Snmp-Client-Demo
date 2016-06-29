package cz.ok1djo.remote4up4dar;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.io.IOException;
import java.util.List;

public class SNMPClient extends Activity implements View.OnClickListener {
	DrawView drawView;
	//
	Context context;

	private static final String TAG = "SNMP CLIENT";
	// command to request from Server
	// TODO must change this value depend on your agent....
	private static final String OIDBASE = "1.3.6.1.3.5573.1.";
	private static final String OIDVOLUME = "6.10";
	private static final String OIDCALL = "3";
	private static final String OIDVOLTAGE = "4";
	private static final String OIDBACKLIGHT = "9.2";
	private static final String OIDMAINDISP = "1.8.1.1";
	private String OIDVALUE = OIDBASE + OIDBACKLIGHT;
	private static final int SNMP_VERSION = SnmpConstants.version1;

	//
	public static Snmp snmp;
	public static CommunityTarget comtarget;
	static PDU pdu;
	public int type;
	public Variable value;
	static OID oid;
	public  VariableBinding req;

	// UI
	private Button sendBtn;
	private ToggleButton BackLt;
	private ToggleButton Mute;
	private EditText console;
	private ProgressBar mSpinner;
	private StringBuffer logResult = new StringBuffer();
	private String CallSign;
	private Integer Supply;
	private Integer Volume;
	private Integer BackLight;
	private TextView textView;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		context = this;
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		// Initialize UI
		iniUI();
	}

	private void iniUI() {
		sendBtn = (Button) findViewById(R.id.sendBtn);
		sendBtn.setOnClickListener(this);
		BackLt = (ToggleButton) findViewById(R.id.toggleButtonLight);
		BackLt.setOnClickListener(this);
		Mute = (ToggleButton) findViewById(R.id.toggleButtonMute);
		Mute.setOnClickListener(this);
		console = (EditText) findViewById(R.id.console);
		mSpinner = (ProgressBar) findViewById(R.id.progressBar);
		textView= (TextView) findViewById(R.id.textView);
//		imageView = (ImageView) findViewById(R.id.imageView);
//		drawView = (DrawView) findViewById(R.id.drawView);
		SimpleDrawingView.fillBitmap();
    }

	class DrawView extends View {
		Paint paint = new Paint();

		public DrawView(Context context) {
			super(context);
			paint.setColor(Color.BLUE);
		}
		@Override
		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawLine(10, 20, 30, 40, paint);
			canvas.drawLine(20, 10, 50, 20, paint);

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.preferences: {
				Intent intent = new Intent();
				intent.setClassName(this, "cz.ok1djo.remote4up4dar.MyPreferenceActivity");
				startActivity(intent);
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
//		type = PDU.GET;
		switch (v.getId()) {
			case R.id.toggleButtonLight: {
				if (BackLt.isChecked()) {
					value = new Integer32(5);
					logResult.append("entered 5\n");
				} else {
					value = new Integer32(0);
					logResult.append("entered 0\n");
				}
				doMagic(OIDBASE + OIDBACKLIGHT,PDU.SET,value);
				break;
			}

			case R.id.toggleButtonMute: {
				if (Mute.isChecked()) {
					value = new Integer32(-57);
					logResult.append("entered -57\n");
				} else {
					value = new Integer32(-10);
					logResult.append("entered -10\n");
				}
				doMagic(OIDBASE + OIDVOLUME,PDU.SET,value);
				break;
			}

			case R.id.sendBtn: {
				doMagic(OIDBASE + OIDCALL,PDU.GET,null);
				CallSign=req.getVariable().toString();
				doMagic(OIDBASE + OIDVOLTAGE,PDU.GET,null);
				Supply=req.getVariable().toInt();
				doMagic(OIDBASE + OIDVOLUME,PDU.GET,null);
				Volume=req.getVariable().toInt();
				doMagic(OIDBASE + OIDBACKLIGHT,PDU.GET,null);
				BackLight=req.getVariable().toInt();
				doMagic(OIDBASE + OIDMAINDISP,PDU.GET,null);
				SimpleDrawingView.paintBitmap(req.getVariable().toString());
				textView.setText("CallSign: "+CallSign+"\nSupply: "+Supply+" mV\nBacklight: "+BackLight+"\nVolume: "+Volume);
				if (Volume==-57) {
					Mute.setChecked(true);
				} else {
					Mute.setChecked(false);
				}
				if (BackLight==0) {
					BackLt.setChecked(false);
				} else {
					BackLt.setChecked(true);
				}
				break;
			}
			//.... etc

		}

	}
	public void doMagic(String lOIDVALUE, int ltype, Variable lvalue) {

			mSpinner.setVisibility(View.VISIBLE);
			try{
				logResult.append("sleeping \n");
				Thread.sleep(100);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
//		semafor=true;
		AsyncTask<Void, Void, Void> mAsyncTask = new AsyncTask<Void, Void, Void>() {

			protected void onPreExecute() {
				mSpinner.setVisibility(View.VISIBLE);
			}

			@Override
			protected Void doInBackground(Void... params) {
				try {
					sendSnmpRequest(lOIDVALUE, ltype, lvalue);
				} catch (Exception e) {
					Log.d(TAG,
							"Error sending snmp request - Error: " + e.getMessage());
				}
				return null;
			}

			protected void onPostExecute(Void result) {
				console.setText("");
				console.append(logResult);
				mSpinner.setVisibility(View.GONE);
			}
		};
		mAsyncTask.execute();
		try{
			logResult.append("sleeping \n");
			Thread.sleep(100);
		}catch(InterruptedException e){
			e.printStackTrace();
		}

	}

	private void sendSnmpRequest(String cmd, int typ, Variable var) throws Exception {
		// Create TransportMapping and Listen
		Log.d(TAG, "entered sendsnmpRequest");
		logResult.append("entered sendsnmpRequest\n");

		TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
		transport.listen();

		// Get variables from preferences
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String ipAddress = sharedPref.getString("ip", "DEFAULT");
		String community = sharedPref.getString("comm", "DEFAULT");
		String port = sharedPref.getString("port", "DEFAULT");


		Log.d(TAG, "Create Target Address object");
		logResult.append("Create Target Address object\n");
		// Create Target Address object
		CommunityTarget comtarget = new CommunityTarget();
		comtarget.setCommunity(new OctetString(community));
		comtarget.setVersion(SNMP_VERSION);

		Log.d(TAG, "-address: " + ipAddress + "/" + port);
		logResult.append("-address: " + ipAddress + "/" + port + "\n");

		comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
		comtarget.setRetries(2);
		comtarget.setTimeout(1000);

		Log.d(TAG, "Prepare PDU " + cmd);
		logResult.append("Prepare PDU\n");
		// create the PDU
		PDU pdu = new PDU();
		if (typ == PDU.GET) {
			pdu.add(new VariableBinding(new OID(cmd)));
		} else {
			pdu.add(new VariableBinding(new OID(cmd), var));
		}
		pdu.setType(typ);

		Snmp snmp = new Snmp(transport);
		Log.d(TAG, "Sending Request to Agent...");
		logResult.append("Sending Request to Agent...\n");

		// send the PDU
		ResponseEvent response = snmp.send(pdu, comtarget);

		// Process Agent Response
		if (response != null) {
			// extract the response PDU (could be null if timed out)
			PDU responsePDU = response.getResponse();
			// extract the address used by the agent to send the response:
			Address peerAddress = response.getPeerAddress();
			Log.d(TAG, "peerAddress " + peerAddress);
			if (responsePDU != null) {
				int errorStatus = responsePDU.getErrorStatus();
				int errorIndex = responsePDU.getErrorIndex();
				String errorStatusText = responsePDU.getErrorStatusText();

				if (errorStatus == PDU.noError) {
					Log.d(TAG,
							"Snmp Get Response = "
									+ responsePDU.getVariableBindings());
					logResult.append("Snmp Get Response = "
							+ responsePDU.getVariableBindings() + "\n");
					req=responsePDU.getVariableBindings().get(0);
				} else {
					Log.d(TAG, "Error: Request Failed");
					Log.d(TAG, "Error Status = " + errorStatus);
					Log.d(TAG, "Error Index = " + errorIndex);
					Log.d(TAG, "Error Status Text = " + errorStatusText);

					logResult.append("Error: Request Failed"
							+ "Error Status = " + errorStatus
							+ "Error Index = " + errorIndex
							+ "Error Status Text = " + errorStatusText + "\n");
				}
			} else {
				Log.d(TAG, "Error: Response PDU is null");
				logResult.append("Error: Response PDU is null \n");
			}
		} else {
			Log.d(TAG, "Error: Agent Timeout... \n");
			logResult.append("Error: Agent Timeout... \n");
		}
		snmp.close();
//		semafor=false;
	}
}






