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
package biblivre.cataloging.enums;

import biblivre.core.utils.BiblivreEnum;
import org.apache.commons.lang3.StringUtils;

public enum ImportFormat implements BiblivreEnum {
    AUTO_DETECT,
    MARC,
    XML,
    ISO2709;

    public static ImportFormat fromString(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        str = str.toLowerCase();

        for (ImportFormat importFormat : ImportFormat.values()) {
            if (str.equals(importFormat.name().toLowerCase())) {
                return importFormat;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    public String getString() {
        return this.toString();
    }
}
