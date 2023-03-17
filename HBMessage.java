import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HBMessage implements Serializable {
	
	public String toString() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
}
