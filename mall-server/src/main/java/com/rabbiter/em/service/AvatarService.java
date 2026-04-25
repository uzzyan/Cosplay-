package com.rabbiter.em.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.Avatar;
import com.rabbiter.em.exception.ServiceException;
import com.rabbiter.em.mapper.AvatarMapper;
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
import java.util.List;
import java.util.UUID;

@Service
public class AvatarService {

    private static final Logger log = LoggerFactory.getLogger(AvatarService.class);

    @Resource
    private AvatarMapper avatarMapper;

    public String upload(MultipartFile uploadFile){
        String url = null;
        //通过md5判断文件是否已经存在，防止在服务器存储相同文件
        InputStream inputStream = null;
        try {
            inputStream = uploadFile.getInputStream();
        } catch (IOException e) {
            log.error("获取文件输入流失败", e);
        }
        String md5 = SecureUtil.md5(inputStream);
        Avatar dbAvatar = avatarMapper.selectByMd5(md5);
        if(dbAvatar==null){
            String originalFilename = uploadFile.getOriginalFilename(); //文件原始名字
            String type = originalFilename.substring(originalFilename.lastIndexOf(".")+1);  //文件后缀
            long size = uploadFile.getSize() / 1024; //文件大小，单位kb
            //文件不存在，则保存文件
            File folder = new File(Constants.avatarFolderPath);
            if(!folder.exists()){
                folder.mkdir();
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
            url = "/avatar/"+finalFileName;
            Avatar avatar = new Avatar(type, size, url, md5);
            avatarMapper.save(avatar);
            return url;
        }
        return dbAvatar.getUrl();
    }
    //根据文件名下载文件
    public void download(String fileName, HttpServletResponse response){
        File file = new File(Constants.avatarFolderPath+fileName);
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

    public int delete(int id) {
        Avatar avatar = avatarMapper.selectById(id);
        int delete = avatarMapper.delete(id);
        if(delete==1){
            String fileName = StrUtil.subAfter(avatar.getUrl(),"/",true);
            File file = new File(Constants.avatarFolderPath+fileName);
            if(file.exists()){
                file.delete();
            }
        }
        return delete;
    }

    public List<Avatar> selectPage(int index, int pageSize) {
        return avatarMapper.selectPage(index,pageSize);
    }

    public int getTotal() {
        return avatarMapper.getTotal();
    }
}
