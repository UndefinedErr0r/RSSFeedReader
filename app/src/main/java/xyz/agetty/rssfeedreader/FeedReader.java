package xyz.agetty.rssfeedreader;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import android.webkit.WebView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FeedReader extends AppCompatActivity {

    private SQLiteDatabase db;
    private String currentURL;
    public JSONObject nodeString;
    public JSONArray nodeArray;
    public  RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_reader);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = openOrCreateDatabase("feeds", MODE_PRIVATE,null);

        queue = Volley.newRequestQueue(getApplicationContext());

        Bundle extras = getIntent().getExtras();
        currentURL = extras.get("url").toString();
        ((TextView)findViewById(R.id.feedtext)).setText(currentURL);
        loadFeedData(currentURL);




    }


    private void loadFeedData(String url) {
        //RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        TextView feedText = ((TextView)findViewById(R.id.feedtext));
                        Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "Load Complete", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        //((TextView)findViewById(R.id.feedtext)).setText(response);

                        nodeString = new JSONObject();
                        nodeArray = new JSONArray();

                        XmlPullParserFactory factory = null;
                        try {

                            android.support.v7.app.ActionBar ab = getSupportActionBar();

                            factory = XmlPullParserFactory.newInstance();
                            factory.setNamespaceAware(true);
                            XmlPullParser xpp = factory.newPullParser();
                            xpp.setInput(new StringReader(response));

                            String currentTag = "";
                            StringBuffer sb = new StringBuffer();

                            int eventType = xpp.getEventType();
                            int itemID = 0;


                            String cv_title = "", cv_desc = "", cv_link = "";



                            while (eventType != XmlPullParser.END_DOCUMENT) {
                                if(eventType == XmlPullParser.START_DOCUMENT) {
                                    //System.out.println("Start document");
                                } else if(eventType == XmlPullParser.START_TAG) {
                                    currentTag += xpp.getName() + ".";
                                    //System.out.println("CURR " + currentTag);

                                    if(xpp.getName().equals("item")) {
                                        sb.append("<div style='color:grey; box-shadow: 0px 0px 5px black; border-radius: 5px; margin:15px; font-family: verdana;'>");
                                    }

                                } else if(eventType == XmlPullParser.END_TAG) {

                                    if(xpp.getName().equals("item")) {
                                        sb.append("<h2 style='color:white; background-color:#3F51B5; padding:5px;'>" + cv_title + "</h2>");
                                        sb.append("<span style='color:grey; padding:5px; display:block;'>" + cv_desc + "</span>");
                                        sb.append("<br/><a style='padding:5px;' href='" + cv_link + "'>Full Article</a><br/><br/>");
                                        sb.append("</div>");

                                        cv_link = "<a href='" + cv_link + "'>Full Article</a><br/>";
                                        nodeArray.put(new JSONObject().put("title", cv_title).put("desc", cv_desc + "<br/><br/>" + cv_link));
                                        itemID++;

                                    }

                                    currentTag = currentTag.replace(xpp.getName() + ".", "");
                                    //System.out.println("CLSR " + currentTag);
                                } else if(eventType == XmlPullParser.TEXT) {
                                    switch(currentTag) {
                                        case "rss.channel.title.":
                                                ab.setTitle(xpp.getText());
                                                break;
                                        case "rss.channel.description.":
                                                ab.setSubtitle(xpp.getText());
                                                break;
                                        case "rss.channel.item.title.":
                                                cv_title = xpp.getText();
                                                break;
                                        case "rss.channel.item.link.":
                                                cv_link = xpp.getText();
                                                break;
                                        case "rss.channel.item.description.":
                                                cv_desc = xpp.getText().replaceAll("\\<.*?>","");
                                                break;
                                    }

                                }
                                eventType = xpp.next();


                                ((WebView)findViewById(R.id.feedtextwebview)).loadData(sb.toString(), "text/html", null);
                                ((WebView)findViewById(R.id.feedtextwebview)).setVerticalScrollBarEnabled(true);
                                //((WebView)findViewById(R.id.feedtextwebview)).setHorizontalScrollBarEnabled(true);

                                /*

                                feedText.setMovementMethod(LinkMovementMethod.getInstance());

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    feedText.setText(Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY));
                                } else {
                                    feedText.setText(Html.fromHtml(sb.toString()));
                                }
                                */

                                ((View)findViewById(R.id.progressBar1)).setVisibility(View.GONE);

                            }

                           // System.out.println(nodeString.toString());
                            //feedText.setText(nodeString.toString());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


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
        getMenuInflater().inflate(R.menu.menu_feedreader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            db.execSQL("DELETE FROM feedslist where url = '" + currentURL + "'");
            finish();
            return true;
        }

        if (id == R.id.action_upload) {
            uploadToNode();
            Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "Uploading...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void uploadToNode() {
        //nodeString


        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://agetty.xyz:8081/save",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "Uploaded data to postgresql database.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "Could not connect to server.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("data", nodeArray.toString());

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,  DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }
}
