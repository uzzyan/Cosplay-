package com.rabbiter.em.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.MyFile;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.mapper.FileMapper;
import com.sun.xml.fastinfoset.stax.events.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService extends ServiceImpl<FileMapper, MyFile> {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    @Resource
    private FileMapper fileMapper;

    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "ico",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "zip", "rar"
    ));

    public String upload(MultipartFile uploadFile){
        String originalFilename = uploadFile.getOriginalFilename();
        String type = originalFilename.substring(originalFilename.lastIndexOf(".")+1).toLowerCase();
        if (!ALLOWED_TYPES.contains(type)) {
            throw new ServiceException(Constants.CODE_500, "不支持的文件类型: " + type);
        }
        long size = uploadFile.getSize() / 1024;
        String url;
        MyFile myFile = new MyFile();  //用于保存于数据库的实体类Files
        myFile.setName(originalFilename);
        myFile.setSize(size);
        myFile.setType(type);

        //通过md5判断文件是否已经存在，防止在服务器存储相同文件
        String md5;
        try (InputStream inputStream = uploadFile.getInputStream()) {
            md5 = SecureUtil.md5(inputStream);
        } catch (IOException e) {
            log.error("获取文件输入流失败", e);
            throw new ServiceException(Constants.CODE_500, "文件处理失败");
        }
        QueryWrapper<MyFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5",md5);
        List<MyFile> dbMyFileList = fileMapper.selectList(queryWrapper);
        if(dbMyFileList.size() != 0){
            //数据库中已有该md5，则拷贝其url
            url = dbMyFileList.get(0) .getUrl();
            myFile.setUrl(url);
        }else{
            //文件不存在，则保存文件
            File folder = new File(Constants.fileFolderPath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            String folderPath = folder.getAbsolutePath()+"/";   //文件存储文件夹的位置
            //将文件保存为UUID的名字，通过uuid生成url
            String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
            String finalFileName = uuid+"."+type;
            File targetFile = new File(folderPath + finalFileName);
            try {
                uploadFile.transferTo(targetFile);
            } catch (IOException e) {
                log.error("文件保存失败", e);
            }
            url = "/file/"+finalFileName;
            myFile.setUrl(url);
        }
        myFile.setMd5(md5);

        fileMapper.insert(myFile);
        return url;
    }

    //根据文件名下载文件
    public void download(String fileName, HttpServletResponse response){
        File file = new File(Constants.fileFolderPath+fileName);
        if(!file.exists()){
            throw new ServiceException(Constants.CODE_500,"文件不存在");
        }
        try {
            ServletOutputStream os = response.getOutputStream();
            response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileName,"UTF-8"));
            response.setContentType("application/octet-stream");
            os.write(FileUtil.readBytes(file));
            os.flush();
            os.close();
        } catch (IOException e) {
            log.error("文件下载失败", e);
        }
    }
    public int fakeDelete(int id){
        UpdateWrapper<MyFile> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id).set("is_delete",true);
        return fileMapper.update(null,updateWrapper);
    }

    public IPage<MyFile> selectPage(int pageNum,int pageSize,String fileName) {
        IPage<MyFile> filesPage = new Page<>(pageNum, pageSize);
        QueryWrapper<MyFile> filesQueryWrapper = new QueryWrapper<>();
        if(!Util.isEmptyString(fileName)){
            filesQueryWrapper.like("name",fileName);
        }
        filesQueryWrapper.eq("is_delete",false);
        filesQueryWrapper.orderByDesc("id");
        return page(filesPage, filesQueryWrapper);
    }


    public int changeEnable(int id, boolean enable) {
        UpdateWrapper<MyFile> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id).set("enable",enable);
        return fileMapper.update(null, updateWrapper);
    }
}
