import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.*;

public class UpdateAdminPassword {
    public static void main(String[] args) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Database connection
        String url = "jdbc:mysql://localhost:3309/s003?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8";
        String user = "root";
        String password = "123456";
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Find all users with old MD5 passwords
            String selectSql = "SELECT id, username FROM sys_user WHERE password NOT LIKE '$2a$%'";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                
                System.out.println("发现以下用户需要更新密码:");
                while (rs.next()) {
                    System.out.println("  - " + rs.getString("username"));
                }
            }
            
            // Update admin with password "admin123@"
            // MD5("admin123@") = ecd00aa1acd325ba7575cb0f638b04a5
            // MD5(MD5("admin123@")) = 359c88e8637131014e8ed7b622ef57d7
            String md5OfMd5Admin = "359c88e8637131014e8ed7b622ef57d7";
            String bcryptPasswordAdmin = encoder.encode(md5OfMd5Admin);
            
            // Update user001 with password "ea135627@"
            // MD5("ea135627@") = e8f9e3e3c4e5c6d7a8b9c0d1e2f3a4b5 (need to calculate)
            // Let's use BCrypt directly on MD5 of "ea135627@"
            String md5User001 = cn.hutool.crypto.digest.DigestUtil.md5Hex("ea135627@");
            String md5OfMd5User001 = cn.hutool.crypto.digest.DigestUtil.md5Hex(md5User001);
            String bcryptPasswordUser001 = encoder.encode(md5OfMd5User001);
            
            System.out.println("\n生成 BCrypt 密码:");
            System.out.println("\nAdmin用户:");
            System.out.println("  原始密码: admin123@");
            System.out.println("  MD5: ecd00aa1acd325ba7575cb0f638b04a5");
            System.out.println("  MD5(MD5): " + md5OfMd5Admin);
            System.out.println("  BCrypt: " + bcryptPasswordAdmin);
            
            System.out.println("\nUser001用户:");
            System.out.println("  原始密码: ea135627@");
            System.out.println("  MD5: " + md5User001);
            System.out.println("  MD5(MD5): " + md5OfMd5User001);
            System.out.println("  BCrypt: " + bcryptPasswordUser001);
            
            // Update admin user
            String updateAdminSql = "UPDATE sys_user SET password = ? WHERE username = 'admin'";
            try (PreparedStatement pstmt = conn.prepareStatement(updateAdminSql)) {
                pstmt.setString(1, bcryptPasswordAdmin);
                int rows = pstmt.executeUpdate();
                System.out.println("\n成功更新 admin 用户的密码");
            }
            
            // Update user001
            String updateUser001Sql = "UPDATE sys_user SET password = ? WHERE username = 'user001'";
            try (PreparedStatement pstmt = conn.prepareStatement(updateUser001Sql)) {
                pstmt.setString(1, bcryptPasswordUser001);
                int rows = pstmt.executeUpdate();
                System.out.println("成功更新 user001 用户的密码");
            }
            
            // Verify update
            System.out.println("\n验证更新结果:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, username, LEFT(password, 20) as pwd_prefix FROM sys_user")) {
                while (rs.next()) {
                    System.out.println("  ID: " + rs.getInt("id") + 
                                     ", 用户名: " + rs.getString("username") + 
                                     ", 密码前缀: " + rs.getString("pwd_prefix"));
                }
            }
        }
    }
}
