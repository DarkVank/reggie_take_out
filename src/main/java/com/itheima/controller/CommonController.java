package com.itheima.controller;

import com.itheima.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){

        //获取原始文件名，分离后缀名
        String filename = file.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));

        //随机生成文件名 UUID
        String newFilename = UUID.randomUUID().toString() + suffix;
       //创建文件
        File dir = new File(basePath);
        //如果目录不存在，则创建
        if(!dir.exists()){
            dir.mkdirs();
        }
        //转存文件到指定地址
        try {
            file.transferTo(new File(basePath + newFilename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(newFilename);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        //通过输入流读取文件内容
        try {
            InputStream inputStream = new FileInputStream(new File(basePath + name));
            //通过输出流写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            //读取数据
            int len=0;
            byte[] bytes = new byte[1024];
            while((len = inputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            outputStream.close();
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
