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
package biblivre.circulation.reservation;

import biblivre.administration.indexing.IndexingGroupDTO;
import biblivre.administration.indexing.IndexingGroups;
import biblivre.cataloging.HttpRequestSearchHelper;
import biblivre.cataloging.RecordBO;
import biblivre.cataloging.bibliographic.BiblioRecordDTO;
import biblivre.cataloging.enums.RecordType;
import biblivre.cataloging.search.SearchDTO;
import biblivre.cataloging.search.SearchQueryDTO;
import biblivre.circulation.user.UserBO;
import biblivre.circulation.user.UserDTO;
import biblivre.circulation.user.UserSearchDTO;
import biblivre.core.AbstractHandler;
import biblivre.core.DTOCollection;
import biblivre.core.ExtendedRequest;
import biblivre.core.ExtendedResponse;
import biblivre.core.auth.AuthorizationPoints;
import biblivre.core.enums.ActionResult;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONException;

public class Handler extends AbstractHandler {

    public void search(ExtendedRequest request, ExtendedResponse response) {
        String searchParameters = request.getString("search_parameters");

        SearchQueryDTO searchQuery = new SearchQueryDTO(searchParameters);

        AuthorizationPoints authorizationPoints = request.getAuthorizationPoints();

        String schema = request.getSchema();

        RecordType recordType = RecordType.BIBLIO;

        RecordBO bo = RecordBO.getInstance(schema, recordType);

        SearchDTO search = bo.search(searchQuery, recordType, authorizationPoints);

        if (search.size() == 0) {
            this.setMessage(ActionResult.WARNING, "cataloging.error.no_records_found");
        }

        if (CollectionUtils.isEmpty(search)) {
            this.setMessage(ActionResult.WARNING, "cataloging.error.no_records_found");
            return;
        }

        ReservationBO rbo = ReservationBO.getInstance(schema);
        rbo.populateReservationInfoByBiblio(search);

        List<IndexingGroupDTO> groups = IndexingGroups.getGroups(request.getSchema(), recordType);

        try {
            this.json.put("search", search.toJSONObject());

            for (IndexingGroupDTO group : groups) {
                this.json.accumulate("indexing_groups", group.toJSONObject());
            }
        } catch (JSONException e) {
            this.setMessage(ActionResult.WARNING, "error.invalid_json");
            return;
        }
    }

    public void paginate(ExtendedRequest request, ExtendedResponse response) {
        SearchDTO search = HttpRequestSearchHelper.paginate(request, RecordType.BIBLIO);

        if (search == null) {
            this.setMessage(ActionResult.WARNING, "cataloging.error.no_records_found");

            return;
        }

        String schema = request.getSchema();

        RecordBO recordBO = RecordBO.getInstance(schema, RecordType.BIBLIO);

        recordBO.paginateSearch(search, request.getAuthorizationPoints());

        if (CollectionUtils.isEmpty(search)) {
            this.setMessage(ActionResult.WARNING, "cataloging.error.no_records_found");
            return;
        }

        ReservationBO reservationBO = ReservationBO.getInstance(schema);

        reservationBO.populateReservationInfoByBiblio(search);

        this.json.put("search", search.toJSONObject());

        List<IndexingGroupDTO> groups = IndexingGroups.getGroups(schema, RecordType.BIBLIO);

        for (IndexingGroupDTO group : groups) {
            this.json.accumulate("indexing_groups", group.toJSONObject());
        }
    }

    public void userSearch(ExtendedRequest request, ExtendedResponse response) {
        String schema = request.getSchema();

        biblivre.circulation.user.Handler userHandler = new biblivre.circulation.user.Handler();
        DTOCollection<UserDTO> userList = userHandler.searchHelper(request, response, this);

        if (userList == null || userList.size() == 0) {
            this.setMessage(ActionResult.WARNING, "circulation.error.no_users_found");
            return;
        }

        DTOCollection<ReservationListDTO> list = new DTOCollection<>();
        list.setPaging(userList.getPaging());

        for (UserDTO user : userList) {
            list.add(this.populateReservationList(schema, user));
        }

        try {
            this.json.put("search", list.toJSONObject());
        } catch (JSONException e) {
            this.setMessage(ActionResult.WARNING, "error.invalid_json");
        }
    }

    private ReservationListDTO populateReservationList(String schema, UserDTO user) {
        ReservationBO resBo = ReservationBO.getInstance(schema);

        List<ReservationInfoDTO> infos = resBo.listReservationInfo(user);

        ReservationListDTO reservationList = new ReservationListDTO();
        reservationList.setUser(user);
        reservationList.setId(user.getId());
        reservationList.setReservationInfoList(infos);
        return reservationList;
    }

