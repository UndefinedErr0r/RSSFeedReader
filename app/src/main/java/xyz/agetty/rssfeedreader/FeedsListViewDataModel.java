package xyz.agetty.rssfeedreader;

/**
 * Created by Royston on 6/9/2017.
 */

public class FeedsListViewDataModel {
    String name;
    String url;

    public FeedsListViewDataModel(String name, String url) {
        this.name = name;
        this.url = url;

    }

    public String getName() {
        return name;
    }

    public String getURL() {
        return url;
    }
}
