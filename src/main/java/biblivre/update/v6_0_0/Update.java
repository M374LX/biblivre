package biblivre.update.v6_0_0;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import biblivre.core.translations.Translations;
import biblivre.core.utils.Constants;
import biblivre.update.UpdateService;

@Component
public class Update implements UpdateService {

	public void doUpdateScopedBySchema(Connection connection) throws SQLException {
		for (Map.Entry<String, Map<String, String>> entry: _TRANSLATIONS.entrySet()) {
			for (Map.Entry<String, String> entry2: entry.getValue().entrySet()) {
				String key = entry.getKey();

				String language = entry2.getKey();

				String translation = entry2.getValue();

				Translations.addSingleTranslation(
						connection.getSchema(), language, key, translation,
						Constants.ADMIN_LOGGED_USER_ID);
			}
		}
	}

	@Override
	public String getVersion() {
		return "6.0.0";
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	private Map<String, Map<String, String>> _TRANSLATIONS = new HashMap() {{
		put("cataloging.reservation.error.limit_exceeded", new HashMap() {{
			put("en-US", "The selected reader surpassed the limit of authorized loans");
			put("es", "El lector seleccionado excedió el límite de reservas permitidas");
			put("pt-BR", "O leitor selecionado ultrapassou o limite de reservas permitidas");
		}});
	}};
	
}
