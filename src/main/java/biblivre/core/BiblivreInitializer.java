/*******************************************************************************
 * Este arquivo é parte do Biblivre5.
 * 
 * Biblivre5 é um software livre; você pode redistribuí-lo e/ou 
 * modificá-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença, ou (caso queira) qualquer versão posterior.
 * 
 * Este programa é distribuído na esperança de que possa ser  útil, 
 * mas SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * MERCANTIBILIDADE OU ADEQUAÇÃO PARA UM FIM PARTICULAR. Veja a
 * Licença Pública Geral GNU para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU junto
 * com este programa, Se não, veja em <http://www.gnu.org/licenses/>.
 * 
 * @author Alberto Wagner <alberto@biblivre.org.br>
 * @author Danniel Willian <danniel@biblivre.org.br>
 ******************************************************************************/
package biblivre.core;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import biblivre.update.BiblivreWebConfiguration;
import biblivre.z3950.server.Z3950ServerBOAdapter;

public class BiblivreInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	private static boolean initialized = false;
	public static Z3950ServerBOAdapter Z3950server = null;

	public synchronized static void initialize() {
		if (!BiblivreInitializer.initialized) {
			try {
				Updates.fixPostgreSQL81();
				Updates.globalUpdate();

				BiblivreInitializer.Z3950server = new Z3950ServerBOAdapter();
				BiblivreInitializer.Z3950server.startServer();

				BiblivreInitializer.initialized = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized static void destroy() {
		if (BiblivreInitializer.Z3950server != null) {
			BiblivreInitializer.Z3950server.stopServer();
		}
	}

	public synchronized static void reloadZ3950Server() {
		if (BiblivreInitializer.Z3950server != null) {
			BiblivreInitializer.Z3950server.reloadServer();
		}
	}

//	@Override
//	public void onStartup(ServletContext container) {
//		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
//		context.register(AppConfig.class);
//		context.refresh();
//
//		ServletRegistration.Dynamic registration =
//				container.addServlet("dispatcher", new DispatcherServlet(context));
//
//		registration.setLoadOnStartup(1);
//		registration.addMapping("/api/*");
//	}

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return null;
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[] { AppConfig.class, BiblivreWebConfiguration.class };
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/api/*" };
	}
}
