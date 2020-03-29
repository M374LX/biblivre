package biblivre.administration.accesscontrol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import biblivre.administration.accesscards.AccessCardConfig;
import biblivre.circulation.accesscontrol.AccessControlBO;
import biblivre.circulation.accesscontrol.AccessControlDAO;
import biblivre.circulation.accesscontrol.AccessControlPersistence;
import biblivre.circulation.accesscontrol.Handler;

@Configuration
@Import(AccessCardConfig.class)
public class AccessControlConfig {
	@Autowired
	AccessCardConfig _accessCardsConfig;
	
	@Bean
	public AccessControlBO accessControlBO() {
		return new AccessControlBO(getAccessControlPersistence(), _accessCardsConfig.accessCardBO());
	}

	@Bean
	public AccessControlPersistence getAccessControlPersistence() {
		return AccessControlDAO.getInstance("global");
	}

	@Bean
	public Handler handler() {
		return new Handler(accessControlBO(), _accessCardsConfig.accessCardBO());
	}
}
