package com.quickbite.rx2020.managers;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.TimeUtils;
import com.quickbite.rx2020.util.Logger;

import java.util.HashMap;

/**
 * Created by Paha on 2/19/2015.
 */
public class EasyAssetManager extends AssetManager {
    public static boolean log = false;

    private HashMap<String, DataReference> dataMap = new HashMap<String, DataReference>(20);

    /**
     * Helper function to load all pictures from a baseDir. Doesn't have to be used...
     * @param baseDir The base directory handle to start in.
     */
    public void loadALlPictures(FileHandle baseDir){
        Long startTime = TimeUtils.millis();

        TextureLoader.TextureParameter params = new TextureLoader.TextureParameter();
        params.genMipMaps = true;
        params.magFilter = Texture.TextureFilter.Linear;
        params.minFilter = Texture.TextureFilter.Linear;

        FileHandle[] files = baseDir.list();

        for(FileHandle handle : files){
            if(handle.isDirectory())
                loadALlPictures(handle);

            if(handle.name().endsWith(".png")) {
                this.load(handle.path(), Texture.class, params);
                if(log) System.out.println("Loaded "+handle.path());
            }
        }

        Long time = TimeUtils.millis() - startTime;
        Logger.log("AssetManager", "Took "+(time/1000f)+"s to load art.", Logger.LogLevel.Info);
    }

    /**
     * Helper function to load all fonts from a baseDir. Doesn't have to be used...
     * @param baseDir The base directory handle to start in.
     */
    public void loadAllFonts(FileHandle baseDir){
        Long startTime = TimeUtils.millis();

        BitmapFontLoader.BitmapFontParameter params = new BitmapFontLoader.BitmapFontParameter();
        params.genMipMaps = true;
        params.magFilter = Texture.TextureFilter.Linear;
        params.minFilter = Texture.TextureFilter.Linear;

        FileHandle[] files = baseDir.list();

        for(FileHandle handle : files){
            if(handle.isDirectory())
                loadAllFonts(handle);

            if(handle.name().endsWith(".fnt")) {
                this.load(handle.path(), BitmapFont.class);
                if(log) System.out.println("Loaded "+handle.path());
            }
        }

        Long time = TimeUtils.millis() - startTime;
        Logger.log("AssetManager", "Took "+(time/1000f)+"s to load fonts.", Logger.LogLevel.Info);
    }

    /**
     * Helper function to load all texture atlases. Doesn't have to be used...
     * @param baseDir The base directory handle to start in.
     */
    public void loadAllImageSheets(FileHandle baseDir){
        Long startTime = TimeUtils.millis();

        TextureAtlasLoader.TextureAtlasParameter params = new TextureAtlasLoader.TextureAtlasParameter();

        FileHandle[] files = baseDir.list(); //Get the list of files.

        //For each file, try and load it.
        for(FileHandle handle : files){

            //If it's not a dir and it ends with .pack, load it!
            if(!handle.isDirectory() && handle.name().endsWith(".pack")) {
                this.load(handle.path(), TextureAtlas.class, params);
                if(log) System.out.println("Loaded "+handle.path());
            }
        }

        Long time = TimeUtils.millis() - startTime;
        Logger.log("AssetManager", "Took "+(time/1000f)+"s to load texture atlases..", Logger.LogLevel.Info);
    }

    public synchronized <T> T get(String commonName, Class<T> type) {
        //Get the reference from the data map.
        DataReference ref = dataMap.get(commonName);
        if(ref == null) {
            //If it's null, let's try to find it in the underlying AssetManager by the common name. This really only is useful in cases where the underlying AssetManager loads its own file,
            //for instance, when you load an Atlas file and it loads the images for you.
            if(this.isLoaded(commonName)) {
                return super.get(commonName, type);
            }

            Logger.log("AssetManager", "Data with name "+commonName+" does not exist.", Logger.LogLevel.Warning);
            return null;
        }

        T data = super.get(dataMap.get(commonName).path, type);
        if(data == null) Logger.log("AssetManager", "Data with name "+commonName+" does not exist.", Logger.LogLevel.Info);
        return data;
    }

    /**
     * Loads an asset.
     * @param fileName The filename, relative to the assets folder.
     * @param commonName The common/simplified name of the asset for retrieving later.
     * @param type The type of file (ex: Texture.class)
     * @param <T> The type.
     */
    public synchronized <T> void load(String fileName, String commonName, Class<T> type) {
        super.load(fileName, type);
        dataMap.put(commonName, new DataReference(commonName, fileName));
    }

    /**
     * Loads an asset, using the fileName to generate a commonName, thus 'art/Something.png' will end up as 'Something'
     * @param fileName The file name relative to the assets folder.
     * @param type The type of file, ex: Texture.class
     * @param <T> The type.
     */
    @Override
    public synchronized <T> void load(String fileName, Class<T> type) {
        super.load(fileName, type);
        String commonName = fileName.substring(fileName.lastIndexOf("/")+1, fileName.indexOf("."));
        dataMap.put(commonName, new DataReference(commonName, fileName));
    }

    @Override
    public synchronized <T> void load(String fileName, Class<T> type,  AssetLoaderParameters<T> param) {
        super.load(fileName, type, param);
        String commonName = fileName.substring(fileName.lastIndexOf("/")+1, fileName.indexOf("."));
        dataMap.put(commonName, new DataReference(commonName, fileName));
    }

    public synchronized <T> void load(String fileName, String commonName, Class<T> type, AssetLoaderParameters<T> param) {
        super.load(fileName, type, param);
        dataMap.put(commonName, new DataReference(commonName, fileName));
    }

    /**
     * Basically holds a link from a simple name to the actual path. This makes it
     * a lot easier to load an asset from the manager ("somePicture" vs "img/misc/buttons/somePicture.png")
     */
    private class DataReference{
        private String name, path;

        public DataReference(String name, String path){
            this.name = name;
            this.path = path;
        }
    }
}