    public void reserve(ExtendedRequest request, ExtendedResponse response) {
        String schema = request.getSchema();
        int recordId = request.getInteger("record_id");
        int userId = request.getInteger("user_id");

        RecordBO rbo = RecordBO.getInstance(schema, RecordType.BIBLIO);
        BiblioRecordDTO record = (BiblioRecordDTO) rbo.get(recordId, RecordBO.MARC_INFO);

        UserBO userBo = UserBO.getInstance(schema);
        UserDTO user = userBo.get(userId);

        ReservationBO reservationBo = ReservationBO.getInstance(schema);
        int reservationId = reservationBo.reserve(record, user, request.getLoggedUserId());

        if (reservationId > 0) {
            this.setMessage(ActionResult.SUCCESS, "circulation.reservation.reserve_success");

            ReservationDTO reservation = reservationBo.get(reservationId);
            ReservationInfoDTO info = new ReservationInfoDTO();
            info.setReservation(reservation);
            info.setBiblio(record);
            info.setUser(user);

            try {
                this.json.put("data", info.toJSONObject());
                this.json.put("full_data", true);
            } catch (JSONException e) {
                this.setMessage(ActionResult.WARNING, "error.invalid_json");
                return;
            }

        } else {
            this.setMessage(ActionResult.WARNING, "circulation.reservation.reserve_failure");
        }
    }

    public void delete(ExtendedRequest request, ExtendedResponse response) {
        String schema = request.getSchema();
        int reserveId = request.getInteger("id");

        ReservationBO reservationBo = ReservationBO.getInstance(schema);
        boolean success = reservationBo.delete(reserveId);

        if (success) {
            this.setMessage(ActionResult.SUCCESS, "circulation.reservation.delete_success");
        } else {
            this.setMessage(ActionResult.WARNING, "circulation.reservation.delete_failure");
        }
    }

    public void selfSearch(ExtendedRequest request, ExtendedResponse response) {

        String searchParameters = request.getString("search_parameters");

        SearchQueryDTO searchQuery = new SearchQueryDTO(searchParameters);

        AuthorizationPoints authorizationPoints = request.getAuthorizationPoints();

        String schema = request.getSchema();

        RecordType recordType = RecordType.BIBLIO;

        RecordBO bo = RecordBO.getInstance(schema, recordType);

        SearchDTO search = bo.search(searchQuery, recordType, authorizationPoints);

        if (search.size() == 0) {
            this.setMessage(ActionResult.WARNING, "cataloging.error.no_records_found");
        }

        if (CollectionUtils.isEmpty(search)) {
            this.setMessage(ActionResult.WARNING, "cataloging.error.no_records_found");
            return;
        }

        List<IndexingGroupDTO> groups =
                IndexingGroups.getGroups(request.getSchema(), RecordType.BIBLIO);

        try {
            this.json.put("search", search.toJSONObject());

            for (IndexingGroupDTO group : groups) {
                this.json.accumulate("indexing_groups", group.toJSONObject());
            }
        } catch (JSONException e) {
            this.setMessage(ActionResult.WARNING, "error.invalid_json");
            return;
        }
    }

    public void selfReserve(ExtendedRequest request, ExtendedResponse response) {

        try {
            int userId = request.getInteger("user_id");
            int loggedUser = request.getLoggedUserId();
            int dbUserId =
                    UserBO.getInstance(request.getSchema()).getUserByLoginId(loggedUser).getId();

            if (userId != dbUserId) {
                this.setMessage(ActionResult.WARNING, "circulation.error.no_users_found");
                return;
            }
        } catch (NumberFormatException nfe) {
            this.setMessage(ActionResult.WARNING, "circulation.error.no_users_found");
            return;
        }

        this.reserve(request, response);
    }

    public void selfOpen(ExtendedRequest request, ExtendedResponse response) {
        String schema = request.getSchema();

        String searchParameters = request.getString("search_parameters");
        UserSearchDTO searchDto = new UserSearchDTO(searchParameters);

        try {
            int userId = Integer.valueOf(searchDto.getQuery());
            int loggedUser = request.getLoggedUserId();
            int dbUserId = UserBO.getInstance(schema).getUserByLoginId(loggedUser).getId();

            if (userId != dbUserId) {
                this.setMessage(ActionResult.WARNING, "circulation.error.no_users_found");
                return;
            }
        } catch (NumberFormatException nfe) {
            this.setMessage(ActionResult.WARNING, "circulation.error.no_users_found");
            return;
        }

        biblivre.circulation.user.Handler userHandler = new biblivre.circulation.user.Handler();
        DTOCollection<UserDTO> userList = userHandler.searchHelper(request, response, this);

        if (userList == null || userList.size() == 0) {
            this.setMessage(ActionResult.WARNING, "circulation.error.no_users_found");
            return;
        }

        DTOCollection<ReservationListDTO> list = new DTOCollection<>();
        list.setPaging(userList.getPaging());

        for (UserDTO user : userList) {
            list.add(this.populateReservationList(schema, user));
        }

        try {
            this.json.put("search", list.toJSONObject());
        } catch (JSONException e) {
            this.setMessage(ActionResult.WARNING, "error.invalid_json");
        }
    }

    public void selfDelete(ExtendedRequest request, ExtendedResponse response) {
        String schema = request.getSchema();
        int reservationId = request.getInteger("id");
        int loggedUser = request.getLoggedUserId();

        ReservationBO reservationBo = ReservationBO.getInstance(schema);
        ReservationDTO reservationDto = reservationBo.get(reservationId);

        if (reservationDto == null) {
            this.setMessage(ActionResult.WARNING, "circulation.reservation.delete_failure");
            return;
        }

        int userId = reservationDto.getUserId();

        if (userId != loggedUser) {
            this.setMessage(ActionResult.WARNING, "circulation.reservation.delete_failure");
            return;
        }

        this.delete(request, response);
    }
}
