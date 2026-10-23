/*
 PatientTicketApiClient.java

 API client for the Patient Ticket endpoints

 Author: Aidan Barends (230255639)

 Date: 11th August 2026
*/

package za.ac.cput.api;
import com.fasterxml.jackson.core.type.TypeReference;
import za.ac.cput.model.domain.PatientTicket;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PatientTicketApiClient {

    private final BaseApiClient client;

    public PatientTicketApiClient(BaseApiClient client) {
        this.client = client;
    }

    public BaseApiClient.ApiResult<PatientTicket> create(PatientTicket ticket) {
        return client.post("/patienttickets/create", ticket, PatientTicket.class);
    }

    public BaseApiClient.ApiResult<PatientTicket> read(Integer id) {
        return client.get("/patienttickets/read/" + id, PatientTicket.class);
    }

    public BaseApiClient.ApiResult<PatientTicket> update(PatientTicket ticket) {
        return client.put("/patienttickets/update", ticket, PatientTicket.class);
    }

    public BaseApiClient.ApiResult<Void> delete(Integer id) {
        return client.delete("/patienttickets/delete/" + id);
    }

    public BaseApiClient.ApiResult<List<PatientTicket>> getAll() {
        return client.getList("/patienttickets/getall", new TypeReference<List<PatientTicket>>() {});
    }

    // Ticket status calls used by the doctor.

    /** GET /status/{status} */
    public BaseApiClient.ApiResult<List<PatientTicket>> findByCurrentStatus(String status) {
        return client.getList("/patienttickets/status/" + status, new TypeReference<List<PatientTicket>>() {});
    }

    /** GET /patient/{patientId} */
    public BaseApiClient.ApiResult<List<PatientTicket>> findByPatientUserId(int patientId) {
        return client.getList("/patienttickets/patient/" + patientId, new TypeReference<List<PatientTicket>>() {});
    }

    // PATCH /{ticketId}/status. newStatus is the StatusType name, e.g. IN_PROGRESS.
    public BaseApiClient.ApiResult<PatientTicket> progressStatus(int ticketId, String newStatus, String notes) {
        StringBuilder endpoint = new StringBuilder("/patienttickets/" + ticketId + "/status?newStatus=" + newStatus);
        if (notes != null && !notes.isBlank()) {
            endpoint.append("&notes=").append(URLEncoder.encode(notes, StandardCharsets.UTF_8));
        }
        return client.patch(endpoint.toString(), null, PatientTicket.class);
    }

    /** GET /byappointment/{appointmentId} */
    public BaseApiClient.ApiResult<PatientTicket> findByAppointmentId(int appointmentId) {
        return client.get("/patienttickets/byappointment/" + appointmentId, PatientTicket.class);
    }
}
