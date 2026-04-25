import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.*;

public class UpdateAdminPassword {
    public static void main(String[] args) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 前端会对密码进行MD5加密
        // admin123@ 的 MD5 值是 ecd00aa1acd325ba7575cb0f638b04a5
        String rawPassword = "admin123@";
        String md5Password = "ecd00aa1acd325ba7575cb0f638b04a5";
        
        // 对MD5值进行BCrypt加密
        String bcryptPassword = encoder.encode(md5Password);
        
        System.out.println("原始密码: " + rawPassword);
        System.out.println("MD5密码: " + md5Password);
        System.out.println("BCrypt加密后: " + bcryptPassword);
        
        // 更新数据库
        String url = "jdbc:mysql://localhost:3309/s003?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8";
        String user = "root";
        String password = "123456";
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "UPDATE sys_user SET password = ? WHERE username = 'admin'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, bcryptPassword);
                int rows = pstmt.executeUpdate();
                System.out.println("成功更新 " + rows + " 行数据");
                
                // 验证更新结果
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT username, LEFT(password, 40) as pwd_prefix FROM sys_user WHERE username='admin'")) {
                    if (rs.next()) {
                        System.out.println("验证 - 用户名: " + rs.getString("username"));
                        System.out.println("验证 - 密码前缀: " + rs.getString("pwd_prefix"));
                    }
                }
            }
        }
    }
}
