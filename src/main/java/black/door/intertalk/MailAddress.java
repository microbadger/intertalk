package black.door.intertalk;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import javaslang.control.Try;
import org.apache.commons.validator.routines.EmailValidator;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Created by nfischer on 9/11/2016.
 */
@JsonSerialize(using = ToStringSerializer.class)
@JsonDeserialize(using = MailAddress.MailAddressDeserializer.class)
public class MailAddress implements Comparable{
	final String local;
	final String domain;

	public MailAddress(String local, String domain) throws AddressException {
		this.local = requireNonNull(local).toLowerCase();
		this.domain = requireNonNull(domain).toLowerCase();
		// Create address string
		String addr = local + '@' + domain;
		// Check if address is valid, and if so create the new InternetAddress
		if(Validate(addr)) {
			new InternetAddress(local + '@' + domain, true);
		}
		else {
			throw new AddressException();
		}
	}

	public static Try<MailAddress> parse(String addr){
		requireNonNull(addr);
		int index = addr.lastIndexOf('@');
		return Try.of(() -> new MailAddress(addr.substring(0, index), addr.substring(index+1, addr.length())));
	}

	// Check if given address (string value) is a valid email address
	// Uses Apache Commons email validator + javax mail validator (Apache commons is more complete)
	public static boolean Validate(String addr) {
		boolean result = true;
		boolean local = true;
		try {
			InternetAddress email = new InternetAddress(addr);
			email.validate();

			EmailValidator.getInstance(local).isValid(addr);
			// if (email.toString().contains("localhost")) result = false;
			// if (email.toString().contains("127.0.0.1")) result = false;

		} catch (AddressException ex) {
			result = false;
		}

		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MailAddress)) return false;

		MailAddress that = (MailAddress) o;

		return local.equalsIgnoreCase(that.local)
				&& domain.equalsIgnoreCase(that.domain);

	}

	@Override
	public int hashCode() {
		int result = local.hashCode();
		result = 31 * result + domain.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return local + '@' + domain;
	}

	@Override
	public int compareTo(Object o) {
		return this.toString().compareTo(String.valueOf(o));
	}

	public static class MailAddressDeserializer extends StdDeserializer<MailAddress>{

		protected MailAddressDeserializer() {
			super(MailAddress.class);
		}

		@Override
		public MailAddress deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			return parse(p.getText()).get();
		}
	}
}