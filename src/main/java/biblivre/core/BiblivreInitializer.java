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

import biblivre.z3950.server.Z3950ServerBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BiblivreInitializer {
    private static final Logger _logger = LoggerFactory.getLogger(BiblivreInitializer.class);

    private static boolean initialized = false;
    public static Z3950ServerBO Z3950server = null;

    public static synchronized void initialize() {
        if (!BiblivreInitializer.initialized) {
            try {
                Updates.fixPostgreSQL81();
                Updates.globalUpdate();

                DigitalMediaMigrator.processMigration();

                BiblivreInitializer.Z3950server = new Z3950ServerBO();
                BiblivreInitializer.Z3950server.startServer();

                BiblivreInitializer.initialized = true;
            } catch (Exception e) {
                _logger.error(e.getMessage(), e);
            }
        }
    }

    public static synchronized void destroy() {
        if (BiblivreInitializer.Z3950server != null) {
            BiblivreInitializer.Z3950server.stopServer();
        }
    }

    public static synchronized void reloadZ3950Server() {
        if (BiblivreInitializer.Z3950server != null) {
            BiblivreInitializer.Z3950server.reloadServer();
        }
    }
}
