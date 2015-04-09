package remotecontroller.example.com.remotecontroller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    public static String consoleText;
    EditText consoleEditText;
    EditText commandEditText;
    Button runButton;
    Toast networkNotAvailableToast;

    public MainActivity() {
        consoleText = new String("bash > ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        StrictMode.ThreadPolicy policy = new StrictMode.
//        ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
        Context context = getApplicationContext();
        CharSequence errorText = "Network Not Available";
        int duration = Toast.LENGTH_SHORT;
        networkNotAvailableToast = Toast.makeText(context, errorText, duration);
        consoleEditText = (EditText) findViewById(R.id.consoleEditText);
        consoleEditText.setKeyListener(null);
        commandEditText = (EditText) findViewById(R.id.commandEditText);
        runButton = (Button) findViewById(R.id.run_button);
        consoleEditText.setText(consoleText);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commandText = commandEditText.getText().toString();
                if (commandText.length() <= 0) {
                    consoleText = consoleText + "\nbash >";
                } else {
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        executeRemoteCommand(commandText);
                    } else {
                        networkNotAvailableToast.show();
                    }
                }
                consoleEditText.setText(consoleText);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void executeRemoteCommand(String commandText) {
        class ExecuteRemoteCommand extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                Log.i("AsyncTask", "Inside doInBackground");
                if (params.length > 0) {
                    String commandText = params[0];
                    return runCommandOverHttpConnection(commandText);
                }
                return null;
            }

            private String runCommandOverHttpConnection(String command) {
                Log.i("AsyncTask", "Inside runCommandOverHttpConnection");
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://172.28.142.38:3000/");
                Log.i("AsyncTask", "Made httpPost connection");

                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                    nameValuePairs.add(new BasicNameValuePair("command", command));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    Log.i("AsyncTask", "calling execute for command " + command);
                    HttpResponse response = httpClient.execute(httpPost);
                    Log.i("AsyncTask", "response " + response.toString());
                    InputStream inputStream = response.getEntity().getContent();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String bufferedStrChunk = null;
                    while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
                        stringBuilder.append(bufferedStrChunk);
                    }
                    String responseBody = stringBuilder.toString();
                    Log.i("ASyncTask", "response String " + responseBody);
                    consoleText += command;
                    consoleText += "\n";
                    return responseBody;
                } catch (UnsupportedEncodingException ex) {
                    Log.e("AsyncTask", ex.toString());
                    ex.printStackTrace();
                } catch (ClientProtocolException ex) {
                    Log.e("AsyncTask", ex.toString());
                    ex.printStackTrace();
                } catch (IOException ex) {
                    Log.e("AsyncTask", ex.toString());
                    ex.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(String result) {
                consoleText += result;
                consoleText += "\nbash >";
                consoleEditText.setText(consoleText);

            }
        }
        new ExecuteRemoteCommand().execute(commandText);
    }
}


