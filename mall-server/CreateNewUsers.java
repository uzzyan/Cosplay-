import java.security.MessageDigest;
import java.util.Base64;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CreateNewUsers {
    public static void main(String[] args) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 管理员账号
        String adminUsername = "manager";
        String adminPassword = "Admin@123";  // 8位，包含字母、数字、特殊符号
        String adminMd5 = md5(md5(adminPassword));
        String adminBcrypt = encoder.encode(adminMd5);
        
        // 普通用户账号
        String userUsername = "customer";
        String userPassword = "User@123";  // 8位，包含字母、数字、特殊符号
        String userMd5 = md5(md5(userPassword));
        String userBcrypt = encoder.encode(userMd5);
        
        System.out.println("=== 管理员账号 ===");
        System.out.println("用户名: " + adminUsername);
        System.out.println("密码: " + adminPassword);
        System.out.println("MD5: " + md5(adminPassword));
        System.out.println("MD5(MD5): " + adminMd5);
        System.out.println("BCrypt: " + adminBcrypt);
        System.out.println();
        
        System.out.println("=== 普通用户账号 ===");
        System.out.println("用户名: " + userUsername);
        System.out.println("密码: " + userPassword);
        System.out.println("MD5: " + md5(userPassword));
        System.out.println("MD5(MD5): " + userMd5);
        System.out.println("BCrypt: " + userBcrypt);
        System.out.println();
        
        System.out.println("=== SQL插入语句 ===");
        System.out.println("INSERT INTO sys_user (username, nickname, password, role, email, phone) VALUES");
        System.out.println("('" + adminUsername + "', '系统管理员', '" + adminBcrypt + "', 'admin', 'admin@shop.com', '13900000001'),");
        System.out.println("('" + userUsername + "', '普通用户', '" + userBcrypt + "', 'user', 'user@shop.com', '13900000002');");
    }
    
    public static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(input.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}public class CreateNewUsers {
    
}
