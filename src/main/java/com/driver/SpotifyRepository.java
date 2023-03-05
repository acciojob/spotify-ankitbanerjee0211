package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User newUser = new User(name, mobile);
        users.add(newUser);
        userPlaylistMap.put(newUser, new ArrayList<>());
        return newUser;
    }

    public Artist createArtist(String name) {
        Artist newArtist = new Artist(name);
        artists.add(newArtist);
        artistAlbumMap.put(newArtist, new ArrayList<>());
        return newArtist;
    }

    public Album createAlbum(String title, String artistName) {
        Album newAlbum = new Album(title);

        // checking if the artist exists
        boolean found = false;
        Artist currArtist = null;
        for(Artist artist: artists){
            if(artistName.equals(artist.getName())){
                found = true;
                currArtist = artist;
                break;
            }
        }
        // otherwise creating artist
        if(!found){
            currArtist = createArtist(artistName);
        }

        // mapping
        albums.add(newAlbum);
        albumSongMap.put(newAlbum, new ArrayList<>());
        artistAlbumMap.get(currArtist).add(newAlbum);

        return newAlbum;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Song newSong = new Song(title, length);

        boolean found = false;
        Album currAlbum = null;
        for(Album album: albums){
            if(albumName.equals(album.getTitle())){
                found = true;
                currAlbum = album;
                break;
            }
        }
        if(!found){
            throw new Exception("Album does not exist");
        }

        songs.add(newSong);
        songLikeMap.put(newSong, new ArrayList<>());
        albumSongMap.get(currAlbum).add(newSong);

        return newSong;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        //Create a playlist with given title and add all songs having the given length in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception

        Playlist newPlaylist = new Playlist(title);

        boolean found = false;
        User currUser = null;
        for(User user: users){
            if(mobile.equals(user.getMobile())){
                found = true;
                currUser = user;
                break;
            }
        }
        if(!found){
            throw new Exception("User does not exist");
        }

        List<Song> songList = new ArrayList<>();
        for(Song song: songs){
            if(song.getLength() == length) songList.add(song);
        }
        playlistSongMap.put(newPlaylist, songList);

        List<User> listnerList = new ArrayList<>();
        listnerList.add(currUser);
        playlistListenerMap.put(newPlaylist, listnerList);

        creatorPlaylistMap.put(currUser, newPlaylist);

        userPlaylistMap.get(currUser).add(newPlaylist);

        playlists.add(newPlaylist);

        return newPlaylist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        //Create a playlist with given title and add all songs having the given titles in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception

        Playlist newPlaylist = new Playlist(title);

        boolean found = false;
        User currUser = null;
        for(User user: users){
            if(mobile.equals(user.getMobile())){
                found = true;
                currUser = user;
                break;
            }
        }
        if(!found){
            throw new Exception("User does not exist");
        }

        List<Song> songList = new ArrayList<>();
        for(Song song: songs){
            if(songTitles.contains(song.getTitle())) songList.add(song);
        }
        playlistSongMap.put(newPlaylist, songList);

        List<User> listnerList = new ArrayList<>();
        listnerList.add(currUser);
        playlistListenerMap.put(newPlaylist, listnerList);

        creatorPlaylistMap.put(currUser, newPlaylist);

        userPlaylistMap.get(currUser).add(newPlaylist);

        playlists.add(newPlaylist);

        return newPlaylist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //Find the playlist with given title and add user as listener of that playlist and update user accordingly
        //If the user is creater or already a listener, do nothing
        //If the user does not exist, throw "User does not exist" exception
        //If the playlist does not exists, throw "Playlist does not exist" exception
        // Return the playlist after updating

        // finding user
        boolean found = false;
        User currUser = null;
        for(User user: users){
            if(mobile.equals(user.getMobile())){
                found = true;
                currUser = user;
                break;
            }
        }
        if(!found){
            throw new Exception("User does not exist");
        }

        // finding playlist
        found = false;
        Playlist currPlaylist = null;
        for(Playlist playlist: playlists){
            if(playlistTitle.equals(playlist.getTitle())){
                found = true;
                currPlaylist = playlist;
                break;
            }
        }
        if(!found){
            throw new Exception("Playlist does not exist");
        }

        // playlist and user found
        // adding listener to playlist
        List<User> userList = playlistListenerMap.get(currPlaylist);
        if(!userList.contains(currUser)){
            userList.add(currUser);
        }

        // adding playlist to user
        List<Playlist> playlistList = userPlaylistMap.get(currUser);
        if(!playlistList.contains(currPlaylist)){
            playlistList.add(currPlaylist);
        }

        return currPlaylist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        //The user likes the given song. The corresponding artist of the song gets auto-liked
        //A song can be liked by a user only once. If a user tried to like a song multiple times, do nothing
        //However, an artist can indirectly have multiple likes from a user, if the user has liked multiple songs of that artist.
        //If the user does not exist, throw "User does not exist" exception
        //If the song does not exist, throw "Song does not exist" exception
        //Return the song after updating

        // finding user
        boolean found = false;
        User currUser = null;
        for(User user: users){
            if(mobile.equals(user.getMobile())){
                found = true;
                currUser = user;
                break;
            }
        }
        if(!found){
            throw new Exception("User does not exist");
        }

        // finding song
        found = false;
        Song currSong = null;
        for(Song song: songs){
            if(songTitle.equals(song.getTitle())){
                found = true;
                currSong = song;
                break;
            }
        }
        if(!found){
            throw new Exception("Song does not exist");
        }
        // song and user found

        // updating song likes
        List<User> likedUsers = songLikeMap.get(currSong);
        if(!likedUsers.contains(currUser)) {

            likedUsers.add(currUser);
            currSong.setLikes(likedUsers.size());

            // update artist likes
            Artist currArtist = null;
            for (Artist artist : artistAlbumMap.keySet()) {
                List<Album> albumList = artistAlbumMap.get(artist);
                for (Album album : albumList) {
                    if (albumSongMap.get(album).contains(currSong)) {
                        currArtist = artist;
                        currArtist.setLikes(currArtist.getLikes() + 1);
                    }
                }
            }
        }

        return currSong;
    }

    public String mostPopularArtist() {
        Artist mostPopular = artists.get(0);
        for(Artist artist: artists){
            if(artist.getLikes() > mostPopular.getLikes()){
                mostPopular = artist;
            }
        }

        return mostPopular.getName();
    }

    public String mostPopularSong() {
        Song mostPopular = songs.get(0);
        for(Song song: songs){
            if(song.getLikes() > mostPopular.getLikes()){
                mostPopular = song;
            }
        }

        return mostPopular.getTitle();
    }
}
