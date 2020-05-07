package util;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JiexiZIPUtil {
    private static final int BUFFEREDSIZE = 1024;
    public static void main(String[] args) throws IOException, ZipException {
        modifyFile();
    }
    /***
     * 修改更新内容方法
     * @throws IOException
     * @throws ZipException
     */
    public static void modifyFile() throws IOException, ZipException {
        //文件存放基础路径
        String basic_url=SystemMessage.getString("absolutePath")+File.separator;
        //ZIP压缩包存放路径
        String project_zip = basic_url;
        System.out.println(project_zip);
        //临时文件路径
        String temp="";
        //域名路径
        String domainNamePath="";
        //域名称
        String domainName="";
        //修改后压缩包ZIP存放的路径
        String new_zip = basic_url+"new_zip";
        File f_zip =new File(new_zip);
        if (!f_zip.isDirectory()){
            f_zip.mkdirs();
        }
        File f = new File(project_zip);
        //目录下文件的个数
        File[] files = f.listFiles();
        for (File fname : files) {
            if (!fname.isDirectory()) {
                String newPath = project_zip + File.separator + fname.getName();
                System.out.println(newPath+"=========1111====");
                boolean zipMsg = getZIPMsg(newPath);
                if (zipMsg) {
                    String fName = readZipFileName(newPath);
                    System.out.println("fName====="+fName);
                    temp=basic_url+File.separator+fname.getName().split("\\.")[0];
                    //temp=basic_url+File.separator+fName;
                    File file = new File(temp);
                    if (!file.isDirectory() || !file.exists()) {
                        file.mkdirs();
                    }
                    //1解压
                    decompression(newPath, temp);
                    //获取域名
                    domainNamePath=temp+File.separator+fname.getName().split("\\.")[0]+File.separator+"spiders";
                    //domainNamePath=temp+File.separator+fName+File.separator+"spiders";
                    File fdomain=new File(domainNamePath);
                    File[] fdomains = fdomain.listFiles();
                    for(File value:fdomains){
                        if(!"__init__".equals(value.getName().split("\\.")[0])){
                            domainName=value.getName().split("\\.")[0];
                            break;
                        }
                    }
                    //2修改
                    String settingsPath = temp + File.separator + fname.getName().split("\\.")[0] + File.separator;
                    //JiexiZIPUtil.writerFile();
                    SettingPipelinesUpdateFile(settingsPath + "settings.py", settingsPath + "pipelines.py",settingsPath+"items.py",domainName,domainNamePath);
                    //3压缩到指定地方
                    //createZip(temp,new_zip+File.separator+fname.getName());
                    zip(new_zip+File.separator+fname.getName(),temp,false);
                    //createZip(temp,new_zip);
                    //4删除文件
                    deleteFile(file);
                } else {
                    System.out.println("目标ZIP不是我要的文件格式");
                    continue;
                }
            }
        }
    }
    /***
     * 解压文件(用了zip4j这个包)
     * @zipPath zip压缩包文件的路径：XX/DD/dd.zip
     * @aimPath 解压后的文件存放目录
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
        //创建文件夹
        if (file.isDirectory() && !file.exists()) {
            //创建文件夹,mkdirs()不依赖父目录，而mkdir()依赖父目录
            file.mkdirs();
        }
        //解压到aimPath路径中
        zfile.extractAll(aimPath);
    }
    /***
     * 删除zip
     */
    public void deleteZIP(String zipLocation){
        //压缩文件存放的位置
        File fi=new File(zipLocation);
        if (fi.isDirectory()) {
            File[] files = fi.listFiles();
            for (File f : files) {
                // zip文件  判断 是否存在
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
    /***
     * 读取文件内容，并更改部分值
     * @param path
     * @param start
     * @param end
     * @param content
     * @throws IOException
     */
    public static void readUpdateFile(String path,int start,int end,String content) throws IOException {
        Path path1 = Paths.get(path);
        byte[] bytes = Files.readAllBytes(path1);
        String s=new String(bytes,"UTF-8");
        StringBuilder sb=new StringBuilder(s);
        sb.replace(start,end,content);
        //覆盖重写文件
        writerFile(path,sb.toString(),false);
    }
    /***
     * 更新修改setting、item、pipelines文件的内容
     * @param settingsPath
     * @param pipelinesPath
     * @param itemsPath
     * @throws IOException
     */
    public static void SettingPipelinesUpdateFile(String settingsPath,String pipelinesPath,String itemsPath,String domainName,String domainNamePath) throws IOException {
        String domainPath=domainNamePath+File.separator+domainName+".py";
        System.out.println(settingsPath);
        System.out.println(pipelinesPath);
        System.out.println(itemsPath);
        System.out.println(domainPath);
        //s为settings更新内容
        String s=new String(Files.readAllBytes(Paths.get(settingsPath)),"UTF-8");
        String s1 = s.replaceAll("ROBOTSTXT_OBEY = True", "ROBOTSTXT_OBEY = False");
        //stringbulider对stringbuffer速度快很多，但如果要求线程安全必须使用stringbuffer
        StringBuilder sb=new StringBuilder(s1);
        //i为item更新内容
        String i=new String(Files.readAllBytes(Paths.get(itemsPath)),"UTF-8");
        String i1=i.replaceAll("pass","");
        StringBuilder isb=new StringBuilder(i1);
        //p为pipelines更新内容
        String p=new String(Files.readAllBytes(Paths.get(pipelinesPath)),"UTF-8");
        String pipelines=p.replace("# See: http://doc.scrapy.org/en/latest/topics/item-pipeline.html",SystemMessage.getString("pipelinesImportContent"));
        StringBuilder psb=new StringBuilder(pipelines);
        //spiderContent为爬虫程序更新内容
        String spiderContent=new String(Files.readAllBytes(Paths.get(domainPath)),"UTF-8");
        String spiders=spiderContent.replace("from __future__ import absolute_import",SystemMessage.getString("spiderContent"));
        StringBuilder spiderSb=new StringBuilder(spiders);
        //pipelines
        File file = new File(settingsPath);
        String parent = file.getParent();
        System.out.println("parent===="+parent);
        File file1=new File(parent);
        String fileName=file1.getName();
        System.out.println("====="+fileName+"=======================");
        String upperCase =fileName.substring(0,1).toUpperCase()+fileName.toLowerCase().substring(1);
        //String str="\n\t"+"'"+fileName+".pipelines."+upperCase+"Pipeline': 300,";
        //String str1="\n\t"+"'"+fileName+".pipelines.ElasticsearchPipeline': 400,";
        //String str2="\n\t"+"'"+fileName+".pipelines.RedisPipeline': 500,";
        //String modifyFileName=  fileName.substring(0,1).toUpperCase()+fileName.substring(1).toLowerCase();
        String sMongo="\n\t"+"'"+upperCase+".pipelines.MongoPipeline': 300,";
        String str3="ITEM_PIPELINES = {";
        String str4="\n}\n";
        //String settingContent=str3+str+str1+str2+str4;
        String settingContent=str3+sMongo+str4;
        //String settingConfigureContent= SystemMessage.getString("settingContent")+"'"+domainName+"'";
        String settingConfigureContent= SystemMessage.getString("settingContent");
        String pipelinesContent=SystemMessage.getString("pipelinesContent");
        String itemContent=SystemMessage.getString("itemContent");
        //False添加重写settings文件
        writerFile(settingsPath,sb.toString(),false);
        //False添加重写items文件
        writerFile(itemsPath,isb.toString(),false);
        //False添加重写pipelines文件
        writerFile(pipelinesPath,psb.toString(),false);
        //False添加重写spider爬虫文件domainName,domainNamePath
        //writerFile(domainPath,spiderSb.toString(),false);
        //追加settings文件
        writerFile(settingsPath,settingContent,true);
        writerFile(settingsPath,settingConfigureContent,true);
        //追加pipelines文件
        writerFile(pipelinesPath,pipelinesContent,true);
        //追加item内容
        //writerFile(itemsPath,itemContent,true);
    }
    /***
     * 在文件的末尾追加内容content
     * @param path 文件的路径
     * @param content 要追加的内容
     * @throws IOException
     */
    public static void writerFile(String path, String content,boolean flag) throws IOException {
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件（不是覆盖）
            FileWriter writer = new FileWriter(path, flag);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /***
     * 解析zip压缩包是否符合我们需要的目录结构
     * @param zipPath
     * @return
     * @throws IOException
     */
    public static boolean getZIPMsg(String zipPath) throws IOException {
        boolean flag=false;
        //获取文件输入流
        FileInputStream input = new FileInputStream(zipPath);
        //获取ZIP输入流(一定要指定字符集Charset.forName("GBK")否则会报java.lang.IllegalArgumentException: MALFORMED)
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(input), Charset.forName("GBK"));
        //定义ZipEntry置为null,避免由于重复调用zipInputStream.getNextEntry造成的不必要的问题
        ZipEntry ze = null;
        //循环遍历
        while ((ze = zipInputStream.getNextEntry()) != null) {
            String name = ze.getName();
            System.out.println("name1="+name);
            if(name.split("/").length==2) {
                name = name.split("/")[1];
                System.out.println("name===="+name);
                if("pipelines.py".equals(name)){
                    flag=true;
                    break;
                }
            }
        }
        //一定记得关闭流
        zipInputStream.closeEntry();
        input.close();
        return flag;
    }
    /***
     * 删除临时文件夹
     * @param file
     */
    public static void deleteFile(File file) {
        //判断文件是否存在
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
                System.out.println("删除");
            }
        } else {
            System.out.println("所删除的文件不存在");
        }
    }

    /**
     *      * 压缩创建ZIP文件
     *      * @param sourcePath 文件或文件夹路径
     *      * @param zipPath 生成的zip文件存在路径（包括文件名）  
     */
    public static void createZip(String sourcePath, String zipPath) {
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipPath);
            zos = new ZipOutputStream(fos);
            Charset.forName("GBK");
            writeZip(new File(sourcePath), "", zos);
        } catch (FileNotFoundException e) {
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
            }
        }
    }
    private static void writeZip(File file, String parentPath, ZipOutputStream zos) {
        if (file.exists()) {
            //处理文件夹  
            if (file.isDirectory()) {
                parentPath += file.getName() + File.separator;
                File[] files = file.listFiles();
                if (files.length != 0) {
                    for (File f : files) {
                        writeZip(f, parentPath, zos);
                    }
                } else { //空目录则创建当前目录  
                    try {
                        zos.putNextEntry(new ZipEntry(parentPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    ZipEntry ze = new ZipEntry(parentPath + file.getName());
                    zos.putNextEntry(ze);
                    byte[] content = new byte[1024];
                    int len;
                    while ((len = fis.read(content)) != -1) {
                        zos.write(content, 0, len);
                        zos.flush();
                    }
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                } finally {
                    try {
                        if (fis != null) {
                            fis.close();
                        }
                    } catch (IOException e) {

                    }
                }
            }
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
                byte[] buff = new byte[BUFFEREDSIZE];
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



    public static String readZipFileName(String path) throws IOException {
        ZipEntry zipEntry = null;
        String str = null;
        File file = new File(path);
        if(file.exists()){ //判断文件是否存在
            ZipInputStream zipInputStream = new ZipInputStream( new FileInputStream(path), Charset.forName("GBK")); //解决包内文件存在中文时的中文乱码问题
            zipEntry=zipInputStream.getNextEntry();
            str = zipEntry.getName().split("/")[0];
        }
        return  str;
    }
}

