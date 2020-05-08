package util;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class OperateZipsUtil {
    public static void main(String[] args) throws IOException, ZipException {
        modifyFile();
    }
    /**
     * 解压文件
     * @param zipPath zip压缩包文件的路径：DD/dd.zip
     * @param aimPath 解压后的文件存放目录
     * @throws ZipException
     */
    public static void decompression(String zipPath, String aimPath) throws ZipException {
        ZipFile zfile = new ZipFile(zipPath);
        //防止中文文件名称乱码
        zfile.setFileNameCharset("UTF-8");
        if (!zfile.isValidZipFile()) {
            throw new ZipException("压缩文件不存在，请检查路径");
        }
        File file = new File(aimPath);
        if (file.isDirectory() && !file.exists()) {
            file.mkdirs();
        }
        //解压到aimPath路径中
        zfile.extractAll(aimPath);
    }

    /**
     * 修改文件（pipelines、item、setting）
     * @throws IOException
     * @throws ZipException
     */
    public static void modifyFile() throws IOException, ZipException {
        //ZIP压缩包存放路径
        String basic_url=SystemMessage.getString("absolutePath")+File.separator;
        String temp=null;
        //修改后压缩包ZIP存放的路径
        String newZip = basic_url+"new_zip";
        File newFileZip =new File(newZip);
        if (!newFileZip.isDirectory()){
            newFileZip.mkdirs();
        }
        File f = new File(basic_url);
        //目录下文件的个数
        File[] files = f.listFiles();
        for (File fname : files) {
            if (!fname.isDirectory()) {
                String newPath = basic_url  + fname.getName();
                boolean zipMsg = getZIPMsg(newPath);
                if (zipMsg) {
                    String inZipFileName = readZipFileName(newPath);
                    temp=basic_url+File.separator+"portiaTemp";
                    File file = new File(temp);
                    if (!file.isDirectory() || !file.exists()) {
                        file.mkdirs();
                    }
                    //1解压
                    decompression(newPath, temp);
                    //2修改
                    String basicPath = temp + File.separator + inZipFileName + File.separator;
                    SettingUpdateFile(basicPath + "settings.py", basicPath + "pipelines.py",basicPath+"items.py", inZipFileName);
                    //3压缩
                    zip(newZip+File.separator+fname.getName(),temp,false);
                    //4删除文件
                    deleteFile(file);
                } else {
                    //System.out.println("目标文件不是我需要的文件格式");
                    continue;
                }
            }
        }
    }
    /**
     * 删除zip
     * @param zipLocation
     */
    public void deleteZIP(String zipLocation){
        //压缩文件存放的位置
        File fi=new File(zipLocation);
        if (fi.isDirectory()) {
            File[] files = fi.listFiles();
            for (File f : files) {
                // zip文件判断是否存在
                if (f.getName().endsWith(".zip")) {
                    if(f.delete()) {
                        System.out.println("zip文件成功被删除");
                    }else{
                        System.out.println("zip文件删除失败");
                    }
                }
            }
        }
    }
    /**
     * 更新、修改setting、item、pipelines文件的内容
     * @param settingsPath
     * @param pipelinesPath
     * @param itemsPath
     * @param unZipfileName
     * @throws IOException
     */
    public static void SettingUpdateFile(String settingsPath,String pipelinesPath,String itemsPath, String unZipfileName) throws IOException {
        String settingFile=new String(Files.readAllBytes(Paths.get(settingsPath)),"UTF-8");
        String updateSettingFile = settingFile.replaceAll("ROBOTSTXT_OBEY = True", "ROBOTSTXT_OBEY = False");
        StringBuilder sb=new StringBuilder(updateSettingFile);

        String itemFile=new String(Files.readAllBytes(Paths.get(itemsPath)),"UTF-8");
        String updateItemsFile=itemFile.replaceAll("pass","");
        StringBuilder isb=new StringBuilder(updateItemsFile);

        String pipelinesFile=new String(Files.readAllBytes(Paths.get(pipelinesPath)),"UTF-8");
        String updatePipelinesFile=pipelinesFile.replace("# Define your item pipelines here",SystemMessage.getString("pipelinesImportContent"));
        StringBuilder psb=new StringBuilder(updatePipelinesFile);

        File file1=new File(new File(settingsPath).getParent());
        String sMongo="\n\t"+"'"+unZipfileName+".pipelines.MongoPipeline': 300,";
        String str3="ITEM_PIPELINES = {";
        String str4="\n}\n";
        String settingContent=str3+sMongo+str4;
        String settingConfigureContent= SystemMessage.getString("settingContent");
        String pipelinesContent=SystemMessage.getString("pipelinesContent");
        String itemContent=SystemMessage.getString("itemContent");
        writerFile(settingsPath,sb.toString(),false);
        writerFile(itemsPath,isb.toString(),false);
        writerFile(pipelinesPath,psb.toString(),false);
        writerFile(settingsPath,settingContent,true);
        writerFile(settingsPath,settingConfigureContent,true);
        writerFile(pipelinesPath,pipelinesContent,true);
        //writerFile(itemsPath,itemContent,true);
    }
    /**
     * 对文件进行写入
     * @param path 文件的路径
     * @param content 要修改的内容
     * @param flag 是否覆盖 true表示要追加内容（不是覆盖）
     * @throws IOException
     */
    public static void writerFile(String path, String content,boolean flag) throws IOException {
        try {
            FileWriter writer = new FileWriter(path, flag);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 解析zip压缩包是否符合是我们需要的目录结构
     * @param zipPath 目标zip文件路径
     * @return
     * @throws IOException
     */
    public static boolean getZIPMsg(String zipPath) throws IOException {
        boolean flag=false;
        FileInputStream input = new FileInputStream(zipPath);
        //获取ZIP输入流(一定要指定字符集Charset.forName("GBK")否则会报java.lang.IllegalArgumentException: MALFORMED)
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(input), Charset.forName("GBK"));
        //定义ZipEntry置为null,避免由于重复调用zipInputStream.getNextEntry造成的不必要的问题
        ZipEntry ze = null;
        //循环遍历
        while ((ze = zipInputStream.getNextEntry()) != null) {
            String name = ze.getName();
            if(name.split("/").length==2) {
                name = name.split("/")[1];
                if("pipelines.py".equals(name)){
                    flag=true;
                    break;
                }
            }
        }
        zipInputStream.closeEntry();
        input.close();
        return flag;
    }
    /**
     * 删除临时文件夹
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.exists()) {
            //判断是否是文件
            if (file.isFile()) {
                //删除文件
                file.delete();
                //否则如果它是一个目录
            } else if (file.isDirectory()) {
                //声明目录下所有的文件 files[];
                File[] files = file.listFiles();
                //遍历目录下所有的文件
                for (int i = 0; i < files.length; i++) {
                    //把每个文件用这个方法进行迭代
                    deleteFile(files[i]);
                }
                //删除文件夹
                file.delete();
            }
        } else {
            System.out.println("所删除的文件不存在");
        }
    }

    /**
     * 压缩文件
     * @param zipFileName
     *      保存的压缩包文件路径
     * @param filePath
     *      需要压缩的文件夹或者文件路径
     * @param isDelete
     *      是否删除源文件
     * @throws Exception
     */
    public static void zip(String zipFileName, String filePath, boolean isDelete){
        zip(zipFileName, new File(filePath), isDelete);
    }
    /**
     * 压缩文件
     *
     * @param zipFileName
     *      保存的压缩包文件路径
     * @param inputFile
     *      需要压缩的文件夹或者文件
     * @param isDelete
     *      是否删除源文件
     * @throws Exception
     */
    public static void zip(String zipFileName, File inputFile, boolean isDelete){
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
            if (!inputFile.exists()) {
                throw new FileNotFoundException("在指定路径未找到需要压缩的文件！");
            }
            zip(out, inputFile, "", isDelete);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 递归压缩方法
     *
     * @param out
     *      压缩包输出流
     * @param inputFile
     *      需要压缩的文件
     * @param base
     *      压缩的路径
     * @param isDelete
     *      是否删除源文件
     * @throws Exception
     */
    private static void zip(ZipOutputStream out, File inputFile, String base, boolean isDelete) throws IOException {
        if (inputFile.isDirectory()) { // 如果是目录
            File[] inputFiles = inputFile.listFiles();
            out.putNextEntry(new ZipEntry(base + "/"));
            base = base.length() == 0 ? "" : base + "/";
            for (int i = 0; i < inputFiles.length; i++) {
                zip(out, inputFiles[i], base + inputFiles[i].getName(), isDelete);
            }
        } else { // 如果是文件
            if (base.length() > 0) {
                out.putNextEntry(new ZipEntry(base));
            } else {
                out.putNextEntry(new ZipEntry(inputFile.getName()));
            }
            FileInputStream in = new FileInputStream(inputFile);
            try {
                int len;
                byte[] buff = new byte[2048];
                while ((len = in.read(buff)) != -1) {
                    out.write(buff, 0, len);
                }
            } catch (IOException e) {
                e.getStackTrace();
            } finally {
                in.close();
            }
        }
        if (isDelete) {
            inputFile.delete();
        }
    }


    /**
     * 不解压读取压缩包里需要的文件名称
     * @param path zip文件的路径
     * @return
     * @throws IOException
     */
    public static String readZipFileName(String path) throws IOException {
        ZipEntry zipEntry = null;
        File file = new File(path);
        String str=null;
        if(file.exists()){
            ZipInputStream zipInputStream = new ZipInputStream( new FileInputStream(path), Charset.forName("GBK"));
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if(zipEntry.isDirectory()){
                }else{
                    str+=";"+zipEntry.getName().substring(zipEntry.getName().lastIndexOf("/")+1);
                    if(zipEntry.getName().split("/")[0].split("\\.").length==1){
                        str = zipEntry.getName().split("/")[0];
                        break;
                    }
                }
            }
        }
        return  str;
    }
}

