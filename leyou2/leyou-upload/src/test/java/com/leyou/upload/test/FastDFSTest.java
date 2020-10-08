package com.leyou.upload.test;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FastDFSTest {
    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private ThumbImageConfig thumbImageConfig;

    @Test
    public void testUpload() throws FileNotFoundException{
        //要上传的文件
        File file =new File("C:\\Users\\陈航\\Pictures\\Feedback\\{9AD177D1-0342-4F64-8652-00CAB51A4832}\\Capture001.png");
        //上传并保存图片，参数：1-上传的文件流 2-文件的大小 3-文件的后缀 4-可以不管它
        StorePath storePath=storageClient.uploadFile(
                new FileInputStream(file),file.length(),"png",null
        );
        //带分组的路径
        System.out.println(storePath.getFullPath());
        //不带分组的路径
        System.out.println(storePath.getPath());
    }
    @Test
    public void testUploadAndCreateThumb() throws FileNotFoundException{
        //要上传的文件
        File file =new File("C:\\Users\\陈航\\Pictures\\Feedback\\{9AD177D1-0342-4F64-8652-00CAB51A4832}\\Capture001.png");
        //上传并保存图片，参数：1-上传的文件流 2-文件的大小 3-文件的后缀 4-可以不管它
        StorePath storePath=storageClient.uploadImageAndCrtThumbImage(
                new FileInputStream(file),file.length(),"png",null
        );
        //带分组的路径
        System.out.println(storePath.getFullPath());
        //不带分组的路径
        System.out.println(storePath.getPath());
        //获取缩略图路径
        String path=thumbImageConfig.getThumbImagePath(storePath.getPath());
        System.out.println(path);
    }
}
