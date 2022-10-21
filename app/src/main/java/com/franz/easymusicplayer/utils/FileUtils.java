package com.franz.easymusicplayer.utils;

import android.icu.text.DecimalFormat;
import android.os.Environment;
import android.util.Log;

import com.franz.easymusicplayer.base.BaseApplication;
import com.franz.easymusicplayer.bean.DownloadBean;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtils {
    private static final String PATH = BaseApplication.getContext().getFilesDir().getPath();
    private static final String TAG = "FileUtils";
    private static FileUtils spInstant = null;

    private FileUtils() {

    }

    public static FileUtils getInstance() {
        if (spInstant == null) {
            Sync();
        }
        return spInstant;
    }

    private static synchronized void Sync() {
        if (spInstant == null) {
            spInstant = new FileUtils();
        }
    }

    public String createFile(String catalogue, String sub) {
        //通过创建对应路径的下是否有相应的文件夹。
            File dir = new File(catalogue, sub);
            if (!dir.exists()) {
                //如果文件存在则删除已存在的文件夹。
                dir.mkdirs();
            }
            return dir.toString();
    }

    public String createFile(String path){
        //通过创建对应路径的下是否有相应的文件夹。
        File dir = new File(path);
        try {
            if (!dir.exists()) {
                //如果文件存在则删除已存在的文件夹。
                //dir.mkdirs();
                dir.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return dir.toString();
    }

    public String mainCatalogue() {
        String path = "EasyMusicDownload";
        File dir = new File(BaseApplication.getContext().getCacheDir(), path);
        File movie = BaseApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (movie != null) {
            dir = new File(movie, path);
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.toString();
    }

    public String createDirectory(String name) {
        File dir = new File(BaseApplication.getContext().getCacheDir(), name);
        File file = BaseApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (file != null) {
            dir = new File(file, name);
        }
        if (!dir.exists()) {
           dir.mkdirs();
        }
        return dir.toString();
    }


    public static final int SIZETYPE_B = 1;//获取文件大小单位为B的double值
    public static final int SIZETYPE_KB = 2;//获取文件大小单位为KB的double值
    public static final int SIZETYPE_MB = 3;//获取文件大小单位为MB的double值
    public static final int SIZETYPE_GB = 4;//获取文件大小单位为GB的double值

    /**
     * 获取文件指定文件的指定单位的大小
     *
     * @param filePath 文件路径
     * @param sizeType 获取大小的类型1为B、2为KB、3为MB、4为GB
     * @return double值的大小
     */
    public double getFileOrFilesSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "获取文件大小失败!");
        }
        return FormetFileSize(blockSize, sizeType);
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "获取文件大小失败!");
        }
        return FormetFileSize(blockSize);
    }

    public long getFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "获取文件大小失败!");
        }
        return blockSize;
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
            Log.d(TAG, "获取文件大小不存在!");
        }
        return size;
    }

    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 转换文件大小,指定转换的类型
     *
     * @param fileS
     * @param sizeType
     * @return
     */
    public double FormetFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZETYPE_B:
                fileSizeLong = Double.valueOf(df.format((double) fileS));
                break;
            case SIZETYPE_KB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
                break;
            case SIZETYPE_MB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
                break;
            case SIZETYPE_GB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }





    /**
     * useful*/
    private boolean deleteDirectory(File tempFile) {
        boolean flag = false;
        try {
            if(!tempFile.exists()) return false;
            if(tempFile.isDirectory()){
                File[] files = tempFile.listFiles();
                if(files == null || files.length == 0) {
                    return tempFile.delete();
                }
                for (File file: files){
                    if(file.isFile()){
                        flag = file.delete();
                    } else if(file.isDirectory()){
                        deleteDirectory(file);
                    }
                }
            }
            flag = tempFile.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    public boolean deleteDirectory(DownloadBean downloadBean){
        if (downloadBean == null)return false;
        String locationDir = FileUtils.getInstance().mainCatalogue();
        String name = locationDir +File.separator+ downloadBean.getSongId()+downloadBean.getSongName()+".mp3";
        File file = new File(name);
        return deleteDirectory(file);
    }
}
