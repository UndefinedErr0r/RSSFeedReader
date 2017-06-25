package xyz.agetty.rssfeedreader;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

public class ViewDatabase extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_database);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadDatabase("http://agetty.xyz:8081/list");

    }

    private void loadDatabase(String url) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        TextView feedText = ((TextView)findViewById(R.id.databasetext));
                        Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "Load Complete", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                        StringBuffer sb = new StringBuffer();


                        try {
                            //obj = new JSONObject(response);
                            System.out.println("DATA" + response);
                            //JSONObject obj = new JSONObject(response);
                            JSONArray obj = new JSONArray(response);
                            for (int i = 0; i < obj.length(); i++)
                            {
                                //JSONObject item = obj.getJSONObject("item_" + i);
                                JSONObject item = obj.getJSONObject(i);

                                sb.append("<h2>");
                                sb.append(item.get("title"));
                                sb.append("</h2>");


                                sb.append("");
                                sb.append(item.get("description"));
                                sb.append("<br/<br/>");

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //String data = obj.getJSONObject("sdfds").getString("sdfsdf");


                        feedText.setMovementMethod(LinkMovementMethod.getInstance());


                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            feedText.setText(Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            feedText.setText(Html.fromHtml(sb.toString()));
                        }

                        ((View)findViewById(R.id.progressBar1)).setVisibility(View.GONE);



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "Could not connect to server.", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                ((View)findViewById(R.id.progressBar1)).setVisibility(View.GONE);
                                finish();
                            }
                        }
                        , 1500);

            }
        });
        queue.add(stringRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_database, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear) {

            ((View)findViewById(R.id.progressBar1)).setVisibility(View.VISIBLE);
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://agetty.xyz:8081/clear",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            TextView feedText = ((TextView)findViewById(R.id.databasetext));
                            Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "Remote database cleared!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            ((View)findViewById(R.id.progressBar1)).setVisibility(View.GONE);
                            finish();


                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "Could not connect to server.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    ((View) findViewById(R.id.progressBar1)).setVisibility(View.GONE);

                                }
                            }
                            , 1500);

                }

            });
            queue.add(stringRequest);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}


