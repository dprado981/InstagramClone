package com.codepath.parsetagram.data.model;

import com.parse.ParseObject;
import com.parse.ParseClassName;
import com.parse.ParseUser;

@ParseClassName("Comment")
public class Comment extends ParseObject {

    public static final String KEY_TEXT = "text";
    public static final String KEY_USER = "user";
    public static final String KEY_POST = "post";

    public void setText(String text) { put(KEY_TEXT, text); }

    public String getText() { return getString(KEY_TEXT); }

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setPost(Post post) { put(KEY_POST, post.getObjectId()); }

    public Post getPost() { return (Post) get(KEY_POST); }

}
