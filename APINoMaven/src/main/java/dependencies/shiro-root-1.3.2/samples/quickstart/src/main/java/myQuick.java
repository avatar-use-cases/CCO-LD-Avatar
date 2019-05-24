import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class myQuick{

 	public static void main(String[] args){
		Subject currentUser = SecurityUtils.getSubject();

		Session session = currentUser.getSession();
		session.setAttribute("someKey", "AValue");
	
		if( !currentUser.isAuthenticated() ) {
			UsernamePasswordToken token = new UsernamePasswordToken("lonestar", "vespa");
		
			token.setRememberMe(true);
			currentUser.login(token);
		}//if

	}//main
}//myQuick
