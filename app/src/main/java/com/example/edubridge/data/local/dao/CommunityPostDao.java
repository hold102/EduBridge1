package com.example.edubridge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.edubridge.data.local.entity.LocalCommunityPost;

import java.util.List;

/**
 * Data Access Object for LocalCommunityPost entities.
 */
@Dao
public interface CommunityPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LocalCommunityPost> posts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LocalCommunityPost post);

    @Query("SELECT * FROM community_posts ORDER BY createdAt DESC")
    LiveData<List<LocalCommunityPost>> getAllPosts();

    @Query("SELECT * FROM community_posts ORDER BY createdAt DESC")
    List<LocalCommunityPost> getAllPostsSync();

    @Query("DELETE FROM community_posts")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM community_posts")
    int getPostCount();
}
