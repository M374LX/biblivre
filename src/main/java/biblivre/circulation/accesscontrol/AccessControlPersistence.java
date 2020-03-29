package biblivre.circulation.accesscontrol;

import java.util.List;

import biblivre.core.AbstractDTO;

public interface AccessCardPersistence {

	boolean save(AccessControlDTO dto);

	boolean saveFromBiblivre3(List<? extends AbstractDTO> dtoList);

	boolean update(AccessControlDTO dto);

	AccessControlDTO getByCardId(Integer cardId);

	AccessControlDTO getByUserId(Integer userId);
}