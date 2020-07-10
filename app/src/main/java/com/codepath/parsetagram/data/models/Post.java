package com.codepath.parsetagram.data.models;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseClassName;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_USER = "user";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String KEY_LIKES = "likes";
    public static final String KEY_COMMENTS = "comments";

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setImage(ParseFile parseFile) {
        put(KEY_IMAGE, parseFile);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public List<String> getLikesList() { return getList(KEY_LIKES); }

    public void addLike(ParseUser user) {
        List<String> likesList = getLikesList();
        likesList.add(user.getUsername());
        put(KEY_LIKES, likesList);
    }

    public void deleteLike(ParseUser user) {
        List<String> likesList = getLikesList();
        likesList.remove(user.getUsername());
        put(KEY_LIKES, likesList);
    }

    public List<String> getCommentsList() { return getList(KEY_COMMENTS); }

    public void addComment(@NotNull ParseUser user) {
        List<String> commentsList = getCommentsList();
        commentsList.add(user.getUsername());
        put(KEY_COMMENTS, commentsList);
    }

    public void deleteComment(@NotNull ParseUser user) {
        List<String> commentsList = getCommentsList();
        commentsList.remove(user.getUsername());
        put(KEY_COMMENTS, commentsList);
    }


}
