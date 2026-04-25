import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptForMD5 {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // 前端传来的 MD5 值
        String md5Password = "e99a18c428cb38d5f260853678922e03";
        String encodedPassword = encoder.encode(md5Password);
        System.out.println("==========================================");
        System.out.println("MD5 password: " + md5Password);
        System.out.println("BCrypt encoded: " + encodedPassword);
        System.out.println("==========================================");
        System.out.println("SQL:");
        System.out.println("UPDATE `sys_user` SET `password` = '" + encodedPassword + "' WHERE `username` = 'admin';");
        System.out.println("==========================================");
    }
}
