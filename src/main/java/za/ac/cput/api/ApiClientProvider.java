/*
 ApiClientProvider.java

 Creates the API clients and shares them across the app

 Author: Joshua Reid Adams (230317693)

 Date: 11th August 2026
*/

package za.ac.cput.api;
// One shared client for the whole app, so the token set at login works on every screen.
public class ApiClientProvider {

    private static final ApiClientProvider INSTANCE = new ApiClientProvider();

    private final BaseApiClient baseApiClient;

    private final AuthApiClient authApiClient;
    private final AppointmentApiClient appointmentApiClient;
    private final ClinicStaffApiClient clinicStaffApiClient;
    private final DoctorApiClient doctorApiClient;
    private final NotificationApiClient notificationApiClient;
    private final PatientApiClient patientApiClient;
    private final PatientTicketApiClient patientTicketApiClient;
    private final PaymentApiClient paymentApiClient;
    private final TicketStatusApiClient ticketStatusApiClient;

    private ApiClientProvider() {
        this.baseApiClient = new BaseApiClient();

        this.authApiClient = new AuthApiClient(baseApiClient);
        this.appointmentApiClient = new AppointmentApiClient(baseApiClient);
        this.clinicStaffApiClient = new ClinicStaffApiClient(baseApiClient);
        this.doctorApiClient = new DoctorApiClient(baseApiClient);
        this.notificationApiClient = new NotificationApiClient(baseApiClient);
        this.patientApiClient = new PatientApiClient(baseApiClient);
        this.patientTicketApiClient = new PatientTicketApiClient(baseApiClient);
        this.paymentApiClient = new PaymentApiClient(baseApiClient);
        this.ticketStatusApiClient = new TicketStatusApiClient(baseApiClient);
    }

    public static ApiClientProvider getInstance() {
        return INSTANCE;
    }

    public BaseApiClient getBaseApiClient() { return baseApiClient; }

    public AuthApiClient auth() { return authApiClient; }
    public AppointmentApiClient appointments() { return appointmentApiClient; }
    public ClinicStaffApiClient clinicStaff() { return clinicStaffApiClient; }
    public DoctorApiClient doctors() { return doctorApiClient; }
    public NotificationApiClient notifications() { return notificationApiClient; }
    public PatientApiClient patients() { return patientApiClient; }
    public PatientTicketApiClient patientTickets() { return patientTicketApiClient; }
    public PaymentApiClient payments() { return paymentApiClient; }
    public TicketStatusApiClient ticketStatus() { return ticketStatusApiClient; }
}
