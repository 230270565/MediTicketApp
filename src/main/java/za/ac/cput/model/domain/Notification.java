package za.ac.cput.model.domain;

import java.time.LocalDateTime;

public class Notification {
    private int notificationId;
    private String notificationType;   // SMS, EMAIL
    private String notificationStatus; // SENT, FAILED, PENDING
    private String notificationMessage;
    private Patient patient;
    private Doctor doctor;
    private ClinicStaff clinicStaff;
    private PatientTicket ticket;
    private Appointment appointment;
    private LocalDateTime notificationDate;
    private String relatedStatus;   // stage at creation: TICKET_OPEN, PAYMENT_DUE, ...
    private boolean emailSent;      // true when an email copy was also delivered

    public Notification() {}

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getNotificationStatus() { return notificationStatus; }
    public void setNotificationStatus(String notificationStatus) { this.notificationStatus = notificationStatus; }

    public String getNotificationMessage() { return notificationMessage; }
    public void setNotificationMessage(String notificationMessage) { this.notificationMessage = notificationMessage; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public ClinicStaff getClinicStaff() { return clinicStaff; }
    public void setClinicStaff(ClinicStaff clinicStaff) { this.clinicStaff = clinicStaff; }

    public PatientTicket getTicket() { return ticket; }
    public void setTicket(PatientTicket ticket) { this.ticket = ticket; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public LocalDateTime getNotificationDate() { return notificationDate; }
    public void setNotificationDate(LocalDateTime notificationDate) { this.notificationDate = notificationDate; }
    public String getRelatedStatus() { return relatedStatus; }
    public void setRelatedStatus(String relatedStatus) { this.relatedStatus = relatedStatus; }
    public boolean isEmailSent() { return emailSent; }
    public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }

    /** Unread = anything not yet opened. Older rows saved as PENDING count as unread too. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isUnread() { return !"READ".equals(notificationStatus); }
}
