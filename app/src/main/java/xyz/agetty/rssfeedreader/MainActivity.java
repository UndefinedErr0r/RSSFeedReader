package xyz.agetty.rssfeedreader;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private EditText promptEditText;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mListView = (ListView) findViewById(R.id.feeds_list_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewFeed();
            }
        });


        db = openOrCreateDatabase("feeds", MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS feedslist (name VARCHAR, desc VARCHAR, url VARCHAR);");

        Cursor resultSet = db.rawQuery("Select * from feedslist", null);
        if(resultSet.getCount() == 0) {
            db.execSQL("INSERT INTO feedslist VALUES('Times of India','The Times of India: Breaking news, views, reviews, cricket from across India', 'http://timesofindia.indiatimes.com/rssfeedstopstories.cms');");
            db.execSQL("INSERT INTO feedslist VALUES('Top Headlines - The Times of India','Times of India brings the Latest & Top Breaking News on Politics and Current Affairs in India & around the World, Cricket, Sports, Business, Bollywood News and Entertainment, Science, Technology, Health & Fitness news & opinions from leading columnists.', 'http://timesofindia.indiatimes.com/rssfeeds/1221656.cms');");
            Toast.makeText(getApplicationContext(), "No feeds found. 2 Sample feeds added to list", Toast.LENGTH_LONG).show();
        }


        new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    loadFeeds();
                }
            }
        , 1500);
        Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "Loading Feed List...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    @Override
    public void onResume(){
        super.onResume();
        System.out.println("Resuming...");
        new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    loadFeeds();
                }
            }
        , 1500);
    }

    private void loadFeeds() {
        Cursor resultSet = db.rawQuery("Select * from feedslist", null);
        resultSet.moveToFirst();
        int feeds = 0;

        TextView feedsListStatus = (TextView)findViewById(R.id.feedliststatus);
        feedsListStatus.setText("");
        ArrayList feedsListItems = new ArrayList<>();

        while(!resultSet.isAfterLast()) {
            String name = resultSet.getString(0);
            String desc  = resultSet.getString(1);
            String url  = resultSet.getString(2);
            feedsListItems.add(new FeedsListViewDataModel(name, desc));
            feeds++;
            resultSet.moveToNext();
        }


        FeedsListViewAdapter adapter= new FeedsListViewAdapter(MainActivity.this, feedsListItems, getApplicationContext());

        // ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, listItems);
        mListView.setAdapter(adapter);

        ((View)findViewById(R.id.progressBar1)).setVisibility(View.GONE);

        if(feeds > 0) feedsListStatus.setText("");
        else feedsListStatus.setText("No Feeds!");
    }


    private void addNewFeed(String url) {
        Cursor resultSet = db.rawQuery("Select * from feedslist WHERE url='" + url + "'", null);
        if(resultSet.getCount() != 0) {
            Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "This feed already exists.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return;
        }

        final StringBuffer feedName = new StringBuffer(), feedDesc = new StringBuffer(), feedURL = new StringBuffer(url);

        ((View)findViewById(R.id.progressBar1)).setVisibility(View.VISIBLE);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {



                        XmlPullParserFactory factory = null;
                        try {


                            factory = XmlPullParserFactory.newInstance();
                            factory.setNamespaceAware(true);
                            XmlPullParser xpp = factory.newPullParser();
                            xpp.setInput(new StringReader(response));

                            String currentTag = "";
                            StringBuffer sb = new StringBuffer();

                            int eventType = xpp.getEventType();
                            while (eventType != XmlPullParser.END_DOCUMENT) {
                                if(eventType == XmlPullParser.START_DOCUMENT) {
                                    System.out.println("Start document");
                                } else if(eventType == XmlPullParser.START_TAG) {
                                    currentTag += xpp.getName() + ".";
                                    System.out.println("CURR " + currentTag);
                                } else if(eventType == XmlPullParser.END_TAG) {
                                    currentTag = currentTag.replace(xpp.getName() + ".", "");
                                    System.out.println("CLSR " + currentTag);
                                } else if(eventType == XmlPullParser.TEXT) {
                                    switch(currentTag) {
                                        case "rss.channel.title.":
                                            feedName.append(xpp.getText());
                                            break;
                                        case "rss.channel.description.":
                                            feedDesc.append(xpp.getText());
                                            break;

                                    }

                                }
                                eventType = xpp.next();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        db.execSQL("INSERT INTO feedslist VALUES(" + DatabaseUtils.sqlEscapeString(feedName.toString()) + "," + DatabaseUtils.sqlEscapeString(feedDesc.toString()) + ", " + DatabaseUtils.sqlEscapeString(feedURL.toString()) + ");");
                        Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "Added '" + feedName + "'", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        ((View)findViewById(R.id.progressBar1)).setVisibility(View.GONE);
                        loadFeeds();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "Add failed: Could not connect to server.", Snackbar.LENGTH_LONG).setAction("Action", null).show();


                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                ((View)findViewById(R.id.progressBar1)).setVisibility(View.GONE);
                            }
                        }
                 , 1500);

            }
        });
        queue.add(stringRequest);



    }


    /*

    private void loadFeeds() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.0.100/rss/feed.txt";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout), "Load Complete", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                        Cursor resultSet = db.rawQuery("Select * from feedslist", null);
                        resultSet.moveToFirst();

                        ArrayList feedsListItems= new ArrayList<>();

                        while(!resultSet.isAfterLast()) {
                            String name = resultSet.getString(0);
                            String desc  = resultSet.getString(1);
                            String url  = resultSet.getString(2);
                            feedsListItems.add(new FeedsListViewDataModel(name, desc));
                        }


                        FeedsListViewAdapter adapter= new FeedsListViewAdapter(feedsListItems, getApplicationContext());

                       // ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, listItems);
                       mListView.setAdapter(adapter);

                        ((View)findViewById(R.id.progressBar1)).setVisibility(View.GONE);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make((CoordinatorLayout) findViewById(R.id.coordinatorLayout),  error.toString(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        queue.add(stringRequest);
    }
    */


    private void addNewFeed() {

        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String url = promptEditText.getText().toString();
                Intent openFeedPage = new Intent(MainActivity.this, FeedReader.class);
                openFeedPage.putExtra("url", url);

                switch(whichButton) {
                    case BUTTON_POSITIVE: addNewFeed(url); return;
                    case BUTTON_NEGATIVE: return;
                    case BUTTON_NEUTRAL:  startActivity(openFeedPage); return;
                }
            }
        };

        promptEditText = new EditText(this);
        promptEditText.setText("http://feeds.reuters.com/reuters/technologyNews?format=xml");
        new AlertDialog.Builder(this).setTitle("Add New Feed")
        .setMessage("Enter an RSS Feed URL:")
        .setView(promptEditText)
        .setPositiveButton("Add", dialogOnClickListener)
        .setNegativeButton("Cancel", dialogOnClickListener)
        .setNeutralButton("Open Only", dialogOnClickListener)
        .show();

        //.setView(LayoutInflater.from(MainActivity.this).inflate(R.layout.add_feed, null))
    }

    public void readFeed(String feedName) {
        System.out.println("opening " + feedName);
        Cursor resultSet = db.rawQuery("Select * from feedslist where name = '" + feedName + "'", null);
        resultSet.moveToFirst();
        String name = resultSet.getString(0);
        String desc  = resultSet.getString(1);
        String url  = resultSet.getString(2);
        Intent openFeedPage = new Intent(MainActivity.this, FeedReader.class);
        openFeedPage.putExtra("url", url);
        startActivity(openFeedPage);

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

            Intent dbview = new Intent(MainActivity.this, ViewDatabase.class);
            startActivity(dbview);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
