package lab.java.demo.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import lab.java.demo.Models.Appointment;
import lab.java.demo.Models.Notifier;
import lab.java.demo.Models.Patient;

/**
 * Issue 16 follow-up: a real SMS provider (Twilio), as an alternative to
 * the {@code ConsoleSMSNotifier} demo stand-in. Active only when
 * {@code app.notifications.sms.provider=twilio} (application.properties)
 * -- otherwise this bean is never created and Console remains the
 * default (see the {@code @ConditionalOnProperty} below and
 * {@code ConsoleSMSNotifier}'s Javadoc).
 *
 * <p>Account SID / auth token / sender number come from
 * {@code app.notifications.sms.*} properties, overridable via
 * {@code APP_NOTIFICATIONS_SMS_ACCOUNT_SID} /
 * {@code APP_NOTIFICATIONS_SMS_AUTH_TOKEN} /
 * {@code APP_NOTIFICATIONS_SMS_FROM_NUMBER} environment variables --
 * never written into source, consistent with Issue 21's "credentials
 * supplied at configuration time" requirement, applied here too.
 * {@code Twilio.init(...)} just caches those statically; it makes no
 * network call by itself (that happens lazily on the first actual send).
 */
@Component("smsNotifier")
@ConditionalOnProperty(name = "app.notifications.sms.provider", havingValue = "twilio")
public class TwilioSmsNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsNotifier.class);

    private final String fromNumber;

    public TwilioSmsNotifier(@Value("${app.notifications.sms.account-sid}") String accountSid,
                              @Value("${app.notifications.sms.auth-token}") String authToken,
                              @Value("${app.notifications.sms.from-number}") String fromNumber) {
        Twilio.init(accountSid, authToken);
        this.fromNumber = fromNumber;
    }

    @Override
    public void send(Appointment appt) {
        Patient patient = appt.getPatient();
        String to = patient.getPhone();

        if (to == null || to.isBlank()) {
            // Don't fail the whole reminder batch over one patient who
            // was registered without a phone number -- just skip them
            // and say why, same severity as any other "couldn't deliver
            // this one" condition.
            log.warn("Skipping SMS reminder for appointment id={}: patientId={} has no phone number on file",
                    appt.getId(), patient.getId());
            return;
        }

        String body = "Reminder: you have an appointment with Dr. " + appt.getDoctor().getName()
                + " on " + appt.getDateTime() + ".";

        sendSms(to, body);
        // Ids only -- never the phone number or message body -- same
        // reasoning as SmtpEmailNotifier (see its Javadoc) and Issue 22.
        log.info("[SMS/Twilio] Reminder sent for appointment id={} (status={})", appt.getId(), appt.getStatus());
    }

    /**
     * The actual Twilio REST API call, isolated behind this overridable
     * seam so a test can record calls instead of hitting Twilio's
     * network API or needing real credentials -- see
     * TwilioSmsNotifierTest.
     */
    protected void sendSms(String to, String body) {
        Message.creator(new PhoneNumber(to), new PhoneNumber(fromNumber), body).create();
    }
}
