package com.leyou.goods.service.impl;

import com.leyou.goods.service.GoodsHtmlService;
import com.leyou.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

@Service
public class GoodsHtmlServiceImpl implements GoodsHtmlService {

    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private GoodsService goodsService;

    /**
     * 静态html保存本地服务器
     * */
    public void createHtml(Long spuId){
        try {
            Thread.sleep(500);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //初始化运行上下文
            Context context = new Context();
            context.setVariables(this.goodsService.loadData(spuId));
            //文件写入流
            PrintWriter printWriter = null;
            try {
                //把静态文件生成到本地服务器
                File file = new File("E:\\IDEA\\tools\\nginx-1.14.0\\html\\item\\" + spuId + ".html");
                printWriter = new PrintWriter(file);
                this.templateEngine.process("item", context, printWriter);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (printWriter != null) {
                    printWriter.close();
                }
            }
        }
    }

    /**
     * 删除本地服务器保存的html静态页面
     * */
    @Override
    public void deleteHtml(Long spuId) {
        try {
            Thread.sleep(500);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            File file = new File("E:\\IDEA\\tools\\nginx-1.14.0\\html\\item\\" + spuId + ".html");
            file.deleteOnExit();
        }
    }
}
